import java.sql.*;

/**
 * Test the new reorder alert generation system
 */
public class TestReorderAlertGeneration {
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    public static void main(String[] args) {
        System.out.println("Testing Reorder Alert Generation System");
        System.out.println("======================================");
        
        // First, check current product stock
        checkCurrentProductStock();
        
        // Generate alerts using the new system
        System.out.println("\n🔄 Testing generateReorderAlerts method...");
        testGenerateReorderAlerts();
        
        // Check what alerts were generated
        checkGeneratedAlerts();
        
        // Test the UI display method
        testDisplayReorderAlerts();
    }
    
    private static void checkCurrentProductStock() {
        System.out.println("\n📊 Current Product Stock Status:");
        System.out.println("─".repeat(50));
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT p.product_code, p.product_name, " +
                           "COALESCE(SUM(pi.current_quantity), 0) as total_stock " +
                           "FROM product p " +
                           "LEFT JOIN batch b ON p.product_id = b.product_id " +
                           "LEFT JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                           "GROUP BY p.product_code, p.product_name " +
                           "ORDER BY total_stock ASC";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        String productCode = rs.getString("product_code");
                        String productName = rs.getString("product_name");
                        int totalStock = rs.getInt("total_stock");
                        
                        String status = "✅ OK";
                        if (totalStock < 50) {
                            status = "🔴 CRITICAL";
                        } else if (totalStock < 80) {
                            status = "🟡 CONSIDER";
                        }
                        
                        System.out.printf("%-15s %-25s %3d units %s%n", 
                            productCode, productName, totalStock, status);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking product stock: " + e.getMessage());
        }
    }
    
    private static void testGenerateReorderAlerts() {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Clear existing alerts first
                String clearSql = "DELETE FROM reorder_alert WHERE alert_type LIKE 'PRODUCT_%'";
                conn.createStatement().executeUpdate(clearSql);
                System.out.println("✅ Cleared existing product-level alerts");
                
                // This simulates calling the generateReorderAlerts method
                // Get all unique products that have physical inventory
                String productSql = "SELECT DISTINCT p.product_code FROM physical_inventory pi " +
                                  "JOIN batch b ON pi.batch_id = b.batch_id " +
                                  "JOIN product p ON b.product_id = p.product_id";
                ResultSet productRs = conn.createStatement().executeQuery(productSql);
                
                int alertsGenerated = 0;
                
                while (productRs.next()) {
                    String productCode = productRs.getString("product_code");
                    int totalStock = getTotalStock(conn, productCode);
                    String velocity = getSalesVelocityCategory(conn, productCode);
                    
                    String alertType = null;
                    
                    // Apply TestBetterReorderLogic rules
                    if (totalStock < 50) {
                        // Critical: Always alert for products below 50 units
                        alertType = "PRODUCT_CRITICAL";
                        alertsGenerated++;
                    } else if ("FAST".equals(velocity) && totalStock < 80) {
                        // Fast-moving products between 50-80 units need consideration
                        alertType = "PRODUCT_CONSIDER";
                        alertsGenerated++;
                    }
                    
                    // Insert alert if criteria met
                    if (alertType != null) {
                        String insertSql = "INSERT INTO reorder_alert " +
                            "(product_code, current_quantity, threshold_quantity, alert_type, status, product_id, location_id, created_at) " +
                            "VALUES (?, ?, ?, ?, 'ACTIVE', (SELECT product_id FROM product WHERE product_code = ?), 1, datetime('now'))";
                        
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, productCode);
                            insertStmt.setInt(2, totalStock);
                            insertStmt.setInt(3, "FAST".equals(velocity) ? 80 : 50);
                            insertStmt.setString(4, alertType);
                            insertStmt.setString(5, productCode); // For the subquery
                            insertStmt.executeUpdate();
                            
                            System.out.println("🔔 Generated " + alertType + " alert for " + productCode + 
                                             " (" + velocity + " velocity, " + totalStock + " units)");
                        }
                    }
                }
                
                System.out.println("\n✅ Generated " + alertsGenerated + " reorder alerts");
            }
        } catch (Exception e) {
            System.err.println("Error generating reorder alerts: " + e.getMessage());
        }
    }
    
    private static int getTotalStock(Connection conn, String productCode) {
        String sql = "SELECT COALESCE(SUM(current_quantity), 0) as total_quantity " +
                    "FROM physical_inventory pi " +
                    "JOIN batch b ON pi.batch_id = b.batch_id " +
                    "JOIN product p ON b.product_id = p.product_id " +
                    "WHERE p.product_code = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total_quantity");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total stock for product " + productCode + ": " + e.getMessage());
        }
        
        return 0;
    }
    
    private static String getSalesVelocityCategory(Connection conn, String productCode) {
        String sql = "SELECT COALESCE(SUM(ti.quantity), 0) as total_sold_30_days " +
                    "FROM sales_transaction st " +
                    "INNER JOIN transaction_items ti ON st.transaction_id = ti.transaction_id " +
                    "WHERE ti.product_code = ? AND st.transaction_date >= date('now', '-30 days')";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int totalSold30Days = rs.getInt("total_sold_30_days");
                
                // Based on TestBetterReorderLogic velocity classification
                if (totalSold30Days >= 15) {
                    return "FAST";
                } else if (totalSold30Days >= 8) {
                    return "MEDIUM";
                } else {
                    return "SLOW";
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating velocity for product " + productCode + ": " + e.getMessage());
        }
        
        return "SLOW"; // Default to SLOW if error
    }
    
    private static void checkGeneratedAlerts() {
        System.out.println("\n📋 Generated Reorder Alerts:");
        System.out.println("─".repeat(80));
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT ra.product_code, ra.current_quantity, ra.threshold_quantity, " +
                           "ra.alert_type, ra.status, ra.created_at " +
                           "FROM reorder_alert ra " +
                           "WHERE ra.alert_type LIKE 'PRODUCT_%' " +
                           "ORDER BY ra.created_at DESC";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    if (!rs.next()) {
                        System.out.println("❌ No product-level alerts found in database");
                        return;
                    }
                    
                    do {
                        String productCode = rs.getString("product_code");
                        int currentQty = rs.getInt("current_quantity");
                        int thresholdQty = rs.getInt("threshold_quantity");
                        String alertType = rs.getString("alert_type");
                        String status = rs.getString("status");
                        String created = rs.getString("created_at");
                        
                        String priority = alertType.equals("PRODUCT_CRITICAL") ? "🔴 CRITICAL" : "🟡 CONSIDER";
                        
                        System.out.printf("%-15s %s Stock: %d/%d %s (%s)%n", 
                            productCode, priority, currentQty, thresholdQty, status, created);
                    } while (rs.next());
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking generated alerts: " + e.getMessage());
        }
    }
    
    private static void testDisplayReorderAlerts() {
        System.out.println("\n🖥️  Testing UI Display Method:");
        System.out.println("─".repeat(50));
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // This simulates the displayReorderAlerts method
                String sql = "SELECT p.product_code, p.product_name, ra.current_quantity, " +
                           "ra.alert_type, ra.created_at " +
                           "FROM reorder_alert ra " +
                           "LEFT JOIN product p ON ra.product_id = p.product_id " +
                           "WHERE ra.status = 'ACTIVE' " +
                           "ORDER BY ra.created_at DESC";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    System.out.printf("%-15s %-25s %-10s %-15s %-10s%n", 
                        "Product Code", "Product Name", "Stock", "Alert Type", "Status");
                    System.out.println("─".repeat(80));
                    
                    boolean hasResults = false;
                    while (rs.next()) {
                        hasResults = true;
                        String productCode = rs.getString("product_code");
                        String productName = rs.getString("product_name");
                        int currentStock = rs.getInt("current_quantity");
                        String alertType = rs.getString("alert_type");
                        
                        String status = "✅ IMPROVED";
                        if (currentStock < 50) {
                            status = "🔴 URGENT";
                        } else if (alertType.equals("PRODUCT_CONSIDER")) {
                            status = "🟡 CONSIDER";
                        }
                        
                        System.out.printf("%-15s %-25s %-10d %-15s %s%n",
                            productCode, productName, currentStock, alertType, status);
                    }
                    
                    if (!hasResults) {
                        System.out.println("No active reorder alerts found.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error testing display method: " + e.getMessage());
        }
    }
}