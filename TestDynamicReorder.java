import java.sql.*;
import java.math.BigDecimal;

public class TestDynamicReorder {
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    public static void main(String[] args) {
        TestDynamicReorder tester = new TestDynamicReorder();
        
        System.out.println("🧪 Testing Dynamic Reorder Logic");
        System.out.println("═".repeat(50));
        
        // Test with sample product
        String productCode = "PRD-LAFA0002";
        
        int totalStock = tester.getTotalStock(productCode);
        int totalCapacity = tester.getTotalCapacity(productCode);
        int dynamicReorderLevel = tester.calculateDynamicReorderLevel(productCode, totalCapacity);
        String salesAnalysis = tester.getReorderAnalysis(productCode);
        String stockStatus = tester.getStockStatus(productCode, totalStock, totalCapacity);
        
        System.out.println("Product Code: " + productCode);
        System.out.println("Total Stock: " + totalStock + " units");
        System.out.println("Estimated Capacity: " + totalCapacity + " units");
        System.out.println("Dynamic Reorder Level: " + dynamicReorderLevel + " units");
        System.out.println("Sales Analysis: " + salesAnalysis);
        System.out.println("Stock Status: " + stockStatus);
        System.out.println();
        
        // Show business logic breakdown
        System.out.println("📊 Business Logic Applied:");
        System.out.println("• Fast Moving (>10 transactions OR >50 units/month): 40% capacity, min 20");
        System.out.println("• Medium Moving (3-10 transactions OR 15-50 units/month): 25% capacity, min 10"); 
        System.out.println("• Slow Moving (1-3 transactions OR 1-15 units/month): 15% capacity, min 5");
        System.out.println("• New Products (no sales): 20% capacity, min 10");
        System.out.println();
        
        double percentage = (double) dynamicReorderLevel / totalCapacity * 100;
        System.out.printf("🎯 Result: %d units reorder level = %.1f%% of capacity%n", 
            dynamicReorderLevel, percentage);
    }
    
    private int getTotalStock(String productCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT COALESCE(SUM(pi.current_quantity), 0) as total_stock " +
                           "FROM product p " +
                           "LEFT JOIN batch b ON p.product_id = b.product_id " +
                           "LEFT JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                           "JOIN inventory_location il ON pi.location_id = il.location_id " +
                           "WHERE p.product_code = ? AND p.is_active = 1 " +
                           "AND il.location_code IN ('SHELF', 'WAREHOUSE')";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, productCode);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        return rs.getInt("total_stock");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting total stock: " + e.getMessage());
        }
        return 0;
    }
    
    private int getTotalCapacity(String productCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT " +
                           "COALESCE(MAX(pi.current_quantity), 0) as max_historical_stock, " +
                           "COALESCE(p.shelf_capacity, 100) as shelf_capacity " +
                           "FROM product p " +
                           "LEFT JOIN batch b ON p.product_id = b.product_id " +
                           "LEFT JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                           "WHERE p.product_code = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, productCode);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int maxHistorical = rs.getInt("max_historical_stock");
                        int shelfCapacity = rs.getInt("shelf_capacity");
                        
                        int estimatedCapacity = Math.max(
                            Math.max((int)(maxHistorical * 1.2), shelfCapacity * 2), 
                            100
                        );
                        
                        return estimatedCapacity;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting capacity: " + e.getMessage());
        }
        return 150;
    }
    
    private int calculateDynamicReorderLevel(String productCode, int totalCapacity) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
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
                
                int reorderLevel;
                
                if (transactionCount >= 10 || totalUnitsSold >= 50) {
                    reorderLevel = Math.max((int)(totalCapacity * 0.40), 20);
                } else if (transactionCount >= 3 || totalUnitsSold >= 15) {
                    reorderLevel = Math.max((int)(totalCapacity * 0.25), 10);
                } else if (transactionCount >= 1 || totalUnitsSold >= 1) {
                    reorderLevel = Math.max((int)(totalCapacity * 0.15), 5);
                } else {
                    reorderLevel = Math.max((int)(totalCapacity * 0.20), 10);
                }
                
                if (totalCapacity <= 50) {
                    return Math.min(reorderLevel, 25);
                } else if (totalCapacity <= 200) {
                    return Math.min(reorderLevel, 80);
                } else {
                    return Math.min(reorderLevel, Math.min(totalCapacity / 2, 100));
                }
                
            }
        } catch (Exception e) {
            System.err.println("Error calculating dynamic reorder level: " + e.getMessage());
        }
        
        if (totalCapacity <= 50) {
            return Math.max((int)(totalCapacity * 0.25), 5);
        } else {
            return Math.min((int)(totalCapacity * 0.20), 50);
        }
    }
    
    private String getReorderAnalysis(String productCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
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
                            return "❄️  No Recent Sales (0 transactions in 30 days)";
                        }
                    }
                }
            }
        } catch (Exception e) {
            return "📊 Sales analysis unavailable: " + e.getMessage();
        }
        return "📊 New Product - No sales history";
    }
    
    private String getStockStatus(String productCode, int totalStock, int totalCapacity) {
        int dynamicReorderLevel = calculateDynamicReorderLevel(productCode, totalCapacity);
        double stockPercentage = (double) totalStock / totalCapacity * 100;
        
        if (totalStock >= totalCapacity * 0.6) {
            return "✅ Excellent Stock (" + String.format("%.1f", stockPercentage) + "% capacity)";
        } else if (totalStock >= dynamicReorderLevel) {
            return "✅ Good Stock (" + String.format("%.1f", stockPercentage) + "% capacity, reorder at " + dynamicReorderLevel + ")";
        } else if (totalStock >= dynamicReorderLevel * 0.5) {
            return "⚠️  Approaching Reorder (" + String.format("%.1f", stockPercentage) + "% capacity, reorder level: " + dynamicReorderLevel + ")";
        } else if (totalStock > 0) {
            return "❗ REORDER NOW (" + String.format("%.1f", stockPercentage) + "% capacity, below reorder level: " + dynamicReorderLevel + ")";
        } else {
            return "❌ OUT OF STOCK - URGENT REORDER (reorder level: " + dynamicReorderLevel + ")";
        }
    }
}