package org.example.voucher_buying_system.voucher.service;

import org.example.voucher_buying_system.voucher.entity.VoucherOrder;
import org.example.voucher_buying_system.voucher.repository.VoucherOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Service
@Slf4j
public class VoucherOrderService {
    
    @Autowired
    private VoucherOrderRepository voucherOrderRepository;
    
    public List<VoucherOrder> getAllOrders() {
        log.info("查询所有抢购记录");
        return voucherOrderRepository.findAll();
    }
} 