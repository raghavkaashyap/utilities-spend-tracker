package com.ust.backend.bill;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Best-effort parser for utility bills from raw text.
 * Conservative heuristics; fields left null if not confidently parsed.
 */
@Service
public class DefaultBillParser implements BillParser {

    // Currency like $123.45 or 1,234.56 or 123.45
    private static final Pattern MONEY_PATTERN = Pattern.compile("(?i)(total\\s*(amount)?\\s*(due|payment|balance)[^\\n\\r:$]*[:$]?\\s*([$])?\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2})?|[0-9]+\\.[0-9]{2}))|([$])\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2})?)");

    private static final Pattern AMOUNT_TOKEN = Pattern.compile("([$])?\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2})|[0-9]+\\.[0-9]{2})");

    // Due date hints
    private static final Pattern DUE_DATE_LINE = Pattern.compile("(?i)(due\\s*date|payment\\s*due|pay\\s*by|please\\s*pay\\s*by|due\\s*on)[^\\n\\r]*");

    // Common date formats
    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yy"),
            DateTimeFormatter.ofPattern("MM/dd/yy"),
            DateTimeFormatter.ofPattern("M-d-yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("M-d-yy"),
            DateTimeFormatter.ofPattern("MM-dd-yy"),
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ISO_LOCAL_DATE
    };

    // Service/Billing period hints
    private static final Pattern PERIOD_LINE = Pattern.compile("(?i)(service|billing|statement)\\s*(period|month|for|cycle)[:\\s-]*([A-Za-z]{3,9}\\s+\\d{4}|\\d{1,2}/\\d{4}|\\d{4}-\\d{1,2})");

    private static final Pattern YEAR_MONTH_TOKEN = Pattern.compile("(?i)([A-Za-z]{3,9}\\s+\\d{4}|\\d{1,2}/\\d{4}|\\d{4}-\\d{1,2})");

    // Service date range hints (e.g. 12/01/2023 - 01/01/2024)
    private static final Pattern DATE_RANGE = Pattern.compile("(?i)(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\s*-\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})");

    private static final List<LabelWeight> AMOUNT_LABELS = List.of(
            new LabelWeight("total amount due", 5),
            new LabelWeight("amount due", 4),
            new LabelWeight("total due", 4),
            new LabelWeight("balance due", 3),
            new LabelWeight("current balance", 3),
            new LabelWeight("payment due", 3),
            new LabelWeight("please pay", 2),
            new LabelWeight("amount payable", 2),
            new LabelWeight("new balance", 2),
            new LabelWeight("total payment", 2)
    );

    private static final List<LabelWeight> DUE_DATE_LABELS = List.of(
            new LabelWeight("due date", 5),
            new LabelWeight("payment due", 4),
            new LabelWeight("pay by", 3),
            new LabelWeight("please pay by", 3),
            new LabelWeight("due on", 3)
    );

    private static final List<LabelWeight> SERVICE_MONTH_LABELS = List.of(
            new LabelWeight("service period", 4),
            new LabelWeight("billing period", 4),
            new LabelWeight("billing month", 3),
            new LabelWeight("service month", 3),
            new LabelWeight("statement period", 3),
            new LabelWeight("billing cycle", 2),
            new LabelWeight("service cycle", 2)
    );

    private static final class LabelWeight {
        private final String normalized;
        private final int weight;

        private LabelWeight(String label, int weight) {
            this.normalized = normalize(label);
            this.weight = weight;
        }
    }

    @Override
    public ParsedBill parse(String text, String sourceFilename) {
        if (text == null) text = "";
        String lower = text.toLowerCase(Locale.ENGLISH);
        ParsedBill pb = new ParsedBill();

        // 1) Utility type
        pb.setUtilityType(detectUtilityType(lower, sourceFilename));

        // 2) Amount (prefer a labeled total due; otherwise the largest currency number)
        pb.setAmount(findLikelyTotalAmount(text));

        // 3) Due date
        pb.setDueDate(findDueDate(text));

        // 4) Service month
        pb.setServiceMonth(findServiceMonth(text, pb.getDueDate()));

        return pb;
    }

