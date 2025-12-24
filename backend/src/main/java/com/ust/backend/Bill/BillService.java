package com.ust.backend.bill;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
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

    public List<Bill> getBillsByMonth(String month){
        return billRepository.findAllByServiceMonth(java.time.LocalDate.parse(month).withDayOfMonth(1), null).getContent();
    }

    public List<Bill> getBillsDueInTheNextMonth(){
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate nextMonth = today.plusMonths(1);
        return billRepository.findAllByDueDateBetween(today, nextMonth, null).getContent();
    }

    public List<Bill> getBillByStatus(BillStatus status){
        return billRepository.findAllByStatus(status, null).getContent();
    }

    public List<Bill> getBillByUtilityType(UtilityType utilityType){
        return billRepository.findAllByUtilityType(utilityType, null).getContent();
    }

    public List<Bill> filterBills(String month, BillStatus status, UtilityType utilityType){
        LocalDate start = null;
        LocalDate end = null;
        if (month != null && !month.isBlank()) {
            YearMonth ym = YearMonth.parse(month); // expects YYYY-MM
            start = ym.atDay(1);
            end = ym.plusMonths(1).atDay(1);
        }
        return billRepository.findByOptionalFilters(utilityType, status, start, end);
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

    public Bill saveBill(Bill bill){
        return billRepository.save(bill);
    }

    public void deleteBill(Bill bill){
        billRepository.delete(bill);
    }

}
