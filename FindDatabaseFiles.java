import java.sql.*;
import java.io.File;

public class FindDatabaseFiles {
    public static void main(String[] args) {
        System.out.println("=== SEARCHING FOR DATABASE FILES ===");
        
        // Check different possible database locations
        String[] possiblePaths = {
            "data/syos_inventory.db",
            "./data/syos_inventory.db", 
            "syos_inventory.db",
            "./syos_inventory.db",
            "src/main/resources/database/syos_inventory.db",
            "target/classes/database/syos_inventory.db"
        };
        
        System.out.println("\n1. CHECKING DATABASE FILE LOCATIONS:");
        for (String path : possiblePaths) {
            File dbFile = new File(path);
            System.out.printf("   %s: %s (Size: %d bytes)%n", 
                            path, 
                            dbFile.exists() ? "EXISTS" : "NOT FOUND",
                            dbFile.exists() ? dbFile.length() : 0);
        }
        
        // Check what database the application is actually using
        System.out.println("\n2. CHECKING APPLICATION'S ACTUAL DATABASE:");
        try {
            // Read database configuration
            System.out.println("   Checking application properties...");
            
            // Test connection to each existing database
            for (String path : possiblePaths) {
                File dbFile = new File(path);
                if (dbFile.exists()) {
                    System.out.printf("\n   Testing database: %s%n", path);
                    
                    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path)) {
                        // Count products
                        String countSql = "SELECT COUNT(*) as total FROM product";
                        try (PreparedStatement stmt = conn.prepareStatement(countSql);
                             ResultSet rs = stmt.executeQuery()) {
                            
                            if (rs.next()) {
                                int productCount = rs.getInt("total");
                                System.out.printf("     Products in this DB: %d%n", productCount);
                                
                                // Show recent products
                                String recentSql = "SELECT product_code, product_name FROM product ORDER BY product_id DESC LIMIT 3";
                                try (PreparedStatement stmt2 = conn.prepareStatement(recentSql);
                                     ResultSet rs2 = stmt2.executeQuery()) {
                                    
                                    System.out.println("     Recent products:");
                                    while (rs2.next()) {
                                        System.out.printf("       - %s: %s%n", 
                                                        rs2.getString("product_code"), 
                                                        rs2.getString("product_name"));
                                    }
                                }
                            }
                        }
                        
                        // Count physical inventory
                        String inventoryCountSql = "SELECT COUNT(*) as total FROM physical_inventory";
                        try (PreparedStatement stmt = conn.prepareStatement(inventoryCountSql);
                             ResultSet rs = stmt.executeQuery()) {
                            
                            if (rs.next()) {
                                System.out.printf("     Physical inventory records: %d%n", rs.getInt("total"));
                            }
                        }
                        
                    } catch (Exception e) {
                        System.out.printf("     Error accessing %s: %s%n", path, e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}