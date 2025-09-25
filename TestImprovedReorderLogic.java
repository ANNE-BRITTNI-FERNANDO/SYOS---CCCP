import java.sql.*;

/**
 * Test to demonstrate improved reorder logic:
 * If smart calculation < 50 → Use 50 (safety minimum)
 * If smart calculation ≥ 50 → Use smart calculation
 */
public class TestImprovedReorderLogic {
    
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    private static final int DEFAULT_REORDER_LEVEL = 50;
    
    public static void main(String[] args) {
        System.out.println("🎯 Testing IMPROVED Reorder Logic (Option A)");
        System.out.println("==============================================");
        System.out.println("Logic: If smart < 50 → Use 50, else → Use smart");
        System.out.println();
        
        // Test with existing product
        testImprovedLogic("PRD-LAFA0002");
        
        System.out.println();
        System.out.println("🔄 RESTOCKING EXPLANATION:");
        System.out.println("─".repeat(50));
        System.out.println("📦 Restocking happens when: Current Stock ≤ Reorder Level");
        System.out.println("⚡ Alert triggers automatically in system");
        System.out.println("🛒 Manager places purchase order based on recommendation");
        System.out.println("📈 New inventory flows: Supplier → Warehouse → Shelf → Customer");
    }
    
    private static void testImprovedLogic(String productCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Get product info
                String productName = getProductName(productCode, conn);
                int totalStock = getTotalStock(productCode, conn);
                int capacity = getTotalCapacity(productCode, conn);
                
                System.out.println("📦 Product Analysis: " + productCode + " (" + productName + ")");
                System.out.println("─".repeat(60));
                System.out.println("Current Stock: " + totalStock + " units");
                System.out.println("Estimated Capacity: " + capacity + " units");
                System.out.println();
                
                // Calculate raw smart recommendation (pure sales analysis)
                int rawSmart = calculateRawSmartReorder(productCode, capacity, conn);
                String salesPattern = getSalesPattern(productCode, conn);
                
                // Apply improved logic
                int finalRecommendation = Math.max(rawSmart, DEFAULT_REORDER_LEVEL);
                
                System.out.println("📊 REORDER ANALYSIS:");
                System.out.println("─".repeat(30));
                System.out.println("Sales Pattern: " + salesPattern);
                System.out.println("Raw Smart Analysis: " + rawSmart + " units");
                System.out.println("Safety Minimum: " + DEFAULT_REORDER_LEVEL + " units");
                System.out.println("Final Recommendation: " + finalRecommendation + " units");
                System.out.println();
                
                // Explain the logic
                if (finalRecommendation == DEFAULT_REORDER_LEVEL && rawSmart < DEFAULT_REORDER_LEVEL) {
                    System.out.println("💡 LOGIC APPLIED:");
                    System.out.println("   Raw smart (" + rawSmart + ") < Safety minimum (" + DEFAULT_REORDER_LEVEL + ")");
                    System.out.println("   → Using safety minimum: " + DEFAULT_REORDER_LEVEL + " units");
                    System.out.println("   → Protects against stockouts for slow-moving items");
                } else if (finalRecommendation == rawSmart && rawSmart >= DEFAULT_REORDER_LEVEL) {
                    System.out.println("💡 LOGIC APPLIED:");
                    System.out.println("   Raw smart (" + rawSmart + ") ≥ Safety minimum (" + DEFAULT_REORDER_LEVEL + ")");
                    System.out.println("   → Using smart calculation: " + rawSmart + " units");
                    System.out.println("   → Optimizes inventory for fast-moving items");
                }
                
                // Restocking status
                System.out.println();
                System.out.println("🔄 RESTOCKING STATUS:");
                if (totalStock <= finalRecommendation) {
                    System.out.println("   ❗ RESTOCK NEEDED!");
                    System.out.println("   Current: " + totalStock + " ≤ Reorder Level: " + finalRecommendation);
                    System.out.println("   Action: Place purchase order immediately");
                } else {
                    System.out.println("   ✅ Stock Level OK");
                    System.out.println("   Current: " + totalStock + " > Reorder Level: " + finalRecommendation);
                    System.out.println("   Action: Continue monitoring");
                }
                
            }
        } catch (Exception e) {
            System.err.println("❌ Error testing improved logic: " + e.getMessage());
        }
    }
    
    private static String getProductName(String productCode, Connection conn) throws SQLException {
        String sql = "SELECT product_name FROM product WHERE product_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("product_name") : "Unknown";
        }
    }
    
    private static int getTotalStock(String productCode, Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(SUM(b.quantity_received), 0) as total_stock " +
                    "FROM batch b JOIN product p ON b.product_id = p.product_id " +
                    "WHERE p.product_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("total_stock") : 0;
        }
    }
    
    private static int getTotalCapacity(String productCode, Connection conn) throws SQLException {
        String sql = "SELECT MAX(b.quantity_received) as max_capacity " +
                    "FROM batch b JOIN product p ON b.product_id = p.product_id " +
                    "WHERE p.product_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            int max = rs.next() ? rs.getInt("max_capacity") : 0;
            return Math.max(max, 150); // Reasonable default
        }
    }
    
    private static int calculateRawSmartReorder(String productCode, int capacity, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as transaction_count, " +
                    "COALESCE(SUM(sti.quantity), 0) as total_units_sold " +
                    "FROM sales_transaction_item sti " +
                    "JOIN sales_transaction st ON sti.transaction_id = st.transaction_id " +
                    "WHERE sti.product_code = ? AND st.created_date >= date('now', '-30 days')";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int transactions = rs.getInt("transaction_count");
                int unitsSold = rs.getInt("total_units_sold");
                
                // Raw calculation (no safety minimum)
                if (transactions >= 10 || unitsSold >= 50) {
                    return Math.max((int)(capacity * 0.40), 20); // Fast moving
                } else if (transactions >= 3 || unitsSold >= 15) {
                    return Math.max((int)(capacity * 0.25), 10); // Medium moving
                } else if (transactions >= 1 || unitsSold >= 1) {
                    return Math.max((int)(capacity * 0.15), 5);  // Slow moving
                } else {
                    return Math.max((int)(capacity * 0.20), 10); // New product
                }
            }
        }
        return 30; // Fallback
    }
    
    private static String getSalesPattern(String productCode, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as transaction_count, " +
                    "COALESCE(SUM(sti.quantity), 0) as total_units_sold " +
                    "FROM sales_transaction_item sti " +
                    "JOIN sales_transaction st ON sti.transaction_id = st.transaction_id " +
                    "WHERE sti.product_code = ? AND st.created_date >= date('now', '-30 days')";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int transactions = rs.getInt("transaction_count");
                int unitsSold = rs.getInt("total_units_sold");
                
                if (transactions >= 10 || unitsSold >= 50) {
                    return "🔥 Fast Moving (" + transactions + " transactions, " + unitsSold + " units/month)";
                } else if (transactions >= 3 || unitsSold >= 15) {
                    return "📈 Medium Moving (" + transactions + " transactions, " + unitsSold + " units/month)";
                } else if (transactions >= 1 || unitsSold >= 1) {
                    return "📉 Slow Moving (" + transactions + " transactions, " + unitsSold + " units/month)";
                } else {
                    return "❄️ No Recent Sales (0 transactions in 30 days)";
                }
            }
        }
        return "📊 New Product - No sales history";
    }
}