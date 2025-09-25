import java.sql.*;

public class TestFixedInventory {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/syos_inventory.db";
            
            try (Connection conn = DriverManager.getConnection(url)) {
                System.out.println("=== Testing Fixed Inventory System ===");
                
                // Test 1: Check if product exists
                String productCode = "PRD-TEST0001";
                Long productId = getProductId(conn, productCode);
                System.out.println("Product ID for " + productCode + ": " + productId);
                
                if (productId == null) {
                    // Create test product if doesn't exist
                    System.out.println("Creating test product...");
                    productId = createTestProduct(conn, productCode);
                    System.out.println("Created product with ID: " + productId);
                }
                
                // Test 2: Create batch with correct schema
                System.out.println("\n=== Creating Batch ===");
                Long batchId = createBatch(conn, productId, "B-PRD-TEST0001-202412301400", 100);
                System.out.println("Created batch with ID: " + batchId);
                
                // Test 3: Get location IDs
                System.out.println("\n=== Getting Locations ===");
                Long warehouseId = getLocationId(conn, "WAREHOUSE");
                Long shelfId = getLocationId(conn, "SHELF");
                Long onlineId = getLocationId(conn, "ONLINE");
                System.out.println("Warehouse location ID: " + warehouseId);
                System.out.println("Shelf location ID: " + shelfId);
                System.out.println("Online location ID: " + onlineId);
                
                // Test 4: Create physical inventory records
                if (batchId != null && warehouseId != null && shelfId != null) {
                    System.out.println("\n=== Creating Physical Inventory Records ===");
                    createPhysicalInventory(conn, batchId, warehouseId, 50);
                    System.out.println("Created warehouse inventory: 50 units");
                    
                    createPhysicalInventory(conn, batchId, shelfId, 30);
                    System.out.println("Created shelf inventory: 30 units");
                }
                
                // Test 5: Create online inventory record
                if (batchId != null) {
                    System.out.println("\n=== Creating Online Inventory Record ===");
                    createOnlineInventory(conn, batchId, 20);
                    System.out.println("Created online inventory: 20 units");
                }
                
                // Test 6: Query all inventory records
                System.out.println("\n=== Querying Inventory Records ===");
                queryPhysicalInventory(conn, productId);
                queryOnlineInventory(conn, productId);
                
                System.out.println("\n✅ All inventory tests completed successfully!");
            }
        } catch (Exception e) {
            System.err.println("❌ Error in inventory test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Long getProductId(Connection conn, String productCode) throws Exception {
        String sql = "SELECT product_id FROM product WHERE product_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("product_id");
            }
        }
        return null;
    }
    
    private static Long createTestProduct(Connection conn, String productCode) throws Exception {
        String sql = "INSERT INTO product (product_code, name, is_active) VALUES (?, ?, 1)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, productCode);
            stmt.setString(2, "Test Product for Inventory");
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }
    
    private static Long createBatch(Connection conn, Long productId, String batchNumber, int quantity) throws Exception {
        String sql = "INSERT INTO batch (product_id, batch_number, purchase_date, expiry_date, quantity_received, selling_price) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, productId);
            stmt.setString(2, batchNumber);
            stmt.setString(3, "2024-12-30");
            stmt.setString(4, "2026-12-30");
            stmt.setInt(5, quantity);
            stmt.setDouble(6, 25.99);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }
    
    private static Long getLocationId(Connection conn, String locationCode) throws Exception {
        String sql = "SELECT location_id FROM inventory_location WHERE location_code = ? AND is_active = 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, locationCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("location_id");
            }
        }
        return null;
    }
    
    private static void createPhysicalInventory(Connection conn, Long batchId, Long locationId, int quantity) throws Exception {
        String sql = "INSERT INTO physical_inventory (batch_id, location_id, current_quantity, min_threshold, shelf_capacity) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, batchId);
            stmt.setLong(2, locationId);
            stmt.setInt(3, quantity);
            stmt.setInt(4, 50);
            stmt.setInt(5, 100);
            stmt.executeUpdate();
        }
    }
    
    private static void createOnlineInventory(Connection conn, Long batchId, int quantity) throws Exception {
        String sql = "INSERT INTO online_inventory (batch_id, available_quantity, reserved_quantity) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, batchId);
            stmt.setInt(2, quantity);
            stmt.setInt(3, 0);
            stmt.executeUpdate();
        }
    }
    
    private static void queryPhysicalInventory(Connection conn, Long productId) throws Exception {
        String sql = "SELECT il.location_code, pi.current_quantity, pi.min_threshold, pi.shelf_capacity " +
                    "FROM physical_inventory pi " +
                    "JOIN batch b ON pi.batch_id = b.batch_id " +
                    "JOIN inventory_location il ON pi.location_id = il.location_id " +
                    "WHERE b.product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("Physical Inventory Records:");
            while (rs.next()) {
                System.out.printf("  Location: %s | Quantity: %d | Min: %d | Capacity: %d%n",
                    rs.getString("location_code"),
                    rs.getInt("current_quantity"),
                    rs.getInt("min_threshold"),
                    rs.getInt("shelf_capacity"));
            }
        }
    }
    
    private static void queryOnlineInventory(Connection conn, Long productId) throws Exception {
        String sql = "SELECT oi.available_quantity, oi.reserved_quantity " +
                    "FROM online_inventory oi " +
                    "JOIN batch b ON oi.batch_id = b.batch_id " +
                    "WHERE b.product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("Online Inventory Records:");
            while (rs.next()) {
                System.out.printf("  Available: %d | Reserved: %d%n",
                    rs.getInt("available_quantity"),
                    rs.getInt("reserved_quantity"));
            }
        }
    }
}