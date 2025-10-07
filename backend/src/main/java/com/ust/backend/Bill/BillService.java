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

    public Bill getBillById(long id){
        if (billRepository.existsById(id)){
            return billRepository.findById(id);
        }
        return null;
    }
}
