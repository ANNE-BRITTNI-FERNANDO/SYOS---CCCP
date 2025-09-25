import com.syos.application.services.ProductManagementServiceFixed;
import com.syos.inventory.domain.entity.ProductNew;
import java.math.BigDecimal;
import java.util.List;

public class TestProductCreation {
    public static void main(String[] args) {
        try {
            System.out.println("=== Testing Product Creation ===");
            
            // Create service
            ProductManagementServiceFixed service = new ProductManagementServiceFixed();
            
            // Test 1: Check current products
            System.out.println("\n1. Current products in database:");
            List<ProductNew> currentProducts = service.getAllProducts();
            System.out.println("Found " + currentProducts.size() + " products");
            for (ProductNew product : currentProducts) {
                System.out.println("  - " + product.getProductCode() + ": " + product.getProductName());
            }
            
            // Test 2: Get subcategory ID for FO-SN (Snacks)
            System.out.println("\n2. Testing subcategory lookup for 'FO-SN':");
            // We need to find FO-SN subcategory ID
            // Let's check if we can find subcategories
            
            // Test 3: Create a test product
            System.out.println("\n3. Creating a test product:");
            
            // First find a valid subcategory ID - let's use 1 as a test
            Long subcategoryId = 1L; // Assuming this exists
            
            boolean success = service.createProduct(
                "Test Crispy Chips",
                "Test flavor chips for testing",
                subcategoryId,
                new BigDecimal("250.00"),
                "g", 
                "TestBrand",
                1L  // Admin user ID
            );
            
            System.out.println("Product creation result: " + success);
            
            // Test 4: Check products again
            System.out.println("\n4. Products after creation:");
            List<ProductNew> afterProducts = service.getAllProducts();
            System.out.println("Found " + afterProducts.size() + " products");
            for (ProductNew product : afterProducts) {
                System.out.println("  - " + product.getProductCode() + ": " + product.getProductName() + 
                                 " (Price: LKR " + product.getBasePrice() + ", Active: " + product.isActive() + ")");
            }
            
            System.out.println("\n=== Test Complete ===");
            
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}