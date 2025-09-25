import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Check physical_inventory schema and find ACC011 inventory
 */
public class CheckPhysicalInventorySchema {
    
    public static void main(String[] args) {
        System.out.println("=== Checking Physical Inventory Schema ===");
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found");
            return;
        }
        
        String dbUrl = "jdbc:sqlite:data/syos_inventory.db";
        
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            
            // Check physical_inventory table schema
            System.out.println("1. Physical Inventory Table Schema:");
            String schemaSql = "PRAGMA table_info(physical_inventory)";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(schemaSql)) {
                
                System.out.println("Column Name      | Type         | Not Null | Default");
                System.out.println("---------------- | ------------ | -------- | -------");
                
                while (rs.next()) {
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    int notNull = rs.getInt("notnull");
                    String defaultValue = rs.getString("dflt_value");
                    
                    System.out.printf("%-16s | %-12s | %-8s | %s%n",
                        name, type, notNull == 1 ? "YES" : "NO", 
                        defaultValue != null ? defaultValue : "NULL");
                }
            }
            
            // Check all physical inventory records
            System.out.println("\n2. All Physical Inventory Records:");
            String inventorySql = "SELECT pi.inventory_id, pi.product_id, il.location_name, pi.quantity_on_hand, pi.location_capacity " +
                                 "FROM physical_inventory pi " +
                                 "JOIN inventory_location il ON pi.location_id = il.location_id " +
                                 "ORDER BY pi.inventory_id DESC LIMIT 10";
                                 
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(inventorySql)) {
                
                System.out.println("Inv ID | Prod ID | Location     | Quantity | Capacity");
                System.out.println("------ | ------- | ------------ | -------- | --------");
                
                while (rs.next()) {
                    System.out.printf("%6d | %7d | %-12s | %8d | %8d%n",
                        rs.getInt("inventory_id"), rs.getInt("product_id"),
                        rs.getString("location_name"), rs.getInt("quantity_on_hand"),
                        rs.getInt("location_capacity"));
                }
            }
            
            System.out.println("\n=== Check Complete ===");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}