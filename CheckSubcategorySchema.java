import java.sql.*;

public class CheckSubcategorySchema {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:data/syos_inventory.db")) {
            // Check subcategory table structure
            System.out.println("=== SUBCATEGORY TABLE SCHEMA ===");
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "subcategory", "%");
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                String isNullable = columns.getString("IS_NULLABLE");
                System.out.println("  - " + columnName + " (" + columnType + ") Nullable: " + isNullable);
            }
            columns.close();
            
            // Check what fields are actually being inserted
            System.out.println("\n=== CURRENT SUBCATEGORY DATA ===");
            String sql = "SELECT * FROM subcategory LIMIT 5";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            
            // Print column headers
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            
            // Print data
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}