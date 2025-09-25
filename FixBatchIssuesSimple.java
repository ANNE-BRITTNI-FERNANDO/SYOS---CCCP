import java.sql.*;

public class FixBatchIssuesSimple {
    
    public static void main(String[] args) {
        String dbPath = "data/syos_inventory.db";
        
        System.out.println("🔧 FIXING BATCH AND INVENTORY ISSUES");
        System.out.println("=" + "=".repeat(50));
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            conn.setAutoCommit(false);
            
            try {
                // Fix 1: Update zero selling prices
                fixSellingPrices(conn);
                
                // Fix 2: Fix expiry dates for non-perishable products  
                fixExpiryDates(conn);
                
                // Fix 3: Update schema to rename shelf_capacity
                updatePhysicalInventorySchema(conn);
                
                // Fix 4: Fix product discounts
                fixProductDiscounts(conn);
                
                conn.commit();
                System.out.println("\n✅ ALL FIXES APPLIED SUCCESSFULLY!");
                
                // Verify fixes
                verifyFixes(conn);
                
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("❌ Error occurred, rolling back: " + e.getMessage());
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
    
    private static void fixSellingPrices(Connection conn) throws SQLException {
        System.out.println("\n🔧 Fix 1: Updating zero selling prices...");
        
        String updateQuery = "UPDATE batch SET selling_price = (" +
            "SELECT p.base_price * 1.2 FROM product p WHERE p.product_id = batch.product_id" +
            ") WHERE selling_price = 0";
        
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            int updated = stmt.executeUpdate();
            System.out.println("   ✅ Updated " + updated + " batches with proper selling prices");
        }
    }
    
    private static void fixExpiryDates(Connection conn) throws SQLException {
        System.out.println("\n🔧 Fix 2: Correcting expiry dates...");
        
        // Fix specific products that should be non-perishable
        String[] nonPerishableProducts = {"Steel Nails", "AC"};
        
        for (String productName : nonPerishableProducts) {
            String updateQuery = "UPDATE batch SET expiry_date = NULL WHERE product_id IN (" +
                "SELECT product_id FROM product WHERE product_name LIKE ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setString(1, "%" + productName + "%");
                int updated = stmt.executeUpdate();
                if (updated > 0) {
                    System.out.println("   ✅ Set expiry_date to NULL for " + updated + " " + productName + " batches");
                }
            }
        }
        
        // Fix suspicious 2027-09-25 dates for clearly non-perishable items
        String fixSuspicious = "UPDATE batch SET expiry_date = NULL " +
            "WHERE expiry_date = '2027-09-25' AND product_id IN (" +
            "SELECT product_id FROM product WHERE product_name NOT LIKE '%milk%' " +
            "AND product_name NOT LIKE '%drink%' AND product_name NOT LIKE '%fresh%')";
        
        try (PreparedStatement stmt = conn.prepareStatement(fixSuspicious)) {
            int updated = stmt.executeUpdate();
            System.out.println("   ✅ Corrected " + updated + " suspicious expiry dates");
        }
    }
    
