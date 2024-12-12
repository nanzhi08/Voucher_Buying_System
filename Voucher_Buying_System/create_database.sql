-- 创建数据库
CREATE DATABASE IF NOT EXISTS vouchers;

-- 使用数据库
USE vouchers;

-- 创建代金券信息表
CREATE TABLE vouchers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    dj_name VARCHAR(100) NOT NULL COMMENT '代金券名称',
    price DECIMAL(10,2) NOT NULL COMMENT '金额',
    number INT NOT NULL COMMENT '代金券数量',
    ks_time DATETIME NOT NULL COMMENT '代金券开始时间',
    js_time DATETIME NOT NULL COMMENT '代金券结束时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代金券信息表';

-- 创建用户信息表
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '用户姓名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 创建抢购代金券信息表
CREATE TABLE voucher_orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '用户姓名',
    dj_name VARCHAR(100) NOT NULL COMMENT '代金券名称',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抢购代金券信息表'; 