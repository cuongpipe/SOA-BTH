-- Active: 1794755547425@@127.0.0.1@3306@order_db
CREATE DATABASE IF NOT EXISTS order_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE order_db;

-- 1. Bảng đơn hàng (orders)
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    total_amount DECIMAL(10, 2) DEFAULT 0.00,
    status VARCHAR(50) DEFAULT 'pending', -- pending, completed, cancelled
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Bảng chi tiết đơn hàng (order_items)
CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(255),
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2),
    total_price DECIMAL(10, 2),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 3. Dữ liệu mẫu cho bảng orders (Khớp giá với product_db)
INSERT INTO orders (id, customer_name, customer_email, total_amount, status) VALUES
(1, 'Long Quốc Cường', 'cuong4651050032@st.qnu.edu.vn', 26850000.00, 'completed'),
(2, 'Nguyễn Văn An', 'an.nguyen@example.com', 5900000.00, 'pending'),
(3, 'Trần Thị Bình', 'binh.tran@example.com', 6490000.00, 'cancelled');

-- 4. Dữ liệu mẫu cho bảng order_items
INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, total_price) VALUES
(1, 1, 'Laptop Dell XPS 13', 1, 25000000.00, 25000000.00),
(1, 2, 'Bàn phím cơ Keychron K2', 1, 1850000.00, 1850000.00),
(2, 2, 'Bàn phím cơ Keychron K2', 2, 1850000.00, 3700000.00),
(2, 3, 'Chuột Logitech MX Master 3S', 1, 2200000.00, 2200000.00),
(3, 5, 'Tai nghe Sony WH-1000XM5', 1, 6490000.00, 6490000.00);

