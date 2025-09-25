import java.sql.*;
import java.io.*;
import java.nio.file.*;

public class ApplySchema {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/syos_inventory.db";
            
            // Read schema file
            String schemaContent = Files.readString(Paths.get("src/main/resources/database/schema.sql"));
            
            try (Connection conn = DriverManager.getConnection(url)) {
                System.out.println("Applying database schema...");
                
                // Split schema by statements (simple split on ;)
                String[] statements = schemaContent.split(";");
                
                for (String statement : statements) {
                    statement = statement.trim();
                    if (!statement.isEmpty() && !statement.startsWith("--")) {
                        try {
                            try (Statement stmt = conn.createStatement()) {
                                stmt.execute(statement);
                            }
                        } catch (Exception e) {
                            if (!e.getMessage().contains("already exists")) {
                                System.out.println("Warning: " + e.getMessage());
                            }
                        }
                    }
                }
                
                System.out.println("Schema applied successfully!");
                
                // Verify tables exist
                System.out.println("\nVerifying tables:");
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
                while (tables.next()) {
                    System.out.println("- " + tables.getString("TABLE_NAME"));
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}