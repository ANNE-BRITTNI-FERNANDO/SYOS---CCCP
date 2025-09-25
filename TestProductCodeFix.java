import java.sql.*;

/**
 * Quick test to verify our product code fix and database storage
 */
public class TestProductCodeFix {
    public static void main(String[] args) {
        String dbPath = "data/syos_inventory.db";
        
        System.out.println("=== TESTING PRODUCT CODE FIX ===");
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            // Count total products in database
            String countSql = "SELECT COUNT(*) as total FROM product";
            try (PreparedStatement stmt = conn.prepareStatement(countSql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    int totalProducts = rs.getInt("total");
                    System.out.println("Total products in database: " + totalProducts);
                }
            }
            
            // Show recent products with PRD codes
            String recentSql = "SELECT product_code, product_name, created_at FROM product WHERE product_code LIKE 'PRD-%' ORDER BY created_at DESC LIMIT 5";
            System.out.println("\nRecent products with PRD codes:");
            try (PreparedStatement stmt = conn.prepareStatement(recentSql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    System.out.println("- " + rs.getString("product_code") + ": " + rs.getString("product_name"));
                }
            }
            
            // Check if inventory records exist for PRD products
            String inventorySql = "SELECT p.product_code, pi.location_id, pi.current_quantity FROM product p " +
                                "JOIN batch b ON p.product_id = b.product_id " +
                                "JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                                "WHERE p.product_code LIKE 'PRD-%' LIMIT 5";
            System.out.println("\nInventory records for PRD products:");
            try (PreparedStatement stmt = conn.prepareStatement(inventorySql);
                 ResultSet rs = stmt.executeQuery()) {
                
                boolean hasInventory = false;
                while (rs.next()) {
                    hasInventory = true;
                    System.out.println("- " + rs.getString("product_code") + 
                                     " at location " + rs.getInt("location_id") + 
                                     ": " + rs.getInt("current_quantity") + " units");
                }
                
                if (!hasInventory) {
                    System.out.println("- No inventory records found for PRD products");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error checking database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}