    private UtilityType detectUtilityType(String lower, String filename) {
        if (lower.contains("electric") || lower.contains("kwh")) return UtilityType.ELECTRICITY;
        if (lower.contains("water") || lower.contains("gallons")) return UtilityType.WATER;
        if (lower.contains("gas") || lower.contains("therms")) return UtilityType.GAS;
        if (lower.contains("internet") || lower.contains("wifi") || lower.contains("broadband") || lower.contains("fiber") || lower.contains("isp")) return UtilityType.INTERNET;
        if (lower.contains("sewer") || lower.contains("waste") || lower.contains("drainage")) return UtilityType.SEWER;
        if (lower.contains("processing fee") || lower.contains("convenience fee") || lower.contains("service fee")) return UtilityType.PROCESSING_FEE;
        if (filename != null) {
            String f = filename.toLowerCase(Locale.ENGLISH);
            if (f.contains("electric") || f.contains("power")) return UtilityType.ELECTRICITY;
            if (f.contains("water")) return UtilityType.WATER;
            if (f.contains("gas")) return UtilityType.GAS;
            if (f.contains("internet") || f.contains("wifi") || f.contains("fiber")) return UtilityType.INTERNET;
        }
        return UtilityType.OTHER;
    }

    private BigDecimal findLikelyTotalAmount(String text) {
        if (text == null) return null;
        BigDecimal labeled = findLabeledAmount(text);
        if (labeled != null) return labeled;
        Matcher m = MONEY_PATTERN.matcher(text);
        BigDecimal maxLabeled = null;
        BigDecimal maxUnlabeled = null;

        while (m.find()) {
            String num = null;
            boolean isLabeled = false;
            // groups 5 or 7 depending on which branch matched
            if (m.group(5) != null) {
                num = m.group(5);
                isLabeled = true;
            } else if (m.group(7) != null) {
                num = m.group(7);
            }
            if (num == null) continue;
            num = num.replace(",", "");
            try {
                BigDecimal val = new BigDecimal(num);
                if (isLabeled) {
                    if (maxLabeled == null || val.compareTo(maxLabeled) > 0) maxLabeled = val;
                } else {
                    if (maxUnlabeled == null || val.compareTo(maxUnlabeled) > 0) maxUnlabeled = val;
                }
            } catch (NumberFormatException ignored) {}
        }
        // Prioritize a labeled "Total Due" over just a loose number
        return maxLabeled != null ? maxLabeled : maxUnlabeled;
    }

