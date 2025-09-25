import java.sql.*;
import java.io.File;

public class VerifyCorrectDatabase {
    public static void main(String[] args) {
        String correctDbPath = "data/syos_inventory.db";
        
        System.out.println("=== VERIFYING CORRECT DATABASE ===");
        System.out.println("Database file: " + correctDbPath);
        
        File dbFile = new File(correctDbPath);
        System.out.println("File exists: " + dbFile.exists());
        System.out.println("File size: " + dbFile.length() + " bytes");
        System.out.println("Absolute path: " + dbFile.getAbsolutePath());
        
        if (!dbFile.exists()) {
            System.out.println("ERROR: Database file not found!");
            return;
        }
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + correctDbPath)) {
            System.out.println("\n=== DATABASE CONTENTS ===");
            
            // Check products
            String productQuery = "SELECT COUNT(*) as count FROM products";
            try (PreparedStatement pstmt = conn.prepareStatement(productQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Total products: " + rs.getInt("count"));
                }
            }
            
            // Show recent products with PRD codes
            String recentProductsQuery = "SELECT product_id, product_code, product_name, created_at FROM products ORDER BY product_id DESC LIMIT 5";
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
            
            // Show recent inventory records
            String recentInventoryQuery = "SELECT pi.inventory_id, pi.product_id, p.product_code, pi.location_id, " +
                "il.location_name, pi.quantity_on_hand, pi.last_updated " +
                "FROM physical_inventory pi " +
                "JOIN products p ON pi.product_id = p.product_id " +
                "JOIN inventory_locations il ON pi.location_id = il.location_id " +
                "ORDER BY pi.inventory_id DESC LIMIT 10";
            
            System.out.println("\nRecent inventory records:");
            try (PreparedStatement pstmt = conn.prepareStatement(recentInventoryQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("Inv ID: %d, Product: %s (%s), Location: %s, Qty: %d, Updated: %s%n",
                        rs.getInt("inventory_id"),
                        rs.getString("product_code"),
                        rs.getInt("product_id"),
                        rs.getString("location_name"),
                        rs.getInt("quantity_on_hand"),
                        rs.getString("last_updated"));
                }
            }
            
            // Check inventory locations
            String locationsQuery = "SELECT location_id, location_name, location_type FROM inventory_locations ORDER BY location_id";
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
            
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}