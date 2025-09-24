-- Sample data for SYOS Inventory System
-- This file contains test data for development and testing purposes

-- Sample Users (matching the schema)
INSERT INTO user (user_code, email, password_hash, password_salt, first_name, last_name, phone, role_id, is_active, created_at) VALUES
('USR00001', 'admin@syos.com', 'hashed_password_123', 'salt123', 'System', 'Administrator', '+94771234567', 1, 1, CURRENT_TIMESTAMP),
('USR00002', 'manager@syos.com', 'hashed_password_456', 'salt456', 'Jane', 'Smith', '+94771234568', 2, 1, CURRENT_TIMESTAMP),
('USR00003', 'cashier@syos.com', 'hashed_password_789', 'salt789', 'Bob', 'Johnson', '+94771234569', 2, 1, CURRENT_TIMESTAMP);

-- Sample Categories
INSERT INTO category (category_code, category_name, description, is_active, created_at) VALUES
('LA', 'Laundry Products', 'Cleaning and laundry products', 1, CURRENT_TIMESTAMP),
('FO', 'Food & Beverages', 'Food items and beverages', 1, CURRENT_TIMESTAMP),
('EL', 'Electronics', 'Electronic devices and accessories', 1, CURRENT_TIMESTAMP);

-- Sample Subcategories
INSERT INTO subcategory (category_id, subcategory_code, subcategory_name, description, default_shelf_capacity, is_active) VALUES
(1, 'LA-SO', 'Soap', 'Bar soaps and liquid soaps', 100, 1),
(1, 'LA-DE', 'Detergent', 'Washing detergents and powders', 80, 1),
(1, 'LA-FA', 'Fabric Softener', 'Fabric softeners and conditioners', 60, 1),
(2, 'FO-SN', 'Snacks', 'Cookies, crackers, and snack foods', 150, 1),
(2, 'FO-DR', 'Drinks', 'Beverages and soft drinks', 120, 1),
(2, 'FO-DA', 'Dairy', 'Milk, yogurt, and dairy products', 80, 1),
(3, 'EL-PH', 'Mobile Phones', 'Smartphones and mobile devices', 50, 1),
(3, 'EL-LA', 'Laptops', 'Laptop computers and accessories', 30, 1),
(3, 'EL-AC', 'Accessories', 'Electronic accessories and cables', 200, 1);

-- Sample Products (matching the hardcoded demo data from UI)
INSERT INTO product (product_code, product_name, description, brand, base_price, unit_of_measure, subcategory_id, discount_percentage, discount_amount, final_price, is_active, created_at, created_by) VALUES
('LA-SO-001', 'Sunlight Soap Bar', 'Premium quality soap bar for daily use', 'Sunlight', 85.00, 'pcs', 1, 10.0, 0.00, 76.50, 1, CURRENT_TIMESTAMP, 1),
('LA-DE-001', 'Surf Excel Powder', 'Advanced cleaning powder detergent', 'Surf Excel', 450.00, 'kg', 2, 0.0, 45.00, 405.00, 1, CURRENT_TIMESTAMP, 1),
('FO-SN-001', 'Munchee Lemon Puff', 'Delicious lemon flavored puff biscuits', 'Munchee', 120.00, 'pack', 4, 0.0, 0.00, 120.00, 1, CURRENT_TIMESTAMP, 1),
('FO-DR-001', 'Coca Cola 1.5L', 'Refreshing cola soft drink 1.5 liter bottle', 'Coca Cola', 280.00, 'bottle', 5, 10.0, 0.00, 252.00, 1, CURRENT_TIMESTAMP, 1),
('EL-PH-001', 'Samsung Galaxy S24', 'Latest Samsung flagship smartphone', 'Samsung', 185000.00, 'pcs', 7, 0.0, 18500.00, 166500.00, 1, CURRENT_TIMESTAMP, 1),
('LA-SO-002', 'Lifebuoy Soap', 'Antibacterial protection soap bar', 'Lifebuoy', 75.00, 'pcs', 1, 0.0, 0.00, 75.00, 1, CURRENT_TIMESTAMP, 1),
('LA-DE-002', 'Ariel Powder', 'Super concentrated washing powder', 'Ariel', 520.00, 'kg', 2, 15.0, 0.00, 442.00, 1, CURRENT_TIMESTAMP, 1),
('FO-SN-002', 'Marie Biscuit', 'Classic tea time biscuits', 'Maliban', 80.00, 'pack', 4, 0.0, 0.00, 80.00, 1, CURRENT_TIMESTAMP, 1),
('FO-DR-002', 'Sprite 1L', 'Lemon-lime flavored soft drink', 'Sprite', 220.00, 'bottle', 5, 0.0, 0.00, 220.00, 1, CURRENT_TIMESTAMP, 1),
('EL-AC-001', 'Phone Charger', 'Universal USB-C phone charger', 'Generic', 1500.00, 'pcs', 9, 0.0, 0.00, 1500.00, 1, CURRENT_TIMESTAMP, 1);

-- Sample Inventory Locations
INSERT INTO inventory_location (location_code, location_name, location_type, is_active) VALUES
('WH001', 'Main Warehouse', 'WAREHOUSE', 1),
('SH001', 'Store Shelf A', 'PHYSICAL_SHELF', 1),
('SH002', 'Store Shelf B', 'PHYSICAL_SHELF', 1),
('ON001', 'Online Store', 'ONLINE_INVENTORY', 1);

-- Sample Batches for products
INSERT INTO batch (product_id, batch_number, purchase_date, expiry_date, quantity_received, selling_price, created_at) VALUES
(1, 'BATCH001', '2025-09-01', '2026-03-15', 100, 85.00, CURRENT_TIMESTAMP),
(2, 'BATCH002', '2025-09-01', '2025-12-31', 50, 450.00, CURRENT_TIMESTAMP),
(3, 'BATCH003', '2025-09-15', '2025-11-20', 200, 120.00, CURRENT_TIMESTAMP),
(4, 'BATCH004', '2025-09-10', '2025-10-15', 80, 280.00, CURRENT_TIMESTAMP),
(5, 'BATCH005', '2025-09-01', NULL, 10, 185000.00, CURRENT_TIMESTAMP);

-- Sample Physical Inventory
INSERT INTO physical_inventory (batch_id, location_id, current_quantity, min_threshold, shelf_capacity, last_updated) VALUES
(1, 2, 25, 10, 50, CURRENT_TIMESTAMP),
(2, 2, 12, 5, 20, CURRENT_TIMESTAMP),
(3, 2, 45, 15, 100, CURRENT_TIMESTAMP),
(4, 2, 30, 10, 60, CURRENT_TIMESTAMP),
(5, 2, 3, 2, 10, CURRENT_TIMESTAMP);

-- Sample Sales Channels
INSERT INTO sales_channel (channel_code, channel_name, channel_type, is_active) VALUES
('POS001', 'Main Store POS', 'POS', 1),
('WEB001', 'Online Store', 'ONLINE', 1);

-- Sample Bills
INSERT INTO bill (bill_serial_number, bill_date, sales_channel_id, employee_id, subtotal, total_discount, final_total, created_at) VALUES
('SYOS-20250924-001', CURRENT_TIMESTAMP, 1, 1, 500.00, 50.00, 450.00, CURRENT_TIMESTAMP),
('SYOS-20250924-002', CURRENT_TIMESTAMP, 1, 1, 320.00, 32.00, 288.00, CURRENT_TIMESTAMP);