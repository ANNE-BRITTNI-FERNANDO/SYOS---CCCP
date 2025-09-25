import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Test to verify that inventory creation is working properly
 */
public class TestInventoryFix {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Inventory Creation for Products ===");
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found");
            return;
        }
        
        String dbUrl = "jdbc:sqlite:data/syos_inventory.db";
        
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            
            System.out.println("1. Checking current inventory table schema:");
            String schemaSql = "PRAGMA table_info(physical_inventory)";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(schemaSql)) {
                
                System.out.println("Physical Inventory Table Columns:");
                while (rs.next()) {
                    System.out.printf("  - %s (%s)%n", 
                        rs.getString("name"), rs.getString("type"));
                }
            }
            
            System.out.println("\n2. Checking inventory location codes:");
            String locationSql = "SELECT location_id, location_code, location_name FROM inventory_location WHERE is_active = 1";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(locationSql)) {
                
                System.out.println("Available Locations:");
                while (rs.next()) {
                    System.out.printf("  ID: %d, Code: %s, Name: %s%n",
                        rs.getInt("location_id"), rs.getString("location_code"), rs.getString("location_name"));
                }
            }
            
            System.out.println("\n3. Checking last created products and their inventory:");
            String productSql = "SELECT p.product_id, p.product_code, p.product_name, " +
                               "p.final_price, p.discount_amount " +
                               "FROM product p " +
                               "ORDER BY p.product_id DESC LIMIT 5";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(productSql)) {
                
                System.out.println("Recent Products:");
                while (rs.next()) {
                    Long productId = rs.getLong("product_id");
                    String code = rs.getString("product_code");
                    String name = rs.getString("product_name");
                    System.out.printf("  Product: ID=%d, Code=%s, Name=%s%n", productId, code, name);
                    System.out.printf("    Price: %.2f, Discount: %.2f%n", 
                        rs.getDouble("final_price"), rs.getDouble("discount_amount"));
                    
                    // Check if this product has batch records
                    String batchSql = "SELECT COUNT(*) as batch_count FROM batch WHERE product_id = " + productId;
                    try (Statement batchStmt = conn.createStatement();
                         ResultSet batchRs = batchStmt.executeQuery(batchSql)) {
                        if (batchRs.next()) {
                            int batchCount = batchRs.getInt("batch_count");
                            System.out.printf("    Batch records: %d%n", batchCount);
                        }
                    }
                    
                    // Check if this product has inventory records
                    String inventorySql = "SELECT COUNT(*) as inventory_count FROM physical_inventory pi " +
                                         "JOIN batch b ON pi.batch_id = b.batch_id " +
                                         "WHERE b.product_id = " + productId;
                    try (Statement invStmt = conn.createStatement();
                         ResultSet invRs = invStmt.executeQuery(inventorySql)) {
                        if (invRs.next()) {
                            int inventoryCount = invRs.getInt("inventory_count");
                            System.out.printf("    Inventory records: %d%n", inventoryCount);
                        }
                    }
                    System.out.println();
                }
            }
            
            System.out.println("=== Test Complete ===");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}