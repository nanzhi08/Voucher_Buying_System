package org.example.voucher_buying_system.voucher.repository;

import org.example.voucher_buying_system.voucher.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    // 查询有效的代金券（在有效期内且数量大于0）
    List<Voucher> findByKsTimeBeforeAndJsTimeAfterAndNumberGreaterThan(
        LocalDateTime ksTime, LocalDateTime jsTime, Integer minNumber);
} 