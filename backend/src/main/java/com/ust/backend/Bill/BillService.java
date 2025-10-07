package com.ust.backend.Bill;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillService {

    private final BillRepository billRepository;

    @Autowired
    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public List<Bill> getBills(){
        return billRepository.findAll();
    }

    public List<Object[]> getSumOfAmountByUtilityType(){
        return billRepository.getSumOfAmountByUtilityType();
    }

    public List<Object[]> getSumOfAmountByStatus(){
        return billRepository.getSumOfAmountByStatus();
    }

    public List<Object[]> getMonthlyTotals(){
        return billRepository.getMonthlyTotals();
    }

}
