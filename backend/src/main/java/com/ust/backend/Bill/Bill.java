package com.ust.backend.bill;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "bills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "utilityType is required")
    private UtilityType utilityType;

    @NotNull(message = "serviceMonth is required")
    private LocalDate serviceMonth;

    @NotNull(message = "dueDate is required")
    private LocalDate dueDate;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "amount must be >= 0")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "status is required")
    BillStatus status;
    String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate(){
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate(){
        updatedAt = LocalDateTime.now();
    }

}
