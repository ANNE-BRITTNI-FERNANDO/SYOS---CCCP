import java.sql.*;
import java.io.File;

public class CheckDatabaseSchema {
    public static void main(String[] args) {
        String correctDbPath = "data/syos_inventory.db";
        
        System.out.println("=== CHECKING DATABASE SCHEMA ===");
        System.out.println("Database file: " + correctDbPath);
        
        File dbFile = new File(correctDbPath);
        System.out.println("File exists: " + dbFile.exists());
        System.out.println("File size: " + dbFile.length() + " bytes");
        
        if (!dbFile.exists()) {
            System.out.println("ERROR: Database file not found!");
            return;
        }
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + correctDbPath)) {
            System.out.println("\n=== EXISTING TABLES ===");
            
            // Get all tables in the database
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"});
            
            boolean foundTables = false;
            while (tables.next()) {
                foundTables = true;
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("Table: " + tableName);
                
                // Get columns for each table
                ResultSet columns = meta.getColumns(null, null, tableName, null);
                while (columns.next()) {
                    System.out.printf("  Column: %s (%s)%n", 
                        columns.getString("COLUMN_NAME"),
                        columns.getString("TYPE_NAME"));
                }
                System.out.println();
            }
            
            if (!foundTables) {
                System.out.println("No tables found in database - it's empty!");
            }
            
            // Try to check if there are any SQLite master entries
            System.out.println("\n=== SQLITE MASTER TABLE ===");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT type, name FROM sqlite_master ORDER BY name")) {
                
                boolean foundEntries = false;
                while (rs.next()) {
                    foundEntries = true;
                    System.out.printf("Type: %s, Name: %s%n", 
                        rs.getString("type"), 
                        rs.getString("name"));
                }
                
                if (!foundEntries) {
                    System.out.println("Database is completely empty - no schema defined!");
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}