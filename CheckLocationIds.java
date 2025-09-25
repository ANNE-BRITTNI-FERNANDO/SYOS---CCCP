import java.sql.*;

public class CheckLocationIds {
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                System.out.println("=== INVENTORY LOCATIONS ===");
                String locationQuery = "SELECT location_id, location_code, location_name FROM inventory_location";
                try (PreparedStatement stmt = conn.prepareStatement(locationQuery)) {
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        System.out.printf("ID: %d | Code: %s | Name: %s%n",
                            rs.getInt("location_id"),
                            rs.getString("location_code"),
                            rs.getString("location_name"));
                    }
                }
                
                System.out.println("\n=== REORDER ALERTS WITH DETAILED JOIN ===");
                String alertQuery = "SELECT ra.alert_id, ra.product_code, " +
                                  "ra.current_quantity, ra.alert_type, ra.created_at, " +
                                  "ra.location_id, il.location_name, p.product_name " +
                                  "FROM reorder_alert ra " +
                                  "LEFT JOIN inventory_location il ON ra.location_id = il.location_id " +
                                  "LEFT JOIN product p ON ra.product_id = p.product_id " +
                                  "WHERE DATE(ra.created_at) >= DATE('now', '-7 days') " +
                                  "ORDER BY ra.created_at DESC";
                
                try (PreparedStatement stmt = conn.prepareStatement(alertQuery)) {
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        System.out.printf("Alert ID: %d | Product: %s (%s) | Location ID: %s | Location Name: %s | Type: %s%n",
                            rs.getInt("alert_id"),
                            rs.getString("product_code"),
                            rs.getString("product_name"),
                            rs.getObject("location_id"),
                            rs.getString("location_name"),
                            rs.getString("alert_type"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}