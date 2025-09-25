import java.sql.*;

public class CreateReorderAlertTable {
    private static final String DATABASE_PATH = "syos_inventory.db";
    
    public static void main(String[] args) {
        System.out.println("🔧 CREATING REORDER_ALERT TABLE...");
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                
                // First, let's create the missing tables that should exist
                System.out.println("\n📋 Creating required tables...");
                
                // Create product table if it doesn't exist
                String createProductSql = """
                    CREATE TABLE IF NOT EXISTS product (
                        product_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        product_code VARCHAR(20) UNIQUE NOT NULL,
                        product_name VARCHAR(100) NOT NULL,
                        brand VARCHAR(50),
                        category VARCHAR(50),
                        base_price DECIMAL(10,2),
                        is_active BOOLEAN DEFAULT 1,
                        created_date DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """;
                conn.createStatement().executeUpdate(createProductSql);
                System.out.println("✅ Product table created/verified");
                
                // Create inventory_location table if it doesn't exist
                String createLocationSql = """
                    CREATE TABLE IF NOT EXISTS inventory_location (
                        location_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        location_code VARCHAR(20) UNIQUE NOT NULL,
                        location_name VARCHAR(100) NOT NULL,
                        location_type VARCHAR(20) DEFAULT 'STORAGE',
                        is_active BOOLEAN DEFAULT 1
                    )
                """;
                conn.createStatement().executeUpdate(createLocationSql);
                System.out.println("✅ Inventory location table created/verified");
                
                // Insert default locations
                String insertLocationsSql = """
                    INSERT OR IGNORE INTO inventory_location (location_id, location_code, location_name, location_type) VALUES
                    (1, 'SHELF', 'Physical Display Shelf', 'DISPLAY'),
                    (2, 'WAREHOUSE', 'Main Warehouse Storage', 'STORAGE')
                """;
                conn.createStatement().executeUpdate(insertLocationsSql);
                System.out.println("✅ Default locations inserted");
                
                // Create batch table if it doesn't exist
                String createBatchSql = """
                    CREATE TABLE IF NOT EXISTS batch (
                        batch_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        product_id INTEGER NOT NULL,
                        batch_code VARCHAR(50) UNIQUE NOT NULL,
                        expiry_date DATE,
                        cost_price DECIMAL(10,2),
                        created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (product_id) REFERENCES product(product_id)
                    )
                """;
                conn.createStatement().executeUpdate(createBatchSql);
                System.out.println("✅ Batch table created/verified");
                
                // Create physical_inventory table if it doesn't exist
                String createPhysicalInventorySql = """
                    CREATE TABLE IF NOT EXISTS physical_inventory (
                        inventory_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        batch_id INTEGER NOT NULL,
                        location_id INTEGER NOT NULL,
                        current_quantity INTEGER DEFAULT 0,
                        location_capacity INTEGER DEFAULT 100,
                        min_threshold INTEGER DEFAULT 10,
                        last_updated DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (batch_id) REFERENCES batch(batch_id),
                        FOREIGN KEY (location_id) REFERENCES inventory_location(location_id)
                    )
                """;
                conn.createStatement().executeUpdate(createPhysicalInventorySql);
                System.out.println("✅ Physical inventory table created/verified");
                
                // Create sales_transaction table if it doesn't exist  
                String createSalesTransactionSql = """
                    CREATE TABLE IF NOT EXISTS sales_transaction (
                        transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        transaction_code VARCHAR(50) UNIQUE NOT NULL,
                        customer_name VARCHAR(100),
                        total_amount DECIMAL(10,2),
                        created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                        cashier_id VARCHAR(20)
                    )
                """;
                conn.createStatement().executeUpdate(createSalesTransactionSql);
                System.out.println("✅ Sales transaction table created/verified");
                
                // Create sales_transaction_item table if it doesn't exist
                String createSalesTransactionItemSql = """
                    CREATE TABLE IF NOT EXISTS sales_transaction_item (
                        item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        transaction_id INTEGER NOT NULL,
                        product_code VARCHAR(20) NOT NULL,
                        quantity INTEGER NOT NULL,
                        unit_price DECIMAL(10,2),
                        line_total DECIMAL(10,2),
                        FOREIGN KEY (transaction_id) REFERENCES sales_transaction(transaction_id)
                    )
                """;
                conn.createStatement().executeUpdate(createSalesTransactionItemSql);
                System.out.println("✅ Sales transaction item table created/verified");
                
                // NOW CREATE THE REORDER_ALERT TABLE
                String createReorderAlertSql = """
                    CREATE TABLE IF NOT EXISTS reorder_alert (
                        alert_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        product_id INTEGER,
                        location_id INTEGER NOT NULL,
                        product_code VARCHAR(20),
                        current_quantity INTEGER NOT NULL,
                        threshold_quantity INTEGER,
                        alert_type VARCHAR(20) NOT NULL CHECK (alert_type IN ('LOW_STOCK', 'OUT_OF_STOCK', 'PRODUCT_CRITICAL', 'PRODUCT_CONSIDER')),
                        status VARCHAR(20) DEFAULT 'ACTIVE',
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (product_id) REFERENCES product(product_id),
                        FOREIGN KEY (location_id) REFERENCES inventory_location(location_id)
                    )
                """;
                conn.createStatement().executeUpdate(createReorderAlertSql);
                System.out.println("✅ Reorder alert table created successfully!");
                
                // Insert some sample products for testing
                System.out.println("\n📋 Inserting sample data...");
                
                // Insert sample products
                String insertProductsSql = """
                    INSERT OR IGNORE INTO product (product_id, product_code, product_name, brand, category, base_price) VALUES
                    (1, 'PRD-TEST0001', 'Test Inventory Product', 'TestBrand', 'TEST', 10.00),
                    (2, 'PRD-LAFA0003', 'Lafa Test Product', 'Lafa', 'FOOD', 15.50),
                    (3, 'PRD-LAFA0002', 'Cohoottt', 'Lafa', 'BEVERAGE', 8.75)
                """;
                conn.createStatement().executeUpdate(insertProductsSql);
                System.out.println("✅ Sample products inserted");
                
                // Insert sample batches
                String insertBatchesSql = """
                    INSERT OR IGNORE INTO batch (batch_id, product_id, batch_code, cost_price) VALUES
                    (1, 1, 'BATCH-TEST001', 8.00),
                    (2, 2, 'BATCH-LAFA003', 12.00),
                    (3, 3, 'BATCH-LAFA002', 6.50)
                """;
                conn.createStatement().executeUpdate(insertBatchesSql);
                System.out.println("✅ Sample batches inserted");
                
                // Insert sample inventory
                String insertInventorySql = """
                    INSERT OR IGNORE INTO physical_inventory (batch_id, location_id, current_quantity, location_capacity, min_threshold) VALUES
                    (1, 1, 480, 500, 50),
                    (1, 2, 200, 1000, 100),
                    (2, 1, 45, 100, 20),
                    (2, 2, 0, 500, 50),
                    (3, 1, 4, 50, 10),
                    (3, 2, 25, 200, 30)
                """;
                conn.createStatement().executeUpdate(insertInventorySql);
                System.out.println("✅ Sample inventory inserted");
                
                System.out.println("\n🎉 Database setup complete! Ready for reorder alerts.");
                
            }
            
        } catch (Exception e) {
            System.err.println("Error creating table: " + e.getMessage());
            e.printStackTrace();
        }
    }
}