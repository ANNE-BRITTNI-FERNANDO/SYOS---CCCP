import java.sql.*;
import java.util.Date;

public class TestExpiryQuery {
    public static void main(String[] args) {
        String databaseUrl = "jdbc:sqlite:data/syos_inventory.db";
        
        String sql = "SELECT b.product_id, " +
            "MIN(CASE WHEN b.expiry_date IS NOT NULL THEN b.expiry_date END) as earliest_expiry_date " +
            "FROM batch b " +
            "INNER JOIN online_inventory oi ON b.batch_id = oi.batch_id " +
            "WHERE b.expiry_date IS NULL OR b.expiry_date > date('now') " +
            "GROUP BY b.product_id " +
            "LIMIT 5";
            
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("Testing expiry date query...");
            while (rs.next()) {
                Long productId = rs.getLong("product_id");
                String expiryDateStr = rs.getString("earliest_expiry_date");
                
                Date expiryDate = null;
                if (expiryDateStr != null && !expiryDateStr.isEmpty()) {
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        expiryDate = sdf.parse(expiryDateStr);
                    } catch (Exception parseEx) {
                        System.out.println("Parse error: " + parseEx.getMessage());
                    }
                }
                
                System.out.println("Product ID: " + productId + 
                                 ", Expiry Date String: " + expiryDateStr +
                                 ", Parsed Date: " + (expiryDate != null ? expiryDate.toString() : "NULL"));
            }
            System.out.println("Query completed successfully!");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}