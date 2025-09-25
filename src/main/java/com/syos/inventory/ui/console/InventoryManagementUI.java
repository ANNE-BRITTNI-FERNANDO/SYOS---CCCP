package com.syos.inventory.ui.console;

import com.syos.inventory.domain.entity.User;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

/**
 * Comprehensive Inventory Management UI
 * Handles stock levels, batch management, expiry tracking, and location transfers
 */
public class InventoryManagementUI {
    private final Scanner scanner;
    private final User currentUser;
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    public InventoryManagementUI(Scanner scanner, User currentUser) {
        this.scanner = scanner;
        this.currentUser = currentUser;
    }
    
    public void start() {
        boolean running = true;
        
        while (running) {
            displayInventoryMenu();
            
            try {
                String input = scanner.nextLine().trim();
                
                switch (input) {
                    case "1":
                        displayStockLevels();
                        break;
                    case "2":
                        handleTransferBetweenLocations();
                        break;
                    case "3":
                        checkLowStockAlerts();
                        break;
                    case "4":
                        handleExpiryDateManagement();
                        break;
                    case "5":
                        viewStockMovements();
                        break;
                    case "6":
                        createNewBatch();
                        break;
                    case "7":
                        running = false;
                        break;
                    default:
                        System.out.println("❌ Invalid option. Please try again.");
                        pauseForUser();
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
                pauseForUser();
            }
        }
    }
    
    private void displayInventoryMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("           INVENTORY MANAGEMENT");
        System.out.println("=".repeat(50));
        System.out.println("1. Track Stock Levels");
        System.out.println("2. Transfer Between Locations");
        System.out.println("3. Check Low Stock Alerts");
        System.out.println("4. Expiry Date Management");
        System.out.println("5. View Stock Movements");
        System.out.println("6. Create New Batch");
        System.out.println("7. Back to Main Menu");
        System.out.println("=".repeat(50));
        System.out.print("Choose an option (1-7): ");
    }
    
