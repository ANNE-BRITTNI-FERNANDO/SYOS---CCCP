import com.syos.presentation.ui.ProductManagementUI;
import com.syos.user.domain.entity.User;
import com.syos.user.domain.value.UserRole;

/**
 * Simple test runner for Product Management UI
 */
public class TestProductManagement {
    public static void main(String[] args) {
        // Create a test admin user
        User adminUser = new User("admin", "Admin User", UserRole.ADMIN);
        
        // Create and launch the Product Management UI
        ProductManagementUI productUI = new ProductManagementUI();
        
        System.out.println("=== SYOS Product Management System ===");
        System.out.println("Testing with Admin User: " + adminUser.getUsername());
        System.out.println("=======================================\n");
        
        // Launch the product management interface
        productUI.displayProductManagement(adminUser);
    }
}