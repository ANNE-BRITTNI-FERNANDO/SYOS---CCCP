import java.sql.*;

public class FixAlertConstraint {
    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/syos_inventory.db");
        
        System.out.println("Adding new alert types to constraint...");
        
        // Since SQLite doesn't support ALTER CONSTRAINT, we need to recreate the table
        // First, let's backup the data
        conn.createStatement().executeUpdate("CREATE TEMP TABLE reorder_alert_backup AS SELECT * FROM reorder_alert");
        
        // Drop the original table
        conn.createStatement().executeUpdate("DROP TABLE reorder_alert");
        
        // Recreate with updated constraint
        String createSql = "CREATE TABLE reorder_alert (" +
                          "alert_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                          "product_id INTEGER NOT NULL, " +
                          "location_id INTEGER NOT NULL, " +
                          "current_quantity INTEGER NOT NULL, " +
                          "alert_type VARCHAR(20) NOT NULL CHECK (alert_type IN ('SHELF_RESTOCK', 'NEW_BATCH_ORDER', 'PRODUCT_CRITICAL', 'PRODUCT_CONSIDER')), " +
                          "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                          "product_code VARCHAR(20), " +
                          "threshold_quantity INTEGER, " +
                          "status VARCHAR(20) DEFAULT 'ACTIVE')";
        
        conn.createStatement().executeUpdate(createSql);
        
        // Restore the data
        conn.createStatement().executeUpdate("INSERT INTO reorder_alert SELECT * FROM reorder_alert_backup");
        
        // Drop the backup table
        conn.createStatement().executeUpdate("DROP TABLE reorder_alert_backup");
        
        System.out.println("✅ Successfully updated alert_type constraint!");
        
        // Verify
        ResultSet rs = conn.createStatement().executeQuery("SELECT sql FROM sqlite_master WHERE type='table' AND name='reorder_alert'");
        if (rs.next()) {
            System.out.println("\nUpdated table definition:");
            System.out.println(rs.getString("sql"));
        }
    }
}