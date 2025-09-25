import java.sql.*;

public class CheckDatabase {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:data/syos_inventory.db");
            
            // Check if product table exists
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "product", null);
            if (tables.next()) {
                System.out.println("Product table exists");
                
                // Check how many products are in the table
                PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM product");
                ResultSet countResult = countStmt.executeQuery();
                if (countResult.next()) {
                    System.out.println("Total products in table: " + countResult.getInt(1));
                }
                
                // Check active products
                PreparedStatement activeStmt = conn.prepareStatement("SELECT COUNT(*) FROM product WHERE is_active = 1");
                ResultSet activeResult = activeStmt.executeQuery();
                if (activeResult.next()) {
                    System.out.println("Active products in table: " + activeResult.getInt(1));
                }
                
                // Show all products
                PreparedStatement allStmt = conn.prepareStatement("SELECT product_code, product_name, is_active FROM product ORDER BY product_code");
                ResultSet allResult = allStmt.executeQuery();
                System.out.println("\nAll products:");
                while (allResult.next()) {
                    System.out.println(allResult.getString(1) + " | " + allResult.getString(2) + " | Active: " + allResult.getBoolean(3));
                }
                
            } else {
                System.out.println("Product table does not exist");
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}