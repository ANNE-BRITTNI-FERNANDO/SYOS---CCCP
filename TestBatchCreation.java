import java.sql.*;

public class TestBatchCreation {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/syos_inventory.db";
            
            try (Connection conn = DriverManager.getConnection(url)) {
                System.out.println("Available inventory locations:");
                String sql = "SELECT location_id, location_code, location_name FROM inventory_location WHERE is_active = 1";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        System.out.printf("ID: %d, Code: %s, Name: %s%n", 
                            rs.getInt("location_id"),
                            rs.getString("location_code"), 
                            rs.getString("location_name"));
                    }
                }
                
                System.out.println("\nStock movement types allowed:");
                // This will show us what movement types we can use
                try {
                    String testSql = "SELECT name FROM pragma_table_info('stock_movement') WHERE name = 'movement_type'";
                    PreparedStatement stmt = conn.prepareStatement(testSql);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        System.out.println("Movement type column exists");
                    }
                } catch (Exception e) {
                    System.out.println("Error checking movement types: " + e.getMessage());
                }
                
                System.out.println("\nTesting batch creation workflow...");
                System.out.println("1. WAREHOUSE - receives bulk inventory from suppliers");
                System.out.println("2. ONLINE - direct-to-online inventory for e-commerce");
                System.out.println("This matches the business flow: WAREHOUSE → SHELF → CUSTOMER (physical)");
                System.out.println("                              or ONLINE → CUSTOMER (e-commerce)");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}