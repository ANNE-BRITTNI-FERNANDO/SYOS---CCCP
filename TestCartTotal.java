import java.sql.*;
import com.syos.application.services.*;
import com.syos.application.services.ShoppingCartService.*;
import java.util.UUID;

public class TestCartTotal {
    public static void main(String[] args) {
        try {
            String databaseUrl = "jdbc:sqlite:data/syos_inventory.db";
            String sessionId = UUID.randomUUID().toString();
            
            // Create services
            OnlineInventoryService inventoryService = new OnlineInventoryService(databaseUrl);
            ShoppingCartService cartService = new ShoppingCartService(inventoryService);
            
            System.out.println("=== Testing Cart Total Calculation ===");
            System.out.println("Session ID: " + sessionId);
            
            // Get initial cart
            Cart cart = cartService.getCart(sessionId);
            System.out.println("Initial cart total: " + cart.getFinalTotal());
            
            // Add a product to cart
            CartOperationResult result1 = cartService.addToCart(sessionId, "PRD-TEST0001", 2);
            System.out.println("Add PRD-TEST0001 result: " + result1.getMessage());
            
            cart = cartService.getCart(sessionId);
            System.out.println("After adding PRD-TEST0001 x2:");
            System.out.println("- Items: " + cart.getItemCount());
            System.out.println("- Subtotal: " + cart.getSubtotal());
            System.out.println("- Final Total: " + cart.getFinalTotal());
            
            // Add another product
            CartOperationResult result2 = cartService.addToCart(sessionId, "PRD-MASU0001", 1);
            System.out.println("Add PRD-MASU0001 result: " + result2.getMessage());
            
            cart = cartService.getCart(sessionId);
            System.out.println("After adding PRD-MASU0001 x1:");
            System.out.println("- Items: " + cart.getItemCount());
            System.out.println("- Subtotal: " + cart.getSubtotal());
            System.out.println("- Final Total: " + cart.getFinalTotal());
            
            // Test creating new service instance (simulating checkout scenario)
            System.out.println("\n=== Testing New Service Instance (Checkout Scenario) ===");
            OnlineInventoryService newInventoryService = new OnlineInventoryService(databaseUrl);
            ShoppingCartService newCartService = new ShoppingCartService(newInventoryService);
            
            Cart cartFromNewService = newCartService.getCart(sessionId);
            System.out.println("Cart from new service:");
            System.out.println("- Items: " + cartFromNewService.getItemCount());
            System.out.println("- Subtotal: " + cartFromNewService.getSubtotal());
            System.out.println("- Final Total: " + cartFromNewService.getFinalTotal());
            
            if (cartFromNewService.getFinalTotal().compareTo(cart.getFinalTotal()) == 0) {
                System.out.println("✅ Cart persistence working correctly!");
            } else {
                System.out.println("❌ Cart persistence issue detected!");
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}