    /**
     * Display comprehensive stock level tracking
     */
    private void displayStockLevels() {
        System.out.println("\n🔍 STOCK LEVEL TRACKING");
        System.out.println("═".repeat(120));
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT " +
                           "p.product_code, p.product_name, p.brand, " +
                           "COALESCE(shelf_inv.quantity, 0) as shelf_qty, " +
                           "COALESCE(shelf_inv.capacity, 50) as shelf_capacity, " +
                           "COALESCE(warehouse_inv.quantity, 0) as warehouse_qty, " +
                           "COALESCE(online_inv.quantity, 0) as online_qty, " +
                           "(COALESCE(shelf_inv.quantity, 0) + COALESCE(warehouse_inv.quantity, 0) + COALESCE(online_inv.quantity, 0)) as total_qty, " +
                           "COALESCE(shelf_inv.reorder_level, 20) as reorder_level " +
                           "FROM product p " +
                           "LEFT JOIN (" +
                           "    SELECT b.product_id, " +
                           "           SUM(pi.current_quantity) as quantity, " +
                           "           MAX(pi.location_capacity) as capacity, " +
                           "           MIN(pi.min_threshold) as reorder_level " +
                           "    FROM physical_inventory pi " +
                           "    JOIN batch b ON pi.batch_id = b.batch_id " +
                           "    JOIN inventory_location il ON pi.location_id = il.location_id " +
                           "    WHERE il.location_code = 'SHELF' " +
                           "    GROUP BY b.product_id" +
                           ") shelf_inv ON p.product_id = shelf_inv.product_id " +
                           "LEFT JOIN (" +
                           "    SELECT b.product_id, SUM(pi.current_quantity) as quantity " +
                           "    FROM physical_inventory pi " +
                           "    JOIN batch b ON pi.batch_id = b.batch_id " +
                           "    JOIN inventory_location il ON pi.location_id = il.location_id " +
                           "    WHERE il.location_code = 'WAREHOUSE' " +
                           "    GROUP BY b.product_id" +
                           ") warehouse_inv ON p.product_id = warehouse_inv.product_id " +
                           "LEFT JOIN (" +
                           "    SELECT b.product_id, SUM(oi.available_quantity) as quantity " +
                           "    FROM online_inventory oi " +
                           "    JOIN batch b ON oi.batch_id = b.batch_id " +
                           "    GROUP BY b.product_id" +
                           ") online_inv ON p.product_id = online_inv.product_id " +
                           "WHERE p.is_active = 1 " +
                           "ORDER BY p.product_code";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    // Print header
                    System.out.printf("%-12s %-20s %-15s %-10s %-10s %-8s %-8s %-8s %-12s%n",
                        "Code", "Product Name", "Brand", "Shelf", "Warehouse", "Online", "Total", "Reorder", "Status");
                    System.out.println("═".repeat(120));
                    
                    boolean hasProducts = false;
                    while (rs.next()) {
                        hasProducts = true;
                        
                        String productCode = rs.getString("product_code");
                        String productName = rs.getString("product_name");
                        String brand = rs.getString("brand");
                        int shelfQty = rs.getInt("shelf_qty");
                        int shelfCapacity = rs.getInt("shelf_capacity");
                        int warehouseQty = rs.getInt("warehouse_qty");
                        int onlineQty = rs.getInt("online_qty");
                        int totalQty = rs.getInt("total_qty");
                        int reorderLevel = rs.getInt("reorder_level");
                        
                        // Truncate long names
                        String shortName = productName.length() > 18 ? productName.substring(0, 15) + "..." : productName;
                        String shortBrand = brand != null && brand.length() > 13 ? brand.substring(0, 10) + "..." : (brand != null ? brand : "N/A");
                        
                        // Determine status
                        String status = "";
                        if (shelfQty == 0) {
                            status = "[OUT OF STOCK]";
                        } else if (totalQty <= reorderLevel) {
                            status = "[LOW STOCK]";
                        }
                        
                        System.out.printf("%-12s %-20s %-15s %3d/%-6d %-10d %-8d %-8d %-8d %-12s%n",
                            productCode, shortName, shortBrand, 
                            shelfQty, shelfCapacity, warehouseQty, onlineQty, totalQty, reorderLevel, status);
                    }
                    
                    if (!hasProducts) {
                        System.out.println("No products found in inventory.");
                    }
                    
                    System.out.println("═".repeat(120));
                    System.out.println("Legend: Shelf shows current/capacity, [LOW STOCK] = below reorder level, [OUT OF STOCK] = shelf empty");
                }
            }
        } catch (Exception e) {
            System.err.println("Error displaying stock levels: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    /**
     * Handle transfers between locations
     */
    private void handleTransferBetweenLocations() {
        System.out.println("\n📦 TRANSFER BETWEEN LOCATIONS");
        System.out.println("─".repeat(40));
        
        try {
            // Show available products first
            System.out.println("Available products:");
            displayProductsForTransfer();
            
            System.out.print("Enter product code: ");
            String productCode = scanner.nextLine().trim().toUpperCase();
            
            // Show current stock levels for this product
            showProductStock(productCode);
            
            System.out.println("\nAvailable locations:");
            System.out.println("1. WAREHOUSE - Main Warehouse Storage");
            System.out.println("2. SHELF - Physical Display Shelf");
            System.out.println("3. ONLINE - Online Store Inventory");
            
            System.out.print("From location (1-3): ");
            String fromChoice = scanner.nextLine().trim();
            String fromLocation = getLocationCode(fromChoice);
            
            System.out.print("To location (1-3): ");
            String toChoice = scanner.nextLine().trim();
            String toLocation = getLocationCode(toChoice);
            
            if (fromLocation == null || toLocation == null || fromLocation.equals(toLocation)) {
                System.out.println("❌ Invalid location selection.");
                pauseForUser();
                return;
            }
            
            System.out.print("Quantity to transfer: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Reason for transfer: ");
            String reason = scanner.nextLine().trim();
            
            // Perform transfer
            if (transferStock(productCode, fromLocation, toLocation, quantity, reason)) {
                System.out.println("✅ Transfer completed successfully!");
            } else {
                System.out.println("❌ Transfer failed.");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error during transfer: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    /**
     * Check and display low stock alerts
     */
    private void checkLowStockAlerts() {
        System.out.println("\n⚠️ LOW STOCK ALERTS");
        System.out.println("═".repeat(80));
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT " +
                           "p.product_code, p.product_name, " +
                           "COALESCE(SUM(pi.current_quantity), 0) as current_stock, " +
                           "COALESCE(MIN(pi.min_threshold), 20) as reorder_level " +
                           "FROM product p " +
                           "LEFT JOIN batch b ON p.product_id = b.product_id " +
                           "LEFT JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                           "WHERE p.is_active = 1 " +
                           "GROUP BY p.product_code, p.product_name " +
                           "HAVING current_stock <= reorder_level " +
                           "ORDER BY current_stock ASC";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    System.out.printf("%-15s %-25s %-12s %-12s %-10s%n",
                        "Product Code", "Product Name", "Current Stock", "Reorder Level", "Priority");
                    System.out.println("─".repeat(80));
                    
                    boolean hasAlerts = false;
                    while (rs.next()) {
                        hasAlerts = true;
                        
                        String productCode = rs.getString("product_code");
                        String productName = rs.getString("product_name");
                        int currentStock = rs.getInt("current_stock");
                        int reorderLevel = rs.getInt("reorder_level");
                        
                        String priority = currentStock == 0 ? "🔴 CRITICAL" : 
                                        currentStock < reorderLevel / 2 ? "🟡 HIGH" : "🟠 MEDIUM";
                        
                        String shortName = productName.length() > 23 ? productName.substring(0, 20) + "..." : productName;
                        
                        System.out.printf("%-15s %-25s %12d %12d %-10s%n",
                            productCode, shortName, currentStock, reorderLevel, priority);
                    }
                    
                    if (!hasAlerts) {
                        System.out.println("✅ No low stock alerts! All products are adequately stocked.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking low stock alerts: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    /**
     * Handle expiry date management
     */
    private void handleExpiryDateManagement() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n📅 EXPIRY DATE MANAGEMENT");
            System.out.println("═".repeat(40));
            System.out.println("1. View Expiring Items (30 days)");
            System.out.println("2. View Critical Items (7 days)");
            System.out.println("3. Remove Expired Items");
            System.out.println("4. Back");
            System.out.println("─".repeat(40));
            System.out.print("Choose option (1-4): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    viewExpiringItems(30);
                    break;
                case "2":
                    viewExpiringItems(7);
                    break;
                case "3":
                    removeExpiredItems();
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.println("❌ Invalid option.");
                    pauseForUser();
            }
        }
    }
    
    /**
     * View items expiring within specified days
     */
    private void viewExpiringItems(int days) {
        System.out.println("\n⏰ ITEMS EXPIRING WITHIN " + days + " DAYS");
        System.out.println("═".repeat(90));
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT " +
                           "p.product_code, p.product_name, b.batch_number, b.expiry_date, " +
                           "SUM(COALESCE(pi.current_quantity, 0)) as total_quantity, " +
                           "julianday(b.expiry_date) - julianday('now') as days_to_expiry " +
                           "FROM product p " +
                           "JOIN batch b ON p.product_id = b.product_id " +
                           "LEFT JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                           "WHERE b.expiry_date IS NOT NULL " +
                           "AND julianday(b.expiry_date) - julianday('now') <= ? " +
                           "AND julianday(b.expiry_date) - julianday('now') >= 0 " +
                           "GROUP BY p.product_code, p.product_name, b.batch_number, b.expiry_date " +
                           "HAVING total_quantity > 0 " +
                           "ORDER BY days_to_expiry ASC";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, days);
                    ResultSet rs = stmt.executeQuery();
                    
                    System.out.printf("%-12s %-20s %-15s %-12s %-8s %-10s%n",
                        "Product Code", "Product Name", "Batch", "Expiry Date", "Quantity", "Days Left");
                    System.out.println("─".repeat(90));
                    
                    boolean hasExpiring = false;
                    while (rs.next()) {
                        hasExpiring = true;
                        
                        String productCode = rs.getString("product_code");
                        String productName = rs.getString("product_name");
                        String batchNumber = rs.getString("batch_number");
                        String expiryDate = rs.getString("expiry_date");
                        int quantity = rs.getInt("total_quantity");
                        int daysLeft = (int) rs.getDouble("days_to_expiry");
                        
                        String shortName = productName.length() > 18 ? productName.substring(0, 15) + "..." : productName;
                        String shortBatch = batchNumber.length() > 13 ? batchNumber.substring(0, 10) + "..." : batchNumber;
                        
                        System.out.printf("%-12s %-20s %-15s %-12s %8d %10d%n",
                            productCode, shortName, shortBatch, expiryDate, quantity, daysLeft);
                    }
                    
                    if (!hasExpiring) {
                        System.out.println("✅ No items expiring within " + days + " days.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error viewing expiring items: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    /**
     * Remove expired items from inventory
     */
    private void removeExpiredItems() {
        System.out.println("\n🗑️ REMOVE EXPIRED ITEMS");
        System.out.println("─".repeat(40));
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // First, show expired items
                String selectSql = "SELECT " +
                                 "p.product_code, p.product_name, b.batch_number, b.expiry_date, " +
                                 "SUM(COALESCE(pi.current_quantity, 0)) as total_quantity " +
                                 "FROM product p " +
                                 "JOIN batch b ON p.product_id = b.product_id " +
                                 "LEFT JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                                 "WHERE b.expiry_date IS NOT NULL " +
                                 "AND julianday(b.expiry_date) < julianday('now') " +
                                 "GROUP BY p.product_code, p.product_name, b.batch_number, b.expiry_date " +
                                 "HAVING total_quantity > 0";
                
                try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    System.out.println("Expired items found:");
                    System.out.printf("%-12s %-20s %-15s %-12s %-8s%n",
                        "Product Code", "Product Name", "Batch", "Expired Date", "Quantity");
                    System.out.println("─".repeat(70));
                    
                    boolean hasExpired = false;
                    while (rs.next()) {
                        hasExpired = true;
                        
                        String productCode = rs.getString("product_code");
                        String productName = rs.getString("product_name");
                        String batchNumber = rs.getString("batch_number");
                        String expiryDate = rs.getString("expiry_date");
                        int quantity = rs.getInt("total_quantity");
                        
                        String shortName = productName.length() > 18 ? productName.substring(0, 15) + "..." : productName;
                        String shortBatch = batchNumber.length() > 13 ? batchNumber.substring(0, 10) + "..." : batchNumber;
                        
                        System.out.printf("%-12s %-20s %-15s %-12s %8d%n",
                            productCode, shortName, shortBatch, expiryDate, quantity);
                    }
                    
                    if (!hasExpired) {
                        System.out.println("✅ No expired items found.");
                        pauseForUser();
                        return;
                    }
                }
                
                System.out.print("\nRemove all expired items? (y/N): ");
                String confirm = scanner.nextLine().trim().toLowerCase();
                
                if (confirm.equals("y") || confirm.equals("yes")) {
                    // Update expired inventory to 0
                    String updateSql = "UPDATE physical_inventory SET current_quantity = 0 " +
                                     "WHERE batch_id IN (" +
                                     "    SELECT b.batch_id FROM batch b " +
                                     "    WHERE b.expiry_date IS NOT NULL " +
                                     "    AND julianday(b.expiry_date) < julianday('now')" +
                                     ")";
                    
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        int updated = updateStmt.executeUpdate();
                        System.out.println("✅ Removed " + updated + " expired inventory records.");
                        
                        // Log the removal
                        logStockMovement(conn, "SYSTEM", "EXPIRED_REMOVAL", 0, 
                            "Automatic removal of expired items", "SYSTEM");
                    }
                } else {
                    System.out.println("❌ Removal cancelled.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error removing expired items: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    /**
     * View stock movements history
     */
    private void viewStockMovements() {
        System.out.println("\n📋 STOCK MOVEMENTS HISTORY");
        System.out.println("═".repeat(100));
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT " +
                           "sm.movement_date, sm.movement_type, sm.quantity, " +
                           "p.product_code, fl.location_name as from_location, " +
                           "tl.location_name as to_location, u.first_name, sm.notes " +
                           "FROM stock_movement sm " +
                           "LEFT JOIN batch b ON sm.batch_id = b.batch_id " +
                           "LEFT JOIN product p ON b.product_id = p.product_id " +
                           "LEFT JOIN inventory_location fl ON sm.from_location_id = fl.location_id " +
                           "LEFT JOIN inventory_location tl ON sm.to_location_id = tl.location_id " +
                           "LEFT JOIN user u ON sm.moved_by = u.user_id " +
                           "ORDER BY sm.movement_date DESC LIMIT 20";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    System.out.printf("%-12s %-15s %-8s %-12s %-15s %-15s %-10s%n",
                        "Date", "Type", "Quantity", "Product", "From", "To", "User");
                    System.out.println("─".repeat(100));
                    
                    boolean hasMovements = false;
                    while (rs.next()) {
                        hasMovements = true;
                        
                        String date = rs.getString("movement_date");
                        String type = rs.getString("movement_type");
                        int quantity = rs.getInt("quantity");
                        String productCode = rs.getString("product_code");
                        String fromLocation = rs.getString("from_location");
                        String toLocation = rs.getString("to_location");
                        String user = rs.getString("first_name");
                        
                        String shortDate = date != null ? date.substring(0, 10) : "N/A";
                        String shortFrom = fromLocation != null && fromLocation.length() > 13 ? 
                            fromLocation.substring(0, 10) + "..." : (fromLocation != null ? fromLocation : "N/A");
                        String shortTo = toLocation != null && toLocation.length() > 13 ? 
                            toLocation.substring(0, 10) + "..." : (toLocation != null ? toLocation : "N/A");
                        
                        System.out.printf("%-12s %-15s %8d %-12s %-15s %-15s %-10s%n",
                            shortDate, type, quantity, productCode != null ? productCode : "N/A", 
                            shortFrom, shortTo, user != null ? user : "System");
                    }
                    
                    if (!hasMovements) {
                        System.out.println("No stock movements recorded.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error viewing stock movements: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    /**
     * Create new batch with expiry date
     */
    private void createNewBatch() {
        System.out.println("\n📦 CREATE NEW BATCH");
        System.out.println("─".repeat(40));
        
        try {
            // Show available products
            System.out.println("Available products:");
            showAvailableProducts();
            
            System.out.print("Product code: ");
            String productCode = scanner.nextLine().trim().toUpperCase();
            
            System.out.print("Quantity received: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Expiry date (YYYY-MM-DD) or leave empty for 2 years: ");
            String expiryDate = scanner.nextLine().trim();
            if (expiryDate.isEmpty()) {
                expiryDate = LocalDate.now().plusYears(2).format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
            
            System.out.println("Select initial location:");
            System.out.println("1. WAREHOUSE");
            System.out.println("2. ONLINE");
            System.out.print("Location (1-2): ");
            String locationChoice = scanner.nextLine().trim();
            
            String locationCode = locationChoice.equals("1") ? "WAREHOUSE" : "ONLINE";
            
            if (createBatch(productCode, quantity, expiryDate, locationCode)) {
                System.out.println("✅ Batch created successfully!");
            } else {
                System.out.println("❌ Failed to create batch.");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error creating batch: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    // Helper methods
    
    /**
     * Display products that have existing inventory for transfer operations
     */
    private void displayProductsForTransfer() {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT DISTINCT p.product_code, p.product_name " +
                           "FROM product p " +
                           "JOIN batch b ON p.product_id = b.product_id " +
                           "JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                           "WHERE p.is_active = 1 AND pi.current_quantity > 0 " +
                           "ORDER BY p.product_code LIMIT 10";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        System.out.printf("  %s - %s%n", 
                            rs.getString("product_code"), 
                            rs.getString("product_name"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error displaying products: " + e.getMessage());
        }
    }
    
    private void showProductStock(String productCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT " +
                           "il.location_name, SUM(COALESCE(pi.current_quantity, 0)) as quantity " +
                           "FROM product p " +
                           "JOIN batch b ON p.product_id = b.product_id " +
                           "LEFT JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                           "LEFT JOIN inventory_location il ON pi.location_id = il.location_id " +
                           "WHERE p.product_code = ? " +
                           "GROUP BY il.location_name";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, productCode);
                    ResultSet rs = stmt.executeQuery();
                    
                    System.out.println("\nCurrent stock levels for " + productCode + ":");
                    while (rs.next()) {
                        System.out.printf("  %s: %d units%n", 
                            rs.getString("location_name"), 
                            rs.getInt("quantity"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error showing product stock: " + e.getMessage());
        }
    }
    
    private String getLocationCode(String choice) {
        switch (choice) {
            case "1": return "WAREHOUSE";
            case "2": return "SHELF";
            case "3": return "ONLINE";
            default: return null;
        }
    }
    
    private boolean transferStock(String productCode, String fromLocation, String toLocation, 
                                int quantity, String reason) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                conn.setAutoCommit(false);
                
                // Get location IDs
                Long fromLocationId = getLocationId(conn, fromLocation);
                Long toLocationId = getLocationId(conn, toLocation);
                Long productId = getProductId(conn, productCode);
                
                if (fromLocationId == null || toLocationId == null || productId == null) {
                    conn.rollback();
                    return false;
                }
                
                // Check available quantity
                int availableQty = getStockAtLocation(conn, productId, fromLocationId);
                if (availableQty < quantity) {
                    System.out.println("❌ Insufficient stock. Available: " + availableQty + " units");
                    conn.rollback();
                    return false;
                }
                
                // Reduce from source
                reduceStockAtLocation(conn, productId, fromLocationId, quantity);
                
                // Add to destination
                addStockAtLocation(conn, productId, toLocationId, quantity);
                
                // Log movement
                logStockMovement(conn, productCode, "WAREHOUSE_TO_SHELF", quantity, reason, currentUser.getFirstName());
                
                conn.commit();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Transfer error: " + e.getMessage());
            return false;
        }
    }
    
    private boolean createBatch(String productCode, int quantity, String expiryDate, String locationCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                conn.setAutoCommit(false);
                
                Long productId = getProductId(conn, productCode);
                Long locationId = getLocationId(conn, locationCode);
                
                if (productId == null || locationId == null) {
                    conn.rollback();
                    return false;
                }
                
                // Get product price
                BigDecimal productPrice = getProductPrice(conn, productId);
                
                // Create batch
                String batchNumber = "B-" + productCode + "-" + System.currentTimeMillis();
                
                String insertBatchSql = "INSERT INTO batch (product_id, batch_number, purchase_date, expiry_date, quantity_received, selling_price) VALUES (?, ?, ?, ?, ?, ?)";
                Long batchId;
                
                try (PreparedStatement stmt = conn.prepareStatement(insertBatchSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setLong(1, productId);
                    stmt.setString(2, batchNumber);
                    stmt.setString(3, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                    stmt.setString(4, expiryDate);
                    stmt.setInt(5, quantity);
                    stmt.setBigDecimal(6, productPrice);
                    
                    stmt.executeUpdate();
                    
                    ResultSet keys = stmt.getGeneratedKeys();
                    if (keys.next()) {
                        batchId = keys.getLong(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
                
                // Add to appropriate inventory table based on location
                if (locationCode.equals("ONLINE")) {
                    // Add to online_inventory table
                    String insertOnlineInventorySql = "INSERT INTO online_inventory (batch_id, available_quantity, min_threshold) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertOnlineInventorySql)) {
                        stmt.setLong(1, batchId);
                        stmt.setInt(2, quantity);
                        stmt.setInt(3, 20); // Default threshold
                        stmt.executeUpdate();
                    }
                } else {
                    // Add to physical_inventory table (WAREHOUSE)
                    String insertInventorySql = "INSERT INTO physical_inventory (batch_id, location_id, current_quantity, min_threshold, location_capacity) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertInventorySql)) {
                        stmt.setLong(1, batchId);
                        stmt.setLong(2, locationId);
                        stmt.setInt(3, quantity);
                        stmt.setInt(4, 20); // Default threshold
                        stmt.setInt(5, 200); // Default warehouse capacity
                        stmt.executeUpdate();
                    }
                }
                
                // Log the stock movement (STOCK_IN for new inventory received)
                try {
                    logStockMovement(conn, productCode, "STOCK_IN", quantity, "New batch: " + batchNumber, currentUser.getFirstName());
                } catch (SQLException e) {
                    System.out.println("Warning: Failed to log stock movement: " + e.getMessage());
                }
                
                conn.commit();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Batch creation error: " + e.getMessage());
            return false;
        }
    }
    
    private void showAvailableProducts() {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Show ALL active products, not limited to 15 and include brand info
                String sql = "SELECT product_code, product_name, brand FROM product WHERE is_active = 1 ORDER BY product_code";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        String brand = rs.getString("brand");
                        System.out.printf("  %s - %s%s%n", 
                            rs.getString("product_code"), 
                            rs.getString("product_name"),
                            brand != null && !brand.trim().isEmpty() ? " (" + brand + ")" : "");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error showing products: " + e.getMessage());
        }
    }
    
    // Database helper methods
    
    private Long getLocationId(Connection conn, String locationCode) throws SQLException {
        String sql = "SELECT location_id FROM inventory_location WHERE location_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, locationCode);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getLong("location_id") : null;
        }
    }
    
    private Long getProductId(Connection conn, String productCode) throws SQLException {
        String sql = "SELECT product_id FROM product WHERE product_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getLong("product_id") : null;
        }
    }
    
    private BigDecimal getProductPrice(Connection conn, Long productId) throws SQLException {
        String sql = "SELECT final_price FROM product WHERE product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getBigDecimal("final_price") : BigDecimal.ZERO;
        }
    }
    
    private int getStockAtLocation(Connection conn, Long productId, Long locationId) throws SQLException {
        String sql = "SELECT SUM(COALESCE(pi.current_quantity, 0)) FROM physical_inventory pi " +
                    "JOIN batch b ON pi.batch_id = b.batch_id " +
                    "WHERE b.product_id = ? AND pi.location_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            stmt.setLong(2, locationId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    private void reduceStockAtLocation(Connection conn, Long productId, Long locationId, int quantity) throws SQLException {
        String sql = "UPDATE physical_inventory SET current_quantity = current_quantity - ? " +
                    "WHERE batch_id IN (SELECT batch_id FROM batch WHERE product_id = ?) " +
                    "AND location_id = ? AND current_quantity >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setLong(2, productId);
            stmt.setLong(3, locationId);
            stmt.setInt(4, quantity);
            stmt.executeUpdate();
        }
    }
    
    private void addStockAtLocation(Connection conn, Long productId, Long locationId, int quantity) throws SQLException {
        // First try to update existing record
        String updateSql = "UPDATE physical_inventory SET current_quantity = current_quantity + ? " +
                          "WHERE batch_id IN (SELECT batch_id FROM batch WHERE product_id = ?) " +
                          "AND location_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setInt(1, quantity);
            stmt.setLong(2, productId);
            stmt.setLong(3, locationId);
            
            if (stmt.executeUpdate() == 0) {
                // No existing record, create new one with a batch
                Long batchId = getOrCreateBatch(conn, productId);
                if (batchId != null) {
                    String insertSql = "INSERT INTO physical_inventory (batch_id, location_id, current_quantity, min_threshold, location_capacity) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setLong(1, batchId);
                        insertStmt.setLong(2, locationId);
                        insertStmt.setInt(3, quantity);
                        insertStmt.setInt(4, 20);
                        insertStmt.setInt(5, 100);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }
    
    private Long getOrCreateBatch(Connection conn, Long productId) throws SQLException {
        // Try to get existing batch
        String getBatchSql = "SELECT batch_id FROM batch WHERE product_id = ? ORDER BY batch_id DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(getBatchSql)) {
            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("batch_id");
            }
        }
        
        // Create new batch if none exists
        String createBatchSql = "INSERT INTO batch (product_id, batch_number, purchase_date, expiry_date, quantity_received, selling_price) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(createBatchSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, productId);
            stmt.setString(2, "AUTO-" + System.currentTimeMillis());
            stmt.setString(3, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            stmt.setString(4, LocalDate.now().plusYears(2).format(DateTimeFormatter.ISO_LOCAL_DATE));
            stmt.setInt(5, 0);
            stmt.setBigDecimal(6, BigDecimal.ZERO);
            
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            return keys.next() ? keys.getLong(1) : null;
        }
    }
    
    private void logStockMovement(Connection conn, String productCode, String movementType, 
                                int quantity, String notes, String userName) throws SQLException {
        try {
            // Get batch_id and location_ids for the movement
            Long productId = getProductId(conn, productCode);
            if (productId == null) return;
            
            // Get first batch for this product
            String getBatchSql = "SELECT batch_id FROM batch WHERE product_id = ? LIMIT 1";
            Long batchId = null;
            try (PreparedStatement stmt = conn.prepareStatement(getBatchSql)) {
                stmt.setLong(1, productId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    batchId = rs.getLong("batch_id");
                }
            }
            
            if (batchId == null) return;
            
            // Get user ID
            Long userId = getUserId(conn, userName);
            
            // For STOCK_IN, use a simpler approach - just use "SHELF_ADJUSTMENT" type 
            // which is already allowed and makes sense for new inventory
            String actualMovementType = movementType.equals("STOCK_IN") ? "SHELF_ADJUSTMENT" : movementType;
            
            String sql = "INSERT INTO stock_movement (batch_id, from_location_id, to_location_id, movement_type, quantity, moved_by, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, batchId);
                stmt.setLong(2, 14); // From warehouse (dummy for STOCK_IN)
                stmt.setLong(3, 14); // To warehouse (or actual destination)
                stmt.setString(4, actualMovementType);
                stmt.setInt(5, quantity);
                stmt.setLong(6, userId != null ? userId : 1);
                stmt.setString(7, notes);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            // Don't fail the main operation if logging fails
            System.err.println("Warning: Failed to log stock movement: " + e.getMessage());
        }
    }
    
    private Long getUserId(Connection conn, String userName) throws SQLException {
        String sql = "SELECT user_id FROM user WHERE first_name = ? OR user_code = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userName);
            stmt.setString(2, userName);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getLong("user_id") : null;
        }
    }
    
    private void pauseForUser() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}