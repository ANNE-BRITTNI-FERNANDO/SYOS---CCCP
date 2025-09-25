import java.sql.*;

public class FixBatchAndInventoryIssues {
    
    public static void main(String[] args) {
        String dbPath = "data/syos_inventory.db";
        
        System.out.println("🔧 FIXING BATCH AND INVENTORY ISSUES");
        System.out.println("=" + "=".repeat(50));
        
        try (Connection conn = DriverManager.getConnection(dbPath)) {
            conn.setAutoCommit(false); // Start transaction
            
            try {
                // Fix 1: Update zero selling prices with reasonable values based on product base_price
                fixZeroSellingPrices(conn);
                
                // Fix 2: Set expiry_date to NULL for non-perishable products
                fixIncorrectExpiryDates(conn);
                
                // Fix 3: Rename shelf_capacity to location_capacity for clarity
                renameShelfCapacityColumn(conn);
                
                // Fix 4: Fix product discount calculations
                fixProductDiscounts(conn);
                
                // Fix 5: Verify all fixes
                verifyFixes(conn);
                
                conn.commit(); // Commit all changes
                System.out.println("\n✅ ALL FIXES APPLIED SUCCESSFULLY!");
                
            } catch (Exception e) {
                conn.rollback(); // Rollback on error
                System.err.println("❌ Error occurred, rolling back: " + e.getMessage());
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
    
    /**
     * Fix 1: Update zero selling prices
     */
    private static void fixZeroSellingPrices(Connection conn) throws SQLException {
        System.out.println("\n🔧 Fix 1: Updating zero selling prices...");
        
        // Get batches with zero selling price and update based on product base_price
        String updateQuery = "UPDATE batch " +
            "SET selling_price = (" +
            "    SELECT p.base_price * 1.2 " +
            "    FROM product p " +
            "    WHERE p.product_id = batch.product_id" +
            ") " +
            "WHERE selling_price = 0";
        
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            int updated = stmt.executeUpdate();
            System.out.println("   ✅ Updated " + updated + " batches with proper selling prices (base_price + 20% margin)");
        }
    }
    
    /**
     * Fix 2: Set expiry_date to NULL for non-perishable products
     */
    private static void fixIncorrectExpiryDates(Connection conn) throws SQLException {
        System.out.println("\n🔧 Fix 2: Correcting expiry dates for non-perishable products...");
        
        // Products that should be non-perishable (based on name patterns)
        String[] nonPerishablePatterns = {"Steel", "Nails", "AC", "Hardware", "Tool", "Equipment"};
        
        for (String pattern : nonPerishablePatterns) {
            String updateQuery = """
                UPDATE batch 
                SET expiry_date = NULL 
                WHERE product_id IN (
                    SELECT product_id FROM product 
                    WHERE product_name LIKE ? OR description LIKE ? OR brand LIKE ?
                )
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                String searchPattern = "%" + pattern + "%";
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                stmt.setString(3, searchPattern);
                
                int updated = stmt.executeUpdate();
                if (updated > 0) {
                    System.out.println("   ✅ Set expiry_date to NULL for " + updated + " " + pattern + "-related batches");
                }
            }
        }
        
        // Also fix the suspicious 2027-09-25 dates for products that are clearly non-perishable
        String fixSuspiciousQuery = """
            UPDATE batch 
            SET expiry_date = NULL 
            WHERE expiry_date = '2027-09-25' 
            AND product_id IN (
                SELECT product_id FROM product 
                WHERE product_name NOT LIKE '%milk%' 
                AND product_name NOT LIKE '%fresh%' 
                AND product_name NOT LIKE '%drink%'
                AND product_name NOT LIKE '%food%'
            )
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(fixSuspiciousQuery)) {
            int updated = stmt.executeUpdate();
            System.out.println("   ✅ Corrected " + updated + " suspicious expiry dates (2027-09-25 → NULL)");
        }
    }
    
    /**
     * Fix 3: Rename shelf_capacity to location_capacity
     */
    private static void renameShelfCapacityColumn(Connection conn) throws SQLException {
        System.out.println("\n🔧 Fix 3: Renaming shelf_capacity to location_capacity...");
        
        // Check if column rename is needed
        try {
            String checkQuery = "SELECT location_capacity FROM physical_inventory LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
                stmt.executeQuery();
                System.out.println("   ℹ️  Column 'location_capacity' already exists, skipping rename");
                return;
            }
        } catch (SQLException e) {
            // Column doesn't exist, proceed with rename
        }
        
        try {
            // SQLite doesn't support column rename directly, so we need to recreate the table
            System.out.println("   🔄 Creating new table structure...");
            
            // Step 1: Create new table with correct column name
            String createNewTable = """
                CREATE TABLE physical_inventory_new (
                    inventory_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    batch_id INTEGER NOT NULL,
                    location_id INTEGER NOT NULL,
                    current_quantity INTEGER NOT NULL DEFAULT 0,
                    min_threshold INTEGER DEFAULT 50,
                    location_capacity INTEGER DEFAULT 100,
                    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (batch_id) REFERENCES batch(batch_id),
                    FOREIGN KEY (location_id) REFERENCES inventory_location(location_id),
                    UNIQUE(batch_id, location_id)
                )
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(createNewTable)) {
                stmt.executeUpdate();
            }
            
            // Step 2: Copy data from old table to new table
            String copyData = """
                INSERT INTO physical_inventory_new 
                (inventory_id, batch_id, location_id, current_quantity, min_threshold, location_capacity, last_updated)
                SELECT inventory_id, batch_id, location_id, current_quantity, min_threshold, shelf_capacity, last_updated
                FROM physical_inventory
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(copyData)) {
                int copied = stmt.executeUpdate();
                System.out.println("   ✅ Copied " + copied + " inventory records to new table");
            }
            
            // Step 3: Drop old table and rename new table
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP TABLE physical_inventory");
                stmt.executeUpdate("ALTER TABLE physical_inventory_new RENAME TO physical_inventory");
                System.out.println("   ✅ Successfully renamed 'shelf_capacity' to 'location_capacity'");
            }
            
        } catch (SQLException e) {
            System.err.println("   ❌ Failed to rename column: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Fix 4: Fix product discount calculations
     */
    private static void fixProductDiscounts(Connection conn) throws SQLException {
        System.out.println("\n🔧 Fix 4: Fixing product discount calculations...");
        
        // Find products that might have discount issues
        String findDiscountIssues = """
            SELECT product_id, product_name, base_price, discount_percentage, discount_amount, final_price 
            FROM product 
            WHERE (discount_percentage > 0 AND discount_amount = 0) 
               OR (discount_percentage = 0 AND discount_amount > 0)
               OR (final_price = base_price AND (discount_percentage > 0 OR discount_amount > 0))
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(findDiscountIssues);
             ResultSet rs = stmt.executeQuery()) {
            
            int fixedCount = 0;
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                double basePrice = rs.getDouble("base_price");
                double discountPercentage = rs.getDouble("discount_percentage");
                double discountAmount = rs.getDouble("discount_amount");
                
                // Calculate correct values
                double correctDiscountAmount = (discountPercentage > 0) ? (basePrice * discountPercentage / 100) : discountAmount;
                double correctFinalPrice = basePrice - correctDiscountAmount;
                
                // Update the product
                String updateProduct = "UPDATE product SET discount_amount = ?, final_price = ? WHERE product_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateProduct)) {
                    updateStmt.setDouble(1, correctDiscountAmount);
                    updateStmt.setDouble(2, correctFinalPrice);
                    updateStmt.setInt(3, productId);
                    updateStmt.executeUpdate();
                    fixedCount++;
                    
                    System.out.printf("   ✅ Fixed product ID %d: discount_amount=%.2f, final_price=%.2f%n", 
                        productId, correctDiscountAmount, correctFinalPrice);
                }
            }
            
            if (fixedCount == 0) {
                System.out.println("   ℹ️  No discount calculation issues found");
            } else {
                System.out.println("   ✅ Fixed " + fixedCount + " products with discount calculation issues");
            }
        }
    }
    
    /**
     * Verify all fixes were applied correctly
     */
    private static void verifyFixes(Connection conn) throws SQLException {
        System.out.println("\n🔍 VERIFYING FIXES:");
        System.out.println("-".repeat(30));
        
        // Check 1: Zero selling prices
        String checkZeroPrices = "SELECT COUNT(*) as count FROM batch WHERE selling_price = 0";
        try (PreparedStatement stmt = conn.prepareStatement(checkZeroPrices);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("✅ Batches with zero selling price: " + count + " (should be 0)");
            }
        }
        
        // Check 2: Suspicious expiry dates
        String checkExpiry = "SELECT COUNT(*) as count FROM batch WHERE expiry_date = '2027-09-25'";
        try (PreparedStatement stmt = conn.prepareStatement(checkExpiry);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("✅ Suspicious expiry dates (2027-09-25): " + count + " (should be reduced)");
            }
        }
        
        // Check 3: Column rename
        try {
            String checkColumn = "SELECT location_capacity FROM physical_inventory LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(checkColumn)) {
                stmt.executeQuery();
                System.out.println("✅ Column 'location_capacity' exists and accessible");
            }
        } catch (SQLException e) {
            System.out.println("❌ Column 'location_capacity' not accessible: " + e.getMessage());
        }
        
        // Check 4: Discount calculations
        String checkDiscounts = "SELECT COUNT(*) as count FROM product WHERE discount_percentage > 0 AND discount_amount = 0";
        try (PreparedStatement stmt = conn.prepareStatement(checkDiscounts);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("✅ Products with discount percentage but zero amount: " + count + " (should be 0)");
            }
        }
    }
}