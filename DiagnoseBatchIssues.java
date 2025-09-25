import java.sql.*;

public class DiagnoseBatchIssues {
    public static void main(String[] args) {
        String dbPath = "data/syos_inventory.db";
        
        System.out.println("🔍 DIAGNOSING BATCH AND INVENTORY ISSUES");
        System.out.println("=" + "=".repeat(50));
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            
            // Issue 1: Check batch selling_price values
            System.out.println("\n📊 BATCH TABLE ANALYSIS:");
            System.out.println("-".repeat(50));
            
            String batchQuery = "SELECT batch_id, product_id, batch_number, selling_price, expiry_date, created_at FROM batch ORDER BY batch_id DESC LIMIT 5";
            try (PreparedStatement stmt = conn.prepareStatement(batchQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                System.out.printf("%-8s %-10s %-30s %-12s %-12s %-20s%n", 
                    "BatchID", "ProductID", "BatchNumber", "SellPrice", "ExpiryDate", "Created");
                System.out.println("-".repeat(100));
                
                while (rs.next()) {
                    System.out.printf("%-8d %-10d %-30s %-12.2f %-12s %-20s%n",
                        rs.getInt("batch_id"),
                        rs.getInt("product_id"),
                        rs.getString("batch_number"),
                        rs.getDouble("selling_price"),
                        rs.getString("expiry_date") != null ? rs.getString("expiry_date") : "NULL",
                        rs.getString("created_at"));
                }
            }
            
            // Issue 2: Check product discount values
            System.out.println("\n💰 PRODUCT DISCOUNT ANALYSIS:");
            System.out.println("-".repeat(50));
            
            String productQuery = "SELECT product_id, product_code, product_name, base_price, discount_percentage, discount_amount, final_price FROM product ORDER BY product_id DESC LIMIT 5";
            try (PreparedStatement stmt = conn.prepareStatement(productQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                System.out.printf("%-4s %-15s %-20s %-10s %-8s %-8s %-10s%n",
                    "ID", "Code", "Name", "BasePrice", "Disc%", "DiscAmt", "FinalPrice");
                System.out.println("-".repeat(85));
                
                while (rs.next()) {
                    System.out.printf("%-4d %-15s %-20s %-10.2f %-8.2f %-8.2f %-10.2f%n",
                        rs.getInt("product_id"),
                        rs.getString("product_code"),
                        rs.getString("product_name").substring(0, Math.min(20, rs.getString("product_name").length())),
                        rs.getDouble("base_price"),
                        rs.getDouble("discount_percentage"),
                        rs.getDouble("discount_amount"),
                        rs.getDouble("final_price"));
                }
            }
            
            // Issue 3: Check physical inventory structure
            System.out.println("\n📦 PHYSICAL INVENTORY ANALYSIS:");
            System.out.println("-".repeat(50));
            
            String inventoryQuery = "SELECT pi.inventory_id, pi.batch_id, il.location_name, pi.current_quantity, pi.shelf_capacity FROM physical_inventory pi " +
                                  "JOIN inventory_location il ON pi.location_id = il.location_id ORDER BY pi.inventory_id DESC LIMIT 8";
            
            try (PreparedStatement stmt = conn.prepareStatement(inventoryQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                System.out.printf("%-8s %-8s %-25s %-8s %-12s%n",
                    "InvID", "BatchID", "Location", "Qty", "Capacity");
                System.out.println("-".repeat(70));
                
                while (rs.next()) {
                    System.out.printf("%-8d %-8d %-25s %-8d %-12d%n",
                        rs.getInt("inventory_id"),
                        rs.getInt("batch_id"),
                        rs.getString("location_name"),
                        rs.getInt("current_quantity"),
                        rs.getInt("shelf_capacity"));
                }
            }
            
            // Issue 4: Check for data inconsistencies
            System.out.println("\n⚠️  DATA INCONSISTENCY CHECKS:");
            System.out.println("-".repeat(50));
            
            // Check batches with zero selling price
            String zeroPrice = "SELECT COUNT(*) as count FROM batch WHERE selling_price = 0";
            try (PreparedStatement stmt = conn.prepareStatement(zeroPrice);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("❌ Batches with zero selling price: " + count);
                }
            }
            
            // Check products with wrong expiry dates
            String wrongExpiry = "SELECT COUNT(*) as count FROM batch WHERE expiry_date = '2027-09-25'";
            try (PreparedStatement stmt = conn.prepareStatement(wrongExpiry);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("❌ Batches with suspicious expiry date (2027-09-25): " + count);
                }
            }
            
            // Check products with zero discount but non-zero discount_amount
            String discountIssue = "SELECT COUNT(*) as count FROM product WHERE discount_percentage = 0 AND discount_amount > 0";
            try (PreparedStatement stmt = conn.prepareStatement(discountIssue);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("❌ Products with discount amount but zero percentage: " + count);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}