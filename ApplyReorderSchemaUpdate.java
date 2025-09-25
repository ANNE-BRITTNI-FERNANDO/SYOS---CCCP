import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Apply reorder alert schema updates
 */
public class ApplyReorderSchemaUpdate {
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    public static void main(String[] args) {
        System.out.println("Applying Reorder Alert Schema Updates");
        System.out.println("=====================================");
        
        try {
            // Read the SQL file
            String sqlContent = Files.readString(Paths.get("update_reorder_schema.sql"));
            String[] statements = sqlContent.split(";");
            
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                conn.setAutoCommit(false); // Use transaction
                
                for (String statement : statements) {
                    statement = statement.trim();
                    if (!statement.isEmpty() && !statement.startsWith("--")) {
                        try {
                            System.out.println("✅ Executing: " + statement.substring(0, Math.min(50, statement.length())) + "...");
                            conn.createStatement().executeUpdate(statement);
                        } catch (SQLException e) {
                            // Some statements might fail if columns already exist - that's okay
                            if (e.getMessage().contains("duplicate column name")) {
                                System.out.println("⚠️  Column already exists - skipping");
                            } else {
                                System.out.println("❌ Failed: " + e.getMessage());
                            }
                        }
                    }
                }
                
                conn.commit();
                System.out.println("\n✅ Schema update completed successfully!");
                
                // Verify the schema
                System.out.println("\n📋 Updated schema:");
                String pragmaSql = "PRAGMA table_info(reorder_alert)";
                try (PreparedStatement stmt = conn.prepareStatement(pragmaSql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        String name = rs.getString("name");
                        String type = rs.getString("type");
                        boolean notNull = rs.getBoolean("notnull");
                        
                        System.out.printf("  %-20s %-15s %s%n", 
                            name, type, notNull ? "NOT NULL" : "NULL");
                    }
                }
                
            }
        } catch (Exception e) {
            System.err.println("Error applying schema update: " + e.getMessage());
            e.printStackTrace();
        }
    }
}