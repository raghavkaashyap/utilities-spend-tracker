package com.ust.backend.bill;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Value object representing the result of parsing a bill from text.
 * All fields are optional; null means "not confidently parsed".
 */
public class ParsedBill {
    private UtilityType utilityType;
    private LocalDate serviceMonth; // normalized to first day of month when available
    private LocalDate dueDate;
    private BigDecimal amount;

    public UtilityType getUtilityType() {
        return utilityType;
    }

    public void setUtilityType(UtilityType utilityType) {
        this.utilityType = utilityType;
    }

    public LocalDate getServiceMonth() {
        return serviceMonth;
    }

    public void setServiceMonth(LocalDate serviceMonth) {
        this.serviceMonth = serviceMonth;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
