# ============================================================================
# SYOS INITIAL DATA SETUP
# ============================================================================

-- Insert Roles
INSERT OR IGNORE INTO role (role_name, description) VALUES 
('ADMIN', 'System Administrator - Can also work as cashier'),
('EMPLOYEE', 'Store Employee/Cashier'),
('CUSTOMER', 'Customer'),
('MANAGER', 'Store Manager');

-- Insert Sales Channels
INSERT OR IGNORE INTO sales_channel (channel_code, channel_name, channel_type) VALUES 
('POS001', 'Point of Sale', 'POS'),
('WEB001', 'Online Store', 'ONLINE');

-- Insert Inventory Locations
INSERT OR IGNORE INTO inventory_location (location_code, location_name, location_type) VALUES 
('WH001', 'Main Warehouse', 'WAREHOUSE'),
('SH001', 'Physical Store Shelf', 'PHYSICAL_SHELF'),
('ON001', 'Online Inventory', 'ONLINE_INVENTORY');

-- Insert Default Admin User (password: admin123 - should be hashed in real implementation)
INSERT OR IGNORE INTO user (user_code, email, password_hash, first_name, last_name, role_id, address) VALUES 
('AD001', 'admin@syos.com', 'admin123', 'System', 'Administrator', 1, 'SYOS Main Office, Colombo');

-- Insert Sample Categories
INSERT OR IGNORE INTO category (category_code, category_name, description) VALUES 
('GR', 'Groceries', 'Food and beverage items'),
('HH', 'Household', 'Household cleaning and maintenance items'),
('PR', 'Personal Care', 'Personal hygiene and care products'),
('EL', 'Electronics', 'Electronic items and accessories');

-- Insert Sample Subcategories
INSERT OR IGNORE INTO subcategory (category_id, subcategory_code, subcategory_name, description, default_shelf_capacity) VALUES 
(1, 'GR001', 'Dairy Products', 'Milk, cheese, yogurt, etc.', 50),
(1, 'GR002', 'Beverages', 'Soft drinks, juices, water', 100),
(1, 'GR003', 'Snacks', 'Chips, crackers, cookies', 75),
(1, 'GR004', 'Cereals', 'Breakfast cereals and grains', 60),
(2, 'HH001', 'Cleaning Supplies', 'Detergents, soaps, cleaners', 40),
(2, 'HH002', 'Kitchen Items', 'Utensils, containers, foil', 30),
(3, 'PR001', 'Bath & Body', 'Soaps, shampoos, lotions', 45),
(3, 'PR002', 'Oral Care', 'Toothpaste, toothbrush, mouthwash', 35),
(4, 'EL001', 'Batteries', 'AA, AAA, 9V batteries', 25),
(4, 'EL002', 'Cables', 'USB, charging cables', 20);

-- Insert Sample Products
INSERT OR IGNORE INTO product (product_code, product_name, description, brand, base_price, unit_of_measure, subcategory_id, discount_percentage, created_by) VALUES 
('GR001001', 'Fresh Milk 1L', 'Fresh whole milk 1 liter pack', 'Highland', 450.00, 'Bottle', 1, 0.00, 1),
('GR001002', 'Cheddar Cheese 200g', 'Aged cheddar cheese slice pack', 'Anchor', 890.00, 'Pack', 1, 5.00, 1),
('GR002001', 'Coca Cola 330ml', 'Coca Cola soft drink can', 'Coca-Cola', 120.00, 'Can', 2, 0.00, 1),
('GR002002', 'Orange Juice 1L', 'Fresh orange juice carton', 'Minute Maid', 380.00, 'Carton', 2, 0.00, 1),
('GR003001', 'Potato Chips 150g', 'Salted potato chips family pack', 'Lays', 250.00, 'Pack', 3, 10.00, 1),
('HH001001', 'Dishwashing Liquid 500ml', 'Concentrated dishwashing liquid', 'Sunlight', 320.00, 'Bottle', 5, 0.00, 1),
('HH001002', 'Laundry Powder 1kg', 'High efficiency laundry detergent', 'Surf', 580.00, 'Box', 5, 0.00, 1),
('PR001001', 'Shampoo 400ml', 'Anti-dandruff shampoo for all hair types', 'Head & Shoulders', 950.00, 'Bottle', 7, 15.00, 1),
('PR002001', 'Toothpaste 100ml', 'Fluoride toothpaste for cavity protection', 'Colgate', 180.00, 'Tube', 8, 0.00, 1),
('EL001001', 'AA Batteries 4-Pack', 'Alkaline AA batteries pack of 4', 'Duracell', 450.00, 'Pack', 9, 0.00, 1);

-- Insert Sample Employee
INSERT OR IGNORE INTO user (user_code, email, password_hash, first_name, last_name, role_id, phone, address) VALUES 
('EM001', 'cashier@syos.com', 'cashier123', 'John', 'Doe', 2, '0771234567', 'Colombo 03'),
('EM002', 'manager@syos.com', 'manager123', 'Jane', 'Smith', 4, '0777654321', 'Colombo 05');

-- Insert Sample Customer
INSERT OR IGNORE INTO user (user_code, email, password_hash, first_name, last_name, role_id, phone, address) VALUES 
('CU001', 'customer1@email.com', 'customer123', 'Alice', 'Johnson', 3, '0712345678', 'Colombo 07'),
('CU002', 'customer2@email.com', 'customer456', 'Bob', 'Wilson', 3, '0765432109', 'Kandy');