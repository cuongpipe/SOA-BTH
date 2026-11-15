-- Active: 1794755547425@@127.0.0.1@3306@product_db
-- Khởi tạo database và bảng dữ liệu cho Product Service (Bài TH3)
CREATE DATABASE IF NOT EXISTS product_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE product_db;

CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2),
    quantity INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Nạp dữ liệu mẫu ban đầu
INSERT INTO products (name, description, price, quantity, created_at, updated_at) VALUES
('Laptop Dell XPS 13', 'Ultrabook mỏng nhẹ core i7, RAM 16GB, SSD 512GB', 25000000.00, 10, NOW(), NOW()),
('Bàn phím cơ Keychron K2', 'Bàn phím cơ không dây layout 75%, Brown Switch', 1850000.00, 25, NOW(), NOW()),
('Chuột Logitech MX Master 3S', 'Chuột không dây công thái học chống ồn', 2200000.00, 15, NOW(), NOW()),
('Màn hình LG UltraGear 24 inch', 'Tấm nền IPS, tần số quét 144Hz, 1ms', 3990000.00, 8, NOW(), NOW()),
('Tai nghe Sony WH-1000XM5', 'Tai nghe chụp tai chống ồn chủ động Bluetooth', 6490000.00, 12, NOW(), NOW());

SELECT * from products