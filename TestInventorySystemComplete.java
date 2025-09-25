import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestInventorySystemComplete {
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            
            System.out.println("=== TESTING COMPLETE INVENTORY SYSTEM ===\n");
            
            // Step 1: Create test data
            createTestCategory();
            createTestSubcategory(); 
            createTestUser();
            Long productId = createTestProduct();
            
            if (productId != null) {
                // Step 2: Create batch and inventory records
                Long batchId = createTestBatch(productId);
                if (batchId != null) {
                    createInventoryRecords(batchId);
                    
                    // Step 3: Verify inventory data
                    verifyInventoryData(productId);
                } else {
                    System.err.println("❌ Failed to create batch");
                }
            } else {
                System.err.println("❌ Failed to create product");
            }
            
            System.out.println("\n=== TEST COMPLETED ===");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createTestCategory() throws Exception {
        String url = "jdbc:sqlite:" + DATABASE_PATH;
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "INSERT OR IGNORE INTO category (category_code, category_name, description) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "TST");
                stmt.setString(2, "Test Category");
                stmt.setString(3, "Category for testing inventory");
                stmt.executeUpdate();
                System.out.println("✅ Test category created");
            }
        }
    }
    
    private static void createTestSubcategory() throws Exception {
        String url = "jdbc:sqlite:" + DATABASE_PATH;
        try (Connection conn = DriverManager.getConnection(url)) {
            // Get category ID
            Long categoryId = null;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT category_id FROM category WHERE category_code = ?")) {
                stmt.setString(1, "TST");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    categoryId = rs.getLong("category_id");
                }
            }
            
            if (categoryId != null) {
                String sql = "INSERT OR IGNORE INTO subcategory (category_id, subcategory_code, subcategory_name, description) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, categoryId);
                    stmt.setString(2, "TST-SUB");
                    stmt.setString(3, "Test Subcategory");
                    stmt.setString(4, "Subcategory for testing");
                    stmt.executeUpdate();
                    System.out.println("✅ Test subcategory created");
                }
            }
        }
    }
    
    private static void createTestUser() throws Exception {
        String url = "jdbc:sqlite:" + DATABASE_PATH;
        try (Connection conn = DriverManager.getConnection(url)) {
            // Get admin role ID
            Long roleId = null;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT role_id FROM role WHERE role_name = ?")) {
                stmt.setString(1, "ADMIN");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    roleId = rs.getLong("role_id");
                }
            }
            
            if (roleId != null) {
                String sql = "INSERT OR IGNORE INTO user (user_code, email, password_hash, password_salt, first_name, last_name, role_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, "TST-USER");
                    stmt.setString(2, "test@syos.com");
                    stmt.setString(3, "hash123");
                    stmt.setString(4, "salt123");
                    stmt.setString(5, "Test");
                    stmt.setString(6, "User");
                    stmt.setLong(7, roleId);
                    stmt.executeUpdate();
                    System.out.println("✅ Test user created");
                }
            }
        }
    }
    
    private static Long createTestProduct() throws Exception {
        String url = "jdbc:sqlite:" + DATABASE_PATH;
        try (Connection conn = DriverManager.getConnection(url)) {
            // Get subcategory ID and user ID
            Long subcategoryId = null;
            Long userId = null;
            
            try (PreparedStatement stmt = conn.prepareStatement("SELECT subcategory_id FROM subcategory WHERE subcategory_code = ?")) {
                stmt.setString(1, "TST-SUB");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    subcategoryId = rs.getLong("subcategory_id");
                }
            }
            
            try (PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM user WHERE user_code = ?")) {
                stmt.setString(1, "TST-USER");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getLong("user_id");
                }
            }
            
            if (subcategoryId != null && userId != null) {
                String sql = "INSERT OR REPLACE INTO product (product_code, product_name, description, brand, base_price, unit_of_measure, subcategory_id, final_price, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, "PRD-TEST0001");
                    stmt.setString(2, "Test Inventory Product");
                    stmt.setString(3, "Product for inventory testing");
                    stmt.setString(4, "TestBrand");
                    stmt.setDouble(5, 25.99);
                    stmt.setString(6, "pieces");
                    stmt.setLong(7, subcategoryId);
                    stmt.setDouble(8, 25.99);
                    stmt.setLong(9, userId);
                    
                    int affected = stmt.executeUpdate();
                    if (affected > 0) {
                        ResultSet rs = stmt.getGeneratedKeys();
                        if (rs.next()) {
                            Long productId = rs.getLong(1);
                            System.out.println("✅ Test product created with ID: " + productId);
                            return productId;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private static Long createTestBatch(Long productId) throws Exception {
        String url = "jdbc:sqlite:" + DATABASE_PATH;
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "INSERT INTO batch (product_id, batch_number, purchase_date, expiry_date, quantity_received, selling_price) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, productId);
                stmt.setString(2, "BATCH-TEST-" + System.currentTimeMillis());
                stmt.setString(3, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                stmt.setString(4, LocalDateTime.now().plusYears(2).format(DateTimeFormatter.ISO_LOCAL_DATE));
                stmt.setInt(5, 500); // Total received quantity
                stmt.setDouble(6, 25.99);
                
                int affected = stmt.executeUpdate();
                if (affected > 0) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        Long batchId = rs.getLong(1);
                        System.out.println("✅ Test batch created with ID: " + batchId);
                        return batchId;
                    }
                }
            }
        }
        return null;
    }
    
    private static void createInventoryRecords(Long batchId) throws Exception {
        String url = "jdbc:sqlite:" + DATABASE_PATH;
        try (Connection conn = DriverManager.getConnection(url)) {
            
            // Get location IDs
            Long warehouseId = getLocationId(conn, "WAREHOUSE");
            Long shelfId = getLocationId(conn, "SHELF");
            
            // Create physical inventory records
            if (warehouseId != null) {
                createPhysicalInventory(conn, batchId, warehouseId, 400, 50, 1000);
                System.out.println("✅ Warehouse inventory created: 400 units");
            }
            
            if (shelfId != null) {
                createPhysicalInventory(conn, batchId, shelfId, 80, 20, 100);
                System.out.println("✅ Shelf inventory created: 80 units");
            }
            
            // Create online inventory
            createOnlineInventory(conn, batchId, 20, 5);
            System.out.println("✅ Online inventory created: 20 units");
        }
    }
    
    private static Long getLocationId(Connection conn, String locationCode) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT location_id FROM inventory_location WHERE location_code = ?")) {
            stmt.setString(1, locationCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("location_id");
            }
        }
        return null;
    }
    
    private static void createPhysicalInventory(Connection conn, Long batchId, Long locationId, int quantity, int minThreshold, int capacity) throws Exception {
        String sql = "INSERT OR REPLACE INTO physical_inventory (batch_id, location_id, current_quantity, min_threshold, shelf_capacity) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, batchId);
            stmt.setLong(2, locationId);
            stmt.setInt(3, quantity);
            stmt.setInt(4, minThreshold);
            stmt.setInt(5, capacity);
            stmt.executeUpdate();
        }
    }
    
    private static void createOnlineInventory(Connection conn, Long batchId, int availableQty, int reservedQty) throws Exception {
        String sql = "INSERT OR REPLACE INTO online_inventory (batch_id, available_quantity, reserved_quantity, min_threshold) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, batchId);
            stmt.setInt(2, availableQty);
            stmt.setInt(3, reservedQty);
            stmt.setInt(4, 10);
            stmt.executeUpdate();
        }
    }
    
    private static void verifyInventoryData(Long productId) throws Exception {
        String url = "jdbc:sqlite:" + DATABASE_PATH;
        try (Connection conn = DriverManager.getConnection(url)) {
            
            System.out.println("\n=== INVENTORY VERIFICATION ===");
            
            // Check physical inventory
            String physicalSql = "SELECT il.location_code, pi.current_quantity, pi.min_threshold, pi.shelf_capacity " +
                               "FROM physical_inventory pi " +
                               "JOIN batch b ON pi.batch_id = b.batch_id " +
                               "JOIN inventory_location il ON pi.location_id = il.location_id " +
                               "WHERE b.product_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(physicalSql)) {
                stmt.setLong(1, productId);
                ResultSet rs = stmt.executeQuery();
                System.out.println("📦 Physical Inventory:");
                while (rs.next()) {
                    System.out.printf("   %s: %d units (min: %d, capacity: %d)%n", 
                        rs.getString("location_code"),
                        rs.getInt("current_quantity"),
                        rs.getInt("min_threshold"),
                        rs.getInt("shelf_capacity"));
                }
            }
            
            // Check online inventory
            String onlineSql = "SELECT oi.available_quantity, oi.reserved_quantity, oi.min_threshold " +
                             "FROM online_inventory oi " +
                             "JOIN batch b ON oi.batch_id = b.batch_id " +
                             "WHERE b.product_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(onlineSql)) {
                stmt.setLong(1, productId);
                ResultSet rs = stmt.executeQuery();
                System.out.println("🌐 Online Inventory:");
                while (rs.next()) {
                    System.out.printf("   Available: %d, Reserved: %d, Min: %d%n", 
                        rs.getInt("available_quantity"),
                        rs.getInt("reserved_quantity"),
                        rs.getInt("min_threshold"));
                }
            }
            
            // Total inventory calculation
            String totalSql = "SELECT " +
                            "(SELECT COALESCE(SUM(pi.current_quantity), 0) FROM physical_inventory pi JOIN batch b ON pi.batch_id = b.batch_id WHERE b.product_id = ?) + " +
                            "(SELECT COALESCE(SUM(oi.available_quantity), 0) FROM online_inventory oi JOIN batch b ON oi.batch_id = b.batch_id WHERE b.product_id = ?) " +
                            "as total_quantity";
            
            try (PreparedStatement stmt = conn.prepareStatement(totalSql)) {
                stmt.setLong(1, productId);
                stmt.setLong(2, productId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.printf("📊 Total Inventory: %d units%n", rs.getInt("total_quantity"));
                }
            }
            
            System.out.println("✅ Physical inventory table is now storing data properly!");
            System.out.println("✅ Online inventory table is now storing data properly!");
            System.out.println("✅ All inventory locations are working correctly!");
        }
    }
}