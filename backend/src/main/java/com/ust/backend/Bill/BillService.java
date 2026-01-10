package com.ust.backend.bill;

import com.ust.backend.user.AppUser;
import com.ust.backend.user.AppUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final BillParser billParser;
    private final AppUserRepo userRepo;

    @Autowired
    public BillService(BillRepository billRepository, PdfParseService pdfParseService, BillParser billParser, AppUserRepo userRepo) {
        this.billRepository = billRepository;
        this.pdfParseService = pdfParseService;
        this.billParser = billParser;
        this.userRepo = userRepo;
    }

    private AppUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public List<Bill> getBills(){
        return billRepository.findAllByUserId(getCurrentUser().getId());
    }

    public List<Bill> getBillsDueInTheNextMonth(){
        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusMonths(1);
        return billRepository.findAllByUserIdAndDueDateBetween(getCurrentUser().getId(), today, nextMonth);
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
        return billRepository.findByOptionalFilters(getCurrentUser().getId(), utilityType, status, start, end);
    }

    public List<Object[]> getSumOfAmountByUtilityType(){
        return billRepository.getSumOfAmountByUtilityType(getCurrentUser().getId());
    }

    public List<Object[]> getSumOfAmountByStatus(){
        return billRepository.getSumOfAmountByStatus(getCurrentUser().getId());
    }

    public List<Object[]> getMonthlyTotals(){
        return billRepository.getMonthlyTotals(getCurrentUser().getId());
    }

    @Transactional
    public Bill saveBill(Bill bill){
        bill.setUser(getCurrentUser());
        return billRepository.save(bill);
    }

    public Bill getBillById(long id){
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found: " + id));
        if (bill.getUser().getId() != getCurrentUser().getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return bill;
    }

    @Transactional
    public Bill updateBill(long id, Bill updated){
        Bill existing = getBillById(id); // Checks ownership
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
        Bill existing = getBillById(id); // Checks ownership
        existing.setStatus(status);
        return billRepository.save(existing);
    }

    @Transactional
    public void deleteById(long id){
        Bill bill = getBillById(id); // Checks ownership
        billRepository.delete(bill);
    }

    @Transactional
    public void deleteAll(){
        List<Bill> bills = billRepository.findAllByUserId(getCurrentUser().getId());
        billRepository.deleteAll(bills);
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
            ParsedBill parsed = billParser.parse(text, file.getOriginalFilename());

            // Clean up notes: collapse excessive whitespace/newlines and truncate
            String cleanedText = text != null ? text.replaceAll("\\s+", " ").trim() : "";
            String notes = (cleanedText.length() > 4000) ? cleanedText.substring(0, 4000) + "..." : cleanedText;

            Bill.BillBuilder builder = Bill.builder()
                    .notes(notes)
                    .status(BillStatus.UNPAID);
            if (parsed != null) {
                if (parsed.getUtilityType() != null) builder.utilityType(parsed.getUtilityType());
                if (parsed.getAmount() != null) builder.amount(parsed.getAmount());
                if (parsed.getDueDate() != null) builder.dueDate(parsed.getDueDate());
                if (parsed.getServiceMonth() != null) builder.serviceMonth(parsed.getServiceMonth());
            }
            Bill bill = builder.build();
            bill.setUser(getCurrentUser());
            return billRepository.save(bill);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to parse PDF: " + e.getMessage());
        }
    }

}
