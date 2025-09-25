package com.syos.inventory.ui.console;

import com.syos.inventory.domain.value.UserRole;
import com.syos.inventory.domain.entity.User;
import com.syos.inventory.domain.entity.ProductNew;
import com.syos.inventory.domain.entity.Category;
import com.syos.inventory.domain.entity.Subcategory;
import com.syos.application.services.ProductManagementServiceFixed;
import com.syos.shared.patterns.strategy.*;
import java.math.BigDecimal;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Product Management Console UI for ADMIN users
 */
public class ProductManagementUI {
    
    private static final Logger LOGGER = Logger.getLogger(ProductManagementUI.class.getName());
    private final Scanner scanner;
    private final User currentUser;
    private final ProductManagementServiceFixed productService;
    
    public ProductManagementUI(Scanner scanner, User currentUser, ProductManagementServiceFixed productService) {
        this.scanner = scanner;
        this.currentUser = currentUser;
        this.productService = productService;
    }
    
    /**
     * Display product management menu and handle user interactions
     */
    public void displayProductManagement() {
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            System.out.println("? Access Denied: Product management requires ADMIN privileges.");
            return;
        }
        
        boolean running = true;
        while (running) {
            displayProductManagementMenu();
            int choice = readIntChoice();
            
            switch (choice) {
                case 1:
                    viewAllProducts();
                    break;
                case 2:
                    addNewProduct();
                    break;
                case 3:
                    editProduct();
                    break;
                case 4:
                    deleteProduct();
                    break;
                case 5:
                    manageCategories();
                    break;
                case 6:
                    setDiscounts();
                    break;
                case 7:
                    running = false;
                    break;
                default:
                    System.out.println("? Invalid option. Please try again.");
            }
        }
    }
    
    /**
     * Display the product management menu
     */
    private void displayProductManagementMenu() {
        System.out.println();
        System.out.println("PRODUCT MANAGEMENT SYSTEM");
        System.out.println("==================================================");
        System.out.printf("User: %s (%s)%n", currentUser.getFullName(), "ADMIN");
        System.out.println("Store: SYOS Main Branch");
        System.out.println("Date: September 24, 2025");
        System.out.println("==================================================");
        System.out.println("1. View All Products");
        System.out.println("2. Add New Product");
        System.out.println("3. Edit Product");
        System.out.println("4. Delete Product");
        System.out.println("5. Manage Categories");
        System.out.println("6. Set Discounts");
        System.out.println("7. Back to Main Menu");
        System.out.println("==================================================");
        System.out.print("Choose an option (1-7): ");
    }
    
    /**
     * View all products with detailed information
     */
    private void viewAllProducts() {
        System.out.println();
        System.out.println("VIEW ALL PRODUCTS");
        System.out.println("========================================================================================================");
        System.out.printf("%-4s %-12s %-20s %-20s %-12s %-10s %-10s %-8s %-6s%n",
            "ID", "Code", "Product Name", "Description", "Category", "Price", "Final", "Unit", "Active");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        
        // Get products from database
        List<ProductNew> products = productService.getAllProducts();
        
        if (products.isEmpty()) {
            System.out.println("No products found in the database.");
        } else {
            int count = 1;
            int withDiscounts = 0;
            
            for (ProductNew product : products) {
                // Get category info for this product
                String categoryName = getCategoryNameForProduct(product);
                
                String description = product.getDescription();
                if (description == null || description.trim().isEmpty()) {
                    description = "N/A";
                }
                
                System.out.printf("%-4d %-12s %-20s %-20s %-12s %-10.2f %-10.2f %-8s %-6s%n",
                    count++,
                    product.getProductCode(),
                    truncateString(product.getProductName(), 19),
                    truncateString(description, 19),
                    truncateString(categoryName, 11),
                    product.getBasePrice().doubleValue(),
                    product.getFinalPrice().doubleValue(),
                    product.getUnitOfMeasure(),
                    product.isActive() ? "Yes" : "No"
                );
                
                if (product.hasDiscount()) {
                    System.out.println("    " + product.getDiscountDescription());
                    withDiscounts++;
                }
                System.out.println();
            }
        }
        
        System.out.println("========================================================================================================");
        System.out.printf("Total Products: %d | Active: %d%n", 
            products.size(), 
            products.size()); // All returned products are active from findAllActive()
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Helper method to get category name for a product
     */
    private String getCategoryNameForProduct(ProductNew product) {
        try {
            Category category = productService.getCategoryBySubcategory(product.getSubcategoryId());
            if (category != null) {
                return category.getCategoryName() + "/" + productService.getSubcategoryName(product.getSubcategoryId());
            }
            return "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * Helper method to truncate long strings
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Helper method to display sample product data
     */
    private void displaySampleProduct(String id, String code, String name, String category, 
                                    String price, String finalPrice, String unit, String expiry, String active) {
        System.out.printf("%-4s %-12s %-25s %-15s %-10s %-10s %-8s %-12s %-6s%n",
            id, code, name, category, price, finalPrice, unit, expiry, active);
    }
    
    /**
     * Add new product process
     */
    private void addNewProduct() {
        System.out.println();
        System.out.println("ADD NEW PRODUCT");
        System.out.println("==================================================");
        
        displayAvailableCategories();
        
        System.out.print("Enter Subcategory Code: ");
        String subcategoryCode = scanner.nextLine().trim().toUpperCase();
        
        if (!isValidSubcategory(subcategoryCode)) {
            System.out.println("? Invalid subcategory code. Please try again.");
            return;
        }
        
        System.out.println("Valid subcategory selected: " + getSubcategoryName(subcategoryCode));
        System.out.println();
        
        System.out.print("Product Name: ");
        String productName = scanner.nextLine().trim();
        
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        
        System.out.print("Unit Price (LKR): ");
        String priceInput = scanner.nextLine().trim();
        
        System.out.print("Brand: ");
        String brand = scanner.nextLine().trim();
        
        displayUnitOptions();
        System.out.print("Select unit (1-8): ");
        int unitChoice = readIntChoice();
        String unit = getUnitByChoice(unitChoice);
        
        System.out.print("Quantity: ");
        String quantity = scanner.nextLine().trim();
        
        System.out.print("Expiry Date (YYYY-MM-DD) or 'N/A' for no expiry: ");
        String expiryDate = scanner.nextLine().trim();
        
        // Enhanced inventory type selection with Strategy Pattern
        displayEnhancedInventoryTypeOptions();
        System.out.print("Select inventory type (1-3): ");
        int inventoryTypeChoice = readIntChoice();
        
        displayDiscountOptions();
        System.out.print("Select option (1-3): ");
        int discountChoice = readIntChoice();
        
        String discountInfo = "";
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal discountPercent = BigDecimal.ZERO;
        
        if (discountChoice == 2) {
            System.out.print("Enter discount amount (LKR): ");
            String discountAmountStr = scanner.nextLine().trim();
            try {
                discountAmount = new BigDecimal(discountAmountStr);
                discountInfo = "LKR " + discountAmountStr + " off";
            } catch (NumberFormatException e) {
                System.out.println("Invalid discount amount. No discount applied.");
                discountChoice = 1; // Reset to no discount
            }
        } else if (discountChoice == 3) {
            System.out.print("Enter discount percentage: ");
            String discountPercentStr = scanner.nextLine().trim();
            try {
                discountPercent = new BigDecimal(discountPercentStr);
                discountInfo = discountPercentStr + "% discount applied";
            } catch (NumberFormatException e) {
                System.out.println("Invalid discount percentage. No discount applied.");
                discountChoice = 1; // Reset to no discount
            }
        }
        
        try {
            // Convert price to BigDecimal
            BigDecimal basePrice = new BigDecimal(priceInput);
            
            // Convert quantity to integer for inventory configuration
            int totalQuantity;
            try {
                totalQuantity = Integer.parseInt(quantity);
            } catch (NumberFormatException e) {
                System.out.println("? Error: Invalid quantity. Product not created.");
                System.out.print("Press Enter to continue...");
                scanner.nextLine();
                return;
            }
            
            // Configure inventory distribution based on selected strategy
            InventoryTypeContext inventoryContext = new InventoryTypeContext();
            inventoryContext.setStrategyByIndex(inventoryTypeChoice);
            
            InventoryTypeStrategy.InventoryConfiguration inventoryConfig = 
                inventoryContext.configureInventory(totalQuantity);
            
            System.out.println("\nInventory Configuration Preview:");
            System.out.println(inventoryConfig.getConfigurationSummary());
            
            // Only ask for shelf configuration if required
            if (inventoryConfig.requiresShelfConfig()) {
                displayShelfConfiguration();
            }
            
            // Get subcategory ID by code
            Long subcategoryId = getSubcategoryIdByCode(subcategoryCode);
            if (subcategoryId == null) {
                System.out.println("? Error: Invalid subcategory code. Product not created.");
                System.out.print("Press Enter to continue...");
                scanner.nextLine();
                return;
            }
            
            // Use the inventory configuration from the strategy
            int physicalQty = inventoryConfig.getPhysicalQuantity();
            int shelfQty = inventoryConfig.getShelfQuantity();
            int onlineQty = inventoryConfig.getOnlineQuantity();
            
            // Create the product WITH inventory using the service
            ProductNew createdProduct;
            if (discountChoice == 1) {
                // No discount - use the inventory creation method
                createdProduct = productService.createProductWithInventory(
                    productName, description, subcategoryId, basePrice, unit, brand, 
                    currentUser.getId(), physicalQty, shelfQty, onlineQty, null, null, expiryDate
                );
            } else {
                // Has discount - create product with inventory and then apply discount
                createdProduct = productService.createProductWithInventory(
                    productName, description, subcategoryId, basePrice, unit, brand, 
                    currentUser.getId(), physicalQty, shelfQty, onlineQty, null, null, expiryDate
                );
                
                if (createdProduct != null) {
                    // Apply discount after product and inventory creation
                    String productCode = createdProduct.getProductCode();
                    if (discountChoice == 2) {
                        // Fixed discount amount
                        productService.setFixedDiscount(productCode, discountAmount);
                    } else if (discountChoice == 3) {
                        // Percentage discount
                        productService.setPercentageDiscount(productCode, discountPercent);
                    }
                }
            }
            
            if (createdProduct != null) {
                // Use the actual product code from the created product
                String productCode = createdProduct.getProductCode();
                
                System.out.println();
                System.out.println("PRODUCT CREATED SUCCESSFULLY!");
                System.out.println("Product Code: " + productCode);
                System.out.println("Product Name: " + productName);
                System.out.println("Brand: " + brand);
                System.out.println("Unit Price: LKR " + priceInput);
                if (!discountInfo.isEmpty()) {
                    System.out.println("Discount: " + discountInfo);
                }
                System.out.println("Unit: " + unit);
                System.out.println("Quantity: " + quantity);
                
                LOGGER.info("New product created: " + productCode + " - " + productName);
            } else {
                System.out.println();
                System.out.println("? Error: Failed to create product. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("? Error: Invalid price format. Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("? Error: " + e.getMessage());
            LOGGER.severe("Error creating product: " + e.getMessage());
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Edit existing product
     */
    private void editProduct() {
        System.out.println();
        System.out.println("EDIT PRODUCT");
        System.out.println("==================================================");
        
        System.out.print("Enter Product Code: ");
        String productCode = scanner.nextLine().trim().toUpperCase();
        
        // Simulate finding product
        if (!productCode.matches("[A-Z]{2}-[A-Z]{2}-[0-9]{3}")) {
            System.out.println("? Product not found. Please check the code and try again.");
            return;
        }
        
        System.out.println("Product found: " + getProductNameByCode(productCode));
        System.out.println("Current details:");
        displayProductDetails(productCode);
        
        System.out.println();
        System.out.println("Enter new details (press Enter to keep current value):");
        
        // Get current product details
        ProductNew currentProduct = null;
        try {
            List<ProductNew> products = productService.getAllProducts();
            for (ProductNew p : products) {
                if (productCode.equals(p.getProductCode())) {
                    currentProduct = p;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("? Error loading product details: " + e.getMessage());
            return;
        }
        
        if (currentProduct == null) {
            System.out.println("? Product not found in database.");
            return;
        }
        
        System.out.print("Product Name: ");
        String newName = scanner.nextLine().trim();
        if (newName.isEmpty()) {
            newName = currentProduct.getProductName();
        }
        
        System.out.print("Description: ");
        String newDescription = scanner.nextLine().trim();
        if (newDescription.isEmpty()) {
            newDescription = currentProduct.getDescription() != null ? currentProduct.getDescription() : "";
        }
        
        System.out.print("Unit Price (LKR): ");
        String newPriceStr = scanner.nextLine().trim();
        BigDecimal newPrice = currentProduct.getBasePrice();
        if (!newPriceStr.isEmpty()) {
            try {
                newPrice = new BigDecimal(newPriceStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid price format, keeping current price.");
            }
        }
        
        System.out.print("Brand: ");
        String newBrand = scanner.nextLine().trim();
        if (newBrand.isEmpty()) {
            newBrand = currentProduct.getBrand() != null ? currentProduct.getBrand() : "";
        }
        
        System.out.print("Confirm changes? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            try {
                // Update the product in the database via service
                boolean success = productService.updateProduct(
                    productCode, 
                    newName, 
                    newDescription, 
                    newPrice, 
                    currentProduct.getUnitOfMeasure(), 
                    newBrand
                );
                
                if (success) {
                    System.out.println();
                    System.out.println("PRODUCT UPDATED SUCCESSFULLY!");
                    System.out.println("Product Code: " + productCode);
                    System.out.println("Updated Name: " + newName);
                    System.out.println("Updated Description: " + newDescription);
                    System.out.println("Updated Price: LKR " + newPrice);
                    System.out.println("Updated Brand: " + newBrand);
                    
                    LOGGER.info("Product updated: " + productCode);
                } else {
                    System.out.println("? Failed to update product in database.");
                }
            } catch (Exception e) {
                System.out.println("? Error updating product: " + e.getMessage());
            }
        } else {
            System.out.println("? Changes cancelled.");
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Delete product
     */
    private void deleteProduct() {
        System.out.println();
        System.out.println("DELETE PRODUCT");
        System.out.println("==================================================");
        System.out.println("? WARNING: This action cannot be undone!");
        System.out.println();
        
        System.out.print("Enter Product Code: ");
        String productCode = scanner.nextLine().trim().toUpperCase();
        
        // Simulate finding product
        if (!productCode.matches("[A-Z]{2}-[A-Z]{2}-[0-9]{3}")) {
            System.out.println("? Product not found. Please check the code and try again.");
            return;
        }
        
        System.out.println("Product found: " + getProductNameByCode(productCode));
        displayProductDetails(productCode);
        
        System.out.println();
        System.out.print("Are you sure you want to delete this product? (type 'DELETE' to confirm): ");
        String confirm = scanner.nextLine().trim();
        
        if (confirm.equals("DELETE")) {
            System.out.println();
            System.out.println("? PRODUCT DELETED SUCCESSFULLY!");
            System.out.println("Product Code: " + productCode);
            LOGGER.info("Product deleted: " + productCode);
        } else {
            System.out.println("? Deletion cancelled.");
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Manage categories and subcategories
     */
    private void manageCategories() {
        System.out.println();
        System.out.println("CATEGORY MANAGEMENT");
        System.out.println("==================================================");
        System.out.println("1. View Categories");
        System.out.println("2. Add Category");
        System.out.println("3. Edit Category");
        System.out.println("4. Back");
        System.out.print("Choose option (1-4): ");
        
        int choice = readIntChoice();
        switch (choice) {
            case 1:
                viewCategories();
                break;
            case 2:
                addCategory();
                break;
            case 3:
                editCategory();
                break;
            case 4:
                return;
            default:
                System.out.println("? Invalid option.");
        }
    }
    
    /**
     * View all categories
     */
    private void viewCategories() {
        System.out.println();
        System.out.println("ALL CATEGORIES");
        System.out.println("==================================================");
        
        try {
            List<Category> categories = productService.getAllCategories();
            List<ProductNew> allProducts = productService.getAllProducts();
            
            for (Category category : categories) {
                System.out.println(category.getCategoryCode() + " - " + category.getCategoryName());
                
                List<Subcategory> subcategories = productService.getSubCategoriesByCategory(category.getCategoryId());
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
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Add new category
     */
    private void addCategory() {
        System.out.println();
        System.out.println("ADD NEW CATEGORY");
        System.out.println("==================================================");
        
        System.out.println("Category Type:");
        System.out.println("1. Main Category");
        System.out.println("2. Sub-Category");
        System.out.print("Select type (1-2): ");
        
        int typeChoice = readIntChoice();
        
        if (typeChoice == 1) {
            addMainCategory();
        } else if (typeChoice == 2) {
            addSubCategory();
        } else {
            System.out.println("? Invalid option.");
        }
    }
    
    /**
     * Add main category
     */
    private void addMainCategory() {
        System.out.println();
        System.out.print("Category Name: ");
        String categoryName = scanner.nextLine().trim();
        
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        
        // Generate category code
        String categoryCode = generateCategoryCode(categoryName);
        
        System.out.println("Auto-generated Code: " + categoryCode);
        System.out.println();
        
        try {
            // Save to database using service
            boolean success = productService.createCategory(categoryCode, categoryName, description);
            
            if (success) {
                System.out.println("CATEGORY CREATED SUCCESSFULLY!");
                System.out.println(categoryCode + " - " + categoryName);
                LOGGER.info("New category created: " + categoryCode + " - " + categoryName);
            } else {
                System.out.println("? Failed to create category. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("? Error creating category: " + e.getMessage());
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Add subcategory
     */
    private void addSubCategory() {
        System.out.println();
        displayAvailableCategories();
        
        System.out.print("Select Main Category Code (e.g., LA, FO, EL): ");
        String mainCategoryCode = scanner.nextLine().trim().toUpperCase();
        
        System.out.print("Subcategory Name: ");
        String subcategoryName = scanner.nextLine().trim();
        
        if (subcategoryName.isEmpty()) {
            System.out.println("? Subcategory name cannot be empty.");
            return;
        }
        
        String subcategoryCode = generateSubcategoryCode(mainCategoryCode, subcategoryName);
        
        System.out.print("Shelf Capacity: ");
        String shelfCapacityStr = scanner.nextLine().trim();
        int shelfCapacity = 20; // Default
        if (!shelfCapacityStr.isEmpty()) {
            try {
                shelfCapacity = Integer.parseInt(shelfCapacityStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid shelf capacity, using default: 20");
            }
        }
        
        System.out.print("Minimum Threshold: ");
        String minThresholdStr = scanner.nextLine().trim();
        int minThreshold = 5; // Default
        if (!minThresholdStr.isEmpty()) {
            try {
                minThreshold = Integer.parseInt(minThresholdStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid minimum threshold, using default: 5");
            }
        }
        
        try {
            // Save to database using service
            boolean success = productService.createSubcategory(mainCategoryCode, subcategoryCode, 
                                                             subcategoryName, shelfCapacity, minThreshold);
            
            if (success) {
                System.out.println();
                System.out.println("SUBCATEGORY CREATED SUCCESSFULLY!");
                System.out.println(subcategoryCode + " - " + subcategoryName + " (" + shelfCapacity + "/" + minThreshold + ")");
                LOGGER.info("New subcategory created: " + subcategoryCode + " - " + subcategoryName);
            } else {
                System.out.println("? Failed to create subcategory. Please check the main category code and try again.");
            }
        } catch (Exception e) {
            System.out.println("? Error creating subcategory: " + e.getMessage());
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Edit category
     */
    private void editCategory() {
        System.out.println();
        System.out.println("EDIT CATEGORY");
        System.out.println("==================================================");
        
        System.out.print("Enter Category/Subcategory Code: ");
        String code = scanner.nextLine().trim().toUpperCase();
        
        System.out.println("Category found: " + getCategoryNameByCode(code));
        System.out.println();
        
        System.out.print("New Name (press Enter to keep current): ");
        scanner.nextLine().trim(); // Collect input but not implemented yet
        
        System.out.print("New Description (press Enter to keep current): ");
        scanner.nextLine().trim(); // Collect input but not implemented yet
        
        System.out.print("Confirm changes? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            System.out.println();
            System.out.println("CATEGORY UPDATED SUCCESSFULLY!");
            LOGGER.info("Category updated: " + code);
        } else {
            System.out.println("? Changes cancelled.");
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Set product discounts
     */
    private void setDiscounts() {
        System.out.println();
        System.out.println("PRODUCT DISCOUNT MANAGEMENT");
        System.out.println("==================================================");
        System.out.println("1. View Products with Discounts");
        System.out.println("2. Set/Update Product Discount");
        System.out.println("3. Remove Product Discount");
        System.out.println("4. Back to Product Management");
        System.out.print("Choose option (1-4): ");
        
        int choice = readIntChoice();
        switch (choice) {
            case 1:
                viewProductsWithDiscounts();
                break;
            case 2:
                setProductDiscount();
                break;
            case 3:
                removeProductDiscount();
                break;
            case 4:
                return;
            default:
                System.out.println("? Invalid option.");
        }
    }
    
    /**
     * View products with discounts
     */
    private void viewProductsWithDiscounts() {
        System.out.println();
        System.out.println("PRODUCTS WITH DISCOUNTS");
        System.out.println("==================================================");
        
        try {
            List<ProductNew> products = productService.getAllProducts();
            List<ProductNew> discountedProducts = new ArrayList<>();
            
            // Filter products with discounts
            for (ProductNew product : products) {
                if (product.hasDiscount()) {
                    discountedProducts.add(product);
                }
            }
            
            if (discountedProducts.isEmpty()) {
                System.out.println("No products have discounts currently.");
            } else {
                System.out.printf("%-12s %-25s %-10s %-10s %-20s%n", "Code", "Product Name", "Original", "Final", "Discount");
                System.out.println("------------------------------------------------------------------");
                
                for (ProductNew product : discountedProducts) {
                    String productName = product.getProductName();
                    if (productName.length() > 25) {
                        productName = productName.substring(0, 22) + "...";
                    }
                    
                    System.out.printf("%-12s %-25s %-10.2f %-10.2f %-20s%n", 
                        product.getProductCode(),
                        productName,
                        product.getBasePrice(),
                        product.getFinalPrice(),
                        product.getDiscountDescription()
                    );
                }
            }
            
        } catch (Exception e) {
            System.out.println("? Error loading products with discounts: " + e.getMessage());
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Set product discount
     */
    private void setProductDiscount() {
        System.out.println();
        System.out.print("Enter Product Code: ");
        String productCode = scanner.nextLine().trim().toUpperCase();
        
        try {
            // Find the product
            List<ProductNew> products = productService.getAllProducts();
            ProductNew product = null;
            
            for (ProductNew p : products) {
                if (productCode.equals(p.getProductCode())) {
                    product = p;
                    break;
                }
            }
            
            if (product == null) {
                System.out.println("? Product not found. Please check the code and try again.");
                return;
            }
            
            System.out.println("Product: " + product.getProductName());
            System.out.println("Current Price: LKR " + product.getBasePrice());
            System.out.println("Current Discount: " + (product.hasDiscount() ? product.getDiscountDescription() : "None"));
            System.out.println();
            
            displayDiscountOptions();
            System.out.print("Select option (1-3): ");
            int choice = readIntChoice();
            
            if (choice == 1) {
                // Remove discount
                if (productService.removeDiscount(productCode)) {
                    System.out.println("? Discount removed successfully!");
                    LOGGER.info("Discount removed from product: " + productCode);
                } else {
                    System.out.println("? Error removing discount.");
                }
            } else if (choice == 2) {
                System.out.print("Enter discount amount (LKR): ");
                String amountStr = scanner.nextLine().trim();
                try {
                    BigDecimal amount = new BigDecimal(amountStr);
                    // Preview the discount
                    BigDecimal newFinalPrice = product.getBasePrice().subtract(amount);
                    System.out.println("New Final Price: LKR " + newFinalPrice);
                    
                    System.out.print("Confirm? (y/n): ");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    
                    if (confirm.equals("y") || confirm.equals("yes")) {
                        if (productService.setFixedDiscount(productCode, amount)) {
                            System.out.println("? DISCOUNT UPDATED SUCCESSFULLY!");
                            LOGGER.info("Fixed discount set for product: " + productCode);
                        } else {
                            System.out.println("? Error setting discount.");
                        }
                    } else {
                        System.out.println("? Changes cancelled.");
                    }
                } catch (Exception e) {
                    System.out.println("? Invalid amount: " + e.getMessage());
                }
            } else if (choice == 3) {
                System.out.print("Enter discount percentage: ");
                String percentageStr = scanner.nextLine().trim();
                try {
                    BigDecimal percentage = new BigDecimal(percentageStr);
                    // Preview the discount
                    BigDecimal discountAmount = product.getBasePrice().multiply(percentage).divide(new BigDecimal("100"));
                    BigDecimal newFinalPrice = product.getBasePrice().subtract(discountAmount);
                    System.out.println("New Final Price: LKR " + newFinalPrice);
                    
                    System.out.print("Confirm? (y/n): ");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    
                    if (confirm.equals("y") || confirm.equals("yes")) {
                        if (productService.setPercentageDiscount(productCode, percentage)) {
                            System.out.println("? DISCOUNT UPDATED SUCCESSFULLY!");
                            LOGGER.info("Percentage discount set for product: " + productCode);
                        } else {
                            System.out.println("? Error setting discount.");
                        }
                    } else {
                        System.out.println("? Changes cancelled.");
                    }
                } catch (Exception e) {
                    System.out.println("? Invalid percentage: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("? Error processing discount: " + e.getMessage());
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Remove product discount
     */
    private void removeProductDiscount() {
        System.out.println();
        System.out.print("Enter Product Code: ");
        String productCode = scanner.nextLine().trim().toUpperCase();
        
        try {
            // Find the product
            List<ProductNew> products = productService.getAllProducts();
            ProductNew product = null;
            
            for (ProductNew p : products) {
                if (productCode.equals(p.getProductCode())) {
                    product = p;
                    break;
                }
            }
            
            if (product == null) {
                System.out.println("? Product not found. Please check the code and try again.");
                return;
            }
            
            if (!product.hasDiscount()) {
                System.out.println("? This product doesn't have any discount to remove.");
                return;
            }
            
            System.out.println("Product: " + product.getProductName());
            System.out.println("Current Discount: " + product.getDiscountDescription());
            System.out.println("Current Final Price: LKR " + product.getFinalPrice());
            System.out.println();
            
            System.out.print("Remove discount? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (confirm.equals("y") || confirm.equals("yes")) {
                if (productService.removeDiscount(productCode)) {
                    System.out.println();
                    System.out.println("DISCOUNT REMOVED SUCCESSFULLY!");
                    System.out.println("Product now at original price: LKR " + product.getBasePrice());
                    LOGGER.info("Discount removed from product: " + productCode);
                } else {
                    System.out.println("? Error removing discount.");
                }
            } else {
                System.out.println("? Operation cancelled.");
            }
        } catch (Exception e) {
            System.out.println("? Error processing request: " + e.getMessage());
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    // Helper methods
    private void displayAvailableCategories() {
        try {
            System.out.println("Available Categories:");
            List<Category> categories = productService.getAllCategories();
            List<ProductNew> allProducts = productService.getAllProducts();
            
            for (Category category : categories) {
                System.out.println(category.getCategoryCode() + " - " + category.getCategoryName());
                
                List<Subcategory> subcategories = productService.getSubCategoriesByCategory(category.getCategoryId());
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
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
            // Fallback to static display
            System.out.println("Available Categories:");
            System.out.println("LA - Laundry Products");
            System.out.println("FO - Food & Beverages");  
            System.out.println("EL - Electronics");
            System.out.println();
        }
    }
    
    private void displayUnitOptions() {
        System.out.println();
        System.out.println("Unit of Measure:");
        System.out.println("1. kg  2. g   3. l   4. ml");
        System.out.println("5. pcs 6. pack 7. box 8. bottle");
    }
    
    private void displayStorageOptions() {
        System.out.println();
        System.out.println("Storage Location:");
        System.out.println("1. Physical Store");
        System.out.println("2. Online Store");
    }
    
    private void displayEnhancedInventoryTypeOptions() {
        System.out.println();
        System.out.println("INVENTORY TYPE SELECTION");
        System.out.println("==================================================");
        System.out.println("Choose how you want to manage this product's inventory:");
        System.out.println();
        System.out.println("1. Physical Store Only");
        System.out.println("   Product stored in warehouse and shelf display.");
        System.out.println("   Requires shelf capacity configuration.");
        System.out.println();
        System.out.println("2. Online Store Only");
        System.out.println("   Product available exclusively online.");
        System.out.println("   No physical storage or shelf management required.");
        System.out.println();
        System.out.println("3. Hybrid (Physical + Online)");
        System.out.println("   Product available both in-store and online.");
        System.out.println("   Inventory distributed across channels with shelf management.");
        System.out.println();
    }
    
    private void displayShelfConfiguration() {
        System.out.println();
        System.out.println("Shelf Configuration:");
        System.out.print("Use Subcategory Default (20/5)? (y/n): ");
        String useDefault = scanner.nextLine().trim().toLowerCase();
        
        if (useDefault.equals("y") || useDefault.equals("yes")) {
            System.out.println("Auto-Restock Enabled: Yes");
        } else {
            System.out.print("Shelf Capacity: ");
            scanner.nextLine();
            System.out.print("Minimum Threshold: ");
            scanner.nextLine();
        }
    }
    
    private void displayDiscountOptions() {
        System.out.println();
        System.out.println("Discount Settings:");
        System.out.println("1. No Discount  2. Fixed Amount  3. Percentage");
    }
    
    private void displayProductDetails(String productCode) {
        try {
            List<ProductNew> products = productService.getAllProducts();
            ProductNew product = null;
            
            for (ProductNew p : products) {
                if (productCode.equals(p.getProductCode())) {
                    product = p;
                    break;
                }
            }
            
            if (product != null) {
                System.out.println("Name: " + product.getProductName());
                System.out.println("Description: " + (product.getDescription() != null ? product.getDescription() : "N/A"));
                System.out.println("Category: " + getCategoryNameForProduct(product));
                System.out.println("Price: LKR " + product.getBasePrice());
                System.out.println("Brand: " + (product.getBrand() != null ? product.getBrand() : "N/A"));
                System.out.println("Unit: " + product.getUnitOfMeasure());
                System.out.println("Status: " + (product.isActive() ? "Active" : "Inactive"));
                if (product.hasDiscount()) {
                    System.out.println("Final Price: LKR " + product.getFinalPrice());
                }
            } else {
                System.out.println("Name: Unknown Product");
                System.out.println("Category: N/A");
                System.out.println("Price: N/A");
                System.out.println("Brand: N/A");
                System.out.println("Unit: N/A");
                System.out.println("Status: N/A");
            }
        } catch (Exception e) {
            System.out.println("Error loading product details: " + e.getMessage());
        }
    }
    
    private String getProductNameByCode(String code) {
        try {
            List<ProductNew> products = productService.getAllProducts();
            for (ProductNew product : products) {
                if (code.equals(product.getProductCode())) {
                    return product.getProductName();
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching product: " + e.getMessage());
        }
        return "Unknown Product";
    }
    
    private String getCategoryNameByCode(String code) {
        // Sample mapping
        switch (code) {
            case "LA": return "Laundry Products";
            case "FO": return "Food & Beverages";
            case "EL": return "Electronics";
            case "LA-SO": return "Soap";
            case "FO-SN": return "Snacks";
            default: return "Unknown Category";
        }
    }
    
    private String getSubcategoryName(String code) {
        try {
            List<Category> categories = productService.getAllCategories();
            for (Category category : categories) {
                List<Subcategory> subcategories = productService.getSubCategoriesByCategory(category.getCategoryId());
                for (Subcategory subcategory : subcategories) {
                    if (code.equals(subcategory.getSubcategoryCode())) {
                        return subcategory.getSubcategoryName();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting subcategory name: " + e.getMessage());
        }
        return "Unknown";
    }
    
    private boolean isValidSubcategory(String code) {
        return code.matches("[A-Z]{2}-[A-Z]{2}");
    }
    
    private String getUnitByChoice(int choice) {
        switch (choice) {
            case 1: return "kg";
            case 2: return "g";
            case 3: return "l";
            case 4: return "ml";
            case 5: return "pcs";
            case 6: return "pack";
            case 7: return "box";
            case 8: return "bottle";
            default: return "pcs";
        }
    }
    
    private String generateProductCode(String subcategoryCode) {
        // Simple counter-based generation for demo
        return subcategoryCode + "-" + String.format("%03d", (int)(Math.random() * 999) + 1);
    }
    
    private String generateCategoryCode(String categoryName) {
        // Generate 2-letter code from category name
        String[] words = categoryName.split(" ");
        if (words.length >= 2) {
            return (words[0].substring(0, 1) + words[1].substring(0, 1)).toUpperCase();
        } else {
            return categoryName.substring(0, Math.min(2, categoryName.length())).toUpperCase();
        }
    }
    
    private String generateSubcategoryCode(String mainCategoryCode, String subcategoryName) {
        // Generate subcategory code
        String subCode = subcategoryName.substring(0, Math.min(2, subcategoryName.length())).toUpperCase();
        return mainCategoryCode + "-" + subCode;
    }
    
    private int readIntChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Get subcategory ID by code using the service
     */
    private Long getSubcategoryIdByCode(String subcategoryCode) {
        try {
            // Get all categories from service to find subcategories
            List<Category> categories = productService.getAllCategories();
            for (Category category : categories) {
                List<Subcategory> subcategories = productService.getSubCategoriesByCategory(category.getCategoryId());
                for (Subcategory subcategory : subcategories) {
                    if (subcategoryCode.equals(subcategory.getSubcategoryCode())) {
                        return subcategory.getSubcategoryId();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error getting subcategory ID by code: " + e.getMessage());
        }
        return null;
    }
}