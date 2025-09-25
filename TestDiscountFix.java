import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

// Import the ProductNew and service classes
import com.syos.inventory.domain.entity.ProductNew;
import com.syos.application.services.ProductManagementServiceFixed;
import com.syos.inventory.infrastructure.database.DatabaseConnectionProvider;
import com.syos.inventory.infrastructure.repository.ProductNewRepositoryImpl;

/**
 * Test to verify that the discount functionality in ProductManagementUI has been fixed
 */
public class TestDiscountFix {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Discount Functionality Fix ===");
        
        try {
            // Setup database connection and service
            DatabaseConnectionProvider connectionProvider = DatabaseConnectionProvider.getInstance();
            ProductNewRepositoryImpl productRepository = new ProductNewRepositoryImpl(connectionProvider);
            ProductManagementServiceFixed service = new ProductManagementServiceFixed(productRepository);
            
            // Test 1: Create product with fixed discount
            System.out.println("\n1. Testing Fixed Discount Creation:");
            boolean fixedDiscountResult = service.createProduct(
                "Test Fixed Discount Product",
                "Product for testing fixed discount storage",
                1L, // Subcategory ID
                new BigDecimal("300.00"), // Base price
                "pcs", // Unit
                "TestBrand",
                1L, // Created by admin
                1, // Discount type 1 = fixed amount
                new BigDecimal("50.00"), // Fixed discount of 50 LKR
                BigDecimal.ZERO // Not used for fixed discount
            );
            
            System.out.println("Fixed discount product created: " + fixedDiscountResult);
            
            // Test 2: Create product with percentage discount
            System.out.println("\n2. Testing Percentage Discount Creation:");
            boolean percentageDiscountResult = service.createProduct(
                "Test Percentage Discount Product",
                "Product for testing percentage discount storage",
                1L, // Subcategory ID
                new BigDecimal("400.00"), // Base price
                "pcs", // Unit
                "TestBrand",
                1L, // Created by admin
                2, // Discount type 2 = percentage
                new BigDecimal("15.00"), // 15% discount
                BigDecimal.ZERO // Not used for percentage discount
            );
            
            System.out.println("Percentage discount product created: " + percentageDiscountResult);
            
            // Test 3: Verify the discounts are stored correctly in database
            System.out.println("\n3. Verifying Discount Storage in Database:");
            verifyDiscountStorage();
            
            // Test 4: Check products through service
            System.out.println("\n4. Checking Products Through Service:");
            List<ProductNew> allProducts = service.getAllProducts();
            for (ProductNew product : allProducts) {
                if (product.getProductName().contains("Test") && product.getProductName().contains("Discount")) {
                    System.out.printf("Product: %s%n", product.getProductName());
                    System.out.printf("  Code: %s%n", product.getProductCode());
                    System.out.printf("  Base Price: %.2f%n", product.getBasePrice());
                    System.out.printf("  Discount %%: %.2f%n", product.getDiscountPercentage());
                    System.out.printf("  Discount Amount: %.2f%n", product.getDiscountAmount());
                    System.out.printf("  Final Price: %.2f%n", product.getFinalPrice());
                    System.out.printf("  Has Discount: %s%n", product.hasDiscount());
                    if (product.hasDiscount()) {
                        System.out.printf("  Discount Description: %s%n", product.getDiscountDescription());
                    }
                    System.out.println();
                }
            }
            
            System.out.println("=== Test Completed ===");
            
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verify discount storage in database directly
     */
    private static void verifyDiscountStorage() {
        String dbUrl = "jdbc:sqlite:data/syos_inventory.db";
        
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            String sql = "SELECT product_code, product_name, base_price, discount_percentage, " +
                        "discount_amount, final_price FROM product " +
                        "WHERE product_name LIKE '%Test%Discount%' AND active = 1 " +
                        "ORDER BY product_id DESC LIMIT 5";
                        
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                System.out.println("Recent Discount Test Products in Database:");
                System.out.println("Code          | Name                           | Base    | Disc%   | DiscAmt | Final");
                System.out.println("------------- | ------------------------------ | ------- | ------- | ------- | -------");
                
                while (rs.next()) {
                    String code = rs.getString("product_code");
                    String name = rs.getString("product_name");
                    double basePrice = rs.getDouble("base_price");
                    double discPerc = rs.getDouble("discount_percentage");
                    double discAmt = rs.getDouble("discount_amount");
                    double finalPrice = rs.getDouble("final_price");
                    
                    // Truncate name if too long
                    if (name.length() > 30) {
                        name = name.substring(0, 27) + "...";
                    }
                    
                    System.out.printf("%-13s | %-30s | %7.2f | %7.2f | %7.2f | %7.2f%n",
                        code, name, basePrice, discPerc, discAmt, finalPrice);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error verifying discount storage: " + e.getMessage());
        }
    }
}