import java.sql.*;

public class CheckInventorySchema {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/syos_inventory.db";
            
            try (Connection conn = DriverManager.getConnection(url)) {
                
                System.out.println("=== BATCH TABLE SCHEMA ===");
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("PRAGMA table_info(batch)");
                    while (rs.next()) {
                        System.out.printf("Column: %s | Type: %s | NotNull: %d | Default: %s%n", 
                            rs.getString("name"), 
                            rs.getString("type"),
                            rs.getInt("notnull"),
                            rs.getString("dflt_value"));
                    }
                } catch (Exception e) {
                    System.out.println("batch table doesn't exist: " + e.getMessage());
                }
                
                System.out.println("\n=== PHYSICAL_INVENTORY TABLE SCHEMA ===");
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("PRAGMA table_info(physical_inventory)");
                    while (rs.next()) {
                        System.out.printf("Column: %s | Type: %s | NotNull: %d | Default: %s%n", 
                            rs.getString("name"), 
                            rs.getString("type"),
                            rs.getInt("notnull"),
                            rs.getString("dflt_value"));
                    }
                } catch (Exception e) {
                    System.out.println("physical_inventory table doesn't exist: " + e.getMessage());
                }
                
                System.out.println("\n=== ONLINE_INVENTORY TABLE SCHEMA ===");
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("PRAGMA table_info(online_inventory)");
                    while (rs.next()) {
                        System.out.printf("Column: %s | Type: %s | NotNull: %d | Default: %s%n", 
                            rs.getString("name"), 
                            rs.getString("type"),
                            rs.getInt("notnull"),
                            rs.getString("dflt_value"));
                    }
                } catch (Exception e) {
                    System.out.println("online_inventory table doesn't exist: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}