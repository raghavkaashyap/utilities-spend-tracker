package com.ust.backend.bill;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Page<Bill> findAllByUtilityType(UtilityType utilityType, Pageable pageable);
    Page<Bill> findAllByStatus(BillStatus status, Pageable pageable);
    Page<Bill> findAllByServiceMonthBetween(LocalDate start, LocalDate end, Pageable pageable);
    Page<Bill> findAllByDueDateBetween(LocalDate start, LocalDate end, Pageable pageable);
    Page<Bill> findAllByServiceMonth(LocalDate month, Pageable pageable);

    @Query("SELECT b.utilityType, SUM(b.amount) from Bill b group by b.utilityType ")
    List<Object[]> getSumOfAmountByUtilityType();

    @Query("SELECT b.status, SUM(b.amount) from Bill b group by b.status ")
    List<Object[]> getSumOfAmountByStatus();

    @Query("SELECT b.serviceMonth, SUM(b.amount) from Bill b group by b.serviceMonth ")
    List<Object[]> getMonthlyTotals();

    @Query("SELECT b FROM Bill b WHERE (:utilityType IS NULL OR b.utilityType = :utilityType) AND (:status IS NULL OR b.status = :status) AND ((:start IS NULL AND :end IS NULL) OR (b.serviceMonth >= :start AND b.serviceMonth < :end))")
    List<Bill> findByOptionalFilters(@Param("utilityType") UtilityType utilityType,
                                     @Param("status") BillStatus status,
                                     @Param("start") LocalDate start,
                                     @Param("end") LocalDate end);
}
