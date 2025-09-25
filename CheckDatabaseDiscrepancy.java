import java.sql.*;

public class CheckDatabaseDiscrepancy {
    public static void main(String[] args) {
        String dbPath = "data/syos_inventory.db";
        
        System.out.println("=== DATABASE DISCREPANCY INVESTIGATION ===");
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            
            // Check total products in database
            System.out.println("\n1. PRODUCT COUNT CHECK:");
            String countSql = "SELECT COUNT(*) as total FROM product";
            try (PreparedStatement stmt = conn.prepareStatement(countSql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    System.out.println("   Total products in DB: " + rs.getInt("total"));
                }
            }
            
            // List all products with their IDs
            System.out.println("\n2. ALL PRODUCTS IN DATABASE:");
            String productsSql = "SELECT product_id, product_code, product_name FROM product ORDER BY product_id";
            try (PreparedStatement stmt = conn.prepareStatement(productsSql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    System.out.printf("   ID: %d | Code: %s | Name: %s%n", 
                                    rs.getInt("product_id"), 
                                    rs.getString("product_code"), 
                                    rs.getString("product_name"));
                }
            }
            
            // Check batch count
            System.out.println("\n3. BATCH COUNT CHECK:");
            String batchCountSql = "SELECT COUNT(*) as total FROM batch";
            try (PreparedStatement stmt = conn.prepareStatement(batchCountSql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    System.out.println("   Total batches in DB: " + rs.getInt("total"));
                }
            }
            
            // Check physical inventory count
            System.out.println("\n4. PHYSICAL INVENTORY COUNT CHECK:");
            String inventoryCountSql = "SELECT COUNT(*) as total FROM physical_inventory";
            try (PreparedStatement stmt = conn.prepareStatement(inventoryCountSql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    System.out.println("   Total physical inventory records: " + rs.getInt("total"));
                }
            }
            
            // Show all physical inventory records
            System.out.println("\n5. ALL PHYSICAL INVENTORY RECORDS:");
            String allInventorySql = "SELECT pi.*, p.product_code, p.product_name FROM physical_inventory pi " +
                                   "JOIN batch b ON pi.batch_id = b.batch_id " +
                                   "JOIN product p ON b.product_id = p.product_id";
            try (PreparedStatement stmt = conn.prepareStatement(allInventorySql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    System.out.printf("   Inventory ID: %d | Batch: %d | Location: %d | Quantity: %d | Product: %s (%s)%n",
                                    rs.getInt("inventory_id"),
                                    rs.getInt("batch_id"),
                                    rs.getInt("location_id"), 
                                    rs.getInt("current_quantity"),
                                    rs.getString("product_code"),
                                    rs.getString("product_name"));
                }
            }
            
            // Check which products have no inventory
            System.out.println("\n6. PRODUCTS WITHOUT INVENTORY:");
            String noInventorySql = "SELECT p.product_id, p.product_code, p.product_name FROM product p " +
                                  "LEFT JOIN batch b ON p.product_id = b.product_id " +
                                  "WHERE b.batch_id IS NULL";
            try (PreparedStatement stmt = conn.prepareStatement(noInventorySql);
                 ResultSet rs = stmt.executeQuery()) {
                
                boolean hasProductsWithoutInventory = false;
                while (rs.next()) {
                    hasProductsWithoutInventory = true;
                    System.out.printf("   Product ID: %d | Code: %s | Name: %s%n",
                                    rs.getInt("product_id"),
                                    rs.getString("product_code"),
                                    rs.getString("product_name"));
                }
                
                if (!hasProductsWithoutInventory) {
                    System.out.println("   All products have inventory records!");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}