package org.example.voucher_buying_system.voucher.repository;

import org.example.voucher_buying_system.voucher.entity.VoucherOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherOrderRepository extends JpaRepository<VoucherOrder, Integer> {
} 