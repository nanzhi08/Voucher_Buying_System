package org.example.voucher_buying_system.voucher.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher_orders")
@Data
public class VoucherOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    
    @Column(name = "dj_name")
    private String djName;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
} 