    private BigDecimal findLabeledAmount(String text) {
        String[] lines = splitLines(text);
        BigDecimal best = null;
        int bestScore = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int score = labelScore(normalize(line), AMOUNT_LABELS);
            if (score == 0) continue;
            BigDecimal amt = extractLargestAmount(line);
            if (amt == null && i + 1 < lines.length) {
                amt = extractLargestAmount(lines[i + 1]);
            }
            if (amt == null) continue;
            if (score > bestScore || best == null || (score == bestScore && amt.compareTo(best) > 0)) {
                best = amt;
                bestScore = score;
            }
        }
        return best;
    }

    private BigDecimal extractLargestAmount(String line) {
        if (line == null) return null;
        Matcher m = AMOUNT_TOKEN.matcher(line);
        BigDecimal best = null;
        while (m.find()) {
            String num = m.group(2);
            if (num == null) continue;
            num = num.replace(",", "");
            try {
                BigDecimal val = new BigDecimal(num);
                if (best == null || val.compareTo(best) > 0) {
                    best = val;
                }
            } catch (NumberFormatException ignored) {}
        }
        return best;
    }

    private LocalDate findDueDate(String text) {
        if (text == null) return null;
        String[] lines = splitLines(text);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (labelScore(normalize(line), DUE_DATE_LABELS) == 0) continue;
            LocalDate d = scanLineForDate(line);
            if (d == null && i + 1 < lines.length) {
                d = scanLineForDate(lines[i + 1]);
            }
            if (d != null) return d;
        }
        Matcher lineM = DUE_DATE_LINE.matcher(text);
        if (lineM.find()) {
            String line = lineM.group();
            LocalDate d = scanLineForDate(line);
            if (d != null) return d;
        }
        // fallback: first date after the word 'due'
        int idx = text.toLowerCase(Locale.ENGLISH).indexOf("due");
        if (idx >= 0) {
            String tail = text.substring(idx, Math.min(text.length(), idx + 120));
            LocalDate d = scanLineForDate(tail);
            if (d != null) return d;
        }
        return null;
    }

    private LocalDate scanLineForDate(String s) {
        if (s == null) return null;
        // simple date token pattern
        Pattern token = Pattern.compile("(\\n|\\r| |\\t|:)?([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4}|[A-Za-z]{3,9} [0-9]{1,2}, [0-9]{4}|[0-9]{4}-[0-9]{2}-[0-9]{2})");
        Matcher m = token.matcher(s);
        while (m.find()) {
            String candidate = m.group(2);
            for (DateTimeFormatter fmt : DATE_FORMATS) {
                try {
                    return LocalDate.parse(candidate, fmt);
                } catch (DateTimeParseException ignored) {}
            }
        }
        return null;
    }

    private LocalDate findServiceMonth(String text, LocalDate dueDate) {
        if (text != null) {
            String[] lines = splitLines(text);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (labelScore(normalize(line), SERVICE_MONTH_LABELS) == 0) continue;
                LocalDate d = extractYearMonth(line);
                if (d == null && i + 1 < lines.length) {
                    d = extractYearMonth(lines[i + 1]);
                }
                if (d == null) {
                    d = extractDateRangeMonth(line);
                }
                if (d == null && i + 1 < lines.length) {
                    d = extractDateRangeMonth(lines[i + 1]);
                }
                if (d != null) return d;
            }
            Matcher m = PERIOD_LINE.matcher(text);
            if (m.find()) {
                String token = m.group(3);
                LocalDate d = parseYearMonthToken(token);
                if (d != null) return d;
            }
            // Also try a bare Month YYYY token
            Pattern my = Pattern.compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)[a-z]*\\n?\\s+\\d{4}");
            Matcher m2 = my.matcher(text);
            if (m2.find()) {
                String t = m2.group();
                YearMonth ym = parseMonthYearWords(t);
                if (ym != null) return ym.atDay(1);
            }
            // Try date range (take the end date's month)
            Matcher m3 = DATE_RANGE.matcher(text);
            if (m3.find()) {
                String endDateStr = m3.group(2);
                for (DateTimeFormatter fmt : DATE_FORMATS) {
                    try { return LocalDate.parse(endDateStr, fmt).withDayOfMonth(1); } catch (Exception ignored) {}
                }
            }
        }
        // Fallback: derive from due date (assume service month = month before due date)
        if (dueDate != null) {
            YearMonth ym = YearMonth.from(dueDate).minusMonths(1);
            return ym.atDay(1);
        }
        return null;
    }

    private LocalDate extractYearMonth(String line) {
        if (line == null) return null;
        Matcher m = YEAR_MONTH_TOKEN.matcher(line);
        if (m.find()) {
            return parseYearMonthToken(m.group(1));
        }
        return null;
    }

    private LocalDate extractDateRangeMonth(String line) {
        if (line == null) return null;
        Matcher m = DATE_RANGE.matcher(line);
        if (m.find()) {
            String endDateStr = m.group(2);
            for (DateTimeFormatter fmt : DATE_FORMATS) {
                try {
                    return LocalDate.parse(endDateStr, fmt).withDayOfMonth(1);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private LocalDate parseYearMonthToken(String token) {
        if (token == null) return null;
        token = token.trim();
        try {
            if (token.matches("\\d{1,2}/\\d{4}")) {
                DateTimeFormatter f = DateTimeFormatter.ofPattern("M/yyyy");
                YearMonth ym = YearMonth.parse(token, f);
                return ym.atDay(1);
            }
            if (token.matches("\\d{4}-\\d{1,2}")) {
                DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-M");
                YearMonth ym = YearMonth.parse(token, f);
                return ym.atDay(1);
            }
            // Month name and year
            YearMonth ym = parseMonthYearWords(token);
            if (ym != null) return ym.atDay(1);
        } catch (Exception ignored) {}
        return null;
    }

    private YearMonth parseMonthYearWords(String s) {
        s = s.replaceAll("\n", " ").trim();
        try {
            DateTimeFormatter f1 = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
            return YearMonth.parse(s, f1);
        } catch (Exception ignored) {}
        try {
            DateTimeFormatter f2 = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
            return YearMonth.parse(s, f2);
        } catch (Exception ignored) {}
        return null;
    }

    private static int labelScore(String normalizedLine, List<LabelWeight> labels) {
        int score = 0;
        for (LabelWeight label : labels) {
            if (normalizedLine.contains(label.normalized)) {
                score = Math.max(score, label.weight);
            }
        }
        return score;
    }

    private static String normalize(String input) {
        if (input == null) return "";
        return input.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static String[] splitLines(String text) {
        if (text == null) return new String[0];
        return text.split("\\r?\\n");
    }
}
