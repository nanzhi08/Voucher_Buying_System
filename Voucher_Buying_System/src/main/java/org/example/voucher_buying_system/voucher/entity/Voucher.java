package org.example.voucher_buying_system.voucher.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Data
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "dj_name")
    private String djName;
    
    private BigDecimal price;
    
    private Integer number;
    
    @Column(name = "ks_time")
    private LocalDateTime ksTime;
    
    @Column(name = "js_time")
    private LocalDateTime jsTime;

    @Override
    public String toString() {
        return "Voucher{" +
                "id=" + id +
                ", djName='" + djName + '\'' +
                ", price=" + price +
                ", number=" + number +
                ", ksTime=" + ksTime +
                ", jsTime=" + jsTime +
                '}';
    }
} 