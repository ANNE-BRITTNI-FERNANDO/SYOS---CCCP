-- ============================================================================
-- SYOS Inventory System - SQLite Database Schema
-- Version: 1.0.0
-- Database: SQLite
-- ============================================================================

-- 1. ROLE TABLE
CREATE TABLE IF NOT EXISTS role (
    role_id INTEGER PRIMARY KEY AUTOINCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. USER TABLE  
CREATE TABLE IF NOT EXISTS user (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_code VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone VARCHAR(15),
    address TEXT,
    role_id INTEGER NOT NULL,
    is_active BOOLEAN DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME,
    FOREIGN KEY (role_id) REFERENCES role(role_id)
);

-- 3. CATEGORY TABLE
CREATE TABLE IF NOT EXISTS category (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_code VARCHAR(10) NOT NULL UNIQUE,
    category_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 4. SUBCATEGORY TABLE
CREATE TABLE IF NOT EXISTS subcategory (
    subcategory_id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id INTEGER NOT NULL,
    subcategory_code VARCHAR(20) NOT NULL UNIQUE,
    subcategory_name VARCHAR(100) NOT NULL,
    description TEXT,
    default_shelf_capacity INTEGER DEFAULT 100,
    is_active BOOLEAN DEFAULT 1,
    FOREIGN KEY (category_id) REFERENCES category(category_id)
);

-- 5. PRODUCT TABLE
CREATE TABLE IF NOT EXISTS product (
    product_id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_code VARCHAR(30) NOT NULL UNIQUE,
    product_name VARCHAR(255) NOT NULL,
    description TEXT,
    brand VARCHAR(100),
    base_price DECIMAL(10,2) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    subcategory_id INTEGER NOT NULL,
    discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    final_price DECIMAL(10,2),
    is_active BOOLEAN DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    FOREIGN KEY (subcategory_id) REFERENCES subcategory(subcategory_id),
    FOREIGN KEY (created_by) REFERENCES user(user_id)
);

-- 6. BATCH TABLE
CREATE TABLE IF NOT EXISTS batch (
    batch_id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    purchase_date DATE NOT NULL,
    expiry_date DATE,
    quantity_received INTEGER NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    UNIQUE(product_id, batch_number)
);

-- 7. INVENTORY_LOCATION TABLE
CREATE TABLE IF NOT EXISTS inventory_location (
    location_id INTEGER PRIMARY KEY AUTOINCREMENT,
    location_code VARCHAR(20) NOT NULL UNIQUE,
    location_name VARCHAR(100) NOT NULL,
    location_type VARCHAR(20) NOT NULL CHECK (location_type IN ('WAREHOUSE', 'PHYSICAL_SHELF', 'ONLINE_INVENTORY')),
    is_active BOOLEAN DEFAULT 1
);

-- 8. PHYSICAL_INVENTORY TABLE (Warehouse + Shelf)
CREATE TABLE IF NOT EXISTS physical_inventory (
    inventory_id INTEGER PRIMARY KEY AUTOINCREMENT,
    batch_id INTEGER NOT NULL,
    location_id INTEGER NOT NULL,
    current_quantity INTEGER NOT NULL DEFAULT 0,
    min_threshold INTEGER DEFAULT 50,
    shelf_capacity INTEGER DEFAULT 100,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES batch(batch_id),
    FOREIGN KEY (location_id) REFERENCES inventory_location(location_id),
    UNIQUE(batch_id, location_id)
);

-- 9. ONLINE_INVENTORY TABLE (Separate Online Stock)
CREATE TABLE IF NOT EXISTS online_inventory (
    online_inventory_id INTEGER PRIMARY KEY AUTOINCREMENT,
    batch_id INTEGER NOT NULL,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER DEFAULT 0,
    min_threshold INTEGER DEFAULT 50,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES batch(batch_id),
    UNIQUE(batch_id)
);

-- 10. SALES_CHANNEL TABLE
CREATE TABLE IF NOT EXISTS sales_channel (
    channel_id INTEGER PRIMARY KEY AUTOINCREMENT,
    channel_code VARCHAR(20) NOT NULL UNIQUE,
    channel_name VARCHAR(50) NOT NULL,
    channel_type VARCHAR(10) NOT NULL CHECK (channel_type IN ('POS', 'ONLINE')),
    is_active BOOLEAN DEFAULT 1
);

-- 11. BILL TABLE
CREATE TABLE IF NOT EXISTS bill (
    bill_id INTEGER PRIMARY KEY AUTOINCREMENT,
    bill_serial_number VARCHAR(50) NOT NULL UNIQUE,
    bill_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    sales_channel_id INTEGER NOT NULL,
    employee_id INTEGER NOT NULL,
    customer_id INTEGER,
    subtotal DECIMAL(10,2) NOT NULL,
    total_discount DECIMAL(10,2) DEFAULT 0.00,
    final_total DECIMAL(10,2) NOT NULL,
    cash_tendered DECIMAL(10,2),
    change_amount DECIMAL(10,2),
    delivery_address TEXT,
    bill_file_path VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sales_channel_id) REFERENCES sales_channel(channel_id),
    FOREIGN KEY (employee_id) REFERENCES user(user_id),
    FOREIGN KEY (customer_id) REFERENCES user(user_id)
);

-- 12. BILL_ITEM TABLE
CREATE TABLE IF NOT EXISTS bill_item (
    bill_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    bill_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    batch_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    line_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bill(bill_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    FOREIGN KEY (batch_id) REFERENCES batch(batch_id)
);

-- 13. STOCK_MOVEMENT TABLE (Warehouse to Shelf transfers)
CREATE TABLE IF NOT EXISTS stock_movement (
    movement_id INTEGER PRIMARY KEY AUTOINCREMENT,
    batch_id INTEGER NOT NULL,
    from_location_id INTEGER NOT NULL,
    to_location_id INTEGER NOT NULL,
    movement_type VARCHAR(30) NOT NULL CHECK (movement_type IN ('WAREHOUSE_TO_SHELF', 'SHELF_ADJUSTMENT', 'SALE_DEDUCTION')),
    quantity INTEGER NOT NULL,
    movement_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    moved_by INTEGER NOT NULL,
    notes TEXT,
    FOREIGN KEY (batch_id) REFERENCES batch(batch_id),
    FOREIGN KEY (from_location_id) REFERENCES inventory_location(location_id),
    FOREIGN KEY (to_location_id) REFERENCES inventory_location(location_id),
    FOREIGN KEY (moved_by) REFERENCES user(user_id)
);

-- 14. REORDER_ALERT TABLE
CREATE TABLE IF NOT EXISTS reorder_alert (
    alert_id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    location_id INTEGER NOT NULL,
    current_quantity INTEGER NOT NULL,
    alert_type VARCHAR(20) NOT NULL CHECK (alert_type IN ('SHELF_RESTOCK', 'NEW_BATCH_ORDER')),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    FOREIGN KEY (location_id) REFERENCES inventory_location(location_id)
);

-- 15. EXPIRY_ALERT TABLE
CREATE TABLE IF NOT EXISTS expiry_alert (
    alert_id INTEGER PRIMARY KEY AUTOINCREMENT,
    batch_id INTEGER NOT NULL,
    expiry_date DATE NOT NULL,
    days_to_expiry INTEGER NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES batch(batch_id)
);

-- 16. AUDIT_LOG TABLE
CREATE TABLE IF NOT EXISTS audit_log (
    audit_id INTEGER PRIMARY KEY AUTOINCREMENT,
    table_name VARCHAR(50) NOT NULL,
    record_id INTEGER NOT NULL,
    action_type VARCHAR(10) NOT NULL CHECK (action_type IN ('INSERT', 'UPDATE', 'DELETE')),
    old_values TEXT,
    new_values TEXT,
    changed_by INTEGER NOT NULL,
    change_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (changed_by) REFERENCES user(user_id)
);

-- ============================================================================
-- TRIGGERS FOR BUSINESS LOGIC (Temporarily removed due to SQL parser limitations)
-- ============================================================================

-- Note: Triggers can be added later with a more sophisticated SQL parser
-- that can handle multi-line statements with embedded semicolons properly

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_product_code ON product(product_code);
CREATE INDEX IF NOT EXISTS idx_batch_expiry ON batch(expiry_date);
CREATE INDEX IF NOT EXISTS idx_bill_date ON bill(bill_date);
CREATE INDEX IF NOT EXISTS idx_user_email ON user(email);
CREATE INDEX IF NOT EXISTS idx_bill_serial ON bill(bill_serial_number);
CREATE INDEX IF NOT EXISTS idx_stock_movement_date ON stock_movement(movement_date);
CREATE INDEX IF NOT EXISTS idx_physical_inventory_location ON physical_inventory(location_id);
CREATE INDEX IF NOT EXISTS idx_online_inventory_batch ON online_inventory(batch_id);
CREATE INDEX IF NOT EXISTS idx_user_code ON user(user_code);
CREATE INDEX IF NOT EXISTS idx_category_code ON category(category_code);
CREATE INDEX IF NOT EXISTS idx_subcategory_code ON subcategory(subcategory_code);

-- ============================================================================
-- INITIAL DATA - ROLES
-- ============================================================================

-- Insert default roles
INSERT OR IGNORE INTO role (role_name, description) VALUES 
    ('ADMIN', 'System Administrator with full access'),
    ('CASHIER', 'Cashier with sales transaction access'),
    ('USER', 'Regular user with basic access');