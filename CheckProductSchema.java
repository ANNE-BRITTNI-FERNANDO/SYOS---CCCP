import java.sql.*;

public class CheckProductSchema {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:syos_inventory.db")) {
            
            System.out.println("PRODUCT TABLE SCHEMA:");
            System.out.println("=====================");
            
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "product", null);
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                String nullable = columns.getString("IS_NULLABLE");
                
                System.out.printf("%-25s %-15s Size:%-5d Nullable:%s%n", 
                    columnName, columnType, columnSize, nullable);
            }
            
            System.out.println("\nSample product data:");
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM product LIMIT 3");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                System.out.println("Product ID: " + rs.getLong("product_id"));
                System.out.println("Code: " + rs.getString("product_code"));
                System.out.println("Name: " + rs.getString("product_name"));
                System.out.println("---");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}