package org.example.voucher_buying_system.voucher.controller;

import org.example.voucher_buying_system.voucher.entity.Voucher;
import org.example.voucher_buying_system.voucher.entity.VoucherOrder;
import org.example.voucher_buying_system.voucher.repository.VoucherRepository;
import org.example.voucher_buying_system.voucher.service.VoucherService;
import org.example.voucher_buying_system.voucher.service.VoucherOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Controller
public class VoucherController {
    
    private static final Logger log = LoggerFactory.getLogger(VoucherController.class);
    
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private VoucherOrderService voucherOrderService;
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    // 显示代金券列表页面
    @GetMapping({"/", "/vouchers"})
    public String showVoucherList(Model model) {
        try {
            log.info("开始获取代金券列表");
            // 使用getVoucherWithCache方法获取代金券列表
            List<Voucher> vouchers = voucherRepository.findAll();
            for (Voucher voucher : vouchers) {
                // 从缓存获取最新信息
                Voucher cachedVoucher = voucherService.getVoucherWithCache(voucher.getId());
                if (cachedVoucher != null) {
                    voucher.setNumber(cachedVoucher.getNumber());
                }
            }
            model.addAttribute("vouchers", vouchers);
            return "index1";
        } catch (Exception e) {
            log.error("获取代金券列表失败", e);
            model.addAttribute("error", "获取代金券列表失败：" + e.getMessage());
            return "index1";
        }
    }
    
    // 显示抢购记录页面
    @GetMapping("/orders")
    public String showOrderList(Model model) {
        try {
            List<VoucherOrder> orders = voucherOrderService.getAllOrders();
            model.addAttribute("orders", orders);
        } catch (Exception e) {
            log.error("获取抢购记录列表失败", e);
            model.addAttribute("error", "获取抢购记录列表失败：" + e.getMessage());
        }
        return "index2";
    }
    
    // 处理抢购请求
    @PostMapping("/buy")
    @ResponseBody
    public Map<String, Object> buyVoucher(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer voucherId = Integer.parseInt(request.get("voucherId").toString());
            String userName = request.get("userName").toString();
            Integer buyNumber = Integer.parseInt(request.get("buyNumber").toString());
            
            // 获取代金券信息
            Voucher voucher = voucherService.getVoucherWithCache(voucherId);
            if (voucher == null) {
                throw new RuntimeException("代金券不存在");
            }
            
            // 调用抢购方法
            voucherService.buyVoucher(voucherId, voucher.getDjName(), userName, buyNumber);
            
            result.put("success", true);
        } catch (Exception e) {
            log.error("抢购失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    // 测试接口也添加相同的限制
    @PostMapping("/test/buy")
    @ResponseBody
    public Map<String, Object> testBuyVoucher(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer voucherId = Integer.parseInt(request.get("voucherId").toString());
            String userName = request.get("userName").toString();
            Integer buyNumber = Integer.parseInt(request.get("buyNumber").toString());
            
            // 使用缓存获取代金券信息
            Voucher voucher = voucherService.getVoucherWithCache(voucherId);
            if (voucher == null) {
                throw new RuntimeException("代金券不存在");
            }
            
            // 直接返回成功，不做异步处理和延迟
            result.put("success", true);
            result.put("message", "请求成功");
            
        } catch (Exception e) {
            log.error("测试抢购失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
} 