import java.sql.*;

/**
 * Check the reorder_alert table schema
 */
public class CheckReorderAlertSchema {
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    public static void main(String[] args) {
        System.out.println("Checking reorder_alert table schema");
        System.out.println("===================================");
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Check if table exists and its columns
                System.out.println("📋 Current reorder_alert table schema:");
                
                String pragmaSql = "PRAGMA table_info(reorder_alert)";
                try (PreparedStatement stmt = conn.prepareStatement(pragmaSql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    boolean hasColumns = false;
                    while (rs.next()) {
                        hasColumns = true;
                        int cid = rs.getInt("cid");
                        String name = rs.getString("name");
                        String type = rs.getString("type");
                        boolean notNull = rs.getBoolean("notnull");
                        String defaultValue = rs.getString("dflt_value");
                        boolean pk = rs.getBoolean("pk");
                        
                        System.out.printf("%d. %-20s %-15s %s %s %s%n", 
                            cid, name, type, 
                            notNull ? "NOT NULL" : "NULL", 
                            defaultValue != null ? "DEFAULT " + defaultValue : "",
                            pk ? "PRIMARY KEY" : "");
                    }
                    
                    if (!hasColumns) {
                        System.out.println("❌ Table reorder_alert does not exist or has no columns");
                    }
                }
                
                // Show current data
                System.out.println("\n📊 Current reorder_alert table data:");
                String dataSql = "SELECT * FROM reorder_alert LIMIT 5";
                try (PreparedStatement stmt = conn.prepareStatement(dataSql)) {
                    ResultSet rs = stmt.executeQuery();
                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();
                    
                    // Print headers
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.printf("%-15s ", meta.getColumnName(i));
                    }
                    System.out.println();
                    System.out.println("─".repeat(columnCount * 16));
                    
                    int rowCount = 0;
                    while (rs.next() && rowCount < 5) {
                        for (int i = 1; i <= columnCount; i++) {
                            System.out.printf("%-15s ", rs.getString(i));
                        }
                        System.out.println();
                        rowCount++;
                    }
                    
                    if (rowCount == 0) {
                        System.out.println("No data in reorder_alert table");
                    }
                }
                
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}