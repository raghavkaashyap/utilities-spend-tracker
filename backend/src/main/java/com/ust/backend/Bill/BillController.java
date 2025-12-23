package com.ust.backend.bill;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping
    public List<Bill> getAllBills() {
        return billService.getBills();
    }

    @GetMapping("/summary/by-utility")
    public List<Object[]> getSumByUtility() {
        return billService.getSumOfAmountByUtilityType();
    }

    @GetMapping("/summary/by-status")
    public List<Object[]> getSumByStatus() {
        return billService.getSumOfAmountByStatus();
    }

    @GetMapping("/summary/monthly")
    public List<Object[]> getMonthlyTotals() {
        return billService.getMonthlyTotals();
    }

    @PostMapping
    public Bill createBill(@RequestBody Bill bill) {
        return billService.saveBill(bill);
    }

    @DeleteMapping("/all")
    public void deleteAllBills() {
        List<Bill> bills = billService.getBills();
        for (Bill bill : bills) {
            billService.deleteBill(bill);
        }
    }
}
