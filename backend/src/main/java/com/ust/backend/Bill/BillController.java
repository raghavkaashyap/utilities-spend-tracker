package com.ust.backend.bill;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping
    public List<Bill> getBills(@RequestParam(required = false) String month,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String utilityType) {
        BillStatus statusEnum = null;
        if (status != null) {
            try {
                statusEnum = BillStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value: " + status);
            }
        }
        UtilityType utilityEnum = null;
        if (utilityType != null) {
            try {
                utilityEnum = UtilityType.valueOf(utilityType.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid utilityType value: " + utilityType);
            }
        }
        // Service handles nulls and parses month
        return billService.filterBills(month, statusEnum, utilityEnum);
    }

    @GetMapping("/{id}")
    public Bill getBillById(@PathVariable long id) {
        return billService.getBillById(id);
    }

    @PutMapping("/{id}")
    public Bill updateBill(@PathVariable long id, @RequestBody Bill bill) {
        return billService.updateBill(id, bill);
    }

    @PatchMapping("/{id}/status")
    public Bill updateBillStatus(@PathVariable long id, @RequestParam String status) {
        try {
            BillStatus statusEnum = BillStatus.valueOf(status.toUpperCase());
            return billService.updateStatus(id, statusEnum);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value: " + status);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteBillById(@PathVariable long id) {
        billService.deleteById(id);
    }

    @GetMapping("/due-next-month")
    public List<Bill> getBillsDueInNextMonth() {
        return billService.getBillsDueInTheNextMonth();
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
        billService.deleteAll();
    }
}
