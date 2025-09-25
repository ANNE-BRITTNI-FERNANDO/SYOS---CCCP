import java.sql.*;

public class DebugReorderAlerts {
    private static final String DATABASE_PATH = "syos_inventory.db";
    
    public static void main(String[] args) {
        System.out.println("🔍 DEBUGGING REORDER ALERTS TABLE...");
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Check all alerts in reorder_alert table
                System.out.println("\n📋 ALL ALERTS IN REORDER_ALERT TABLE:");
                String allAlertsSql = "SELECT * FROM reorder_alert ORDER BY created_at DESC";
                try (PreparedStatement stmt = conn.prepareStatement(allAlertsSql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    boolean hasData = false;
                    while (rs.next()) {
                        hasData = true;
                        System.out.println("Alert ID: " + rs.getInt("alert_id"));
                        System.out.println("Product Code: " + rs.getString("product_code"));
                        System.out.println("Product ID: " + rs.getInt("product_id"));
                        System.out.println("Location ID: " + rs.getInt("location_id"));
                        System.out.println("Current Qty: " + rs.getInt("current_quantity"));
                        System.out.println("Alert Type: " + rs.getString("alert_type"));
                        System.out.println("Status: " + rs.getString("status"));
                        System.out.println("Created: " + rs.getString("created_at"));
                        System.out.println("--------------------");
                    }
                    
                    if (!hasData) {
                        System.out.println("❌ NO ALERTS FOUND IN TABLE!");
                    }
                }
                
                // Check the exact query used in displayReorderAlerts
                System.out.println("\n🔍 TESTING DISPLAY QUERY (Last 7 days):");
                String displaySql = "SELECT ra.alert_id, p.product_code, p.product_name, " +
                                  "ra.current_quantity, ra.alert_type, ra.created_at, " +
                                  "il.location_name " +
                                  "FROM reorder_alert ra " +
                                  "JOIN product p ON ra.product_id = p.product_id " +
                                  "JOIN inventory_location il ON ra.location_id = il.location_id " +
                                  "WHERE DATE(ra.created_at) >= DATE('now', '-7 days') " +
                                  "ORDER BY ra.created_at DESC";
                
                try (PreparedStatement stmt = conn.prepareStatement(displaySql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    boolean hasData = false;
                    while (rs.next()) {
                        hasData = true;
                        System.out.println("Product: " + rs.getString("product_code") + " - " + rs.getString("product_name"));
                        System.out.println("Location: " + rs.getString("location_name"));
                        System.out.println("Current Qty: " + rs.getInt("current_quantity"));
                        System.out.println("Alert Type: " + rs.getString("alert_type"));
                        System.out.println("Created: " + rs.getString("created_at"));
                        System.out.println("--------------------");
                    }
                    
                    if (!hasData) {
                        System.out.println("❌ NO RESULTS FROM DISPLAY QUERY!");
                    }
                }
                
                // Check if PRD-LAFA0003 exists as a product
                System.out.println("\n🔍 CHECKING PRD-LAFA0003 PRODUCT:");
                String productCheckSql = "SELECT product_id, product_name FROM product WHERE product_code = 'PRD-LAFA0003'";
                try (PreparedStatement stmt = conn.prepareStatement(productCheckSql)) {
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        System.out.println("✅ Product found: ID=" + rs.getInt("product_id") + 
                                         ", Name=" + rs.getString("product_name"));
                    } else {
                        System.out.println("❌ PRD-LAFA0003 NOT FOUND IN PRODUCT TABLE!");
                    }
                }
                
                // Check inventory_location table
                System.out.println("\n🔍 CHECKING INVENTORY_LOCATION TABLE:");
                String locationSql = "SELECT location_id, location_name FROM inventory_location";
                try (PreparedStatement stmt = conn.prepareStatement(locationSql)) {
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        System.out.println("Location ID: " + rs.getInt("location_id") + 
                                         ", Name: " + rs.getString("location_name"));
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error debugging alerts: " + e.getMessage());
            e.printStackTrace();
        }
    }
}