package org.example.voucher_buying_system.voucher.service;

import org.example.voucher_buying_system.voucher.entity.User;
import org.example.voucher_buying_system.voucher.entity.Voucher;
import org.example.voucher_buying_system.voucher.entity.VoucherOrder;
import org.example.voucher_buying_system.voucher.repository.UserRepository;
import org.example.voucher_buying_system.voucher.repository.VoucherRepository;
import org.example.voucher_buying_system.voucher.repository.VoucherOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;

@Service
@Slf4j
public class VoucherService {
    @Autowired
    private VoucherRepository voucherRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VoucherOrderRepository voucherOrderRepository;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    // Redis key前缀
    private static final String STOCK_KEY = "voucher:stock:";    // 库存key
    private static final String LOCK_KEY = "voucher:lock:";      // 分布式锁key
    private static final String CACHE_KEY = "voucher:cache:";    // 缓存key
    
    // 1. 缓存功能：系统启动时预热缓存
    @PostConstruct
    public void init() {
        try {
            List<Voucher> vouchers = voucherRepository.findAll();
            
            for (Voucher voucher : vouchers) {
                // 缓存库存信息
                String stockKey = STOCK_KEY + voucher.getId();
                redisTemplate.opsForValue().set(stockKey, voucher.getNumber().toString());
                
                // 缓存代金券详细信息
                String cacheKey = CACHE_KEY + voucher.getId();
                redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(voucher), 30, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            // 异常处理但不打印日志
        }
    }
    
    // 2. 分布式锁：获取锁
    private boolean tryLock(String lockKey) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS));
    }
    
    // 释放锁
    private void unlock(String lockKey) {
        redisTemplate.delete(lockKey);
    }
    
    // 3. 库存控制：检查并扣减库存
    private boolean decrementStock(String stockKey, int buyNumber) {
        Long newStock = redisTemplate.opsForValue().decrement(stockKey, buyNumber);
        if (newStock == null || newStock < 0) {
            // 扣减失败，恢复库存
            if (newStock != null) {
                redisTemplate.opsForValue().increment(stockKey, buyNumber);
            }
            return false;
        }
        return true;
    }
    
    // 获取代金券信息（使用缓存）
    public Voucher getVoucherWithCache(Integer id) {
        // 1. 从缓存获取
        String cacheKey = CACHE_KEY + id;
        String json = redisTemplate.opsForValue().get(cacheKey);
        
        if (json != null) {
            return JSON.parseObject(json, Voucher.class);
        }
        
        // 2. 缓存未命中，查询数据库
        Voucher voucher = voucherRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("代金券不存在"));
            
        // 3. 写入缓存
        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(voucher), 30, TimeUnit.MINUTES);
        
        return voucher;
    }
    
    // 抢购代金券
    @Transactional
    public void buyVoucher(Integer voucherId, String voucherName, String userName, Integer buyNumber) {
        // 1. 获取分布式锁
        String lockKey = LOCK_KEY + voucherId + ":" + userName;
        if (!tryLock(lockKey)) {
            throw new RuntimeException("请勿重复下单");
        }
        
        try {
            // 2. 检查并扣减Redis库存
            String stockKey = STOCK_KEY + voucherId;
            if (!decrementStock(stockKey, buyNumber)) {
                throw new RuntimeException("库存不足");
            }
            
            // 3. 获取代金券信息（从缓存）
            Voucher voucher = getVoucherWithCache(voucherId);
            
            // 4. 保存用户信息
            User user = userRepository.findByName(userName)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setName(userName);
                    return userRepository.save(newUser);
                });
            
            // 5. 更新MySQL库
            voucher.setNumber(voucher.getNumber() - buyNumber);
            voucherRepository.save(voucher);
            
            // 6. 创建订单
            VoucherOrder order = new VoucherOrder();
            order.setName(userName);
            order.setDjName(voucherName);
            order.setCreatedAt(LocalDateTime.now());
            voucherOrderRepository.save(order);
            
            // 7. 更新缓存中的代金券信息
            String cacheKey = CACHE_KEY + voucherId;
            redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(voucher), 30, TimeUnit.MINUTES);
            
        } finally {
            // 8. 释放分布式锁
            unlock(lockKey);
        }
    }
} 