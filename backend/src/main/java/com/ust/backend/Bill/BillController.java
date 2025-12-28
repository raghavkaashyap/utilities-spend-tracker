package com.ust.backend.bill;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
    public ResponseEntity<List<Bill>> getBills(@RequestParam(required = false) String month,
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
        return ResponseEntity.ok(billService.filterBills(month, statusEnum, utilityEnum));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable long id) {
        return ResponseEntity.ok(billService.getBillById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bill> updateBill(@PathVariable long id, @RequestBody Bill bill) {
        return ResponseEntity.ok(billService.updateBill(id, bill));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Bill> updateBillStatus(@PathVariable long id, @RequestParam String status) {
        try {
            BillStatus statusEnum = BillStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(billService.updateStatus(id, statusEnum));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value: " + status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBillById(@PathVariable long id) {
        billService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/due-next-month")
    public ResponseEntity<List<Bill>> getBillsDueInNextMonth() {
        return ResponseEntity.ok(billService.getBillsDueInTheNextMonth());
    }

    @GetMapping("/summary/by-utility")
    public ResponseEntity<List<Object[]>> getSumByUtility() {
        return ResponseEntity.ok(billService.getSumOfAmountByUtilityType());
    }

    @GetMapping("/summary/by-status")
    public ResponseEntity<List<Object[]>> getSumByStatus() {
        return ResponseEntity.ok(billService.getSumOfAmountByStatus());
    }

    @GetMapping("/summary/monthly")
    public ResponseEntity<List<Object[]>> getMonthlyTotals() {
        return ResponseEntity.ok(billService.getMonthlyTotals());
    }

    @PostMapping
    public ResponseEntity<Bill> createBill(@RequestBody Bill bill) {
        Bill saved = billService.saveBill(bill);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllBills() {
        billService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Bill> uploadPdf(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file uploaded");
        }
        Bill saved = billService.createBillFromPdf(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
