import java.sql.*;

public class CheckPhysicalInventory {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/syos_inventory.db";
            
            try (Connection conn = DriverManager.getConnection(url)) {
                System.out.println("=== CHECKING PHYSICAL INVENTORY TABLE ===\n");
                
                // Check all physical inventory records
                String sql = "SELECT pi.inventory_id, pi.batch_id, pi.location_id, pi.current_quantity, " +
                           "b.batch_number, p.product_code, p.product_name, il.location_code " +
                           "FROM physical_inventory pi " +
                           "JOIN batch b ON pi.batch_id = b.batch_id " +
                           "JOIN product p ON b.product_id = p.product_id " +
                           "JOIN inventory_location il ON pi.location_id = il.location_id " +
                           "ORDER BY p.product_code";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    System.out.println("Physical Inventory Records:");
                    System.out.println("Product Code | Product Name | Location | Quantity | Batch");
                    System.out.println("--------------------------------------------------------");
                    
                    boolean hasRecords = false;
                    while (rs.next()) {
                        hasRecords = true;
                        System.out.printf("%-12s | %-15s | %-8s | %-8d | %s%n",
                            rs.getString("product_code"),
                            rs.getString("product_name").substring(0, Math.min(15, rs.getString("product_name").length())),
                            rs.getString("location_code"),
                            rs.getInt("current_quantity"),
                            rs.getString("batch_number"));
                    }
                    
                    if (!hasRecords) {
                        System.out.println("❌ NO PHYSICAL INVENTORY RECORDS FOUND!");
                    }
                }
                
                // Check if LUX SOAP specifically has any batch records
                System.out.println("\n=== CHECKING LUX SOAP BATCH RECORDS ===");
                String batchSql = "SELECT b.batch_id, b.batch_number, b.quantity_received, p.product_code " +
                                "FROM batch b JOIN product p ON b.product_id = p.product_id " +
                                "WHERE p.product_code = 'LA-SO-302'";
                
                try (PreparedStatement stmt = conn.prepareStatement(batchSql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    boolean hasBatch = false;
                    while (rs.next()) {
                        hasBatch = true;
                        System.out.printf("Batch ID: %d, Number: %s, Quantity: %d%n",
                            rs.getLong("batch_id"),
                            rs.getString("batch_number"),
                            rs.getInt("quantity_received"));
                    }
                    
                    if (!hasBatch) {
                        System.out.println("❌ NO BATCH RECORDS FOUND FOR LUX SOAP (LA-SO-302)!");
                    }
                }
                
                // Check latest product
                System.out.println("\n=== LATEST PRODUCTS ===");
                String latestSql = "SELECT product_code, product_name FROM product ORDER BY product_id DESC LIMIT 3";
                try (PreparedStatement stmt = conn.prepareStatement(latestSql)) {
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        System.out.printf("Product: %s - %s%n",
                            rs.getString("product_code"),
                            rs.getString("product_name"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}