import java.sql.*;

public class QuickInventoryTest {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/syos_inventory.db";
            
            try (Connection conn = DriverManager.getConnection(url)) {
                System.out.println("=== CURRENT INVENTORY STATUS ===\n");
                
                // Check latest products with their inventory
                String sql = "SELECT p.product_code, p.product_name, " +
                           "COALESCE(SUM(pi.current_quantity), 0) as physical_qty, " +
                           "COALESCE(SUM(oi.available_quantity), 0) as online_qty, " +
                           "COUNT(DISTINCT b.batch_id) as batch_count " +
                           "FROM product p " +
                           "LEFT JOIN batch b ON p.product_id = b.product_id " +
                           "LEFT JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                           "LEFT JOIN online_inventory oi ON b.batch_id = oi.batch_id " +
                           "WHERE p.product_id > 6 " +  // Only newer products
                           "GROUP BY p.product_id, p.product_code, p.product_name " +
                           "ORDER BY p.product_id DESC";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    System.out.println("Product Code     | Product Name          | Physical | Online | Batches");
                    System.out.println("----------------------------------------------------------------");
                    
                    while (rs.next()) {
                        System.out.printf("%-15s | %-20s | %-8d | %-6d | %d%n",
                            rs.getString("product_code"),
                            rs.getString("product_name").substring(0, Math.min(20, rs.getString("product_name").length())),
                            rs.getInt("physical_qty"),
                            rs.getInt("online_qty"),
                            rs.getInt("batch_count"));
                    }
                }
                
                System.out.println("\n=== INVENTORY BREAKDOWN BY LOCATION ===");
                String locationSql = "SELECT il.location_code, il.location_name, " +
                                   "COUNT(pi.inventory_id) as inventory_records, " +
                                   "SUM(pi.current_quantity) as total_quantity " +
                                   "FROM inventory_location il " +
                                   "LEFT JOIN physical_inventory pi ON il.location_id = pi.location_id " +
                                   "GROUP BY il.location_id ORDER BY il.location_id";
                
                try (PreparedStatement stmt = conn.prepareStatement(locationSql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        System.out.printf("%s (%s): %d records, %d units%n",
                            rs.getString("location_code"),
                            rs.getString("location_name"),
                            rs.getInt("inventory_records"),
                            rs.getInt("total_quantity"));
                    }
                }
                
                // Check online inventory
                System.out.println("\n=== ONLINE INVENTORY ===");
                String onlineSql = "SELECT COUNT(*) as records, SUM(available_quantity) as total FROM online_inventory";
                try (PreparedStatement stmt = conn.prepareStatement(onlineSql)) {
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        System.out.printf("Online inventory: %d records, %d units%n",
                            rs.getInt("records"),
                            rs.getInt("total"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}