package com.ust.backend.Bill;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Bill findById(long id);
    List<Bill> findAll();
    void deleteById(long id);
    void deleteAll();
    Bill save(Bill bill);
    boolean existsById(long id);

}
