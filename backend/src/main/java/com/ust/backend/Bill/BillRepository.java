package com.ust.backend.Bill;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findAllByUtilityType(UtilityType utilityType);
    List<Bill> findAllByStatus(BillStatus status);
    List<Bill> findAllByServiceMonthBetween(LocalDate start, LocalDate end);
    List<Bill> findAllByUtilityType(UtilityType utilityType, Pageable pageable);
    List<Bill> findAllByStatus(BillStatus status, Pageable pageable);
    List<Bill> findAllByServiceMonthBetween(LocalDate start, LocalDate end, Pageable pageable);
}
