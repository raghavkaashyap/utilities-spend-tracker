package com.ust.backend.bill;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final PdfParseService pdfParseService;

    @Autowired
    public BillService(BillRepository billRepository, PdfParseService pdfParseService) {
        this.billRepository = billRepository;
        this.pdfParseService = pdfParseService;
    }

    public List<Bill> getBills(){
        return billRepository.findAll();
    }

    public List<Bill> getBillsDueInTheNextMonth(){
        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusMonths(1);
        return billRepository.findAllByDueDateBetween(today, nextMonth);
    }

    public List<Bill> filterBills(String month, BillStatus status, UtilityType utilityType){
        LocalDate start = null;
        LocalDate end = null;
        if (month != null && !month.isBlank()) {
            try {
                YearMonth ym = YearMonth.parse(month); // expects YYYY-MM
                start = ym.atDay(1);
                end = ym.plusMonths(1).atDay(1);
            } catch (java.time.format.DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid month format. Use YYYY-MM");
            }
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

    @Transactional
    public Bill saveBill(Bill bill){
        return billRepository.save(bill);
    }

    public Bill getBillById(long id){
        return billRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found: " + id));
    }

    @Transactional
    public Bill updateBill(long id, Bill updated){
        Bill existing = getBillById(id);
        // Update mutable fields
        if (updated.getUtilityType() != null) existing.setUtilityType(updated.getUtilityType());
        if (updated.getServiceMonth() != null) existing.setServiceMonth(updated.getServiceMonth());
        if (updated.getDueDate() != null) existing.setDueDate(updated.getDueDate());
        if (updated.getAmount() != null) existing.setAmount(updated.getAmount());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        if (updated.getNotes() != null) existing.setNotes(updated.getNotes());
        return billRepository.save(existing);
    }

    @Transactional
    public Bill updateStatus(long id, BillStatus status){
        Bill existing = getBillById(id);
        existing.setStatus(status);
        return billRepository.save(existing);
    }

    @Transactional
    public void deleteById(long id){
        if (!billRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found: " + id);
        }
        billRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll(){
        billRepository.deleteAll();
    }

    public void deleteBill(Bill bill){
        billRepository.delete(bill);
    }

    public Bill createBillFromPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file uploaded");
        }
        String contentType = file.getContentType();
        if (!org.springframework.http.MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are supported");
        }
        try {
            String text = pdfParseService.extractText(file);
            Bill bill = Bill.builder()
                    .notes(text)
                    .status(BillStatus.UNPAID)
                    .build();
            return billRepository.save(bill);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to parse PDF: " + e.getMessage());
        }
    }

}
