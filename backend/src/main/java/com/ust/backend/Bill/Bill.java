package com.ust.backend.bill;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bill {

    private static final int NOTES_MAX = 60_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private UtilityType utilityType;

    private LocalDate serviceMonth;
    private LocalDate dueDate;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    BillStatus status;

    @Lob
    @Column(columnDefinition = "TEXT")
    String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void setNotes(String notes) {
        if (notes != null && notes.length() > NOTES_MAX) {
            this.notes = notes.substring(0, NOTES_MAX);
        } else {
            this.notes = notes;
        }
    }

    @PrePersist
    void onCreate(){
        createdAt = LocalDateTime.now();
        setNotes(this.notes);
    }

    @PreUpdate
    void onUpdate(){
        updatedAt = LocalDateTime.now();
        setNotes(this.notes);
    }

}
