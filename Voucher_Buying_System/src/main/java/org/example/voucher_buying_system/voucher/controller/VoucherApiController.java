package org.example.voucher_buying_system.voucher.controller;

import org.example.voucher_buying_system.voucher.entity.Voucher;
import org.example.voucher_buying_system.voucher.repository.VoucherRepository;
import org.example.voucher_buying_system.voucher.service.VoucherService;
import org.example.voucher_buying_system.voucher.entity.VoucherOrder;
import org.example.voucher_buying_system.voucher.service.VoucherOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

@RestController
@RequestMapping("/api")
public class VoucherApiController {
    
    private static final Logger log = LoggerFactory.getLogger(VoucherApiController.class);
    
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    @Autowired
    private VoucherOrderService voucherOrderService;
    
    // 获取代金券列表
    @GetMapping("/vouchers")
    public Map<String, Object> getVoucherList() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Voucher> vouchers = voucherRepository.findAll();
            for (Voucher voucher : vouchers) {
                Voucher cachedVoucher = voucherService.getVoucherWithCache(voucher.getId());
                if (cachedVoucher != null) {
                    voucher.setNumber(cachedVoucher.getNumber());
                }
            }
            result.put("success", true);
            result.put("data", vouchers);
        } catch (Exception e) {
            log.error("获取代金券列表失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    // 获取单个代金券
    @GetMapping("/vouchers/{id}")
    public Map<String, Object> getVoucher(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Voucher voucher = voucherService.getVoucherWithCache(id);
            if (voucher == null) {
                throw new RuntimeException("代金券不存在");
            }
            result.put("success", true);
            result.put("data", voucher);
        } catch (Exception e) {
            log.error("获取代金券详情失", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    // 抢购代金券
    @PostMapping("/vouchers/buy")
    public Map<String, Object> buyVoucher(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 参数校验
            if (request.get("voucherId") == null || 
                request.get("userName") == null || 
                request.get("buyNumber") == null) {
                throw new RuntimeException("参数不完整");
            }
            
            Integer voucherId = Integer.parseInt(request.get("voucherId").toString());
            String userName = request.get("userName").toString();
            Integer buyNumber = Integer.parseInt(request.get("buyNumber").toString());
            
            // 获取代金券信息
            Voucher voucher = voucherService.getVoucherWithCache(voucherId);
            if (voucher == null) {
                throw new RuntimeException("代金券不存在");
            }
            
            // 执行抢购
            voucherService.buyVoucher(voucherId, voucher.getDjName(), userName, buyNumber);
            
            result.put("success", true);
            result.put("message", "抢购成功");
        } catch (Exception e) {
            log.error("抢购失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    // 获取订单列表
    @GetMapping("/orders")
    public Map<String, Object> getOrderList() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("开始获取订单列表");
            List<VoucherOrder> orders = voucherOrderService.getAllOrders();
            log.info("成功获取到 {} 条订单记录", orders.size());
            result.put("success", true);
            result.put("data", orders);
        } catch (Exception e) {
            log.error("获取订单列表失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
} 