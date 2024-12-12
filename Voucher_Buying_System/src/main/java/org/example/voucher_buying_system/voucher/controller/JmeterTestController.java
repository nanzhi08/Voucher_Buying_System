package org.example.voucher_buying_system.voucher.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/jmeter/voucher", produces = "application/json;charset=UTF-8")
public class JmeterTestController {
    
    private static final Logger log = LoggerFactory.getLogger(JmeterTestController.class);
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    // Redis key前缀
    private static final String TEST_VOUCHER_STOCK = "test:voucher:stock:";    // 库存
    private static final String TEST_VOUCHER_LOCK = "test:voucher:lock:";      // 分布式锁
    
    // 初始化代金券测试数据
    @PostMapping("/init")
    public Map<String, Object> initVoucherTest(@RequestParam Integer voucherId, 
                                             @RequestParam Integer stock,
                                             @RequestParam(defaultValue = "10") Integer price) {
        try {
            String stockKey = TEST_VOUCHER_STOCK + voucherId;
            // 设置库存
            redisTemplate.opsForValue().set(stockKey, String.valueOf(stock));
            
            return Map.of(
                "success", true,
                "message", "ok"
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    // 代金券抢购压力测试
    @PostMapping(value = "/seckill", produces = "application/json;charset=UTF-8")
    public Map<String, Object> testVoucherSeckill(@RequestBody Map<String, Object> request) {
        try {
            // 1. 参数校验
            Integer voucherId = Integer.parseInt(request.get("voucherId").toString());
            String userName = request.get("userName").toString();
            Integer buyNumber = Integer.parseInt(request.get("buyNumber").toString());
            
            String stockKey = TEST_VOUCHER_STOCK + voucherId;
            
            // 2. 获取分布式锁
            String lockKey = TEST_VOUCHER_LOCK + voucherId + ":" + userName;
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
            if (!locked) {
                return Map.of("success", false, "message", "请勿重复抢购");
            }
            
            try {
                // 3. 检查并扣减库存
                String stockStr = redisTemplate.opsForValue().get(stockKey);
                if (stockStr == null) {
                    return Map.of("success", false, "message", "代金券不存在");
                }
                
                int stock = Integer.parseInt(stockStr);
                if (stock < buyNumber) {
                    return Map.of("success", false, "message", "库存不足");
                }
                
                // 4. 扣减库存
                Long newStock = redisTemplate.opsForValue().decrement(stockKey, buyNumber);
                if (newStock < 0) {
                    // 回滚库存
                    redisTemplate.opsForValue().increment(stockKey, buyNumber);
                    return Map.of("success", false, "message", "库存不足");
                }
                
                // 5. 模拟业务处理耗时
                Thread.sleep(10);
                
                return Map.of(
                    "success", true,
                    "message", "抢购成功",
                    "remainStock", newStock,
                    "orderId", System.currentTimeMillis() + "_" + userName
                );
                
            } finally {
                // 6. 释放锁
                redisTemplate.delete(lockKey);
            }
            
        } catch (Exception e) {
            log.error("压力测试异常", e);
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    // 查询代金券库存
    @GetMapping("/{voucherId}/stock")
    public Map<String, Object> getVoucherStock(@PathVariable Integer voucherId) {
        String stockKey = TEST_VOUCHER_STOCK + voucherId;
        String stock = redisTemplate.opsForValue().get(stockKey);
        return Map.of(
            "success", true,
            "voucherId", voucherId,
            "stock", stock != null ? Integer.parseInt(stock) : 0
        );
    }
    
    // 清理测试数据
    @PostMapping("/cleanup")
    public Map<String, Object> cleanupTestData(@RequestParam Integer voucherId) {
        try {
            String stockKey = TEST_VOUCHER_STOCK + voucherId;
            String lockKeyPattern = TEST_VOUCHER_LOCK + voucherId + ":*";
            
            redisTemplate.delete(stockKey);
            // 清理所有相关的锁
            redisTemplate.delete(redisTemplate.keys(lockKeyPattern));
            
            return Map.of("success", true, "message", "测试数据清理成功");
        } catch (Exception e) {
            log.error("清理测试数据失败", e);
            return Map.of("success", false, "message", e.getMessage());
        }
    }
} 