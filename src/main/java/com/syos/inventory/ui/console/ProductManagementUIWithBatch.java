package com.syos.inventory.ui.console;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Enhanced Product Management UI with Batch Support
 * Handles product creation with expiry date management and batch tracking
 */
public class ProductManagementUIWithBatch {
    
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Enhanced add product method with batch and expiry management
     */
    public static void addProductWithBatch() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📦 CREATE NEW PRODUCT WITH BATCH MANAGEMENT");
        System.out.println("=".repeat(60));
        
        try {
            // Step 1: Collect basic product information
            System.out.print("Enter Product Name: ");
            String productName = scanner.nextLine().trim();
            
            if (productName.isEmpty()) {
                System.out.println("❌ Product name cannot be empty!");
                return;
            }
            
            System.out.print("Enter Product Description: ");
            String description = scanner.nextLine().trim();
            
            System.out.print("Enter Brand: ");
            String brand = scanner.nextLine().trim();
            
            System.out.print("Enter Base Price: $");
            double basePrice = Double.parseDouble(scanner.nextLine().trim());
            
            System.out.print("Enter Unit of Measure (e.g., pieces, kg, liters): ");
            String unitOfMeasure = scanner.nextLine().trim();
            
            System.out.print("Enter Subcategory ID: ");
            int subcategoryId = Integer.parseInt(scanner.nextLine().trim());
            
            // Step 2: Initial Batch Information
            System.out.println("\n" + "-".repeat(50));
            System.out.println("📋 INITIAL BATCH INFORMATION");
            System.out.println("-".repeat(50));
            
            System.out.print("Enter Initial Quantity: ");
            int initialQuantity = Integer.parseInt(scanner.nextLine().trim());
            
            if (initialQuantity <= 0) {
                System.out.println("❌ Quantity must be greater than 0!");
                return;
            }
            
            System.out.print("Enter Purchase Date (YYYY-MM-DD) [Press Enter for today]: ");
            String purchaseDateInput = scanner.nextLine().trim();
            LocalDate purchaseDate = purchaseDateInput.isEmpty() ? 
                LocalDate.now() : LocalDate.parse(purchaseDateInput, DATE_FORMATTER);
            
            // Step 3: Expiry Date Management
            System.out.println("\n" + "-".repeat(50));
            System.out.println("📅 EXPIRY DATE MANAGEMENT");
            System.out.println("-".repeat(50));
            
            LocalDate expiryDate = handleExpiryDateInput();
            
            System.out.print("Enter Selling Price for this batch: $");
            double sellingPrice = Double.parseDouble(scanner.nextLine().trim());
            
            // Step 4: Display summary and confirm
            displayProductSummary(productName, brand, basePrice, initialQuantity, 
                                purchaseDate, expiryDate, sellingPrice);
            
            System.out.print("\nCreate this product? (y/N): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();
            
            if (confirmation.equals("y") || confirmation.equals("yes")) {
                // TODO: Call service to create product with batch
                createProductWithBatchService(productName, description, brand, basePrice, 
                                            unitOfMeasure, subcategoryId, initialQuantity, 
                                            purchaseDate, expiryDate, sellingPrice);
            } else {
                System.out.println("❌ Product creation cancelled.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format! Please enter valid numbers.");
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format! Please use YYYY-MM-DD format.");
        } catch (Exception e) {
            System.out.println("❌ Error creating product: " + e.getMessage());
        }
    }
    
    /**
     * Handles expiry date input with options for perishable/non-perishable items
     */
    private static LocalDate handleExpiryDateInput() {
        System.out.println("Is this product perishable?");
        System.out.println("1. Yes - Has expiry date (perishable)");
        System.out.println("2. No - No expiry date (non-perishable)");
        System.out.print("Choose option (1-2): ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                return getExpiryDateFromUser();
            case "2":
                System.out.println("✅ Product marked as non-perishable (no expiry date)");
                return null;
            default:
                System.out.println("⚠️  Invalid choice. Defaulting to non-perishable.");
                return null;
        }
    }
    
    /**
     * Gets expiry date from user with validation
     */
    private static LocalDate getExpiryDateFromUser() {
        while (true) {
            try {
                System.out.print("Enter Expiry Date (YYYY-MM-DD): ");
                String expiryInput = scanner.nextLine().trim();
                LocalDate expiryDate = LocalDate.parse(expiryInput, DATE_FORMATTER);
                
                if (expiryDate.isBefore(LocalDate.now())) {
                    System.out.println("❌ Expiry date cannot be in the past!");
                    continue;
                }
                
                if (expiryDate.isAfter(LocalDate.now().plusYears(10))) {
                    System.out.println("⚠️  Warning: Expiry date is more than 10 years from now!");
                    System.out.print("Continue anyway? (y/N): ");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    if (!confirm.equals("y") && !confirm.equals("yes")) {
                        continue;
                    }
                }
                
                return expiryDate;
                
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format! Please use YYYY-MM-DD format.");
            }
        }
    }
    
    /**
     * Displays product creation summary
     */
    private static void displayProductSummary(String productName, String brand, double basePrice, 
                                            int initialQuantity, LocalDate purchaseDate, 
                                            LocalDate expiryDate, double sellingPrice) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📋 PRODUCT CREATION SUMMARY");
        System.out.println("=".repeat(60));
        System.out.printf("Product Name     : %s%n", productName);
        System.out.printf("Brand           : %s%n", brand);
        System.out.printf("Base Price      : $%.2f%n", basePrice);
        System.out.printf("Initial Quantity: %d units%n", initialQuantity);
        System.out.printf("Purchase Date   : %s%n", purchaseDate.format(DATE_FORMATTER));
        
        if (expiryDate != null) {
            System.out.printf("Expiry Date     : %s (⚠️  Perishable)%n", expiryDate.format(DATE_FORMATTER));
            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
            System.out.printf("Days Until Expiry: %d days%n", daysUntilExpiry);
        } else {
            System.out.printf("Expiry Date     : No expiry (✅ Non-perishable)%n");
        }
        
        System.out.printf("Selling Price   : $%.2f%n", sellingPrice);
        
        // Calculate inventory distribution
        int warehouseQty = (int) Math.floor(initialQuantity * 0.80);
        int shelfQty = (int) Math.ceil(initialQuantity * 0.20);
        
        System.out.println("\n📦 INVENTORY DISTRIBUTION:");
        System.out.printf("  Warehouse: %d units (80%%)%n", warehouseQty);
        System.out.printf("  Shelf    : %d units (20%%)%n", shelfQty);
        System.out.printf("  Online   : 0 units (manual transfer)%n");
        System.out.println("=".repeat(60));
    }
    
