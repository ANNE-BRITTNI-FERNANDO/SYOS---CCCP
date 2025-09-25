import java.sql.*;

/**
 * Test class to demonstrate both reorder level approaches:
 * 1. Assignment Default: Fixed 50 units
 * 2. Smart Dynamic: Sales velocity-based calculation
 * 
 * This shows compliance with assignment requirements while maintaining intelligent logic
 */
public class TestBothReorderMethods {
    
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    private static final int DEFAULT_REORDER_LEVEL = 50; // Assignment requirement
    
    public static void main(String[] args) {
        System.out.println("🔥 Testing Both Reorder Level Methods");
        System.out.println("=====================================");
        System.out.println("Assignment: Default 50 units vs Smart Dynamic Logic");
        System.out.println();
        
        testProductReorderLevels("PRD-LAFA0002"); // Test with existing product
    }
    
    /**
     * Test both reorder methods for a specific product
     */
    private static void testProductReorderLevels(String productCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Get product basic info
                String productSql = "SELECT product_name FROM product WHERE product_code = ?";
                String productName = "Unknown";
                
                try (PreparedStatement stmt = conn.prepareStatement(productSql)) {
                    stmt.setString(1, productCode);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        productName = rs.getString("product_name");
                    }
                }
                
                // Get stock info
                int totalStock = getTotalStock(productCode, conn);
                int totalCapacity = getTotalCapacity(productCode, conn);
                
                System.out.println("📦 Product Analysis: " + productCode + " (" + productName + ")");
                System.out.println("─".repeat(60));
                System.out.println("Current Total Stock: " + totalStock + " units");
                System.out.println("Estimated Capacity: " + totalCapacity + " units");
                System.out.println();
                
                // Method 1: Assignment Default (Fixed 50)
                int assignmentDefault = Math.min(DEFAULT_REORDER_LEVEL, totalCapacity);
                System.out.println("📋 ASSIGNMENT DEFAULT METHOD:");
                System.out.println("   Reorder Level: " + assignmentDefault + " units (fixed)");
                System.out.println("   Logic: Always use 50 units (or capacity limit)");
                System.out.println("   Compliance: ✅ Meets assignment requirement");
                System.out.println();
                
                // Method 2: Smart Dynamic (Sales Velocity Based)
                int smartDynamic = calculateDynamicReorderLevel(productCode, totalCapacity, conn);
                String salesAnalysis = getSalesAnalysis(productCode, conn);
                System.out.println("🧠 SMART DYNAMIC METHOD:");
                System.out.println("   Reorder Level: " + smartDynamic + " units (calculated)");
                System.out.println("   Logic: Based on sales velocity analysis");
                System.out.println("   Sales Pattern: " + salesAnalysis);
                System.out.println("   Intelligence: ✅ Optimizes inventory costs");
                System.out.println();
                
                // Comparison and recommendation
                System.out.println("💡 COMPARISON & RECOMMENDATION:");
                System.out.println("─".repeat(40));
                
                if (smartDynamic == assignmentDefault) {
                    System.out.println("✅ Both methods suggest same level: " + smartDynamic + " units");
                    System.out.println("   Perfect alignment between assignment and optimization");
                } else if (smartDynamic < assignmentDefault) {
                    double savings = ((double)(assignmentDefault - smartDynamic) / assignmentDefault) * 100;
                    System.out.println("💰 Smart method saves inventory costs:");
                    System.out.println("   Assignment Default: " + assignmentDefault + " units");
                    System.out.println("   Smart Dynamic: " + smartDynamic + " units");
                    System.out.println("   Cost Reduction: " + String.format("%.1f%%", savings));
                    System.out.println("   Recommendation: Use smart logic for real operations");
                } else {
                    double increase = ((double)(smartDynamic - assignmentDefault) / assignmentDefault) * 100;
                    System.out.println("📈 Smart method suggests higher inventory:");
                    System.out.println("   Assignment Default: " + assignmentDefault + " units");
                    System.out.println("   Smart Dynamic: " + smartDynamic + " units");
                    System.out.println("   Increase: " + String.format("%.1f%%", increase));
                    System.out.println("   Reason: Fast-moving product needs higher stock");
                }
                
