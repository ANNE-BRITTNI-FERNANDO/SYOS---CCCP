import java.sql.*;

public class CheckReorderTableExists {
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Check if reorder_alert table exists and its schema
                System.out.println("=== REORDER ALERT TABLE SCHEMA ===");
                String schemaQuery = "PRAGMA table_info(reorder_alert)";
                try (PreparedStatement stmt = conn.prepareStatement(schemaQuery)) {
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        System.out.printf("Column: %-15s Type: %-10s NotNull: %d Default: %s%n",
                            rs.getString("name"), 
                            rs.getString("type"), 
                            rs.getInt("notnull"),
                            rs.getString("dflt_value"));
                    }
                }
                
                System.out.println("\n=== ALL REORDER ALERTS ===");
                String alertQuery = "SELECT ra.alert_id, p.product_code, p.product_name, " +
                                  "ra.current_quantity, ra.alert_type, ra.created_at, " +
                                  "il.location_name, ra.product_code as ra_product_code, " +
                                  "ra.threshold_quantity, ra.status " +
                                  "FROM reorder_alert ra " +
                                  "LEFT JOIN product p ON ra.product_id = p.product_id " +
                                  "LEFT JOIN inventory_location il ON ra.location_id = il.location_id " +
                                  "ORDER BY ra.created_at DESC LIMIT 10";
                
                try (PreparedStatement stmt = conn.prepareStatement(alertQuery)) {
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        System.out.printf("Alert ID: %d | Product: %s (%s) | Type: %s | Qty: %d | Created: %s | Location: %s%n",
                            rs.getInt("alert_id"),
                            rs.getString("product_code"),
                            rs.getString("product_name"),
                            rs.getString("alert_type"),
                            rs.getInt("current_quantity"),
                            rs.getString("created_at"),
                            rs.getString("location_name"));
                    }
                }
                
                System.out.println("\n=== PRODUCT PRD-LAFA0003 DETAILS ===");
                String productQuery = "SELECT p.product_id, p.product_code, p.product_name, " +
                                    "SUM(COALESCE(pi.current_quantity, 0)) as total_stock " +
                                    "FROM product p " +
                                    "LEFT JOIN batch b ON p.product_id = b.product_id " +
                                    "LEFT JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                                    "WHERE p.product_code = 'PRD-LAFA0003' " +
                                    "GROUP BY p.product_id, p.product_code, p.product_name";
                
                try (PreparedStatement stmt = conn.prepareStatement(productQuery)) {
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        System.out.printf("Product ID: %d | Code: %s | Name: %s | Total Stock: %d%n",
                            rs.getInt("product_id"),
                            rs.getString("product_code"),
                            rs.getString("product_name"),
                            rs.getInt("total_stock"));
                    } else {
                        System.out.println("PRD-LAFA0003 not found!");
                    }
                }
                
                System.out.println("\n=== RECENT SALES FOR PRD-LAFA0003 ===");
                String salesQuery = "SELECT st.transaction_code, st.product_code, st.quantity_sold, " +
                                  "st.transaction_date " +
                                  "FROM sales_transaction st " +
                                  "WHERE st.product_code = 'PRD-LAFA0003' " +
                                  "ORDER BY st.transaction_date DESC LIMIT 5";
                
                try (PreparedStatement stmt = conn.prepareStatement(salesQuery)) {
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        System.out.printf("Transaction: %s | Qty: %d | Date: %s%n",
                            rs.getString("transaction_code"),
                            rs.getInt("quantity_sold"),
                            rs.getString("transaction_date"));
                    }
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
