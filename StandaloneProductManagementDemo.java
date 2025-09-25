import java.util.Scanner;

/**
 * Standalone demo of Product Management System
 * This demonstrates the admin functions without requiring the full SYOS compilation
 */
public class StandaloneProductManagementDemo {
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("         SYOS Product Management System          ");
        System.out.println("              ADMIN FUNCTIONS DEMO              ");
        System.out.println("=================================================");
        System.out.println();
        
        // Simulate admin user
        MockUser adminUser = new MockUser("admin", "ADMIN");
        
        // Create and start product management system
        MockProductManagementUI ui = new MockProductManagementUI(adminUser);
        ui.displayProductManagement();
    }
}

/**
 * Mock User class for demo
 */
class MockUser {
    private String username;
    private String role;
    
    public MockUser(String username, String role) {
        this.username = username;
        this.role = role;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getRole() {
        return role;
    }
}

/**
 * Mock Product Management UI - Simplified version for demo
 */
class MockProductManagementUI {
    private MockUser currentUser;
    private Scanner scanner;
    
    public MockProductManagementUI(MockUser user) {
        this.currentUser = user;
        this.scanner = new Scanner(System.in);
    }
    
    public void displayProductManagement() {
        System.out.println("Welcome to Product Management, " + currentUser.getUsername() + "!");
        System.out.println("Current Role: " + currentUser.getRole());
        System.out.println();
        
        while (true) {
            displayMainMenu();
            System.out.print("Enter your choice (1-8): ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                if (choice == 8) {
                    System.out.println("Exiting Product Management...");
                    System.out.println("Thank you for using SYOS Product Management System!");
                    break;
                }
                
                handleMenuChoice(choice);
                
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1-8.");
            }
            
            System.out.println(); // Add spacing between operations
        }
    }
    
    private void displayMainMenu() {
        System.out.println("┌─────────────────────────────────────────────────┐");
        System.out.println("│            PRODUCT MANAGEMENT SYSTEM            │");
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│  1. View All Products                           │");
        System.out.println("│  2. Add New Product                             │");
        System.out.println("│  3. Edit Product                                │");
        System.out.println("│  4. Delete Product                              │");
        System.out.println("│  5. Manage Categories                           │");
        System.out.println("│  6. Manage Subcategories                        │");
        System.out.println("│  7. Set Product Discounts                       │");
        System.out.println("│  8. Back to Main Menu                           │");
        System.out.println("└─────────────────────────────────────────────────┘");
    }
    