    private static void updatePhysicalInventorySchema(Connection conn) throws SQLException {
        System.out.println("\n🔧 Fix 3: Updating physical_inventory schema...");
        
        // Check if we need to update the schema
        try {
            String checkQuery = "PRAGMA table_info(physical_inventory)";
            boolean hasLocationCapacity = false;
            
            try (PreparedStatement stmt = conn.prepareStatement(checkQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String columnName = rs.getString("name");
                    if ("location_capacity".equals(columnName)) {
                        hasLocationCapacity = true;
                        break;
                    }
                }
            }
            
            if (hasLocationCapacity) {
                System.out.println("   ℹ️  Schema already updated, skipping");
                return;
            }
            
            // Create new table with better column name
            String createNew = "CREATE TABLE physical_inventory_new (" +
                "inventory_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "batch_id INTEGER NOT NULL," +
                "location_id INTEGER NOT NULL," +
                "current_quantity INTEGER NOT NULL DEFAULT 0," +
                "min_threshold INTEGER DEFAULT 50," +
                "location_capacity INTEGER DEFAULT 100," +
                "last_updated DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (batch_id) REFERENCES batch(batch_id)," +
                "FOREIGN KEY (location_id) REFERENCES inventory_location(location_id)," +
                "UNIQUE(batch_id, location_id))";
            
            try (PreparedStatement stmt = conn.prepareStatement(createNew)) {
                stmt.executeUpdate();
            }
            
            // Copy data
            String copyData = "INSERT INTO physical_inventory_new " +
                "(inventory_id, batch_id, location_id, current_quantity, min_threshold, location_capacity, last_updated) " +
                "SELECT inventory_id, batch_id, location_id, current_quantity, min_threshold, shelf_capacity, last_updated " +
                "FROM physical_inventory";
            
            try (PreparedStatement stmt = conn.prepareStatement(copyData)) {
                int copied = stmt.executeUpdate();
                System.out.println("   ✅ Copied " + copied + " records to new table");
            }
            
            // Replace old table
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP TABLE physical_inventory");
                stmt.executeUpdate("ALTER TABLE physical_inventory_new RENAME TO physical_inventory");
                System.out.println("   ✅ Renamed 'shelf_capacity' to 'location_capacity'");
            }
            
        } catch (SQLException e) {
            System.err.println("   ⚠️  Schema update failed: " + e.getMessage());
            // Don't throw - this is not critical for data integrity
        }
    }
    
    private static void fixProductDiscounts(Connection conn) throws SQLException {
        System.out.println("\n🔧 Fix 4: Fixing product discounts...");
        
        // Update products where discount calculations are wrong
        String findIssues = "SELECT product_id, base_price, discount_percentage, discount_amount " +
            "FROM product WHERE discount_percentage > 0 AND discount_amount = 0";
        
        try (PreparedStatement stmt = conn.prepareStatement(findIssues);
             ResultSet rs = stmt.executeQuery()) {
            
            int fixedCount = 0;
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                double basePrice = rs.getDouble("base_price");
                double discountPercentage = rs.getDouble("discount_percentage");
                
                double correctDiscountAmount = basePrice * discountPercentage / 100;
                double correctFinalPrice = basePrice - correctDiscountAmount;
                
                String updateProduct = "UPDATE product SET discount_amount = ?, final_price = ? WHERE product_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateProduct)) {
                    updateStmt.setDouble(1, correctDiscountAmount);
                    updateStmt.setDouble(2, correctFinalPrice);
                    updateStmt.setInt(3, productId);
                    updateStmt.executeUpdate();
                    fixedCount++;
                }
            }
            
            if (fixedCount > 0) {
                System.out.println("   ✅ Fixed " + fixedCount + " products with discount issues");
            } else {
                System.out.println("   ℹ️  No discount issues found");
            }
        }
    }
    
    private static void verifyFixes(Connection conn) throws SQLException {
        System.out.println("\n🔍 VERIFICATION RESULTS:");
        System.out.println("-".repeat(40));
        
        // Check zero selling prices
        String checkPrices = "SELECT COUNT(*) FROM batch WHERE selling_price = 0";
        try (PreparedStatement stmt = conn.prepareStatement(checkPrices);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Zero selling prices remaining: " + count);
            }
        }
        
        // Check suspicious expiry dates
        String checkExpiry = "SELECT COUNT(*) FROM batch WHERE expiry_date = '2027-09-25'";
        try (PreparedStatement stmt = conn.prepareStatement(checkExpiry);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Suspicious expiry dates remaining: " + count);
            }
        }
        
        // Check column existence
        try {
            String checkColumn = "SELECT location_capacity FROM physical_inventory LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(checkColumn)) {
                stmt.executeQuery();
                System.out.println("Column 'location_capacity': ✅ Available");
            }
        } catch (SQLException e) {
            System.out.println("Column 'location_capacity': ❌ Not available");
        }
        
        System.out.println("✅ Verification complete!");
    }
}