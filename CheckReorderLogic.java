import java.sql.*;

public class CheckReorderLogic {
    public static void main(String[] args) {
        String dbPath = "data/syos_inventory.db";
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + dbPath;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                System.out.println("=== PRODUCT REORDER LEVELS ===");
                String productSQL = "SELECT product_code, name, reorder_level FROM product WHERE product_code LIKE 'PRD-LAFA%'";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(productSQL)) {
                    while (rs.next()) {
                        System.out.printf("Product: %s | Name: %s | Reorder Level: %d%n",
                            rs.getString("product_code"),
                            rs.getString("name"),
                            rs.getInt("reorder_level"));
                    }
                }
                
                System.out.println("\n=== CURRENT TOTAL STOCK LEVELS ===");
                String stockSQL = "SELECT p.product_code, p.name, p.reorder_level, " +
                                "SUM(pi.current_quantity) as total_stock " +
                                "FROM product p " +
                                "JOIN batch b ON p.product_id = b.product_id " +
                                "JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                                "WHERE p.product_code LIKE 'PRD-LAFA%' " +
                                "GROUP BY p.product_code, p.name, p.reorder_level";
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(stockSQL)) {
                    while (rs.next()) {
                        String productCode = rs.getString("product_code");
                        String name = rs.getString("name");
                        int reorderLevel = rs.getInt("reorder_level");
                        int totalStock = rs.getInt("total_stock");
                        
                        System.out.printf("Product: %s | Total Stock: %d | Reorder Level: %d | Status: %s%n",
                            productCode,
                            totalStock,
                            reorderLevel,
                            (totalStock <= reorderLevel ? "🔴 NEEDS REORDER" : "✅ ABOVE THRESHOLD"));
                    }
                }
                
                System.out.println("\n=== CURRENT REORDER ALERTS ===");
                String alertSQL = "SELECT * FROM reorder_alert WHERE status = 'NEW' OR status = 'IMPROVED'";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(alertSQL)) {
                    
                    boolean hasAlerts = false;
                    while (rs.next()) {
                        hasAlerts = true;
                        System.out.printf("Product: %s | Current: %d | Threshold: %d | Status: %s | Created: %s%n",
                            rs.getString("product_code"),
                            rs.getInt("current_quantity"),
                            rs.getInt("threshold_quantity"), 
                            rs.getString("status"),
                            rs.getString("alert_created"));
                    }
                    
                    if (!hasAlerts) {
                        System.out.println("No active reorder alerts found.");
                    }
                }
                
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}