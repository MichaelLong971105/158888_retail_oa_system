CREATE DATABASE IF NOT EXISTS retail_oa;
USE retail_oa;

CREATE TABLE suppliers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255),
    remark VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_permissions (
    user_id BIGINT NOT NULL,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, permission),
    CONSTRAINT fk_user_permissions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(50),
    barcode VARCHAR(64) UNIQUE,
    brand VARCHAR(100),
    specification VARCHAR(100),
    unit VARCHAR(20) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    min_stock INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description VARCHAR(255),
    supplier_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    supplier_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE inventory_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    change_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    remark VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_logs_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE sale_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_number VARCHAR(50) NOT NULL UNIQUE,
    external_sale_id VARCHAR(100),
    source VARCHAR(20) NOT NULL,
    cashier_id BIGINT,
    cashier_name VARCHAR(100) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    sale_time DATETIME NOT NULL,
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sale_records_cashier FOREIGN KEY (cashier_id) REFERENCES users(id)
);

CREATE TABLE sale_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_record_id BIGINT NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(50),
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    line_amount DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_sale_items_sale_record FOREIGN KEY (sale_record_id) REFERENCES sale_records(id) ON DELETE CASCADE,
    CONSTRAINT fk_sale_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE work_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    week_start_date DATE NOT NULL,
    work_date DATE NOT NULL,
    shift_type VARCHAR(20) NOT NULL,
    start_time TIME NULL,
    end_time TIME NULL,
    break_minutes INT NOT NULL DEFAULT 0,
    note VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_work_schedules_employee_date UNIQUE (employee_id, work_date),
    CONSTRAINT fk_work_schedules_employee FOREIGN KEY (employee_id) REFERENCES users(id),
    CONSTRAINT fk_work_schedules_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id)
);

CREATE TABLE leave_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    applicant_id BIGINT NOT NULL,
    approver_id BIGINT NULL,
    leave_type VARCHAR(20) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    approval_comment VARCHAR(500),
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_requests_applicant FOREIGN KEY (applicant_id) REFERENCES users(id),
    CONSTRAINT fk_leave_requests_approver FOREIGN KEY (approver_id) REFERENCES users(id)
);

CREATE TABLE attendance_punch_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    source VARCHAR(20) NOT NULL,
    punch_type VARCHAR(20) NOT NULL,
    punch_time DATETIME NOT NULL,
    external_record_id VARCHAR(100) UNIQUE,
    device_code VARCHAR(100),
    raw_payload VARCHAR(2000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_punch_records_employee FOREIGN KEY (employee_id) REFERENCES users(id)
);

INSERT INTO users (username, email, password, role, enabled)
VALUES ('admin', 'admin@retail.local', '$2a$10$K0v0whJBFIKHufHNtZF7MO.hpOWN5M.tk5Jy6dZy5UvfadHHrIJh6', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE
    email = VALUES(email),
    password = VALUES(password),
    role = VALUES(role),
    enabled = VALUES(enabled);
