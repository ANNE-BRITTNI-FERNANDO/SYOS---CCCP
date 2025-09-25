package com.syos.inventory.ui.console;

import com.syos.shared.patterns.strategy.*;
import com.syos.inventory.domain.entity.ProductNew;
import com.syos.application.services.ProductManagementServiceFixed;
import com.syos.inventory.domain.entity.User;

import java.math.BigDecimal;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Enhanced Product Addition UI with clear inventory type selection.
 * 
 * Uses Strategy Pattern to handle different inventory types:
 * - Physical Store Only (requires shelf configuration)
 * - Online Store Only (no physical storage)
 * - Hybrid (both physical and online)
 * 
 * Follows Single Responsibility Principle by focusing only on product addition
 * with inventory type management.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class EnhancedProductAdditionUI {
    
    private static final Logger LOGGER = Logger.getLogger(EnhancedProductAdditionUI.class.getName());
    
    private final Scanner scanner;
    private final ProductManagementServiceFixed productService;
    private final User currentUser;
    private final InventoryTypeContext inventoryContext;
    
    public EnhancedProductAdditionUI(Scanner scanner, ProductManagementServiceFixed productService, User currentUser) {
        this.scanner = scanner;
        this.productService = productService;
        this.currentUser = currentUser;
        this.inventoryContext = new InventoryTypeContext();
    }
    
    /**
     * Main method to add a new product with enhanced inventory type selection
     */
    public void addNewProductEnhanced() {
        try {
            System.out.println();
            System.out.println("ADD NEW PRODUCT - ENHANCED");
            System.out.println("==================================================");
            
            // Step 1: Product Basic Information
            ProductBasicInfo basicInfo = collectBasicProductInfo();
            if (basicInfo == null) return;
            
            // Step 2: Inventory Type Selection
            InventoryTypeStrategy.InventoryConfiguration inventoryConfig = selectInventoryType(basicInfo.totalQuantity);
            if (inventoryConfig == null) return;
            
            // Step 3: Shelf Configuration (only if required)
            ShelfConfiguration shelfConfig = null;
            if (inventoryConfig.requiresShelfConfig()) {
                shelfConfig = configureShelfSetup();
                if (shelfConfig == null) return;
            }
            
            // Step 4: Discount Configuration
            DiscountConfiguration discountConfig = configureDiscount();
            
            // Step 5: Create Product with Inventory
            ProductNew createdProduct = createProductWithConfiguration(
                basicInfo, inventoryConfig, shelfConfig, discountConfig);
            
            // Step 6: Display Results
            displayCreationResults(createdProduct, basicInfo, inventoryConfig, discountConfig);
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            LOGGER.severe(() -> "Error in enhanced product addition: " + e.getMessage());
        }
        
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Collect basic product information
     */
    private ProductBasicInfo collectBasicProductInfo() {
        System.out.println("STEP 1: Basic Product Information");
        System.out.println("--------------------------------------------------");
        
        // Display available categories (simplified - you can expand this)
        System.out.println("Available subcategories: EL-AP (Electronics-Appliances), FD-SN (Food-Snacks), etc.");
        System.out.print("Enter Subcategory Code: ");
        String subcategoryCode = scanner.nextLine().trim().toUpperCase();
        
        System.out.print("Product Name: ");
        String productName = scanner.nextLine().trim();
        
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        
        System.out.print("Unit Price (LKR): ");
        String priceInput = scanner.nextLine().trim();
        
        System.out.print("Brand: ");
        String brand = scanner.nextLine().trim();
        
        // Unit selection (simplified)
        System.out.println("\nUnit Options: 1-Pieces, 2-Kg, 3-Liters, 4-Meters");
        System.out.print("Select unit (1-4): ");
        int unitChoice = Integer.parseInt(scanner.nextLine().trim());
        String unit = getUnitByChoice(unitChoice);
        
        System.out.print("Total Quantity: ");
        int totalQuantity = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("Expiry Date (YYYY-MM-DD) or 'N/A': ");
        String expiryDate = scanner.nextLine().trim();
        
        try {
            BigDecimal basePrice = new BigDecimal(priceInput);
            return new ProductBasicInfo(subcategoryCode, productName, description, 
                basePrice, brand, unit, totalQuantity, expiryDate);
        } catch (Exception e) {
            System.out.println("❌ Invalid input. Please try again.");
            return null;
        }
    }
    
    /**
     * Select inventory type using Strategy Pattern
     */
    private InventoryTypeStrategy.InventoryConfiguration selectInventoryType(int totalQuantity) {
        System.out.println("\nSTEP 2: Inventory Type Selection");
        System.out.println("--------------------------------------------------");
        
        InventoryTypeContext.displayInventoryTypeOptions();
        
        System.out.print("Select inventory type (1-3): ");
        int choice = Integer.parseInt(scanner.nextLine().trim());
        
        if (!InventoryTypeContext.isValidInventoryTypeChoice(choice)) {
            System.out.println("❌ Invalid choice. Please try again.");
            return null;
        }
        
        inventoryContext.setStrategyByIndex(choice);
        InventoryTypeStrategy.InventoryConfiguration config = inventoryContext.configureInventory(totalQuantity);
        
        System.out.println("\nInventory Configuration Preview:");
        System.out.println(config.getConfigurationSummary());
        
        System.out.print("\nProceed with this configuration? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("❌ Configuration cancelled.");
            return null;
        }
        
        return config;
    }
    
    /**
     * Configure shelf setup (only for physical/hybrid inventory)
     */
    private ShelfConfiguration configureShelfSetup() {
        System.out.println("\nSTEP 3: Physical Storage Configuration");
        System.out.println("--------------------------------------------------");
        System.out.println("Since you selected physical storage, shelf configuration is required.");
        System.out.println();
        
        System.out.print("Use default shelf settings (Capacity: 20, Minimum: 5)? (y/n): ");
        String useDefault = scanner.nextLine().trim().toLowerCase();
        
        if (useDefault.equals("y") || useDefault.equals("yes")) {
            return new ShelfConfiguration(20, 5, true);
        } else {
            System.out.print("Enter shelf capacity: ");
            int capacity = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter minimum threshold: ");
            int minimum = Integer.parseInt(scanner.nextLine().trim());
            
            return new ShelfConfiguration(capacity, minimum, false);
        }
    }
    
    /**
     * Configure discount settings
     */
    private DiscountConfiguration configureDiscount() {
        System.out.println("\nSTEP 4: Discount Configuration (Optional)");
        System.out.println("--------------------------------------------------");
        System.out.println("1. No discount");
        System.out.println("2. Fixed amount discount");
        System.out.println("3. Percentage discount");
        
        System.out.print("Select option (1-3): ");
        int choice = Integer.parseInt(scanner.nextLine().trim());
        
        switch (choice) {
            case 2:
                System.out.print("Enter discount amount (LKR): ");
                BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
                return new DiscountConfiguration(DiscountType.FIXED_AMOUNT, amount);
                
            case 3:
                System.out.print("Enter discount percentage: ");
                BigDecimal percentage = new BigDecimal(scanner.nextLine().trim());
                return new DiscountConfiguration(DiscountType.PERCENTAGE, percentage);
                
            default:
                return new DiscountConfiguration(DiscountType.NONE, BigDecimal.ZERO);
        }
    }
    
    /**
     * Create product with all configurations
     */
    private ProductNew createProductWithConfiguration(
            ProductBasicInfo basicInfo,
            InventoryTypeStrategy.InventoryConfiguration inventoryConfig,
            ShelfConfiguration shelfConfig,
            DiscountConfiguration discountConfig) {
        
        // Get subcategory ID (simplified - you should implement proper lookup)
        Long subcategoryId = getSubcategoryIdByCode(basicInfo.subcategoryCode);
        
        ProductNew createdProduct = productService.createProductWithInventory(
            basicInfo.productName,
            basicInfo.description,
            subcategoryId,
            basicInfo.basePrice,
            basicInfo.unit,
            basicInfo.brand,
            currentUser.getId(),
            inventoryConfig.getPhysicalQuantity(),
            inventoryConfig.getShelfQuantity(),
            inventoryConfig.getOnlineQuantity(),
            shelfConfig != null ? shelfConfig.capacity : null,
            shelfConfig != null ? shelfConfig.minimum : null,
            basicInfo.expiryDate
        );
        
        // Apply discount if specified
        if (createdProduct != null && discountConfig.type != DiscountType.NONE) {
            String productCode = createdProduct.getProductCode();
            if (discountConfig.type == DiscountType.FIXED_AMOUNT) {
                productService.setFixedDiscount(productCode, discountConfig.value);
            } else if (discountConfig.type == DiscountType.PERCENTAGE) {
                productService.setPercentageDiscount(productCode, discountConfig.value);
            }
        }
        
        return createdProduct;
    }
    
    /**
     * Display creation results
     */
    private void displayCreationResults(ProductNew createdProduct, ProductBasicInfo basicInfo,
                                      InventoryTypeStrategy.InventoryConfiguration inventoryConfig,
                                      DiscountConfiguration discountConfig) {
        
        System.out.println("\n" + "=".repeat(60));
        
        if (createdProduct != null) {
            System.out.println("✅ PRODUCT CREATED SUCCESSFULLY!");
            System.out.println("=".repeat(60));
            System.out.println("Product Code: " + createdProduct.getProductCode());
            System.out.println("Product Name: " + basicInfo.productName);
            System.out.println("Brand: " + basicInfo.brand);
            System.out.println("Unit Price: LKR " + basicInfo.basePrice);
            System.out.println("Unit: " + basicInfo.unit);
            System.out.println("Total Quantity: " + basicInfo.totalQuantity);
            
            System.out.println("\nInventory Distribution:");
            System.out.println("- Physical Stock: " + inventoryConfig.getPhysicalQuantity() + " units");
            System.out.println("- Shelf Display: " + inventoryConfig.getShelfQuantity() + " units");
            System.out.println("- Online Available: " + inventoryConfig.getOnlineQuantity() + " units");
            
            if (discountConfig.type != DiscountType.NONE) {
                System.out.println("\nDiscount Applied: " + discountConfig.getDescription());
            }
            
            System.out.println("\nInventory Type: " + inventoryContext.getCurrentStrategy().getTypeName());
            
        } else {
            System.out.println("❌ PRODUCT CREATION FAILED!");
            System.out.println("Please check your input and try again.");
        }
    }
    
    // Helper methods and data classes
    private String getUnitByChoice(int choice) {
        switch (choice) {
            case 1: return "Pieces";
            case 2: return "Kg";
            case 3: return "Liters";
            case 4: return "Meters";
            default: return "Pieces";
        }
    }
    
    private Long getSubcategoryIdByCode(String code) {
        // Simplified implementation - you should use proper service lookup
        return 1L;
    }
    
    // Data classes for configuration
    private static class ProductBasicInfo {
        final String subcategoryCode, productName, description, brand, unit, expiryDate;
        final BigDecimal basePrice;
        final int totalQuantity;
        
        ProductBasicInfo(String subcategoryCode, String productName, String description,
                        BigDecimal basePrice, String brand, String unit, int totalQuantity, String expiryDate) {
            this.subcategoryCode = subcategoryCode;
            this.productName = productName;
            this.description = description;
            this.basePrice = basePrice;
            this.brand = brand;
            this.unit = unit;
            this.totalQuantity = totalQuantity;
            this.expiryDate = expiryDate;
        }
    }
    
    private static class ShelfConfiguration {
        final int capacity, minimum;
        
        ShelfConfiguration(int capacity, int minimum, boolean isDefault) {
            this.capacity = capacity;
            this.minimum = minimum;
            // isDefault parameter removed to eliminate unused field warning
        }
    }
    
    private enum DiscountType { NONE, FIXED_AMOUNT, PERCENTAGE }
    
    private static class DiscountConfiguration {
        final DiscountType type;
        final BigDecimal value;
        
        DiscountConfiguration(DiscountType type, BigDecimal value) {
            this.type = type;
            this.value = value;
        }
        
        String getDescription() {
            switch (type) {
                case FIXED_AMOUNT: return "LKR " + value + " off";
                case PERCENTAGE: return value + "% discount";
                default: return "No discount";
            }
        }
    }
}