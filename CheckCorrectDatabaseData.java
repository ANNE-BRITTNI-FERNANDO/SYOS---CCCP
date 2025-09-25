import java.sql.*;
import java.io.File;

public class CheckCorrectDatabaseData {
    public static void main(String[] args) {
        String correctDbPath = "data/syos_inventory.db";
        
        System.out.println("=== CHECKING CORRECT DATABASE DATA ===");
        System.out.println("Database file: " + correctDbPath);
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + correctDbPath)) {
            
            // Check products (correct table name: product)
            String productQuery = "SELECT COUNT(*) as count FROM product";
            try (PreparedStatement pstmt = conn.prepareStatement(productQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Total products: " + rs.getInt("count"));
                }
            }
            
            // Show recent products with PRD codes
            String recentProductsQuery = "SELECT product_id, product_code, product_name, created_at FROM product ORDER BY product_id DESC LIMIT 10";
            System.out.println("\nRecent products:");
            try (PreparedStatement pstmt = conn.prepareStatement(recentProductsQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("ID: %d, Code: %s, Name: %s, Created: %s%n", 
                        rs.getInt("product_id"),
                        rs.getString("product_code"),
                        rs.getString("product_name"),
                        rs.getString("created_at"));
                }
            }
            
            // Check physical inventory
            String inventoryQuery = "SELECT COUNT(*) as count FROM physical_inventory";
            try (PreparedStatement pstmt = conn.prepareStatement(inventoryQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\nTotal inventory records: " + rs.getInt("count"));
                }
            }
            
            // Show recent inventory records (correct table names)
            String recentInventoryQuery = "SELECT pi.inventory_id, pi.batch_id, b.product_id, p.product_code, pi.location_id, " +
                "il.location_name, pi.current_quantity, pi.last_updated " +
                "FROM physical_inventory pi " +
                "JOIN batch b ON pi.batch_id = b.batch_id " +
                "JOIN product p ON b.product_id = p.product_id " +
                "JOIN inventory_location il ON pi.location_id = il.location_id " +
                "ORDER BY pi.inventory_id DESC LIMIT 10";
            
            System.out.println("\nRecent inventory records:");
            try (PreparedStatement pstmt = conn.prepareStatement(recentInventoryQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("Inv ID: %d, Batch ID: %d, Product: %s (%d), Location: %s, Qty: %d, Updated: %s%n",
                        rs.getInt("inventory_id"),
                        rs.getInt("batch_id"),
                        rs.getString("product_code"),
                        rs.getInt("product_id"),
                        rs.getString("location_name"),
                        rs.getInt("current_quantity"),
                        rs.getString("last_updated"));
                }
            }
            
            // Check inventory locations (correct table name)
            String locationsQuery = "SELECT location_id, location_name, location_type FROM inventory_location ORDER BY location_id";
            System.out.println("\nInventory locations:");
            try (PreparedStatement pstmt = conn.prepareStatement(locationsQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("Location ID: %d, Name: %s, Type: %s%n",
                        rs.getInt("location_id"),
                        rs.getString("location_name"),
                        rs.getString("location_type"));
                }
            }
            
            // Check batches
            String batchQuery = "SELECT COUNT(*) as count FROM batch";
            try (PreparedStatement pstmt = conn.prepareStatement(batchQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\nTotal batch records: " + rs.getInt("count"));
                }
            }
            
            // Show recent batches
            String recentBatchQuery = "SELECT b.batch_id, b.product_id, p.product_code, b.batch_number, b.quantity_received, b.created_at " +
                "FROM batch b " +
                "JOIN product p ON b.product_id = p.product_id " +
                "ORDER BY b.batch_id DESC LIMIT 10";
            
            System.out.println("\nRecent batches:");
            try (PreparedStatement pstmt = conn.prepareStatement(recentBatchQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("Batch ID: %d, Product: %s (%d), Batch#: %s, Qty: %d, Created: %s%n",
                        rs.getInt("batch_id"),
                        rs.getString("product_code"),
                        rs.getInt("product_id"),
                        rs.getString("batch_number"),
                        rs.getInt("quantity_received"),
                        rs.getString("created_at"));
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}