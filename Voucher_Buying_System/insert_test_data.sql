-- 插入代金券信息
INSERT INTO vouchers (dj_name, price, number, ks_time, js_time) VALUES
('双十二超市购物券', 50.00, 100, '2023-12-12 00:00:00', '2023-12-12 23:59:59'),
('新年餐饮代金券', 100.00, 50, '2024-01-01 00:00:00', '2024-01-07 23:59:59'),
('春节电影票优惠券', 30.00, 200, '2024-02-10 00:00:00', '2024-02-17 23:59:59'),
('美食节代金券', 80.00, 150, '2024-03-01 00:00:00', '2025-03-15 23:59:59'),
('五一购物狂欢券', 200.00, 80, '2024-05-01 00:00:00', '2025-06-07 23:59:59'),
('测试代金券1', 50.00, 100, '2023-12-01 00:00:00', '2024-12-31 23:59:59'),
('测试代金券2', 100.00, 50, '2023-12-01 00:00:00', '2024-12-31 23:59:59'),
('测试代金券3', 200.00, 0, '2023-12-01 00:00:00', '2024-12-31 23:59:59');

-- 插入用户信息
INSERT INTO users (name) VALUES
('张三'),
('李四'),
('王五'),
('赵六'),
('周七');

-- 插入抢购代金券信息
INSERT INTO voucher_orders (name, dj_name, created_at) VALUES
('张三', '双十二超市购物券', '2023-12-12 10:00:00'),
('李四', '新年餐饮代金券', '2024-01-01 09:15:00'),
('王五', '春节电影票优惠券', '2024-02-10 08:30:00'),
('赵六', '美食节代金券', '2024-03-01 12:00:00'),
('周七', '五一购物狂欢券', '2024-05-01 00:01:00'); 