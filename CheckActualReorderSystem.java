import java.sql.*;

public class CheckActualReorderSystem {
    public static void main(String[] args) {
        String dbPath = "data/syos_inventory.db";
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + dbPath;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                
                System.out.println("=== CURRENT TOTAL STOCK FOR PRD-LAFA PRODUCTS ===");
                String stockSQL = "SELECT p.product_code, p.product_name, " +
                                "SUM(pi.current_quantity) as total_stock " +
                                "FROM product p " +
                                "JOIN batch b ON p.product_id = b.product_id " +
                                "JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                                "WHERE p.product_code LIKE 'PRD-LAFA%' " +
                                "GROUP BY p.product_code, p.product_name";
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(stockSQL)) {
                    while (rs.next()) {
                        System.out.printf("Product: %s (%s) | Total Stock: %d units%n",
                            rs.getString("product_code"),
                            rs.getString("product_name"),
                            rs.getInt("total_stock"));
                    }
                }
                
                System.out.println("\n=== REORDER_ALERT TABLE STRUCTURE ===");
                String pragmaSQL = "PRAGMA table_info(reorder_alert)";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(pragmaSQL)) {
                    while (rs.next()) {
                        System.out.printf("Column: %-20s Type: %-15s%n",
                            rs.getString("name"),
                            rs.getString("type"));
                    }
                }
                
                System.out.println("\n=== CURRENT REORDER ALERTS ===");
                String alertSQL = "SELECT ra.*, p.product_code, p.product_name, il.location_code " +
                                "FROM reorder_alert ra " +
                                "JOIN product p ON ra.product_id = p.product_id " +
                                "JOIN inventory_location il ON ra.location_id = il.location_id";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(alertSQL)) {
                    
                    boolean hasAlerts = false;
                    while (rs.next()) {
                        hasAlerts = true;
                        System.out.printf("Product: %s (%s) | Location: %s | Current: %d | Type: %s | Created: %s%n",
                            rs.getString("product_code"),
                            rs.getString("product_name"),
                            rs.getString("location_code"),
                            rs.getInt("current_quantity"),
                            rs.getString("alert_type"),
                            rs.getString("created_at"));
                    }
                    
                    if (!hasAlerts) {
                        System.out.println("No reorder alerts found in database.");
                    }
                }
                
                System.out.println("\n=== CHECKING WHY PRD-LAFA0003 DOESN'T HAVE REORDER ALERT ===");
                String checkSQL = "SELECT p.product_code, p.product_name, il.location_code, " +
                                "pi.current_quantity, pi.min_threshold " +
                                "FROM product p " +
                                "JOIN batch b ON p.product_id = b.product_id " +
                                "JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                                "JOIN inventory_location il ON pi.location_id = il.location_id " +
                                "WHERE p.product_code = 'PRD-LAFA0003'";
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(checkSQL)) {
                    
                    int totalStock = 0;
                    System.out.println("PRD-LAFA0003 Details:");
                    while (rs.next()) {
                        int currentQty = rs.getInt("current_quantity");
                        totalStock += currentQty;
                        System.out.printf("  Location: %s | Current: %d | Min_Threshold: %d | Alert Needed: %s%n",
                            rs.getString("location_code"),
                            currentQty,
                            rs.getInt("min_threshold"),
                            (currentQty <= rs.getInt("min_threshold") ? "YES" : "NO"));
                    }
                    System.out.printf("  TOTAL STOCK: %d units%n", totalStock);
                }
                
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}