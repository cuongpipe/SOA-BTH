-- Active: 1794755547425@@127.0.0.1@3306@auth_db
CREATE DATABASE IF NOT EXISTS auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auth_db;

CREATE TABLE IF NOT EXISTS users (
    id_user INT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    token VARCHAR(500)
);

-- Dữ liệu mẫu để test /login
INSERT INTO users (user_name, password) VALUES
('cuong', '123'),
('admin', '123456');

SELECT * from users