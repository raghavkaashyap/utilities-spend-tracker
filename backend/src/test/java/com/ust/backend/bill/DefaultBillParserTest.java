package com.ust.backend.bill;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultBillParserTest {

    private final DefaultBillParser parser = new DefaultBillParser();

    @Test
    void parseUsesLabeledAmountInSameLine() {
        String text = "Statement\nTotal Amount Due: $123.45\nOther Charges: $9.99";
        ParsedBill parsed = parser.parse(text, "utility.pdf");
        assertEquals(new BigDecimal("123.45"), parsed.getAmount());
    }

    @Test
    void parseUsesLabeledAmountOnNextLine() {
        String text = "Amount Due\n$67.89\nPast Due $120.00";
        ParsedBill parsed = parser.parse(text, "utility.pdf");
        assertEquals(new BigDecimal("67.89"), parsed.getAmount());
    }

    @Test
    void parseUsesLabeledDueDateWithShortYear() {
        String text = "Please pay by 5/24/26";
        ParsedBill parsed = parser.parse(text, "utility.pdf");
        assertEquals(LocalDate.of(2026, 5, 24), parsed.getDueDate());
    }

    @Test
    void parseUsesLabeledServiceMonth() {
        String text = "Billing Cycle: 04/2026";
        ParsedBill parsed = parser.parse(text, "utility.pdf");
        assertEquals(LocalDate.of(2026, 4, 1), parsed.getServiceMonth());
    }
}
