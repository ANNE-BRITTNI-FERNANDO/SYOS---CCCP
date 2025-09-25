package com.syos.presentation.ui;

import com.syos.application.services.*;
import com.syos.application.services.OnlineInventoryService.OnlineProduct;
import com.syos.application.services.ShoppingCartService.Cart;
import com.syos.application.services.ShoppingCartService.CartItem;
import com.syos.application.services.ShoppingCartService.CartOperationResult;
import com.syos.application.services.OnlineCheckoutService.CustomerInfo;
import com.syos.application.services.OnlineCheckoutService.CheckoutResult;

import java.util.*;

/**
 * Online Customer UI for the SYOS Inventory System
 * 
 * Provides a comprehensive online shopping interface for customers including:
 * - Product browsing by category
 * - Product search functionality
 * - Shopping cart management
 * - Secure checkout process
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class OnlineCustomerUI {
    
    private final Scanner scanner;
    private final OnlineInventoryService inventoryService;
    private final ShoppingCartService cartService;
    private final OnlineCheckoutService checkoutService;
    private final String sessionId;
    
    /**
     * Constructor for OnlineCustomerUI
     */
    public OnlineCustomerUI(Scanner scanner, String databaseUrl) {
        this.scanner = scanner;
        this.inventoryService = new OnlineInventoryService(databaseUrl);
        this.cartService = new ShoppingCartService(inventoryService);
        this.checkoutService = new OnlineCheckoutService(databaseUrl);
        this.sessionId = UUID.randomUUID().toString();
    }
    
    /**
     * Display the online shopping interface
     */
    public void displayOnlineStore() {
        System.out.println("*** SYOS ONLINE STORE - Welcome to Online Shopping! ***");
        System.out.println("Session ID: " + sessionId);
        
        boolean running = true;
        while (running) {
            try {
                displayMainMenu();
                System.out.print("Enter your choice: ");
                String input = scanner.nextLine().trim();
                
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1:
                        browseByCategoryFlow();
                        break;
                    case 2:
                        searchProductsFlow();
                        break;
                    case 3:
                        viewShoppingCart();
                        break;
                    case 4:
                        browseNearExpiryProducts();
                        break;
                    case 5:
                        processCheckout();
                        break;
                    case 0:
                        System.out.println("Thank you for visiting SYOS Online Store!");
                        running = false;
                        break;
                    default:
                        System.out.println("[ERROR] Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid number.");
            }
        }
    }
    
    /**
     * Display main menu options
     */
    private void displayMainMenu() {
        Cart cart = cartService.getCart(sessionId);
        
        System.out.println("\n=== ONLINE STORE MENU ===");
        System.out.println("==================================");
        System.out.println("1. Browse Products by Category");
        System.out.println("2. Search Products");
        System.out.println("3. View Shopping Cart (" + cart.getItemCount() + " items)");
        System.out.println("4. Browse Near-Expiry Products");
        System.out.println("5. Proceed to Checkout");
        System.out.println("0. Exit Online Store");
        
        if (!cart.isEmpty()) {
            System.out.println("\n[CART] Summary: " + cart.getItemCount() + 
                             " items, Total: LKR " + cart.getFinalTotal());
        }
    }
    
    /**
     * Browse products by category
     */
    private void browseByCategoryFlow() {
        System.out.println("\n=== BROWSE PRODUCTS BY CATEGORY ===");
        
        try {
            List<String> categories = inventoryService.getAvailableCategories();
            
            if (categories.isEmpty()) {
                System.out.println("[ERROR] No categories available at the moment.");
                return;
            }
            
            System.out.println("Available Categories:");
            for (int i = 0; i < categories.size(); i++) {
                System.out.println((i + 1) + ". " + categories.get(i));
            }
            System.out.println("0. Back to Main Menu");
            
            System.out.print("Select a category: ");
            try {
                int categoryChoice = Integer.parseInt(scanner.nextLine().trim());
                
                if (categoryChoice == 0) {
                    return;
                } else if (categoryChoice > 0 && categoryChoice <= categories.size()) {
                    String selectedCategory = categories.get(categoryChoice - 1);
                    displayProductsByCategory(selectedCategory);
                } else {
                    System.out.println("[ERROR] Invalid category selection.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid number.");
            }
            
        } catch (Exception e) {
            System.out.println("[ERROR] Error loading categories: " + e.getMessage());
        }
    }
    
    /**
     * Display products in a specific category
     */
    private void displayProductsByCategory(String categoryName) {
        System.out.println("\n=== PRODUCTS IN: " + categoryName.toUpperCase() + " ===");
        
        try {
            List<OnlineProduct> products = inventoryService.getProductsByCategory(categoryName);
            
            if (products.isEmpty()) {
                System.out.println("[ERROR] No products available in this category.");
                return;
            }
            
            displayProductList(products);
            
            System.out.print("\nEnter product code to add to cart (or press Enter to go back): ");
            String productCode = scanner.nextLine().trim();
            
            if (!productCode.isEmpty()) {
                addProductToCart(productCode);
            }
            
        } catch (Exception e) {
            System.out.println("[ERROR] Error loading products: " + e.getMessage());
        }
    }
    
    /**
     * Search products by keyword
     */
    private void searchProductsFlow() {
        System.out.println("\n=== SEARCH PRODUCTS ===");
        
        System.out.print("Enter search term: ");
        String searchTerm = scanner.nextLine().trim();
        
        if (searchTerm.isEmpty()) {
            System.out.println("[ERROR] Please enter a search term.");
            return;
        }
        
        try {
            List<OnlineProduct> products = inventoryService.searchProducts(searchTerm);
            
            if (products.isEmpty()) {
                System.out.println("[ERROR] No products found matching '" + searchTerm + "'");
                return;
            }
            
            System.out.println("\n=== Search Results for: '" + searchTerm + "' ===");
            displayProductList(products);
            
            System.out.print("\nEnter product code to add to cart (or press Enter to go back): ");
            String productCode = scanner.nextLine().trim();
            
            if (!productCode.isEmpty()) {
                addProductToCart(productCode);
            }
            
        } catch (Exception e) {
            System.out.println("[ERROR] Error searching products: " + e.getMessage());
        }
    }
    
    /**
     * Browse near-expiry products with discounts
     */
    private void browseNearExpiryProducts() {
        System.out.println("\n--- SPECIAL DEALS ---");
        System.out.println("=== NEAR-EXPIRY PRODUCTS WITH DISCOUNTS ===");
        
        try {
            List<OnlineProduct> products = inventoryService.getFeaturedProducts(10);
            
            if (products.isEmpty()) {
                System.out.println("[ERROR] No featured products available.");
                return;
            }
            
            displayProductList(products);
            
            System.out.print("\nEnter product code to add to cart (or press Enter to go back): ");
            String productCode = scanner.nextLine().trim();
            
            if (!productCode.isEmpty()) {
                addProductToCart(productCode);
            }
            
        } catch (Exception e) {
            System.out.println("[ERROR] Error loading featured products: " + e.getMessage());
        }
    }
    
    /**
     * Display a list of products in a formatted table
     */
    private void displayProductList(List<OnlineProduct> products) {
        System.out.println("\n=== AVAILABLE PRODUCTS ===");
        System.out.println("=".repeat(100));
        System.out.printf("%-10s %-25s %-15s %-10s %-12s %-15s%n", 
            "Code", "Product Name", "Category", "Price", "Stock", "Expiry Date");
        System.out.println("-".repeat(100));
        
        for (OnlineProduct product : products) {
            String expiryDateStr = "N/A";
            if (product.getExpiryDate() != null) {
                expiryDateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(product.getExpiryDate());
            }
            
            System.out.printf("%-10s %-25s %-15s LKR%-7.2f %-12d %-15s%n",
                product.getProductCode(),
                product.getProductName().length() > 25 ? 
                    product.getProductName().substring(0, 22) + "..." : product.getProductName(),
                product.getCategory(),
                product.getFinalPrice(),
                product.getAvailableQuantity(),
                expiryDateStr);
        }
        System.out.println("=".repeat(100));
    }
    
    /**
     * Add a product to the shopping cart
     */
    private void addProductToCart(String productCode) {
        try {
            Optional<OnlineProduct> productOpt = inventoryService.getProductByCode(productCode);
            
            if (!productOpt.isPresent()) {
                System.out.println("[ERROR] Product not found: " + productCode);
                return;
            }
            
            OnlineProduct product = productOpt.get();
            
            if (product.getAvailableQuantity() <= 0) {
                System.out.println("[ERROR] Product is out of stock: " + product.getProductName());
                return;
            }
            
            System.out.println("Product Details:");
            System.out.println("Name: " + product.getProductName());
            System.out.println("Price: LKR " + product.getFinalPrice());
            System.out.println("Available Stock: " + product.getAvailableQuantity());
            
            String expiryInfo = "N/A";
            if (product.getExpiryDate() != null) {
                expiryInfo = new java.text.SimpleDateFormat("yyyy-MM-dd").format(product.getExpiryDate());
            }
            System.out.println("Expiry Date: " + expiryInfo);
            
            System.out.print("Enter quantity to add to cart: ");
            try {
                int quantity = Integer.parseInt(scanner.nextLine().trim());
                
                if (quantity <= 0) {
                    System.out.println("[ERROR] Quantity must be greater than 0.");
                    return;
                }
                
                CartOperationResult result = cartService.addToCart(sessionId, productCode, quantity);
                
                if (result.isSuccess()) {
                    System.out.println("[SUCCESS] " + result.getMessage());
                } else {
                    System.out.println("[ERROR] " + result.getMessage());
                }
                
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid quantity.");
            }
            
        } catch (Exception e) {
            System.out.println("[ERROR] Error adding product to cart: " + e.getMessage());
        }
    }
    
    /**
     * View and manage shopping cart
     */
    private void viewShoppingCart() {
        Cart cart = cartService.getCart(sessionId);
        
        if (cart.isEmpty()) {
            System.out.println("\n[INFO] Your shopping cart is empty.");
            return;
        }
        
        System.out.println("\n=== YOUR SHOPPING CART ===");
        System.out.println("=".repeat(80));
        System.out.printf("%-10s %-25s %-8s %-10s %-12s%n", 
            "Code", "Product", "Qty", "Unit Price", "Subtotal");
        System.out.println("-".repeat(80));
        
        for (CartItem item : cart.getItems()) {
            System.out.printf("%-10s %-25s %-8d LKR%-7.2f LKR%-9.2f%n",
                item.getProductCode(),
                item.getProductName().length() > 25 ? 
                    item.getProductName().substring(0, 22) + "..." : item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal());
        }
        
        System.out.println("=".repeat(80));
        System.out.printf("%-54s LKR%.2f%n", "Subtotal:", cart.getSubtotal());
        System.out.printf("%-54s LKR%.2f%n", "Discount:", cart.getTotalDiscount());
        System.out.printf("%-54s LKR%.2f%n", "TOTAL:", cart.getFinalTotal());
        System.out.println("=".repeat(80));
        
        System.out.println("\nCart Actions:");
        System.out.println("1. Update item quantity");
        System.out.println("2. Remove item");
        System.out.println("3. Clear entire cart");
        System.out.println("0. Back to main menu");
        
        System.out.print("Choose an action: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            
            switch (choice) {
                case 1:
                    updateCartItem();
                    break;
                case 2:
                    removeCartItem();
                    break;
                case 3:
                    clearCart();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("[ERROR] Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Please enter a valid number.");
        }
    }
    
    /**
     * Update quantity of an item in cart
     */
    private void updateCartItem() {
        System.out.print("Enter product code to update: ");
        String productCode = scanner.nextLine().trim();
        
        Cart cart = cartService.getCart(sessionId);
        if (cart.getItem(productCode) == null) {
            System.out.println("[ERROR] Product not found in cart.");
            return;
        }
        
        System.out.print("Enter new quantity (0 to remove): ");
        try {
            int newQuantity = Integer.parseInt(scanner.nextLine().trim());
            
            CartOperationResult result = cartService.updateCartItem(sessionId, productCode, newQuantity);
            
            if (result.isSuccess()) {
                System.out.println("[SUCCESS] " + result.getMessage());
            } else {
                System.out.println("[ERROR] " + result.getMessage());
            }
            
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Please enter a valid quantity.");
        }
    }
    
    /**
     * Remove an item from cart
     */
    private void removeCartItem() {
        System.out.print("Enter product code to remove: ");
        String productCode = scanner.nextLine().trim();
        
        CartOperationResult result = cartService.removeFromCart(sessionId, productCode);
        
        if (result.isSuccess()) {
            System.out.println("[SUCCESS] " + result.getMessage());
        } else {
            System.out.println("[ERROR] " + result.getMessage());
        }
    }
    
    /**
     * Clear all items from cart
     */
    private void clearCart() {
        System.out.print("Are you sure you want to clear your entire cart? (y/N): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if ("y".equals(confirmation) || "yes".equals(confirmation)) {
            CartOperationResult result = cartService.clearCart(sessionId);
            if (result.isSuccess()) {
                System.out.println("[SUCCESS] " + result.getMessage());
            } else {
                System.out.println("[ERROR] " + result.getMessage());
            }
        } else {
            System.out.println("[INFO] Cart clear cancelled.");
        }
    }
    
    /**
     * Process checkout for the current cart
     */
    private void processCheckout() {
        Cart cart = cartService.getCart(sessionId);
        
        if (cart.isEmpty()) {
            System.out.println("\n[ERROR] Your cart is empty. Add some products before checkout.");
            return;
        }
        
        System.out.println("\n=== CHECKOUT PROCESS ===");
        System.out.println("Order Summary:");
        System.out.println("Items: " + cart.getItemCount());
        System.out.println("Total Amount: LKR " + cart.getFinalTotal());
        
        System.out.println("\nPlease provide your details:");
        
        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Email Address: ");
        String email = scanner.nextLine().trim();
        
        System.out.print("Phone Number: ");
        String phone = scanner.nextLine().trim();
        
        System.out.print("Delivery Address: ");
        String address = scanner.nextLine().trim();
        
        CustomerInfo customer = new CustomerInfo(name, email, phone, address);
        
        if (!customer.isValid()) {
            System.out.println("[ERROR] Please provide all required information (name, email, phone).");
            return;
        }
        
        System.out.print("\nConfirm order? (y/N): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (!"y".equals(confirmation) && !"yes".equals(confirmation)) {
            System.out.println("[INFO] Checkout cancelled.");
            return;
        }
        
        try {
            System.out.println("\nProcessing your order...");
            
            CheckoutResult result = checkoutService.processCheckout(sessionId, customer);
            
            if (result.isSuccess()) {
                System.out.println("\n=== ORDER CONFIRMATION ===");
                System.out.println("[SUCCESS] " + result.getMessage());
                System.out.println("Bill Number: " + result.getBillNumber());
                System.out.println("Total Amount: LKR " + result.getTotalAmount());
                System.out.println("Order Date: " + result.getOrderDate());
                System.out.println("\nThank you for your order!");
                System.out.println("You will receive an email confirmation shortly.");
            } else {
                System.out.println("[ERROR] " + result.getMessage());
                System.out.println("Please try again or contact customer support.");
            }
            
        } catch (Exception e) {
            System.out.println("[ERROR] Checkout failed: " + e.getMessage());
        }
    }
}