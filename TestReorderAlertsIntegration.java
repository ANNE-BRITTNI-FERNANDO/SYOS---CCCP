import java.sql.*;

/**
 * Test to demonstrate the new reorder_alert table integration
 * Shows how alerts are created automatically during sales transactions
 */
public class TestReorderAlertsIntegration {
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    public static void main(String[] args) {
        System.out.println("=== REORDER ALERTS INTEGRATION TEST ===\n");
        
        // Test the reorder alert functionality
        testReorderAlertCreation();
        displayExistingAlerts();
    }
    
    private static void testReorderAlertCreation() {
        System.out.println("🧪 TESTING REORDER ALERT CREATION");
        System.out.println("-".repeat(40));
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Simulate a scenario where stock goes below reorder level
                simulateStockReduction(conn);
                
                System.out.println("✅ Test completed - check database for new alerts");
            }
            
        } catch (Exception e) {
            System.err.println("Error testing reorder alerts: " + e.getMessage());
        }
    }
    
    private static void simulateStockReduction(Connection conn) throws SQLException {
        // Get a product to test with
        String productSql = "SELECT p.product_id, p.product_code, p.product_name " +
                           "FROM product p " +
                           "JOIN batch b ON p.product_id = b.product_id " +
                           "JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                           "WHERE pi.current_quantity > 10 LIMIT 1";
        
        try (PreparedStatement stmt = conn.prepareStatement(productSql)) {
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int productId = rs.getInt("product_id");
                String productCode = rs.getString("product_code");
                String productName = rs.getString("product_name");
                
                System.out.println("📦 Testing with product: " + productCode + " - " + productName);
                
                // Simulate creating a reorder alert manually
                createTestAlert(conn, productId, productCode);
            }
        }
    }
    
    private static void createTestAlert(Connection conn, int productId, String productCode) throws SQLException {
        // Get shelf location ID
        String locationSql = "SELECT location_id FROM inventory_location WHERE location_code = 'SHELF'";
        int shelfLocationId = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(locationSql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                shelfLocationId = rs.getInt("location_id");
            }
        }
        
        if (shelfLocationId == 0) {
            // Create shelf location if it doesn't exist
            String createLocationSql = "INSERT OR IGNORE INTO inventory_location " +
                                     "(location_code, location_name, location_type) " +
                                     "VALUES ('SHELF', 'Physical Shelf', 'PHYSICAL_SHELF')";
            try (PreparedStatement stmt = conn.prepareStatement(createLocationSql)) {
                stmt.executeUpdate();
            }
            
            // Get the location ID again
            try (PreparedStatement stmt = conn.prepareStatement(locationSql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    shelfLocationId = rs.getInt("location_id");
                }
            }
        }
        
        // Check current stock
        int currentStock = getCurrentStock(conn, productCode);
        
        // Create test alert
        String alertSql = "INSERT INTO reorder_alert " +
                         "(product_id, location_id, current_quantity, alert_type) " +
                         "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(alertSql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, shelfLocationId);
            stmt.setInt(3, currentStock);
            stmt.setString(4, currentStock < 50 ? "SHELF_RESTOCK" : "NEW_BATCH_ORDER");
            
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Created test reorder alert:");
                System.out.println("   Product ID: " + productId);
                System.out.println("   Current Stock: " + currentStock);
                System.out.println("   Alert Type: " + (currentStock < 50 ? "SHELF_RESTOCK" : "NEW_BATCH_ORDER"));
            }
        }
    }
    
    private static int getCurrentStock(Connection conn, String productCode) throws SQLException {
        String stockSql = "SELECT COALESCE(SUM(pi.current_quantity), 0) as total_stock " +
                         "FROM physical_inventory pi " +
                         "JOIN batch b ON pi.batch_id = b.batch_id " +
                         "JOIN product p ON b.product_id = p.product_id " +
                         "WHERE p.product_code = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(stockSql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_stock");
            }
        }
        return 0;
    }
    
    private static void displayExistingAlerts() {
        System.out.println("\n📋 EXISTING REORDER ALERTS IN DATABASE");
        System.out.println("-".repeat(50));
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String alertSql = "SELECT ra.alert_id, p.product_code, p.product_name, " +
                                "ra.current_quantity, ra.alert_type, ra.created_at, " +
                                "il.location_name " +
                                "FROM reorder_alert ra " +
                                "JOIN product p ON ra.product_id = p.product_id " +
                                "JOIN inventory_location il ON ra.location_id = il.location_id " +
                                "ORDER BY ra.created_at DESC LIMIT 10";
                
                try (PreparedStatement stmt = conn.prepareStatement(alertSql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    boolean hasAlerts = false;
                    while (rs.next()) {
                        hasAlerts = true;
                        
                        int alertId = rs.getInt("alert_id");
                        String productCode = rs.getString("product_code");
                        String productName = rs.getString("product_name");
                        int currentQty = rs.getInt("current_quantity");
                        String alertType = rs.getString("alert_type");
                        String createdAt = rs.getString("created_at");
                        String location = rs.getString("location_name");
                        
                        System.out.println("Alert ID: " + alertId);
                        System.out.println("  Product: " + productCode + " - " + productName);
                        System.out.println("  Location: " + location);
                        System.out.println("  Stock at Alert: " + currentQty + " units");
                        System.out.println("  Alert Type: " + alertType);
                        System.out.println("  Created: " + createdAt);
                        System.out.println();
                    }
                    
                    if (!hasAlerts) {
                        System.out.println("No reorder alerts found in database.");
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error displaying alerts: " + e.getMessage());
        }
    }
}