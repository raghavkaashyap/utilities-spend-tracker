package com.ust.backend.Bill;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private UtilityType utilityType;

    private LocalDate serviceMonth;
    private LocalDate dueDate;
    BigDecimal amount;
    @Enumerated(EnumType.STRING)
    BillStatus status;
    String notes;

}
