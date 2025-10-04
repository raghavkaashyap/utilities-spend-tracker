package com.ust.backend.Bill;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "bills")
public class Bill {

    @Id
    long id;
    UtilityType utilityType;
    LocalDate serviceMonth;
    LocalDate dueDate;
    double amount;
    BillStatus status;
    String notes;

}
