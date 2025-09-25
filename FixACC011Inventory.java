import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * Fix the missing batch and inventory records for ACC011 product
 */
public class FixACC011Inventory {
    
    public static void main(String[] args) {
        System.out.println("=== Fixing ACC011 Batch and Inventory Records ===");
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found");
            return;
        }
        
        String dbUrl = "jdbc:sqlite:data/syos_inventory.db";
        
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            conn.setAutoCommit(false); // Start transaction
            
            // Get ACC011 product details
            System.out.println("1. Getting ACC011 Product Details:");
            String productSql = "SELECT product_id, product_code, product_name, final_price FROM product WHERE product_name = 'ACC011'";
            Long productId = null;
            String productCode = null;
            double finalPrice = 0.0;
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(productSql)) {
                
                if (rs.next()) {
                    productId = rs.getLong("product_id");
                    productCode = rs.getString("product_code");
                    finalPrice = rs.getDouble("final_price");
                    System.out.printf("Product ID: %d, Code: %s, Final Price: %.2f%n", 
                        productId, productCode, finalPrice);
                } else {
                    System.out.println("ACC011 product not found!");
                    return;
                }
            }
            
            // Create batch record
            System.out.println("\n2. Creating Batch Record:");
            String batchNumber = "BTH-ELAC-" + String.format("%04d", productId);
            String insertBatchSql = "INSERT INTO batch (batch_number, product_id, purchase_date, expiry_date, quantity_received, selling_price, created_at) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            
            Long batchId = null;
            try (PreparedStatement stmt = conn.prepareStatement(insertBatchSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, batchNumber);
                stmt.setLong(2, productId);
                stmt.setString(3, LocalDate.now().toString()); // Purchase date
                stmt.setString(4, null); // No expiry date (N/A was selected)
                stmt.setInt(5, 70); // Quantity from the original input
                stmt.setDouble(6, finalPrice); // Use final price (with discount applied)
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        batchId = generatedKeys.getLong(1);
                        System.out.printf("Batch created: ID=%d, Number=%s, Quantity=70, Price=%.2f%n", 
                            batchId, batchNumber, finalPrice);
                    }
                } else {
                    System.out.println("Failed to create batch record");
                    conn.rollback();
                    return;
                }
            }
            
            // Get location IDs (Warehouse = 1, Shelf = 2 based on existing data)
            System.out.println("\n3. Creating Physical Inventory Records:");
            
            // 80% to warehouse (1), 20% to shelf (2) - as per original logic
            int warehouseQty = (int) (70 * 0.8); // 56
            int shelfQty = 70 - warehouseQty;    // 14
            
            // Create warehouse inventory record
            String insertInventorySql = "INSERT INTO physical_inventory (batch_id, location_id, current_quantity, min_threshold, location_capacity) " +
                                       "VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(insertInventorySql)) {
                // Warehouse inventory (location_id = 1)
                stmt.setLong(1, batchId);
                stmt.setInt(2, 1); // Warehouse location ID
                stmt.setInt(3, warehouseQty);
                stmt.setInt(4, 5); // Default min threshold
                stmt.setInt(5, 20); // Default capacity from subcategory
                
                int warehouseRows = stmt.executeUpdate();
                System.out.printf("Warehouse inventory created: Quantity=%d, Rows=%d%n", warehouseQty, warehouseRows);
                
                // Shelf inventory (location_id = 2)  
                stmt.setLong(1, batchId);
                stmt.setInt(2, 2); // Shelf location ID
                stmt.setInt(3, shelfQty);
                stmt.setInt(4, 5); // Default min threshold
                stmt.setInt(5, 20); // Default capacity
                
                int shelfRows = stmt.executeUpdate();
                System.out.printf("Shelf inventory created: Quantity=%d, Rows=%d%n", shelfQty, shelfRows);
                
                if (warehouseRows > 0 && shelfRows > 0) {
                    conn.commit();
                    System.out.println("\n✅ All records created successfully!");
                } else {
                    conn.rollback();
                    System.out.println("❌ Failed to create inventory records");
                }
            }
            
            // Verify the created records
            System.out.println("\n4. Verification:");
            String verifySql = "SELECT pi.inventory_id, pi.batch_id, il.location_name, pi.current_quantity " +
                              "FROM physical_inventory pi " +
                              "JOIN inventory_location il ON pi.location_id = il.location_id " +
                              "WHERE pi.batch_id = ?";
                              
            try (PreparedStatement stmt = conn.prepareStatement(verifySql)) {
                stmt.setLong(1, batchId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf("Inventory ID: %d, Location: %s, Quantity: %d%n",
                            rs.getInt("inventory_id"), rs.getString("location_name"), rs.getInt("current_quantity"));
                    }
                }
            }
            
            System.out.println("\n=== Fix Complete ===");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}