                System.out.println();
                System.out.println("🎯 FINAL IMPLEMENTATION:");
                System.out.println("   ✅ Assignment compliance: DEFAULT_REORDER_LEVEL = 50");
                System.out.println("   ✅ Business optimization: Dynamic calculation available");
                System.out.println("   ✅ Both methods coexist in the same system");
                
            }
        } catch (Exception e) {
            System.err.println("❌ Error testing reorder methods: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get total stock from batch table using product_id
     */
    private static int getTotalStock(String productCode, Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(SUM(b.quantity_received), 0) as total_stock " +
                    "FROM batch b " +
                    "JOIN product p ON b.product_id = p.product_id " +
                    "WHERE p.product_code = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("total_stock") : 0;
        }
    }
    
    /**
     * Estimate total capacity based on historical data
     */
    private static int getTotalCapacity(String productCode, Connection conn) throws SQLException {
        String sql = "SELECT MAX(b.quantity_received) as max_capacity " +
                    "FROM batch b " +
                    "JOIN product p ON b.product_id = p.product_id " +
                    "WHERE p.product_code = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            int maxHistorical = rs.next() ? rs.getInt("max_capacity") : 0;
            
            // Use reasonable capacity estimation
            return Math.max(maxHistorical, 150); // Default shelf capacity assumption
        }
    }
    
    /**
     * Calculate dynamic reorder level based on sales velocity
     */
    private static int calculateDynamicReorderLevel(String productCode, int totalCapacity, Connection conn) throws SQLException {
        // Get sales velocity in last 30 days
        String salesSql = "SELECT COUNT(*) as transaction_count, " +
                         "COALESCE(SUM(sti.quantity), 0) as total_units_sold " +
                         "FROM sales_transaction_item sti " +
                         "JOIN sales_transaction st ON sti.transaction_id = st.transaction_id " +
                         "WHERE sti.product_code = ? AND st.created_date >= date('now', '-30 days')";
        
        int transactionCount = 0;
        int totalUnitsSold = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(salesSql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                transactionCount = rs.getInt("transaction_count");
                totalUnitsSold = rs.getInt("total_units_sold");
            }
        }
        
        // Calculate sales velocity category and apply business rules
        int reorderLevel;
        
        if (transactionCount >= 10 || totalUnitsSold >= 50) {
            // Fast Moving: >10 transactions OR >50 units sold in 30 days
            // 40% of max capacity, min 20 units
            reorderLevel = Math.max((int)(totalCapacity * 0.40), 20);
        } else if (transactionCount >= 3 || totalUnitsSold >= 15) {
            // Medium Moving: 3-10 transactions OR 15-50 units sold in 30 days
            // 25% of max capacity, min 10 units
            reorderLevel = Math.max((int)(totalCapacity * 0.25), 10);
        } else if (transactionCount >= 1 || totalUnitsSold >= 1) {
            // Slow Moving: 1-3 transactions OR 1-15 units sold in 30 days
            // 15% of max capacity, min 5 units
            reorderLevel = Math.max((int)(totalCapacity * 0.15), 5);
        } else {
            // New Products: no sales history
            // 20% of max capacity, min 10 units
            reorderLevel = Math.max((int)(totalCapacity * 0.20), 10);
        }
        
        // Cap the reorder level at reasonable maximums
        if (totalCapacity <= 50) {
            return Math.min(reorderLevel, 25); // Max 50% of small capacity
        } else if (totalCapacity <= 200) {
            return Math.min(reorderLevel, 80); // Max 40% of medium capacity
        } else {
            return Math.min(reorderLevel, Math.min(totalCapacity / 2, 100)); // Conservative max
        }
    }
    
    /**
     * Get sales analysis description
     */
    private static String getSalesAnalysis(String productCode, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as transaction_count, " +
                    "COALESCE(SUM(sti.quantity), 0) as total_units_sold, " +
                    "COALESCE(AVG(sti.quantity), 0) as avg_units_per_transaction " +
                    "FROM sales_transaction_item sti " +
                    "JOIN sales_transaction st ON sti.transaction_id = st.transaction_id " +
                    "WHERE sti.product_code = ? AND st.created_date >= date('now', '-30 days')";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int transactionCount = rs.getInt("transaction_count");
                int totalUnitsSold = rs.getInt("total_units_sold");
                double avgUnitsPerTransaction = rs.getDouble("avg_units_per_transaction");
                
                if (transactionCount >= 10 || totalUnitsSold >= 50) {
                    return String.format("🔥 FAST MOVING (%d transactions, %d units sold, avg %.1f per sale)", 
                        transactionCount, totalUnitsSold, avgUnitsPerTransaction);
                } else if (transactionCount >= 3 || totalUnitsSold >= 15) {
                    return String.format("📈 Medium Moving (%d transactions, %d units sold, avg %.1f per sale)", 
                        transactionCount, totalUnitsSold, avgUnitsPerTransaction);
                } else if (transactionCount >= 1 || totalUnitsSold >= 1) {
                    return String.format("📉 Slow Moving (%d transactions, %d units sold, avg %.1f per sale)", 
                        transactionCount, totalUnitsSold, avgUnitsPerTransaction);
                } else {
                    return "❄️ No Recent Sales (0 transactions in 30 days)";
                }
            }
        }
        return "📊 New Product - No sales history";
    }
}