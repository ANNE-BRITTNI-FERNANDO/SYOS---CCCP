import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.math.BigDecimal;

/**
 * Simple test to create a product with discount and verify it's stored correctly
 */
public class TestDiscountDirectly {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Discount Storage Directly ===");
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found");
            return;
        }
        
        String dbUrl = "jdbc:sqlite:data/syos_inventory.db";
        
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            
            // First, show current Coke product data
            System.out.println("\n1. Current Coke Product Data:");
            showCurrentCokeData(conn);
            
            // Test creating a product with discount directly
            System.out.println("\n2. Creating Test Product with Discount:");
            createProductWithDiscount(conn);
            
            // Show the created product
            System.out.println("\n3. Verifying Created Product:");
            showTestProductData(conn);
            
            // Test updating Coke product with proper discount
            System.out.println("\n4. Fixing Coke Product Discount:");
            fixCokeDiscount(conn);
            
            // Show updated Coke data
            System.out.println("\n5. Updated Coke Product Data:");
            showCurrentCokeData(conn);
            
            System.out.println("\n=== Test Complete ===");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void showCurrentCokeData(Connection conn) throws Exception {
        String sql = "SELECT product_id, product_code, product_name, base_price, " +
                    "discount_percentage, discount_amount, final_price " +
                    "FROM product WHERE product_name = 'Coke' AND active = 1";
                    
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                System.out.printf("ID: %d, Code: %s, Name: %s%n", 
                    rs.getInt("product_id"), 
                    rs.getString("product_code"),
                    rs.getString("product_name"));
                System.out.printf("Base Price: %.2f, Discount %%: %.2f, Discount Amount: %.2f, Final Price: %.2f%n",
                    rs.getDouble("base_price"),
                    rs.getDouble("discount_percentage"), 
                    rs.getDouble("discount_amount"),
                    rs.getDouble("final_price"));
            } else {
                System.out.println("No Coke product found");
            }
        }
    }
    
    private static void createProductWithDiscount(Connection conn) throws Exception {
        String sql = "INSERT INTO product (product_code, product_name, description, brand, " +
                    "base_price, unit_of_measure, subcategory_id, discount_percentage, " +
                    "discount_amount, final_price, active, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            BigDecimal basePrice = new BigDecimal("200.00");
            BigDecimal discountPercentage = new BigDecimal("10.00"); // 10%
            BigDecimal discountAmount = basePrice.multiply(discountPercentage).divide(new BigDecimal("100"));
            BigDecimal finalPrice = basePrice.subtract(discountAmount);
            
            stmt.setString(1, "PRD-TEST001");
            stmt.setString(2, "Test Discount Product");
            stmt.setString(3, "Test product with proper discount");
            stmt.setString(4, "TestBrand");
            stmt.setBigDecimal(5, basePrice);
            stmt.setString(6, "pcs");
            stmt.setLong(7, 1L); // Subcategory ID
            stmt.setBigDecimal(8, discountPercentage);
            stmt.setBigDecimal(9, discountAmount);
            stmt.setBigDecimal(10, finalPrice);
            stmt.setBoolean(11, true);
            stmt.setLong(12, 1L); // Created by admin
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Product created, rows affected: " + rowsAffected);
            System.out.printf("Calculated: Base=%.2f, Discount%%=%.2f, DiscountAmt=%.2f, Final=%.2f%n",
                basePrice, discountPercentage, discountAmount, finalPrice);
        }
    }
    
    private static void showTestProductData(Connection conn) throws Exception {
        String sql = "SELECT product_code, product_name, base_price, " +
                    "discount_percentage, discount_amount, final_price " +
                    "FROM product WHERE product_code = 'PRD-TEST001'";
                    
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                System.out.printf("Code: %s, Name: %s%n", 
                    rs.getString("product_code"),
                    rs.getString("product_name"));
                System.out.printf("Base Price: %.2f, Discount %%: %.2f, Discount Amount: %.2f, Final Price: %.2f%n",
                    rs.getDouble("base_price"),
                    rs.getDouble("discount_percentage"), 
                    rs.getDouble("discount_amount"),
                    rs.getDouble("final_price"));
            } else {
                System.out.println("Test product not found");
            }
        }
    }
    
    private static void fixCokeDiscount(Connection conn) throws Exception {
        // Update Coke product with 12% discount as mentioned in the UI
        String sql = "UPDATE product SET discount_percentage = ?, discount_amount = ?, final_price = ? " +
                    "WHERE product_name = 'Coke' AND active = 1";
                    
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            BigDecimal basePrice = new BigDecimal("250.00"); // Coke's base price
            BigDecimal discountPercentage = new BigDecimal("12.00"); // 12% as shown in UI
            BigDecimal discountAmount = basePrice.multiply(discountPercentage).divide(new BigDecimal("100"));
            BigDecimal finalPrice = basePrice.subtract(discountAmount);
            
            stmt.setBigDecimal(1, discountPercentage);
            stmt.setBigDecimal(2, discountAmount);
            stmt.setBigDecimal(3, finalPrice);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Coke product updated, rows affected: " + rowsAffected);
            System.out.printf("Applied: 12%% discount, Amount=%.2f, Final Price=%.2f%n",
                discountAmount, finalPrice);
        }
    }
}