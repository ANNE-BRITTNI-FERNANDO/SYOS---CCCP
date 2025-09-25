import com.syos.application.services.ProductManagementServiceFixed;
import com.syos.inventory.domain.entity.Category;
import com.syos.inventory.domain.entity.Subcategory;
import com.syos.inventory.domain.entity.ProductNew;

import java.util.List;

public class TestCategoryFixes {
    public static void main(String[] args) {
        try {
            System.out.println("Testing category and subcategory fixes...");
            
            ProductManagementServiceFixed service = new ProductManagementServiceFixed();
            
            // Test 1: Get all categories
            System.out.println("\n=== Categories and Subcategories ===");
            List<Category> categories = service.getAllCategories();
            List<ProductNew> allProducts = service.getAllProducts();
            
            for (Category category : categories) {
                System.out.println(category.getCategoryCode() + " - " + category.getCategoryName());
                
                List<Subcategory> subcategories = service.getSubCategoriesByCategory(category.getCategoryId());
                for (Subcategory subcategory : subcategories) {
                    // Count products in this subcategory
                    long productCount = allProducts.stream()
                        .filter(p -> p.getSubcategoryId().equals(subcategory.getSubcategoryId()) && p.isActive())
                        .count();
                    
                    System.out.println("  " + subcategory.getSubcategoryCode() + " - " + 
                                     subcategory.getSubcategoryName() + " (" + productCount + " products)");
                }
                System.out.println();
            }
            
            // Test 2: Specific subcategory lookups
            System.out.println("\n=== Subcategory Name Lookups ===");
            String[] testCodes = {"LA-SO", "FO-SN", "FO-DR", "EL-PH"};
            
            for (String code : testCodes) {
                String name = "Unknown";
                for (Category category : categories) {
                    List<Subcategory> subcategories = service.getSubCategoriesByCategory(category.getCategoryId());
                    for (Subcategory subcategory : subcategories) {
                        if (code.equals(subcategory.getSubcategoryCode())) {
                            name = subcategory.getSubcategoryName();
                            break;
                        }
                    }
                    if (!"Unknown".equals(name)) break;
                }
                System.out.println(code + " -> " + name);
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}