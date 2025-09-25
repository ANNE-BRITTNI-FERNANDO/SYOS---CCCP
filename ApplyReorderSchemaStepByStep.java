import java.sql.*;

/**
 * Apply reorder alert schema updates step by step
 */
public class ApplyReorderSchemaStepByStep {
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    public static void main(String[] args) {
        System.out.println("Applying Reorder Alert Schema Updates Step by Step");
        System.out.println("==================================================");
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                
                // Step 1: Add product_code column
                try {
                    String sql = "ALTER TABLE reorder_alert ADD COLUMN product_code VARCHAR(20)";
                    conn.createStatement().executeUpdate(sql);
                    System.out.println("✅ Added product_code column");
                } catch (SQLException e) {
                    if (e.getMessage().contains("duplicate column name")) {
                        System.out.println("⚠️  product_code column already exists");
                    } else {
                        System.out.println("❌ Failed to add product_code: " + e.getMessage());
                    }
                }
                
                // Step 2: Add threshold_quantity column
                try {
                    String sql = "ALTER TABLE reorder_alert ADD COLUMN threshold_quantity INTEGER";
                    conn.createStatement().executeUpdate(sql);
                    System.out.println("✅ Added threshold_quantity column");
                } catch (SQLException e) {
                    if (e.getMessage().contains("duplicate column name")) {
                        System.out.println("⚠️  threshold_quantity column already exists");
                    } else {
                        System.out.println("❌ Failed to add threshold_quantity: " + e.getMessage());
                    }
                }
                
                // Step 3: Add status column
                try {
                    String sql = "ALTER TABLE reorder_alert ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE'";
                    conn.createStatement().executeUpdate(sql);
                    System.out.println("✅ Added status column");
                } catch (SQLException e) {
                    if (e.getMessage().contains("duplicate column name")) {
                        System.out.println("⚠️  status column already exists");
                    } else {
                        System.out.println("❌ Failed to add status: " + e.getMessage());
                    }
                }
                
                // Step 4: Add alert_created column
                try {
                    String sql = "ALTER TABLE reorder_alert ADD COLUMN alert_created DATETIME DEFAULT CURRENT_TIMESTAMP";
                    conn.createStatement().executeUpdate(sql);
                    System.out.println("✅ Added alert_created column");
                } catch (SQLException e) {
                    if (e.getMessage().contains("duplicate column name")) {
                        System.out.println("⚠️  alert_created column already exists");
                    } else {
                        System.out.println("❌ Failed to add alert_created: " + e.getMessage());
                    }
                }
                
                // Step 5: Update existing records with product_code
                try {
                    String sql = "UPDATE reorder_alert SET product_code = (" +
                                "SELECT p.product_code FROM product p WHERE p.product_id = reorder_alert.product_id" +
                                ") WHERE product_code IS NULL";
                    int updated = conn.createStatement().executeUpdate(sql);
                    System.out.println("✅ Updated " + updated + " records with product_code values");
                } catch (SQLException e) {
                    System.out.println("❌ Failed to update product_code: " + e.getMessage());
                }
                
                // Step 6: Update existing records with status
                try {
                    String sql = "UPDATE reorder_alert SET status = 'ACTIVE' WHERE status IS NULL";
                    int updated = conn.createStatement().executeUpdate(sql);
                    System.out.println("✅ Updated " + updated + " records with status values");
                } catch (SQLException e) {
                    System.out.println("❌ Failed to update status: " + e.getMessage());
                }
                
                // Step 7: Update existing records with alert_created
                try {
                    String sql = "UPDATE reorder_alert SET alert_created = created_at WHERE alert_created IS NULL";
                    int updated = conn.createStatement().executeUpdate(sql);
                    System.out.println("✅ Updated " + updated + " records with alert_created values");
                } catch (SQLException e) {
                    System.out.println("❌ Failed to update alert_created: " + e.getMessage());
                }
                
                // Verify the schema
                System.out.println("\n📋 Final schema:");
                String pragmaSql = "PRAGMA table_info(reorder_alert)";
                try (PreparedStatement stmt = conn.prepareStatement(pragmaSql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        String name = rs.getString("name");
                        String type = rs.getString("type");
                        boolean notNull = rs.getBoolean("notnull");
                        String defaultValue = rs.getString("dflt_value");
                        
                        System.out.printf("  %-20s %-15s %-8s %s%n", 
                            name, type, notNull ? "NOT NULL" : "NULL",
                            defaultValue != null ? "DEFAULT " + defaultValue : "");
                    }
                }
                
                // Show updated data
                System.out.println("\n📊 Current data after update:");
                String dataSql = "SELECT * FROM reorder_alert LIMIT 3";
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
                    
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            String value = rs.getString(i);
                            if (value != null && value.length() > 14) {
                                value = value.substring(0, 11) + "...";
                            }
                            System.out.printf("%-15s ", value);
                        }
                        System.out.println();
                    }
                }
                
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}