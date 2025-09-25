import java.sql.*;

public class SeedInventoryLocations {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/syos_inventory.db";
            
            try (Connection conn = DriverManager.getConnection(url)) {
                System.out.println("Seeding inventory locations...");
                
                // Clear existing locations first
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("DELETE FROM inventory_location");
                }
                
                // Create the 3 required locations
                String insertSql = "INSERT INTO inventory_location (location_code, location_name, location_type, is_active) VALUES (?, ?, ?, 1)";
                
                try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    // WAREHOUSE location
                    stmt.setString(1, "WAREHOUSE");
                    stmt.setString(2, "Main Warehouse Storage");
                    stmt.setString(3, "WAREHOUSE");
                    stmt.executeUpdate();
                    System.out.println("✅ Created WAREHOUSE location");
                    
                    // SHELF location  
                    stmt.setString(1, "SHELF");
                    stmt.setString(2, "Physical Display Shelf");
                    stmt.setString(3, "PHYSICAL_SHELF");
                    stmt.executeUpdate();
                    System.out.println("✅ Created SHELF location");
                    
                    // ONLINE location
                    stmt.setString(1, "ONLINE");
                    stmt.setString(2, "Online Store Inventory");
                    stmt.setString(3, "ONLINE_INVENTORY");
                    stmt.executeUpdate();
                    System.out.println("✅ Created ONLINE location");
                }
                
                // Verify locations
                System.out.println("\nVerifying inventory locations:");
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT location_code, location_name, location_type FROM inventory_location ORDER BY location_id");
                    while (rs.next()) {
                        System.out.printf("📍 %s - %s (%s)%n", 
                            rs.getString("location_code"), 
                            rs.getString("location_name"), 
                            rs.getString("location_type"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}