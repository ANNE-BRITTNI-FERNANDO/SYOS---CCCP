import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Check the current product schema and latest products with discounts
 */
public class CheckCurrentProducts {
    
    public static void main(String[] args) {
        System.out.println("=== Checking Current Product Schema and Data ===");
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found");
            return;
        }
        
        String dbUrl = "jdbc:sqlite:data/syos_inventory.db";
        
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            
            // Check product table schema
            System.out.println("\n1. Product Table Schema:");
            String schemaSql = "PRAGMA table_info(product)";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(schemaSql)) {
                
                System.out.println("Column Name      | Type         | Not Null | Default");
                System.out.println("---------------- | ------------ | -------- | -------");
                
                while (rs.next()) {
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    int notNull = rs.getInt("notnull");
                    String defaultValue = rs.getString("dflt_value");
                    
                    System.out.printf("%-16s | %-12s | %-8s | %s%n",
                        name, type, notNull == 1 ? "YES" : "NO", 
                        defaultValue != null ? defaultValue : "NULL");
                }
            }
            
            // Check latest products
            System.out.println("\n2. Latest Products (Last 5):");
            String productSql = "SELECT product_id, product_code, product_name, base_price, " +
                               "discount_percentage, discount_amount, final_price " +
                               "FROM product ORDER BY product_id DESC LIMIT 5";
                               
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(productSql)) {
                
                System.out.println("ID | Code         | Name           | Base    | Disc%   | DiscAmt | Final");
                System.out.println("-- | ------------ | -------------- | ------- | ------- | ------- | -------");
                
                while (rs.next()) {
                    int id = rs.getInt("product_id");
                    String code = rs.getString("product_code");
                    String name = rs.getString("product_name");
                    double basePrice = rs.getDouble("base_price");
                    double discPerc = rs.getDouble("discount_percentage");
                    double discAmt = rs.getDouble("discount_amount");
                    double finalPrice = rs.getDouble("final_price");
                    
                    // Truncate name if too long
                    if (name.length() > 14) {
                        name = name.substring(0, 11) + "...";
                    }
                    
                    System.out.printf("%2d | %-12s | %-14s | %7.2f | %7.2f | %7.2f | %7.2f%n",
                        id, code, name, basePrice, discPerc, discAmt, finalPrice);
                }
            }
            
            // Check the specific product we just created (ACC011)
            System.out.println("\n3. ACC011 Product Details:");
            String acc011Sql = "SELECT * FROM product WHERE product_name = 'ACC011'";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(acc011Sql)) {
                
                if (rs.next()) {
                    System.out.println("Product Found:");
                    System.out.printf("  ID: %d%n", rs.getInt("product_id"));
                    System.out.printf("  Code: %s%n", rs.getString("product_code"));
                    System.out.printf("  Name: %s%n", rs.getString("product_name"));
                    System.out.printf("  Description: %s%n", rs.getString("description"));
                    System.out.printf("  Base Price: %.2f%n", rs.getDouble("base_price"));
                    System.out.printf("  Discount Percentage: %.2f%%%n", rs.getDouble("discount_percentage"));
                    System.out.printf("  Discount Amount: %.2f%n", rs.getDouble("discount_amount"));
                    System.out.printf("  Final Price: %.2f%n", rs.getDouble("final_price"));
                    System.out.printf("  Brand: %s%n", rs.getString("brand"));
                    System.out.printf("  Unit: %s%n", rs.getString("unit_of_measure"));
                } else {
                    System.out.println("ACC011 product not found");
                }
            }
            
            System.out.println("\n=== Check Complete ===");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}