package com.ust.backend.bill;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Value object representing the result of parsing a bill from text.
 * All fields are optional; null means "not confidently parsed".
 */
@Setter
@Getter
public class ParsedBill {
    private UtilityType utilityType;
    private LocalDate serviceMonth; // normalized to first day of month when available
    private LocalDate dueDate;
    private BigDecimal amount;

}