    private void handleMenuChoice(int choice) {
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
                manageSubcategories();
                break;
            case 7:
                setProductDiscounts();
                break;
            default:
                System.out.println("Invalid choice. Please select 1-8.");
        }
    }
    
    private void viewAllProducts() {
        System.out.println("=== VIEW ALL PRODUCTS ===");
        System.out.println("┌────────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ ID  │ Name           │ Category    │ Price    │ Stock │ Min Stock │ Status    │");
        System.out.println("├────────────────────────────────────────────────────────────────────────────────┤");
        System.out.println("│ 001 │ Coca Cola      │ Beverages   │ ₱25.00   │ 150   │ 20        │ Active    │");
        System.out.println("│ 002 │ White Bread    │ Bakery      │ ₱45.00   │ 75    │ 10        │ Active    │");
        System.out.println("│ 003 │ Instant Coffee │ Beverages   │ ₱120.00  │ 30    │ 15        │ Active    │");
        System.out.println("│ 004 │ Rice 5kg       │ Grains      │ ₱250.00  │ 5     │ 10        │ Low Stock │");
        System.out.println("│ 005 │ Dish Soap      │ Cleaning    │ ₱85.00   │ 40    │ 8         │ Active    │");
        System.out.println("└────────────────────────────────────────────────────────────────────────────────┘");
        System.out.println("Total Products: 5 | Active: 4 | Low Stock: 1");
        
        System.out.print("\\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private void addNewProduct() {
        System.out.println("=== ADD NEW PRODUCT ===");
        
        System.out.print("Enter Product Name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Enter Description: ");
        String description = scanner.nextLine().trim();
        
        System.out.print("Enter Category: ");
        String category = scanner.nextLine().trim();
        
        System.out.print("Enter Subcategory: ");
        String subcategory = scanner.nextLine().trim();
        
        System.out.print("Enter Price (₱): ");
        String priceInput = scanner.nextLine().trim();
        
        System.out.print("Enter Initial Stock Quantity: ");
        String stockInput = scanner.nextLine().trim();
        
        System.out.print("Enter Minimum Stock Level: ");
        String minStockInput = scanner.nextLine().trim();
        
        System.out.print("Enter Unit of Measure: ");
        String unit = scanner.nextLine().trim();
        
        // Simulate validation and creation
        System.out.println("\\n✅ Product added successfully!");
        System.out.println("Generated Product ID: PROD-" + String.format("%03d", (int)(Math.random() * 999 + 1)));
        System.out.println("Product: " + name);
        System.out.println("Category: " + category + " > " + subcategory);
        System.out.println("Price: ₱" + priceInput);
        System.out.println("Initial Stock: " + stockInput + " " + unit);
        System.out.println("Status: Active");
        
        System.out.print("\\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private void editProduct() {
        System.out.println("=== EDIT PRODUCT ===");
        
        System.out.print("Enter Product ID or Name to search: ");
        String searchTerm = scanner.nextLine().trim();
        
        // Simulate finding product
        System.out.println("\\nFound Product:");
        System.out.println("ID: PROD-001");
        System.out.println("Name: Coca Cola");
        System.out.println("Category: Beverages > Soft Drinks");
        System.out.println("Current Price: ₱25.00");
        System.out.println("Current Stock: 150 pieces");
        System.out.println("Min Stock Level: 20");
        
        System.out.println("\\nWhat would you like to edit?");
        System.out.println("1. Product Information");
        System.out.println("2. Stock Quantity");
        System.out.println("3. Pricing");
        System.out.println("4. Category Assignment");
        System.out.print("Choose option (1-4): ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                editProductInformation();
                break;
            case "2":
                editStockQuantity();
                break;
            case "3":
                editPricing();
                break;
            case "4":
                editCategoryAssignment();
                break;
            default:
                System.out.println("Invalid choice.");
        }
        
        System.out.print("\\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private void editProductInformation() {
        System.out.println("\\n--- Edit Product Information ---");
        
        System.out.print("New Product Name (current: Coca Cola): ");
        String name = scanner.nextLine().trim();
        
        System.out.print("New Description: ");
        String description = scanner.nextLine().trim();
        
        System.out.println("\\n✅ Product information updated successfully!");
    }
    
    private void editStockQuantity() {
        System.out.println("\\n--- Edit Stock Quantity ---");
        System.out.println("Current Stock: 150 pieces");
        
        System.out.println("1. Add Stock (Restock)");
        System.out.println("2. Remove Stock (Adjustment)");
        System.out.println("3. Set New Stock Level");
        System.out.print("Choose action (1-3): ");
        
        String action = scanner.nextLine().trim();
        
        System.out.print("Enter quantity: ");
        String quantity = scanner.nextLine().trim();
        
        System.out.println("\\n✅ Stock updated successfully!");
        System.out.println("Previous Stock: 150");
        System.out.println("New Stock: " + (action.equals("1") ? (150 + Integer.parseInt(quantity)) : 
                                        action.equals("2") ? (150 - Integer.parseInt(quantity)) : quantity));
    }
    
    private void editPricing() {
        System.out.println("\\n--- Edit Pricing ---");
        System.out.println("Current Price: ₱25.00");
        
        System.out.print("Enter new price (₱): ");
        String newPrice = scanner.nextLine().trim();
        
        System.out.println("\\n✅ Price updated successfully!");
        System.out.println("Previous Price: ₱25.00");
        System.out.println("New Price: ₱" + newPrice);
    }
    
    private void editCategoryAssignment() {
        System.out.println("\\n--- Edit Category Assignment ---");
        System.out.println("Current Category: Beverages > Soft Drinks");
        
        System.out.print("Enter new category: ");
        String category = scanner.nextLine().trim();
        
        System.out.print("Enter new subcategory: ");
        String subcategory = scanner.nextLine().trim();
        
        System.out.println("\\n✅ Category updated successfully!");
        System.out.println("Previous Category: Beverages > Soft Drinks");
        System.out.println("New Category: " + category + " > " + subcategory);
    }
    
    private void deleteProduct() {
        System.out.println("=== DELETE PRODUCT ===");
        
        System.out.print("Enter Product ID or Name to delete: ");
        String searchTerm = scanner.nextLine().trim();
        
        // Simulate finding product
        System.out.println("\\nFound Product:");
        System.out.println("ID: PROD-001");
        System.out.println("Name: Coca Cola");
        System.out.println("Category: Beverages > Soft Drinks");
        System.out.println("Current Stock: 150 pieces");
        
        System.out.println("\\n⚠️  WARNING: This action cannot be undone!");
        System.out.print("Are you sure you want to delete this product? (yes/no): ");
        
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (confirmation.equals("yes") || confirmation.equals("y")) {
            System.out.println("\\n✅ Product deleted successfully!");
            System.out.println("Product 'Coca Cola' has been removed from inventory.");
        } else {
            System.out.println("\\nDeletion cancelled.");
        }
        
        System.out.print("\\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private void manageCategories() {
        System.out.println("=== MANAGE CATEGORIES ===");
        
        System.out.println("Current Categories:");
        System.out.println("1. Beverages (Active)");
        System.out.println("2. Bakery (Active)");
        System.out.println("3. Grains (Active)");
        System.out.println("4. Cleaning (Active)");
        System.out.println("5. Dairy (Inactive)");
        
        System.out.println("\\nCategory Management Options:");
        System.out.println("1. Add New Category");
        System.out.println("2. Edit Category");
        System.out.println("3. Activate/Deactivate Category");
        System.out.println("4. View Category Details");
        System.out.print("Choose option (1-4): ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                addNewCategory();
                break;
            case "2":
                editCategory();
                break;
            case "3":
                toggleCategoryStatus();
                break;
            case "4":
                viewCategoryDetails();
                break;
            default:
                System.out.println("Invalid choice.");
        }
        
        System.out.print("\\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private void addNewCategory() {
        System.out.println("\\n--- Add New Category ---");
        
        System.out.print("Enter Category Code: ");
        String code = scanner.nextLine().trim();
        
        System.out.print("Enter Category Name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Enter Description: ");
        String description = scanner.nextLine().trim();
        
        System.out.println("\\n✅ Category added successfully!");
        System.out.println("Code: " + code.toUpperCase());
        System.out.println("Name: " + name);
        System.out.println("Status: Active");
    }
    
    private void editCategory() {
        System.out.println("\\n--- Edit Category ---");
        
        System.out.print("Enter Category Code or Name: ");
        String searchTerm = scanner.nextLine().trim();
        
        System.out.println("\\nFound Category: Beverages");
        System.out.print("New Category Name: ");
        String newName = scanner.nextLine().trim();
        
        System.out.print("New Description: ");
        String newDescription = scanner.nextLine().trim();
        
        System.out.println("\\n✅ Category updated successfully!");
    }
    
    private void toggleCategoryStatus() {
        System.out.println("\\n--- Activate/Deactivate Category ---");
        
        System.out.print("Enter Category Code or Name: ");
        String searchTerm = scanner.nextLine().trim();
        
        System.out.println("\\nFound Category: Dairy (Currently: Inactive)");
        System.out.print("Activate this category? (yes/no): ");
        String action = scanner.nextLine().trim();
        
        if (action.toLowerCase().equals("yes")) {
            System.out.println("\\n✅ Category 'Dairy' activated successfully!");
        } else {
            System.out.println("\\nNo changes made.");
        }
    }
    
    private void viewCategoryDetails() {
        System.out.println("\\n--- Category Details ---");
        System.out.println("Category: Beverages");
        System.out.println("Code: BEV");
        System.out.println("Status: Active");
        System.out.println("Products Count: 12");
        System.out.println("Subcategories:");
        System.out.println("  - Soft Drinks (8 products)");
        System.out.println("  - Coffee & Tea (4 products)");
    }
    
    private void manageSubcategories() {
        System.out.println("=== MANAGE SUBCATEGORIES ===");
        
        System.out.println("Current Subcategories:");
        System.out.println("1. Soft Drinks (Beverages) - 8 products");
        System.out.println("2. Coffee & Tea (Beverages) - 4 products");
        System.out.println("3. Fresh Bread (Bakery) - 5 products");
        System.out.println("4. Rice Products (Grains) - 3 products");
        
        System.out.println("\\nSubcategory Management Options:");
        System.out.println("1. Add New Subcategory");
        System.out.println("2. Edit Subcategory");
        System.out.println("3. View Subcategory Products");
        System.out.println("4. Set Shelf Capacity");
        System.out.print("Choose option (1-4): ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                addNewSubcategory();
                break;
            case "2":
                editSubcategory();
                break;
            case "3":
                viewSubcategoryProducts();
                break;
            case "4":
                setShelfCapacity();
                break;
            default:
                System.out.println("Invalid choice.");
        }
        
        System.out.print("\\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private void addNewSubcategory() {
        System.out.println("\\n--- Add New Subcategory ---");
        
        System.out.println("Available Categories:");
        System.out.println("1. Beverages");
        System.out.println("2. Bakery");
        System.out.println("3. Grains");
        System.out.println("4. Cleaning");
        
        System.out.print("Select Parent Category (1-4): ");
        String categoryChoice = scanner.nextLine().trim();
        
        System.out.print("Enter Subcategory Code: ");
        String code = scanner.nextLine().trim();
        
        System.out.print("Enter Subcategory Name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Enter Shelf Capacity: ");
        String capacity = scanner.nextLine().trim();
        
        System.out.println("\\n✅ Subcategory added successfully!");
        System.out.println("Code: " + code.toUpperCase());
        System.out.println("Name: " + name);
        System.out.println("Parent Category: " + getCategoryName(categoryChoice));
        System.out.println("Shelf Capacity: " + capacity + " units");
    }
    
    private void editSubcategory() {
        System.out.println("\\n--- Edit Subcategory ---");
        
        System.out.print("Enter Subcategory Code or Name: ");
        String searchTerm = scanner.nextLine().trim();
        
        System.out.println("\\nFound Subcategory: Soft Drinks (Beverages)");
        System.out.print("New Subcategory Name: ");
        String newName = scanner.nextLine().trim();
        
        System.out.print("New Shelf Capacity: ");
        String newCapacity = scanner.nextLine().trim();
        
        System.out.println("\\n✅ Subcategory updated successfully!");
    }
    
    private void viewSubcategoryProducts() {
        System.out.println("\\n--- Subcategory Products ---");
        System.out.println("Subcategory: Soft Drinks (Beverages)");
        System.out.println("Shelf Capacity: 200 units");
        System.out.println("Current Usage: 158 units (79% full)");
        System.out.println();
        System.out.println("Products in this subcategory:");
        System.out.println("1. Coca Cola - 150 units");
        System.out.println("2. Pepsi - 8 units");
    }
    
    private void setShelfCapacity() {
        System.out.println("\\n--- Set Shelf Capacity ---");
        
        System.out.print("Enter Subcategory Code or Name: ");
        String searchTerm = scanner.nextLine().trim();
        
        System.out.println("\\nFound Subcategory: Soft Drinks");
        System.out.println("Current Capacity: 200 units");
        System.out.println("Current Usage: 158 units");
        
        System.out.print("Enter new capacity: ");
        String newCapacity = scanner.nextLine().trim();
        
        System.out.println("\\n✅ Shelf capacity updated successfully!");
        System.out.println("Previous Capacity: 200 units");
        System.out.println("New Capacity: " + newCapacity + " units");
    }
    
    private void setProductDiscounts() {
        System.out.println("=== SET PRODUCT DISCOUNTS ===");
        
        System.out.print("Enter Product ID or Name: ");
        String searchTerm = scanner.nextLine().trim();
        
        // Simulate finding product
        System.out.println("\\nFound Product:");
        System.out.println("ID: PROD-001");
        System.out.println("Name: Coca Cola");
        System.out.println("Current Price: ₱25.00");
        System.out.println("Current Discount: None");
        
        System.out.println("\\nDiscount Options:");
        System.out.println("1. Percentage Discount");
        System.out.println("2. Fixed Amount Discount");
        System.out.println("3. Remove Existing Discount");
        System.out.print("Choose option (1-3): ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                setPercentageDiscount();
                break;
            case "2":
                setFixedDiscount();
                break;
            case "3":
                removeDiscount();
                break;
            default:
                System.out.println("Invalid choice.");
        }
        
        System.out.print("\\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private void setPercentageDiscount() {
        System.out.println("\\n--- Set Percentage Discount ---");
        
        System.out.print("Enter discount percentage (1-99): ");
        String percentage = scanner.nextLine().trim();
        
        System.out.print("Enter discount reason: ");
        String reason = scanner.nextLine().trim();
        
        double originalPrice = 25.00;
        double discountPercent = Double.parseDouble(percentage);
        double discountAmount = originalPrice * (discountPercent / 100);
        double finalPrice = originalPrice - discountAmount;
        
        System.out.println("\\n✅ Percentage discount applied successfully!");
        System.out.println("Original Price: ₱" + String.format("%.2f", originalPrice));
        System.out.println("Discount: " + percentage + "% (₱" + String.format("%.2f", discountAmount) + ")");
        System.out.println("Final Price: ₱" + String.format("%.2f", finalPrice));
        System.out.println("Reason: " + reason);
    }
    
    private void setFixedDiscount() {
        System.out.println("\\n--- Set Fixed Amount Discount ---");
        
        System.out.print("Enter discount amount (₱): ");
        String amount = scanner.nextLine().trim();
        
        System.out.print("Enter discount reason: ");
        String reason = scanner.nextLine().trim();
        
        double originalPrice = 25.00;
        double discountAmount = Double.parseDouble(amount);
        double finalPrice = originalPrice - discountAmount;
        
        System.out.println("\\n✅ Fixed discount applied successfully!");
        System.out.println("Original Price: ₱" + String.format("%.2f", originalPrice));
        System.out.println("Discount: ₱" + String.format("%.2f", discountAmount));
        System.out.println("Final Price: ₱" + String.format("%.2f", finalPrice));
        System.out.println("Reason: " + reason);
    }
    
    private void removeDiscount() {
        System.out.println("\\n--- Remove Discount ---");
        System.out.println("Current discount will be removed.");
        
        System.out.print("Confirm removal (yes/no): ");
        String confirmation = scanner.nextLine().trim();
        
        if (confirmation.toLowerCase().equals("yes")) {
            System.out.println("\\n✅ Discount removed successfully!");
            System.out.println("Product price restored to original: ₱25.00");
        } else {
            System.out.println("\\nDiscount removal cancelled.");
        }
    }
    
    private String getCategoryName(String choice) {
        switch (choice) {
            case "1": return "Beverages";
            case "2": return "Bakery";
            case "3": return "Grains";
            case "4": return "Cleaning";
            default: return "Unknown";
        }
    }
}