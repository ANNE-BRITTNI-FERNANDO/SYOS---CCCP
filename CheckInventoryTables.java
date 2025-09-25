import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Check if ACC011 product exists in batch and physical_inventory tables
 */
public class CheckInventoryTables {
    
    public static void main(String[] args) {
        System.out.println("=== Checking Batch and Physical Inventory for ACC011 ===");
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found");
            return;
        }
        
        String dbUrl = "jdbc:sqlite:data/syos_inventory.db";
        
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            
            // Get the product ID for ACC011
            System.out.println("1. ACC011 Product Information:");
            String productSql = "SELECT product_id, product_code, product_name FROM product WHERE product_name = 'ACC011'";
            Long productId = null;
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(productSql)) {
                
                if (rs.next()) {
                    productId = rs.getLong("product_id");
                    System.out.printf("Product ID: %d, Code: %s, Name: %s%n", 
                        productId, rs.getString("product_code"), rs.getString("product_name"));
                } else {
                    System.out.println("ACC011 product not found!");
                    return;
                }
            }
            
            // Check if product exists in batch table
            System.out.println("\n2. Batch Table Check:");
            String batchSql = "SELECT batch_id, batch_number, product_id, quantity_received, selling_price " +
                             "FROM batch WHERE product_id = ?";
            
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(batchSql)) {
                stmt.setLong(1, productId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.printf("Batch ID: %d, Number: %s, Quantity: %d, Selling Price: %.2f%n",
                            rs.getInt("batch_id"), rs.getString("batch_number"), 
                            rs.getInt("quantity_received"), rs.getDouble("selling_price"));
                    } else {
                        System.out.println("❌ No batch record found for ACC011!");
                    }
                }
            }
            
            // Check if product exists in physical_inventory table
            System.out.println("\n3. Physical Inventory Check:");
            String inventorySql = "SELECT pi.inventory_id, pi.product_code, il.location_name, pi.quantity_on_hand, pi.location_capacity " +
                                 "FROM physical_inventory pi " +
                                 "JOIN inventory_location il ON pi.location_id = il.location_id " +
                                 "WHERE pi.product_code = 'PRD-ELAC0002'";
                                 
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(inventorySql)) {
                
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("Inventory ID: %d, Location: %s, Quantity: %d, Capacity: %d%n",
                        rs.getInt("inventory_id"), rs.getString("location_name"),
                        rs.getInt("quantity_on_hand"), rs.getInt("location_capacity"));
                }
                
                if (!found) {
                    System.out.println("❌ No physical inventory records found for ACC011!");
                }
            }
            
            // Show all inventory locations for reference
            System.out.println("\n4. Available Inventory Locations:");
            String locationSql = "SELECT location_id, location_name, location_type FROM inventory_location";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(locationSql)) {
                
                while (rs.next()) {
                    System.out.printf("Location ID: %d, Name: %s, Type: %s%n",
                        rs.getInt("location_id"), rs.getString("location_name"), rs.getString("location_type"));
                }
            }
            
            // Show recent batches for comparison
            System.out.println("\n5. Recent Batches (Last 5):");
            String recentBatchSql = "SELECT b.batch_id, b.batch_number, b.product_id, p.product_name, b.quantity_received " +
                                   "FROM batch b " +
                                   "JOIN product p ON b.product_id = p.product_id " +
                                   "ORDER BY b.batch_id DESC LIMIT 5";
                                   
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(recentBatchSql)) {
                
                System.out.println("Batch ID | Product ID | Product Name | Quantity");
                System.out.println("-------- | ---------- | ------------ | --------");
                while (rs.next()) {
                    System.out.printf("%8d | %10d | %-12s | %8d%n",
                        rs.getInt("batch_id"), rs.getInt("product_id"),
                        rs.getString("product_name"), rs.getInt("quantity_received"));
                }
            }
            
            System.out.println("\n=== Check Complete ===");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}