CREATE DATABASE IF NOT EXISTS photoshooting DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE photoshooting;

CREATE TABLE IF NOT EXISTS ps_user (
  id VARCHAR(50) PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  display_name VARCHAR(100),
  role VARCHAR(50) NOT NULL DEFAULT 'user'
);

INSERT IGNORE INTO ps_user (id, username, password, display_name, role)
VALUES ('admin-user', 'admin', 'Admin@123', '管理员', 'admin');
