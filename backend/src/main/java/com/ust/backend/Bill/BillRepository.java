package com.ust.backend.Bill;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Page<Bill> findAllByUtilityType(UtilityType utilityType, Pageable pageable);
    Page<Bill> findAllByStatus(BillStatus status, Pageable pageable);
    Page<Bill> findAllByServiceMonthBetween(LocalDate start, LocalDate end, Pageable pageable);

    @Query("SELECT b.utilityType, SUM(b.amount) from Bill b group by b.utilityType ")
    List<Object[]> getSumOfAmountByUtilityType();

    @Query("SELECT b.status, SUM(b.amount) from Bill b group by b.status ")
    List<Object[]> getSumOfAmountByStatus();

    @Query("SELECT b.serviceMonth, SUM(b.amount) from Bill b group by b.serviceMonth ")
    List<Object[]> getMonthlyTotals();
}