    /**
     * Creates product with batch using service layer
     * TODO: Implement actual service call
     */
    private static void createProductWithBatchService(String productName, String description, 
                                                    String brand, double basePrice, String unitOfMeasure,
                                                    int subcategoryId, int initialQuantity, 
                                                    LocalDate purchaseDate, LocalDate expiryDate, 
                                                    double sellingPrice) {
        
        System.out.println("\n🔄 Creating product with batch...");
        
        try {
            // TODO: Replace with actual service call
            // ProductWithBatchResult result = productService.createProductWithBatch(...)
            
            // Simulate product creation
            String productCode = generateSimulatedProductCode(productName, brand);
            String batchNumber = generateSimulatedBatchNumber(productCode);
            
            System.out.println("\n✅ PRODUCT CREATED SUCCESSFULLY!");
            System.out.println("=".repeat(50));
            System.out.printf("Product Code : %s%n", productCode);
            System.out.printf("Batch Number : %s%n", batchNumber);
            System.out.printf("Total Quantity: %d units%n", initialQuantity);
            
            if (expiryDate != null) {
                System.out.printf("⚠️  Expiry Alert: %d days until expiry%n", 
                    java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate));
            }
            
            System.out.println("✅ Inventory distributed across locations");
            System.out.println("✅ Batch tracking activated");
            System.out.println("=".repeat(50));
            
        } catch (Exception e) {
            System.out.println("❌ Failed to create product: " + e.getMessage());
        }
    }
    
    /**
     * Generates simulated product code (replace with actual service call)
     */
    private static String generateSimulatedProductCode(String productName, String brand) {
        String name = productName.replaceAll("[^A-Za-z]", "").toUpperCase();
        String brandStr = (brand != null ? brand : "").replaceAll("[^A-Za-z]", "").toUpperCase();
        
        String namePrefix = name.length() >= 2 ? name.substring(0, 2) : (name.length() == 1 ? name + "X" : "XX");
        String brandPrefix = brandStr.length() >= 2 ? brandStr.substring(0, 2) : (brandStr.length() == 1 ? brandStr + "X" : "XX");
        
        int sequence = (int) (Math.random() * 9999) + 1;
        return String.format("PRD-%s%s%04d", namePrefix, brandPrefix, sequence);
    }
    
    /**
     * Generates simulated batch number (replace with actual service call)
     */
    private static String generateSimulatedBatchNumber(String productCode) {
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                          String.format("%04d", (int) (Math.random() * 9999));
        return "B-" + productCode + "-" + timestamp;
    }
    
    /**
     * Menu option for batch management
     */
    public static void showBatchManagementMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📦 BATCH MANAGEMENT MENU");
        System.out.println("=".repeat(60));
        System.out.println("1. Create Product with Initial Batch");
        System.out.println("2. Add New Batch to Existing Product");
        System.out.println("3. View Batch History");
        System.out.println("4. Check Expiry Alerts");
        System.out.println("5. Batch Inventory Report");
        System.out.println("0. Back to Main Menu");
        System.out.println("=".repeat(60));
        
        System.out.print("Choose an option (0-5): ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                addProductWithBatch();
                break;
            case "2":
                System.out.println("🔄 Add New Batch - Feature coming soon!");
                break;
            case "3":
                System.out.println("📋 Batch History - Feature coming soon!");
                break;
            case "4":
                System.out.println("⚠️  Expiry Alerts - Feature coming soon!");
                break;
            case "5":
                System.out.println("📊 Batch Report - Feature coming soon!");
                break;
            case "0":
                System.out.println("↩️  Returning to main menu...");
                break;
            default:
                System.out.println("❌ Invalid choice! Please try again.");
        }
    }
}