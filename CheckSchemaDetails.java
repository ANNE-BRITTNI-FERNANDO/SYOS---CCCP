import java.sql.*;

public class CheckSchemaDetails {
    public static void main(String[] args) {
        String dbPath = "data/syos_inventory.db";
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + dbPath;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Check physical_inventory table structure
                System.out.println("=== PHYSICAL_INVENTORY TABLE STRUCTURE ===");
                String pragmaSQL = "PRAGMA table_info(physical_inventory)";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(pragmaSQL)) {
                    while (rs.next()) {
                        System.out.printf("Column: %-20s Type: %-15s NotNull: %s Default: %s%n",
                            rs.getString("name"),
                            rs.getString("type"),
                            rs.getBoolean("notnull"),
                            rs.getString("dflt_value"));
                    }
                }
                
                System.out.println("\n=== SAMPLE PHYSICAL_INVENTORY DATA ===");
                String sampleSQL = "SELECT pi.*, il.location_code, p.product_code " +
                                 "FROM physical_inventory pi " +
                                 "JOIN inventory_location il ON pi.location_id = il.location_id " +
                                 "JOIN batch b ON pi.batch_id = b.batch_id " +
                                 "JOIN product p ON b.product_id = p.product_id " +
                                 "WHERE p.product_code = 'PRD-LAFA0003' " +
                                 "LIMIT 5";
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sampleSQL)) {
                    while (rs.next()) {
                        System.out.printf("Product: %s | Location: %s | Current: %d | Min_Threshold: %s | Location_Capacity: %d%n",
                            rs.getString("product_code"),
                            rs.getString("location_code"),
                            rs.getInt("current_quantity"),
                            rs.getString("min_threshold"),
                            rs.getInt("location_capacity"));
                    }
                }
                
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}