package com.ust.backend.bill;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
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

    // Due date hints
    private static final Pattern DUE_DATE_LINE = Pattern.compile("(?i)(due\\s*date|payment\\s*due)[^\\n\\r]*");

    // Common date formats
    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ISO_LOCAL_DATE
    };

    // Service/Billing period hints
    private static final Pattern PERIOD_LINE = Pattern.compile("(?i)(service|billing)\\s*(period|month|for)[:\\s-]*([A-Za-z]{3,9}\\s+\\d{4}|\\d{1,2}/\\d{4}|\\d{4}-\\d{1,2})");

    // Service date range hints (e.g. 12/01/2023 - 01/01/2024)
    private static final Pattern DATE_RANGE = Pattern.compile("(?i)(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\s*-\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})");

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

    private LocalDate findDueDate(String text) {
        if (text == null) return null;
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
        Pattern token = Pattern.compile("(\\n|\\r| |\\t|:)?([0-9]{1,2}/[0-9]{1,2}/[0-9]{4}|[A-Za-z]{3,9} [0-9]{1,2}, [0-9]{4}|[0-9]{4}-[0-9]{2}-[0-9]{2})");
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
}
