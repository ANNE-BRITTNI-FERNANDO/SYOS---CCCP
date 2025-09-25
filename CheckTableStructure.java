import java.sql.*;

public class CheckTableStructure {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/syos_inventory.db";
            
            try (Connection conn = DriverManager.getConnection(url)) {
                DatabaseMetaData meta = conn.getMetaData();
                
                String[] tables = {"product", "batch", "sales_transaction_item"};
                for (String tableName : tables) {
                    ResultSet tableCheck = meta.getTables(null, null, tableName, null);
                    
                    if (tableCheck.next()) {
                        System.out.println(tableName.toUpperCase() + " table structure:");
                        ResultSet columns = meta.getColumns(null, null, tableName, null);
                        while (columns.next()) {
                            String columnName = columns.getString("COLUMN_NAME");
                            String columnType = columns.getString("TYPE_NAME");
                            System.out.println("  " + columnName + " (" + columnType + ")");
                        }
                        System.out.println();
                    } else {
                        System.out.println(tableName + " table not found");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}