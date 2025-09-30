package com.ust.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
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
