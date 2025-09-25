import java.sql.*;

public class TestInventoryData {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/syos_inventory.db";
            
            try (Connection conn = DriverManager.getConnection(url)) {
                
                // First, clear existing inventory locations to test fresh setup
                System.out.println("Clearing existing inventory locations...");
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM inventory_location")) {
                    stmt.executeUpdate();
                }
                
                // Seed inventory locations
                System.out.println("Seeding inventory locations...");
                com.syos.inventory.application.seeder.InventoryLocationSeeder.seedInventoryLocations();
                
                // Check what locations were created
                System.out.println("\n=== INVENTORY LOCATIONS ===");
                String locationSql = "SELECT location_id, location_code, location_name, location_type FROM inventory_location";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(locationSql)) {
                    
                    while (rs.next()) {
                        System.out.printf("ID: %d | Code: %s | Name: %s | Type: %s%n",
                            rs.getLong("location_id"),
                            rs.getString("location_code"), 
                            rs.getString("location_name"),
                            rs.getString("location_type"));
                    }
                }
                
                // Test creating inventory records
                System.out.println("\n=== TESTING INVENTORY CREATION ===");
                com.syos.inventory.application.service.InventoryManagementService inventoryService = 
                    new com.syos.inventory.application.service.InventoryManagementService();
                
                // Create a test product for inventory (PRD-XXXXXXXX format)
                com.syos.shared.valueobjects.ProductCode productCode = 
                    new com.syos.shared.valueobjects.ProductCode("PRD-TEST0001");
                com.syos.shared.valueobjects.Money price = 
                    com.syos.shared.valueobjects.Money.of(new java.math.BigDecimal("100.00"));
                
                com.syos.domain.entities.Product testProduct = new com.syos.domain.entities.Product(
                    productCode, "Test Product", "Test Description", price, 
                    "Electronics", "Test Brand", "piece", 25, 50);
                
                // Create inventory for this product
                boolean created = inventoryService.createProductInventory(testProduct, 100, 50, 75);
                System.out.println("Inventory creation result: " + created);
                
                // Check physical_inventory table
                System.out.println("\n=== PHYSICAL INVENTORY DATA ===");
                String physicalSql = "SELECT pi.inventory_id, pi.product_code, il.location_name, pi.quantity_on_hand, pi.batch_id " +
                                   "FROM physical_inventory pi " +
                                   "JOIN inventory_location il ON pi.location_id = il.location_id " +
                                   "WHERE pi.product_code = 'PRD-TEST0001'";
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(physicalSql)) {
                    
                    while (rs.next()) {
                        System.out.printf("ID: %d | Product: %s | Location: %s | Quantity: %d | Batch: %d%n",
                            rs.getLong("inventory_id"),
                            rs.getString("product_code"),
                            rs.getString("location_name"), 
                            rs.getInt("quantity_on_hand"),
                            rs.getLong("batch_id"));
                    }
                }
                
                // Check online_inventory table
                System.out.println("\n=== ONLINE INVENTORY DATA ===");
                String onlineSql = "SELECT oi.online_inventory_id, oi.product_code, il.location_name, oi.available_quantity, oi.batch_id " +
                                 "FROM online_inventory oi " +
                                 "JOIN inventory_location il ON oi.location_id = il.location_id " +
                                 "WHERE oi.product_code = 'PRD-TEST0001'";
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(onlineSql)) {
                    
                    while (rs.next()) {
                        System.out.printf("ID: %d | Product: %s | Location: %s | Quantity: %d | Batch: %d%n",
                            rs.getLong("online_inventory_id"),
                            rs.getString("product_code"),
                            rs.getString("location_name"),
                            rs.getInt("available_quantity"),
                            rs.getLong("batch_id"));
                    }
                }
                
                // Test inventory retrieval
                System.out.println("\n=== INVENTORY RETRIEVAL TEST ===");
                int totalQty = inventoryService.getTotalInventoryQuantity("PRD-TEST0001");
                System.out.println("Total inventory quantity: " + totalQty);
                
                var locationBreakdown = inventoryService.getInventoryByLocation("PRD-TEST0001");
                System.out.println("Location breakdown:");
                for (var entry : locationBreakdown.entrySet()) {
                    System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " units");
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}