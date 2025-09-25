package com.syos.inventory.ui.console;

import com.syos.inventory.domain.entity.User;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.sql.*;

/**
 * POS Terminal UI for processing sales transactions
 * Features: Product search, cart management, checkout, receipt generation
 */
public class POSTerminalUI {
    
    private final Scanner scanner;
    private final User currentUser;
    private final Map<String, CartItem> shoppingCart;
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    // Assignment Requirement: Default reorder level for assignment compliance
    private static final int DEFAULT_REORDER_LEVEL = 50;
    
    // Transaction ID formatting
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public POSTerminalUI(Scanner scanner, User currentUser) {
        this.scanner = scanner;
        this.currentUser = currentUser;
        this.shoppingCart = new LinkedHashMap<>();
    }
    
    /**
     * Main POS Terminal entry point
     */
    public void start() {
        boolean running = true;
        
        while (running) {
            displayPOSMenu();
            System.out.print("Choose an option (1-5): ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1:
                        processSalesTransaction();
                        break;
                    case 2:
                        searchProducts();
                        break;
                    case 3:
                        printReceipts();
                        break;
                    case 4:
                        System.out.println("🔄 Refreshing reorder alerts...");
                        generateReorderAlerts();
                        displayReorderAlerts();
                        pauseForUser();
                        break;
                    case 5:
                        running = false;
                        break;
                    default:
                        System.out.println("⚠️  Invalid option. Please try again.");
                        pauseForUser();
                }
            } catch (NumberFormatException e) {
                System.out.println("⚠️  Please enter a valid number.");
                pauseForUser();
            }
        }
    }
    
    /**
     * Display main POS menu
     */
    private void displayPOSMenu() {
        clearScreen();
        System.out.println("POS TERMINAL");
        System.out.println("========================================");
        System.out.println("1. Process Sales Transaction");
        System.out.println("2. Search Products");
        System.out.println("3. Print Receipts");
        System.out.println("4. View Reorder Alerts");
        System.out.println("5. Back to Main Menu");
        System.out.println("========================================");
    }
    
    /**
     * Process Sales Transaction - Complete workflow
     */
    private void processSalesTransaction() {
        try {
            // Clear previous cart
            shoppingCart.clear();
            
            // Generate transaction ID
            String transactionId = generateTransactionId();
            
            boolean processingTransaction = true;
            
            while (processingTransaction) {
                displayTransactionInterface(transactionId);
                
                System.out.print("Enter command: ");
                String input = scanner.nextLine().trim();
                
                if (input.equalsIgnoreCase("q")) {
                    // Quit transaction
                    System.out.print("Are you sure you want to quit this transaction? (y/n): ");
                    String confirm = scanner.nextLine().trim();
                    if (confirm.equalsIgnoreCase("y")) {
                        processingTransaction = false;
                    }
                } else if (input.equalsIgnoreCase("r")) {
                    // Remove item from cart
                    removeItemFromCart();
                } else if (input.equalsIgnoreCase("c")) {
                    // Checkout
                    if (shoppingCart.isEmpty()) {
                        System.out.println("⚠️  Cart is empty. Add items before checkout.");
                        pauseForUser();
                    } else {
                        processingTransaction = !processCheckout(transactionId);
                    }
                } else if (!input.isEmpty()) {
                    // Try to add product by code
                    addProductToCart(input.toUpperCase());
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error processing transaction: " + e.getMessage());
            pauseForUser();
        }
    }
    
    /**
     * Display transaction interface with cart
     */
    private void displayTransactionInterface(String transactionId) {
        clearScreen();
        
        System.out.println("================================================================================");
        System.out.println("PROCESS SALES TRANSACTION");
        System.out.println("==================================================");
        System.out.println("Transaction Mode: PHYSICAL STORE");
        System.out.println("Cashier: " + currentUser.getFirstName() + " " + currentUser.getLastName() + " (" + currentUser.getRole() + ")");
        System.out.println("Date: " + LocalDateTime.now().format(DISPLAY_DATE_FORMAT));
        System.out.println("Transaction ID: " + transactionId);
        System.out.println();
        
        displayShoppingCart();
        
        System.out.println();
        System.out.println("COMMANDS:");
        System.out.println("• Enter PRODUCT CODE (e.g. PRD-FODR0001) to add item");
        System.out.println("• Type 'r' to remove item from cart");
        System.out.println("• Type 'c' to checkout");
        System.out.println("• Type 'q' to quit transaction");
        System.out.println();
    }
    
    /**
     * Display shopping cart
     */
    private void displayShoppingCart() {
        System.out.println("SHOPPING CART");
        System.out.println("================================================================================");
        System.out.printf("%-4s %-12s %-25s %-4s %-10s %-8s %-10s%n", 
            "No", "Code", "Item Name", "Qty", "Price", "Disc%", "Total");
        System.out.println("================================================================================");
        
        if (shoppingCart.isEmpty()) {
            System.out.println("                              No items in cart                               ");
        } else {
            int itemNo = 1;
            BigDecimal subtotal = BigDecimal.ZERO;
            BigDecimal totalDiscount = BigDecimal.ZERO;
            
            for (CartItem item : shoppingCart.values()) {
                BigDecimal itemTotal = item.getFinalPrice().multiply(new BigDecimal(item.getQuantity()));
                BigDecimal itemDiscount = item.getBasePrice().subtract(item.getFinalPrice()).multiply(new BigDecimal(item.getQuantity()));
                
                System.out.printf("%-4d %-12s %-25s %-4d LKR%-6.2f %-8.1f LKR%-7.2f%n",
                    itemNo++,
                    item.getProductCode(),
                    truncateString(item.getProductName(), 25),
                    item.getQuantity(),
                    item.getBasePrice().doubleValue(),
                    item.getDiscountPercent().doubleValue(),
                    itemTotal.doubleValue()
                );
                
                subtotal = subtotal.add(item.getBasePrice().multiply(new BigDecimal(item.getQuantity())));
                totalDiscount = totalDiscount.add(itemDiscount);
            }
            
            BigDecimal finalTotal = subtotal.subtract(totalDiscount);
            
            System.out.println("================================================================================");
            System.out.printf("Subtotal: LKR %.2f  |  Total Discount: LKR %.2f  |  FINAL TOTAL: LKR %.2f%n",
                subtotal.doubleValue(), totalDiscount.doubleValue(), finalTotal.doubleValue());
        }
        System.out.println("================================================================================");
    }
    
    /**
     * Add product to cart by product code
     */
    private void addProductToCart(String productCode) {
        try {
            Product product = getProductByCode(productCode);
            if (product == null) {
                System.out.println("❌ Product not found: " + productCode);
                pauseForUser();
                return;
            }
            
            // Check stock availability
            int shelfStock = getAvailableStock(productCode);
            int warehouseStock = getWarehouseStock(productCode);
            int totalAvailable = shelfStock + warehouseStock;
            
            if (totalAvailable <= 0) {
                System.out.println("❌ Product out of stock: " + product.getName());
                pauseForUser();
                return;
            }
            
            // Check for automatic shelf restocking using proper min_threshold from database
            if (warehouseStock > 0) {
                try {
                    Class.forName("org.sqlite.JDBC");
                    String url = "jdbc:sqlite:" + DATABASE_PATH;
                    try (Connection conn = DriverManager.getConnection(url)) {
                        // Get shelf min_threshold from database
                        String thresholdSQL = "SELECT pi.min_threshold, pi.location_capacity " +
                                            "FROM physical_inventory pi " +
                                            "JOIN batch b ON pi.batch_id = b.batch_id " +
                                            "JOIN product p ON b.product_id = p.product_id " +
                                            "JOIN inventory_location il ON pi.location_id = il.location_id " +
                                            "WHERE p.product_code = ? AND il.location_code = 'SHELF' " +
                                            "LIMIT 1";
                        
                        try (PreparedStatement stmt = conn.prepareStatement(thresholdSQL)) {
                            stmt.setString(1, productCode);
                            ResultSet rs = stmt.executeQuery();
                            
                            if (rs.next()) {
                                int minThreshold = rs.getInt("min_threshold");
                                
                                // Check if current shelf stock is at or below the reorder threshold
                                if (shelfStock <= minThreshold) {
                                    if (shelfStock == 0) {
                                        System.out.println("📋 Shelf is empty (below min threshold " + minThreshold + "). Performing automatic restocking...");
                                    } else {
                                        System.out.printf("📋 Shelf stock low (%d ≤ %d threshold). Performing automatic restocking...%n", 
                                            shelfStock, minThreshold);
                                    }
                                    
                                    checkAndSuggestShelfRestock(conn, productCode);
                                    // Re-check shelf stock after potential restocking
                                    shelfStock = getAvailableStock(productCode);
                                    warehouseStock = getWarehouseStock(productCode);
                                    totalAvailable = shelfStock + warehouseStock;
                                    System.out.println(); // Add spacing after restock message
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error during restock check: " + e.getMessage());
                }
            }
            
            // Enhanced stock display
            if (shelfStock > 0) {
                System.out.printf("📦 Stock Available - Shelf: %d units", shelfStock);
                if (warehouseStock > 0) {
                    System.out.printf(", Warehouse: %d units (Total: %d)%n", warehouseStock, totalAvailable);
                } else {
                    System.out.println();
                }
            } else if (warehouseStock > 0) {
                System.out.printf("📦 Stock Available - Shelf: 0 units, Warehouse: %d units%n", warehouseStock);
                System.out.println("ℹ️  Note: Items will need to be transferred from warehouse to shelf");
            }
            
            // Get quantity from user
            System.out.print("Enter quantity (Total Available: " + totalAvailable + "): ");
            try {
                int quantity = Integer.parseInt(scanner.nextLine().trim());
                
                if (quantity <= 0) {
                    System.out.println("⚠️  Quantity must be greater than 0.");
                    pauseForUser();
                    return;
                }
                
                if (quantity > totalAvailable) {
                    System.out.printf("⚠️  Insufficient stock. Available - Shelf: %d, Warehouse: %d, Total: %d%n", 
                        shelfStock, warehouseStock, totalAvailable);
                    pauseForUser();
                    return;
                }
                
                // Show stock allocation for large orders
                if (quantity > shelfStock && shelfStock > 0) {
                    int fromWarehouse = quantity - shelfStock;
                    System.out.printf("📋 Order allocation: %d from shelf + %d from warehouse%n", shelfStock, fromWarehouse);
                } else if (shelfStock == 0 && quantity > 0) {
                    System.out.printf("📋 Order allocation: %d units from warehouse (shelf restocking needed)%n", quantity);
                }
                
                // Add to cart or update quantity
                if (shoppingCart.containsKey(productCode)) {
                    CartItem existingItem = shoppingCart.get(productCode);
                    int newQuantity = existingItem.getQuantity() + quantity;
                    
                    if (newQuantity > totalAvailable) {
                        System.out.printf("⚠️  Total quantity would exceed available stock. Available - Shelf: %d, Warehouse: %d, Total: %d%n", 
                            shelfStock, warehouseStock, totalAvailable);
                        pauseForUser();
                        return;
                    }
                    
                    existingItem.setQuantity(newQuantity);
                    System.out.println("✅ Updated quantity for: " + product.getName());
                } else {
                    CartItem cartItem = new CartItem(productCode, product.getName(), product.getBrand(),
                        quantity, product.getBasePrice(), product.getFinalPrice(), product.getDiscountPercent());
                    shoppingCart.put(productCode, cartItem);
                    System.out.println("✅ Added to cart: " + product.getName());
                }
                
                pauseForUser();
                
            } catch (NumberFormatException e) {
                System.out.println("⚠️  Please enter a valid quantity.");
                pauseForUser();
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error adding product: " + e.getMessage());
            pauseForUser();
        }
    }
    
    /**
     * Generate meaningful transaction ID
     */
    private String generateTransactionId() {
        try {
            String dateStr = LocalDateTime.now().format(DATE_FORMAT);
            
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT COALESCE(MAX(daily_sequence), 0) + 1 as next_seq FROM sales_transaction WHERE DATE(created_date) = DATE('now')";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    if (rs.next()) {
                        int sequence = rs.getInt("next_seq");
                        return String.format("TXN-%s-%03d", dateStr, sequence);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error generating transaction ID: " + e.getMessage());
        }
        
        // Fallback to timestamp-based ID
        return "TXN-" + System.currentTimeMillis();
    }
    
    /**
     * Search products functionality
     */
    private void searchProducts() {
        clearScreen();
        System.out.println("PRODUCT SEARCH");
        System.out.println("==============================");
        System.out.print("Enter product code: ");
        String productCode = scanner.nextLine().trim().toUpperCase();
        
        try {
            Product product = getProductByCode(productCode);
            if (product != null) {
                displayProductDetails(product);
            } else {
                System.out.println("❌ Product not found: " + productCode);
            }
        } catch (Exception e) {
            System.out.println("❌ Error searching product: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    /**
     * Placeholder methods - will implement in next steps
     */
    
    private void removeItemFromCart() {
        if (shoppingCart.isEmpty()) {
            System.out.println("\n❌ Shopping cart is empty. Nothing to remove.");
            pauseForUser();
            return;
        }
        
        displayCart();
        System.out.println("\n📝 Remove Item from Cart");
        System.out.println("─".repeat(40));
        
        try {
            System.out.print("Enter product code to remove: ");
            String productCode = scanner.nextLine().trim().toUpperCase();
            
            CartItem itemToRemove = shoppingCart.get(productCode);
            
            if (itemToRemove == null) {
                System.out.println("❌ Product not found in cart.");
                pauseForUser();
                return;
            }
            
            // Check if user wants to remove all or partial quantity
            if (itemToRemove.getQuantity() > 1) {
                System.out.printf("Current quantity in cart: %d%n", itemToRemove.getQuantity());
                System.out.print("Enter quantity to remove (or 'all' for complete removal): ");
                String input = scanner.nextLine().trim();
                
                if (input.equalsIgnoreCase("all")) {
                    shoppingCart.remove(productCode);
                    System.out.printf("✅ Removed all %s from cart.%n", itemToRemove.getProductName());
                } else {
                    try {
                        int quantityToRemove = Integer.parseInt(input);
                        
                        if (quantityToRemove <= 0) {
                            System.out.println("❌ Invalid quantity. Must be greater than 0.");
                            pauseForUser();
                            return;
                        }
                        
                        if (quantityToRemove >= itemToRemove.getQuantity()) {
                            shoppingCart.remove(productCode);
                            System.out.printf("✅ Removed all %s from cart.%n", itemToRemove.getProductName());
                        } else {
                            itemToRemove.setQuantity(itemToRemove.getQuantity() - quantityToRemove);
                            System.out.printf("✅ Removed %d units of %s from cart. Remaining: %d%n", 
                                quantityToRemove, itemToRemove.getProductName(), itemToRemove.getQuantity());
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid input. Please enter a number or 'all'.");
                        pauseForUser();
                        return;
                    }
                }
            } else {
                shoppingCart.remove(productCode);
                System.out.printf("✅ Removed %s from cart.%n", itemToRemove.getProductName());
            }
            
            System.out.println();
            System.out.println("Updated cart summary:");
            displayCart();
            
        } catch (Exception e) {
            System.err.println("Error removing item from cart: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    private boolean processCheckout(String transactionId) {
        if (shoppingCart.isEmpty()) {
            System.out.println("\n❌ Shopping cart is empty. Cannot proceed with checkout.");
            pauseForUser();
            return false;
        }
        
        System.out.println("\n💳 Checkout Process");
        System.out.println("═".repeat(50));
        
        displayCart();
        
        // Calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        
        for (CartItem item : shoppingCart.values()) {
            BigDecimal lineTotal = item.getFinalPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            
            if (item.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal itemDiscount = item.getBasePrice().subtract(item.getFinalPrice())
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
                totalDiscount = totalDiscount.add(itemDiscount);
            }
        }
        
        System.out.println("\n📊 Payment Summary:");
        System.out.println("─".repeat(30));
        System.out.printf("Subtotal: LKR %.2f%n", subtotal.doubleValue());
        if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
            System.out.printf("Total Savings: LKR %.2f%n", totalDiscount.doubleValue());
        }
        System.out.printf("TOTAL AMOUNT: LKR %.2f%n", subtotal.doubleValue());
        System.out.println();
        
        // Get customer details
        System.out.print("Customer Name (or press Enter for 'Walk-in Customer'): ");
        String customerName = scanner.nextLine().trim();
        if (customerName.isEmpty()) {
            customerName = "Walk-in Customer";
        }
        
        // Cash payment processing (only payment method)
        String paymentMethod = "Cash";
        BigDecimal cashReceived = BigDecimal.ZERO;
        BigDecimal changeAmount = BigDecimal.ZERO;
        
        System.out.println("\n💰 Cash Payment Processing");
        System.out.println("─".repeat(30));
        System.out.printf("Total Amount Due: LKR %.2f%n", subtotal.doubleValue());
        
        boolean validPayment = false;
        while (!validPayment) {
            System.out.print("Enter cash received: LKR ");
            try {
                String cashInput = scanner.nextLine().trim();
                cashReceived = new BigDecimal(cashInput);
                
                if (cashReceived.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("❌ Invalid amount. Cash received must be greater than 0.");
                    continue;
                }
                
                if (cashReceived.compareTo(subtotal) < 0) {
                    BigDecimal shortage = subtotal.subtract(cashReceived);
                    System.out.printf("❌ Insufficient cash. Short by LKR %.2f%n", shortage.doubleValue());
                    continue;
                }
                
                changeAmount = cashReceived.subtract(subtotal);
                validPayment = true;
                
                System.out.printf("✅ Cash received: LKR %.2f%n", cashReceived.doubleValue());
                if (changeAmount.compareTo(BigDecimal.ZERO) > 0) {
                    System.out.printf("💵 Change to return: LKR %.2f%n", changeAmount.doubleValue());
                } else {
                    System.out.println("💯 Exact amount received - No change required");
                }
                
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid amount.");
            }
        }
        
        // Confirm transaction
        System.out.println();
        System.out.printf("Confirm transaction for LKR %.2f? (y/n): ", subtotal.doubleValue());
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("❌ Transaction cancelled.");
            pauseForUser();
            return false;
        }
        
        // Process the actual checkout
        try {
            processCheckoutTransaction(new ArrayList<>(shoppingCart.values()), paymentMethod, customerName, cashReceived, changeAmount);
            
            // Clear cart after successful checkout
            shoppingCart.clear();
            System.out.println("\n🛒 Shopping cart cleared. Ready for next transaction.");
            pauseForUser();
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Transaction failed: " + e.getMessage());
            pauseForUser();
            return false;
        }
    }
    
    private void printReceipts() {
        System.out.println("\n📄 Print Previous Receipts");
        System.out.println("═".repeat(50));
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Get recent transactions
                String sql = "SELECT st.transaction_id, st.transaction_code, st.customer_name, " +
                           "st.final_total, st.cash_received, st.change_amount, " +
                           "st.created_date, COUNT(sti.item_id) as item_count " +
                           "FROM sales_transaction st " +
                           "LEFT JOIN sales_transaction_item sti ON st.transaction_id = sti.transaction_id " +
                           "WHERE DATE(st.created_date) = DATE('now') " +
                           "GROUP BY st.transaction_id, st.transaction_code, st.customer_name, " +
                           "st.final_total, st.cash_received, st.change_amount, st.created_date " +
                           "ORDER BY st.created_date DESC LIMIT 10";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    boolean hasTransactions = false;
                    System.out.println("Today's Transactions:");
                    System.out.println("─".repeat(80));
                    System.out.printf("%-4s %-16s %-20s %-6s %-10s %-15s%n", 
                        "No.", "Transaction ID", "Customer", "Items", "Total", "Time");
                    System.out.println("─".repeat(80));
                    
                    int count = 1;
                    while (rs.next()) {
                        hasTransactions = true;
                        System.out.printf("%-4d %-16s %-20s %-6d LKR %-6.2f %-15s%n",
                            count++,
                            rs.getString("transaction_code"),
                            rs.getString("customer_name"),
                            rs.getInt("item_count"),
                            rs.getBigDecimal("final_total").doubleValue(),
                            rs.getString("created_date").substring(11, 16));
                    }
                    
                    if (!hasTransactions) {
                        System.out.println("                    No transactions found for today");
                    } else {
                        System.out.println("─".repeat(80));
                        System.out.print("\nEnter transaction number to reprint receipt (or 0 to go back): ");
                        
                        try {
                            int choice = Integer.parseInt(scanner.nextLine().trim());
                            if (choice > 0 && choice <= count - 1) {
                                reprintReceipt(conn, choice);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Returning to menu.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error accessing receipt data: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    private void reprintReceipt(Connection conn, int transactionNumber) {
        try {
            // Get the specific transaction
            String transactionSql = "SELECT st.transaction_code, st.customer_name, st.final_total, " +
                                  "st.cash_received, st.change_amount, st.created_date " +
                                  "FROM sales_transaction st " +
                                  "WHERE DATE(st.created_date) = DATE('now') " +
                                  "ORDER BY st.created_date DESC LIMIT 1 OFFSET ?";
            
            String transactionCode = "";
            String customerName = "";
            BigDecimal total = BigDecimal.ZERO;
            BigDecimal cashReceived = BigDecimal.ZERO;
            BigDecimal changeAmount = BigDecimal.ZERO;
            String date = "";
            
            try (PreparedStatement stmt = conn.prepareStatement(transactionSql)) {
                stmt.setInt(1, transactionNumber - 1);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    transactionCode = rs.getString("transaction_code");
                    customerName = rs.getString("customer_name");
                    total = rs.getBigDecimal("final_total");
                    cashReceived = rs.getBigDecimal("cash_received") != null ? 
                        rs.getBigDecimal("cash_received") : BigDecimal.ZERO;
                    changeAmount = rs.getBigDecimal("change_amount") != null ? 
                        rs.getBigDecimal("change_amount") : BigDecimal.ZERO;
                    date = rs.getString("created_date");
                }
            }
            
            // Get transaction items
            String itemsSql = "SELECT sti.product_name, sti.quantity, sti.unit_price, sti.line_total " +
                            "FROM sales_transaction_item sti " +
                            "JOIN sales_transaction st ON sti.transaction_id = st.transaction_id " +
                            "WHERE st.transaction_code = ?";
            
            List<String> receiptLines = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(itemsSql)) {
                stmt.setString(1, transactionCode);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    String line = String.format("║ %-20s %3d x %7.2f = %7.2f ║",
                        rs.getString("product_name").length() > 20 ? 
                            rs.getString("product_name").substring(0, 17) + "..." : 
                            rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("unit_price").doubleValue(),
                        rs.getBigDecimal("line_total").doubleValue());
                    receiptLines.add(line);
                }
            }
            
            // Print the receipt
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║            SYOS INVENTORY              ║");
            System.out.println("║              POS RECEIPT               ║");
            System.out.println("║             (REPRINT)                  ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ Transaction ID: " + String.format("%-22s", transactionCode) + " ║");
            System.out.println("║ Customer: " + String.format("%-27s", customerName) + " ║");
            System.out.println("║ Date: " + String.format("%-31s", date.substring(0, 16)) + " ║");
            System.out.println("║ Payment: Cash" + String.format("%29s", "") + " ║");
            System.out.println("║ Cashier: " + String.format("%-30s", currentUser.getUsername().toString()) + " ║");
            System.out.println("╠════════════════════════════════════════╣");
            
            for (String line : receiptLines) {
                System.out.println(line);
            }
            
            System.out.println("╠════════════════════════════════════════╣");
            System.out.printf("║ TOTAL: %32.2f LKR ║%n", total.doubleValue());
            if (cashReceived.compareTo(BigDecimal.ZERO) > 0) {
                System.out.printf("║ CASH RECEIVED: %24.2f LKR ║%n", cashReceived.doubleValue());
            }
            if (changeAmount.compareTo(BigDecimal.ZERO) > 0) {
                System.out.printf("║ CHANGE: %31.2f LKR ║%n", changeAmount.doubleValue());
            }
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("\n        Thank you for your business!");
            System.out.println("          Visit us again soon!");
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Error reprinting receipt: " + e.getMessage());
        }
    }
    
    /**
     * Gets total stock from both shelf and warehouse
     */
    private int getTotalStock(String productCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT COALESCE(SUM(pi.current_quantity), 0) as total_stock " +
                           "FROM product p " +
                           "LEFT JOIN batch b ON p.product_id = b.product_id " +
                           "LEFT JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                           "JOIN inventory_location il ON pi.location_id = il.location_id " +
                           "WHERE p.product_code = ? AND p.is_active = 1 " +
                           "AND il.location_code IN ('SHELF', 'WAREHOUSE')";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, productCode);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        return rs.getInt("total_stock");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting total stock: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Gets total capacity based on historical maximum stock levels and storage constraints
     */
    private int getTotalCapacity(String productCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Get maximum historical stock + location capacity as basis for total capacity
                String sql = "SELECT " +
                           "COALESCE(MAX(pi.current_quantity), 0) as max_historical_stock, " +
                           "COALESCE(MAX(pi.location_capacity), 100) as shelf_capacity " +
                           "FROM product p " +
                           "LEFT JOIN batch b ON p.product_id = b.product_id " +
                           "LEFT JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                           "WHERE p.product_code = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, productCode);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int maxHistorical = rs.getInt("max_historical_stock");
                        int shelfCapacity = rs.getInt("shelf_capacity");
                        
                        // Capacity = max(historical_peak * 1.2, shelf_capacity * 2, minimum_100)
                        int estimatedCapacity = Math.max(
                            Math.max((int)(maxHistorical * 1.2), shelfCapacity * 2), 
                            100 // Minimum capacity
                        );
                        
                        return estimatedCapacity;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting capacity: " + e.getMessage());
        }
        return 150; // Conservative default capacity
    }
    
    /**
     * Determines stock status with SMART BUSINESS LOGIC:
     * - Below 50: Always restock (safety minimum) 
     * - Above 50: Only restock if fast-moving products
     */
    private String getStockStatus(String productCode, int totalStock, int totalCapacity) {
        double stockPercentage = (double) totalStock / totalCapacity * 100;
        
        // Get sales velocity analysis
        String velocityCategory = getSalesVelocityCategory(productCode);
        boolean isFastMoving = velocityCategory.equals("FAST");
        
        // SMART RESTOCK LOGIC:
        if (totalStock < DEFAULT_REORDER_LEVEL) {
            // Below safety minimum (50) - ALWAYS restock regardless of velocity
            return "❗ REORDER NOW (" + String.format("%.1f", stockPercentage) + 
                   "% capacity, below safety minimum: " + DEFAULT_REORDER_LEVEL + 
                   ", Velocity: " + velocityCategory + ")";
        } else if (isFastMoving) {
            // Fast-moving products: Check against higher reorder levels
            int fastReorderLevel = calculateDynamicReorderLevel(productCode, totalCapacity);
            
            if (totalStock <= fastReorderLevel) {
                return "⚠️  FAST-MOVING RESTOCK (" + String.format("%.1f", stockPercentage) + 
                       "% capacity, reorder at " + fastReorderLevel + 
                       " for fast-moving items)";
            } else {
                return "✅ Good Stock (Fast-moving, " + String.format("%.1f", stockPercentage) + 
                       "% capacity, reorder at " + fastReorderLevel + ")";
            }
        } else {
            // Non-fast products above 50: NO RESTOCKING needed
            if (totalStock >= totalCapacity * 0.8) {
                return "✅ Excellent Stock (" + velocityCategory + " velocity, " + 
                       String.format("%.1f", stockPercentage) + "% capacity, no restock needed)";
            } else {
                return "✅ Stock OK (" + velocityCategory + " velocity, " + 
                       String.format("%.1f", stockPercentage) + 
                       "% capacity, above safety min: " + DEFAULT_REORDER_LEVEL + ")";
            }
        }
    }
    
    /**
     * Calculates intelligent reorder level based on sales velocity and product characteristics
     * NEW SMART LOGIC: 
     * - Below 50: Always restock (safety minimum)
     * - Above 50: Only restock if fast-moving, otherwise no need
     */
    private int calculateDynamicReorderLevel(String productCode, int totalCapacity) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Get sales velocity in last 30 days
                String salesSql = "SELECT COUNT(*) as transaction_count, " +
                                "COALESCE(SUM(sti.quantity), 0) as total_units_sold " +
                                "FROM sales_transaction_item sti " +
                                "JOIN sales_transaction st ON sti.transaction_id = st.transaction_id " +
                                "WHERE sti.product_code = ? AND st.created_date >= date('now', '-30 days')";
                
                int transactionCount = 0;
                int totalUnitsSold = 0;
                
                try (PreparedStatement stmt = conn.prepareStatement(salesSql)) {
                    stmt.setString(1, productCode);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        transactionCount = rs.getInt("transaction_count");
                        totalUnitsSold = rs.getInt("total_units_sold");
                    }
                }
                
                // Determine sales velocity category
                String velocityCategory;
                int smartReorderLevel;
                
                if (transactionCount >= 10 || totalUnitsSold >= 50) {
                    // Fast Moving: >10 transactions OR >50 units sold in 30 days
                    velocityCategory = "FAST";
                    smartReorderLevel = Math.max((int)(totalCapacity * 0.40), 60); // Higher minimum for fast
                } else if (transactionCount >= 3 || totalUnitsSold >= 15) {
                    // Medium Moving: 3-10 transactions OR 15-50 units sold in 30 days
                    velocityCategory = "MEDIUM";
                    smartReorderLevel = Math.max((int)(totalCapacity * 0.25), 35);
                } else if (transactionCount >= 1 || totalUnitsSold >= 1) {
                    // Slow Moving: 1-3 transactions OR 1-15 units sold in 30 days
                    velocityCategory = "SLOW";
                    smartReorderLevel = Math.max((int)(totalCapacity * 0.15), 25);
                } else {
                    // New Products: no sales history
                    velocityCategory = "NEW";
                    smartReorderLevel = Math.max((int)(totalCapacity * 0.20), 30);
                }
                
                // NEW SMART BUSINESS LOGIC:
                // 1. Fast-moving products: Use higher reorder levels (even above 50)
                // 2. Non-fast products: Only restock below 50 (safety minimum)
                
                if (velocityCategory.equals("FAST")) {
                    // Fast products need aggressive restocking
                    return Math.min(smartReorderLevel, Math.min(totalCapacity / 2, 120));
                } else {
                    // Slow/Medium/New products: Use safety minimum (50) as ceiling
                    // This prevents over-restocking of slow-moving items
                    return DEFAULT_REORDER_LEVEL; // Always 50 for non-fast items
                }
                
            }
        } catch (Exception e) {
            System.err.println("Error calculating dynamic reorder level: " + e.getMessage());
        }
        
        // Fallback to safety minimum
        return DEFAULT_REORDER_LEVEL;
    }
    
    /**
     * Get sales velocity category for a product
     */
    private String getSalesVelocityCategory(String productCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String salesSql = "SELECT COUNT(*) as transaction_count, " +
                                "COALESCE(SUM(sti.quantity), 0) as total_units_sold " +
                                "FROM sales_transaction_item sti " +
                                "JOIN sales_transaction st ON sti.transaction_id = st.transaction_id " +
                                "WHERE sti.product_code = ? AND st.created_date >= date('now', '-30 days')";
                
                try (PreparedStatement stmt = conn.prepareStatement(salesSql)) {
                    stmt.setString(1, productCode);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int transactionCount = rs.getInt("transaction_count");
                        int totalUnitsSold = rs.getInt("total_units_sold");
                        
                        if (transactionCount >= 10 || totalUnitsSold >= 50) {
                            return "FAST";
                        } else if (transactionCount >= 3 || totalUnitsSold >= 15) {
                            return "MEDIUM";
                        } else if (transactionCount >= 1 || totalUnitsSold >= 1) {
                            return "SLOW";
                        } else {
                            return "NEW";
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting velocity category: " + e.getMessage());
        }
        return "UNKNOWN";
    }
    
    /**
     * Calculate raw smart reorder level (without safety minimum applied)
     * Shows pure sales velocity analysis
     */
    private int calculateRawSmartReorderLevel(String productCode, int totalCapacity) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Get sales velocity in last 30 days
                String salesSql = "SELECT COUNT(*) as transaction_count, " +
                                "COALESCE(SUM(sti.quantity), 0) as total_units_sold " +
                                "FROM sales_transaction_item sti " +
                                "JOIN sales_transaction st ON sti.transaction_id = st.transaction_id " +
                                "WHERE sti.product_code = ? AND st.created_date >= date('now', '-30 days')";
                
                int transactionCount = 0;
                int totalUnitsSold = 0;
                
                try (PreparedStatement stmt = conn.prepareStatement(salesSql)) {
                    stmt.setString(1, productCode);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        transactionCount = rs.getInt("transaction_count");
                        totalUnitsSold = rs.getInt("total_units_sold");
                    }
                }
                
                // Calculate sales velocity category (RAW - no safety minimum)
                if (transactionCount >= 10 || totalUnitsSold >= 50) {
                    return Math.max((int)(totalCapacity * 0.40), 20);
                } else if (transactionCount >= 3 || totalUnitsSold >= 15) {
                    return Math.max((int)(totalCapacity * 0.25), 10);
                } else if (transactionCount >= 1 || totalUnitsSold >= 1) {
                    return Math.max((int)(totalCapacity * 0.15), 5);
                } else {
                    return Math.max((int)(totalCapacity * 0.20), 10);
                }
            }
        } catch (Exception e) {
            System.err.println("Error calculating raw smart reorder level: " + e.getMessage());
        }
        
        // Fallback
        return Math.min((int)(totalCapacity * 0.20), 30);
    }
    
    /**
     * Enhanced method to get sales velocity-based reorder recommendations
     * Analyzes sales patterns for better inventory planning
     */
    private String getReorderAnalysis(String productCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Get detailed sales analysis for last 30 days
                String sql = "SELECT COUNT(*) as transaction_count, " +
                           "COALESCE(SUM(sti.quantity), 0) as total_units_sold, " +
                           "COALESCE(AVG(sti.quantity), 0) as avg_units_per_transaction " +
                           "FROM sales_transaction_item sti " +
                           "JOIN sales_transaction st ON sti.transaction_id = st.transaction_id " +
                           "WHERE sti.product_code = ? AND st.created_date >= date('now', '-30 days')";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, productCode);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int transactionCount = rs.getInt("transaction_count");
                        int totalUnitsSold = rs.getInt("total_units_sold");
                        double avgUnitsPerTransaction = rs.getDouble("avg_units_per_transaction");
                        
                        if (transactionCount >= 10 || totalUnitsSold >= 50) {
                            return String.format("🔥 FAST MOVING (%d transactions, %d units sold, avg %.1f per sale)", 
                                transactionCount, totalUnitsSold, avgUnitsPerTransaction);
                        } else if (transactionCount >= 3 || totalUnitsSold >= 15) {
                            return String.format("📈 Medium Moving (%d transactions, %d units sold, avg %.1f per sale)", 
                                transactionCount, totalUnitsSold, avgUnitsPerTransaction);
                        } else if (transactionCount >= 1 || totalUnitsSold >= 1) {
                            return String.format("📉 Slow Moving (%d transactions, %d units sold, avg %.1f per sale)", 
                                transactionCount, totalUnitsSold, avgUnitsPerTransaction);
                        } else {
                            return "❄️  No Recent Sales (0 transactions in 30 days) - Monitor closely";
                        }
                    }
                }
            }
        } catch (Exception e) {
            return "📊 Sales analysis unavailable: " + e.getMessage();
        }
        return "📊 New Product - No sales history";
    }
    
    /**
     * Display current shopping cart contents
     */
    private void displayCart() {
        if (shoppingCart.isEmpty()) {
            System.out.println("\n🛒 Shopping Cart: Empty");
            return;
        }
        
        System.out.println("\n🛒 Current Shopping Cart:");
        System.out.println("═".repeat(80));
        System.out.printf("%-12s %-25s %-8s %-10s %-10s %-12s%n", 
            "Code", "Product Name", "Qty", "Unit Price", "Discount", "Line Total");
        System.out.println("─".repeat(80));
        
        BigDecimal cartTotal = BigDecimal.ZERO;
        int totalItems = 0;
        
        for (CartItem item : shoppingCart.values()) {
            BigDecimal lineTotal = item.getFinalPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            cartTotal = cartTotal.add(lineTotal);
            totalItems += item.getQuantity();
            
            String discountDisplay = item.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0 
                ? String.format("%.1f%%", item.getDiscountPercent().doubleValue())
                : "None";
            
            System.out.printf("%-12s %-25s %-8d LKR %-7.2f %-10s LKR %-9.2f%n",
                item.getProductCode(),
                item.getProductName().length() > 25 ? item.getProductName().substring(0, 22) + "..." : item.getProductName(),
                item.getQuantity(),
                item.getFinalPrice().doubleValue(),
                discountDisplay,
                lineTotal.doubleValue());
        }
        
        System.out.println("─".repeat(80));
        System.out.printf("Total Items: %d | Cart Total: LKR %.2f%n", totalItems, cartTotal.doubleValue());
        System.out.println("═".repeat(80));
    }
    
    /**
     * Process the actual checkout transaction with database operations
     */
    private void processCheckoutTransaction(List<CartItem> cartItems, String paymentMethod, String customerName, 
                                          BigDecimal cashReceived, BigDecimal changeAmount) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                conn.setAutoCommit(false); // Start transaction
                
                // Generate transaction ID with daily sequence
                String transactionId = generateTransactionId(conn);
                
                // Calculate total amount
                BigDecimal totalAmount = cartItems.stream()
                    .map(item -> item.getFinalPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                // Create sales transaction record
                String transactionSql = "INSERT INTO sales_transaction " +
                    "(transaction_code, daily_sequence, cashier_id, customer_name, " +
                    "subtotal, final_total, cash_received, change_amount, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                int generatedTransactionId;
                try (PreparedStatement transactionStmt = conn.prepareStatement(transactionSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    transactionStmt.setString(1, transactionId);
                    transactionStmt.setInt(2, 1); // Daily sequence
                    transactionStmt.setInt(3, 1); // Admin user ID
                    transactionStmt.setString(4, customerName);
                    transactionStmt.setBigDecimal(5, totalAmount);
                    transactionStmt.setBigDecimal(6, totalAmount);
                    transactionStmt.setBigDecimal(7, cashReceived);
                    transactionStmt.setBigDecimal(8, changeAmount);
                    transactionStmt.setString(9, "COMPLETED");
                    transactionStmt.executeUpdate();
                    
                    // Get the generated transaction ID
                    ResultSet generatedKeys = transactionStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        generatedTransactionId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Failed to get generated transaction ID");
                    }
                }
                
                // Process each cart item
                for (CartItem item : cartItems) {
                    // Add to sales transaction items
                    String itemSql = "INSERT INTO sales_transaction_item " +
                        "(transaction_id, product_code, product_name, quantity, " +
                        "unit_price, line_total, batch_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    
                    try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                        itemStmt.setInt(1, generatedTransactionId);
                        itemStmt.setString(2, item.getProductCode());
                        itemStmt.setString(3, item.getProductName());
                        itemStmt.setInt(4, item.getQuantity());
                        itemStmt.setBigDecimal(5, item.getFinalPrice());
                        itemStmt.setBigDecimal(6, item.getFinalPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                        itemStmt.setInt(7, 1); // Default batch ID - should be improved to use actual batch
                        itemStmt.executeUpdate();
                    }
                    
                    // Deduct inventory using FIFO with expiry prioritization
                    deductInventoryFIFO(conn, item.getProductCode(), item.getQuantity());
                }
                
                conn.commit(); // Complete transaction
                
                System.out.println("\n✅ Transaction completed successfully!");
                System.out.println("Transaction ID: " + transactionId);
                printReceipt(transactionId, customerName, cartItems, totalAmount, paymentMethod, cashReceived, changeAmount);
                
            } catch (SQLException e) {
                System.err.println("Transaction failed: " + e.getMessage());
                throw new RuntimeException("Transaction processing failed", e);
            }
        } catch (Exception e) {
            System.err.println("Checkout error: " + e.getMessage());
            throw new RuntimeException("Checkout processing failed", e);
        }
    }
    
    private String generateTransactionId(Connection conn) throws SQLException {
        // Use timestamp-based unique ID to avoid duplicates
        long timestamp = System.currentTimeMillis();
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        // Add microseconds for extra uniqueness
        String uniqueId = String.format("TXN-%s-%d", timeStr, timestamp % 10000);
        
        // Verify uniqueness in database
        String checkSql = "SELECT COUNT(*) FROM sales_transaction WHERE transaction_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setString(1, uniqueId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // If duplicate found, add random suffix
                uniqueId += "-" + (int)(Math.random() * 1000);
            }
        }
        
        return uniqueId;
    }
    
    private void deductInventoryFIFO(Connection conn, String productCode, int quantityToDeduct) throws SQLException {
        int remainingToDeduct = quantityToDeduct;
        
        // First, deduct from SHELF inventory (prioritizing expiry dates)
        remainingToDeduct = deductFromLocation(conn, productCode, remainingToDeduct, "SHELF");
        
        // If still need more, deduct from WAREHOUSE inventory
        if (remainingToDeduct > 0) {
            System.out.println("📦 Shelf stock insufficient, accessing warehouse inventory...");
            remainingToDeduct = deductFromLocation(conn, productCode, remainingToDeduct, "WAREHOUSE");
        }
        
        if (remainingToDeduct > 0) {
            throw new SQLException("Insufficient stock available. Could not deduct " + remainingToDeduct + " units.");
        }
        
        // Check if shelf needs restocking and suggest warehouse transfer
        checkAndSuggestShelfRestock(conn, productCode);
        
        // Create reorder alert after successful deduction
        checkAndCreateReorderAlert(conn, productCode);
    }
    
    /**
     * Helper method to deduct inventory from a specific location (SHELF or WAREHOUSE)
     */
    private int deductFromLocation(Connection conn, String productCode, int quantityToDeduct, String locationCode) throws SQLException {
        String batchSql = "SELECT pi.inventory_id, pi.batch_id, pi.current_quantity, " +
                         "b.expiry_date, b.purchase_date, il.location_name " +
                         "FROM physical_inventory pi " +
                         "JOIN batch b ON pi.batch_id = b.batch_id " +
                         "JOIN product p ON b.product_id = p.product_id " +
                         "JOIN inventory_location il ON pi.location_id = il.location_id " +
                         "WHERE p.product_code = ? AND il.location_code = ? " +
                         "AND pi.current_quantity > 0 AND b.expiry_date > DATE('now') " +
                         "ORDER BY " +
                         "  CASE WHEN b.expiry_date <= DATE('now', '+30 days') THEN 0 ELSE 1 END, " +
                         "  b.expiry_date ASC, " +
                         "  b.purchase_date ASC";
        
        int remainingToDeduct = quantityToDeduct;
        
        try (PreparedStatement stmt = conn.prepareStatement(batchSql)) {
            stmt.setString(1, productCode);
            stmt.setString(2, locationCode);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next() && remainingToDeduct > 0) {
                int inventoryId = rs.getInt("inventory_id");
                int currentQuantity = rs.getInt("current_quantity");
                String expiryDate = rs.getString("expiry_date");
                String locationName = rs.getString("location_name");
                
                int deductFromThisBatch = Math.min(remainingToDeduct, currentQuantity);
                int newQuantity = currentQuantity - deductFromThisBatch;
                
                // Update physical inventory
                String updateSql = "UPDATE physical_inventory SET current_quantity = ? WHERE inventory_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, newQuantity);
                    updateStmt.setInt(2, inventoryId);
                    updateStmt.executeUpdate();
                }
                
                System.out.printf("⚡ Deducted %d units from %s batch (expires: %s), remaining: %d%n", 
                    deductFromThisBatch, locationName, expiryDate, newQuantity);
                
                remainingToDeduct -= deductFromThisBatch;
            }
        }
        
        return remainingToDeduct;
    }
    
    private void printReceipt(String transactionId, String customerName, List<CartItem> cartItems, 
                             BigDecimal totalAmount, String paymentMethod, BigDecimal cashReceived, BigDecimal changeAmount) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            SYOS INVENTORY              ║");
        System.out.println("║              POS RECEIPT               ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Transaction ID: " + String.format("%-22s", transactionId) + " ║");
        System.out.println("║ Customer: " + String.format("%-27s", customerName) + " ║");
        System.out.println("║ Date: " + String.format("%-31s", java.time.LocalDateTime.now().toString().substring(0, 16)) + " ║");
        System.out.println("║ Payment: " + String.format("%-30s", paymentMethod) + " ║");
        System.out.println("║ Cashier: " + String.format("%-30s", currentUser.getUsername().toString()) + " ║");
        System.out.println("╠════════════════════════════════════════╣");
        
        for (CartItem item : cartItems) {
            System.out.printf("║ %-20s %3d x %7.2f = %7.2f ║%n", 
                item.getProductName().length() > 20 ? item.getProductName().substring(0, 17) + "..." : item.getProductName(),
                item.getQuantity(),
                item.getFinalPrice().doubleValue(),
                item.getFinalPrice().multiply(BigDecimal.valueOf(item.getQuantity())).doubleValue()
            );
        }
        
        System.out.println("╠════════════════════════════════════════╣");
        System.out.printf("║ TOTAL: %32.2f LKR ║%n", totalAmount.doubleValue());
        System.out.printf("║ CASH RECEIVED: %24.2f LKR ║%n", cashReceived.doubleValue());
        if (changeAmount.compareTo(BigDecimal.ZERO) > 0) {
            System.out.printf("║ CHANGE: %31.2f LKR ║%n", changeAmount.doubleValue());
        }
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("\n        Thank you for your business!");
        System.out.println("          Visit us again soon!");
        System.out.println();
    }
    
    /**
     * Utility methods
     */
    
    private void clearScreen() {
        // Simple clear screen simulation
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }
    
    private void pauseForUser() {
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Database helper methods - Real implementations
     */
    
    private Product getProductByCode(String productCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT p.product_code, p.product_name, p.brand, " +
                            "c.category_name || ' - ' || s.subcategory_name as category, " +
                            "p.base_price, p.final_price, p.discount_percentage " +
                            "FROM product p " +
                            "JOIN subcategory s ON p.subcategory_id = s.subcategory_id " +
                            "JOIN category c ON s.category_id = c.category_id " +
                            "WHERE p.product_code = ? AND p.is_active = 1";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, productCode);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        return new Product(
                            rs.getString("product_code"),
                            rs.getString("product_name"),
                            rs.getString("brand"),
                            rs.getString("category"),
                            rs.getBigDecimal("base_price"),
                            rs.getBigDecimal("final_price"),
                            rs.getBigDecimal("discount_percentage")
                        );
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving product: " + e.getMessage());
        }
        return null;
    }
    
    private int getAvailableStock(String productCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Get total available stock from shelf inventory (location_id for SHELF)
                String sql = "SELECT COALESCE(SUM(pi.current_quantity), 0) as total_stock " +
                            "FROM physical_inventory pi " +
                            "JOIN batch b ON pi.batch_id = b.batch_id " +
                            "JOIN product p ON b.product_id = p.product_id " +
                            "JOIN inventory_location il ON pi.location_id = il.location_id " +
                            "WHERE p.product_code = ? AND il.location_code = 'SHELF' " +
                            "AND b.expiry_date > DATE('now')"; // Only non-expired items
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, productCode);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        return rs.getInt("total_stock");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking stock: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Gets warehouse stock for a specific product
     */
    private int getWarehouseStock(String productCode) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Get warehouse stock
                String sql = "SELECT COALESCE(SUM(pi.current_quantity), 0) as warehouse_stock " +
                            "FROM physical_inventory pi " +
                            "JOIN batch b ON pi.batch_id = b.batch_id " +
                            "JOIN product p ON b.product_id = p.product_id " +
                            "JOIN inventory_location il ON pi.location_id = il.location_id " +
                            "WHERE p.product_code = ? AND il.location_code = 'WAREHOUSE' " +
                            "AND b.expiry_date > DATE('now')"; // Only non-expired items
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, productCode);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        return rs.getInt("warehouse_stock");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking warehouse stock: " + e.getMessage());
        }
        return 0;
    }
    
    private void displayProductDetails(Product product) {
        System.out.println();
        System.out.println("Product Found:");
        System.out.println("==================================================");
        System.out.println("Code: " + product.getCode());
        System.out.println("Name: " + product.getName());
        System.out.println("Brand: " + product.getBrand());
        System.out.println("Category: " + product.getCategory());
        System.out.printf("Price: LKR %.2f%n", product.getBasePrice().doubleValue());
        
        if (product.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            System.out.printf("Discount Price: LKR %.2f (%.1f%% off)%n", 
                product.getFinalPrice().doubleValue(), 
                product.getDiscountPercent().doubleValue());
        }
        
        System.out.println();
        System.out.println("Stock Information:");
        int stock = getAvailableStock(product.getCode());
        // Get total stock (shelf + warehouse) and capacity
        int totalStock = getTotalStock(product.getCode());
        int totalCapacity = getTotalCapacity(product.getCode());
        
        System.out.println("Shelf Stock: " + stock + " units");
        System.out.println("Total Stock: " + totalStock + " units (Shelf + Warehouse)");
        System.out.println("Estimated Capacity: " + totalCapacity + " units");
        System.out.println("Storage Location: Physical Shelf");
        
        // Dynamic stock status with intelligent reorder logic
        String status = getStockStatus(product.getCode(), totalStock, totalCapacity);
        System.out.println("Stock Status: " + status);
        
        // Sales velocity analysis for better inventory planning
        String reorderAnalysis = getReorderAnalysis(product.getCode());
        System.out.println("Sales Velocity: " + reorderAnalysis);
        
        // Display reorder level analysis
        System.out.println();
        System.out.println("Reorder Level Analysis:");
        System.out.println("─".repeat(40));
        
        // Calculate raw smart recommendation (without safety minimum)
        int rawSmartReorder = calculateRawSmartReorderLevel(product.getCode(), totalCapacity);
        
        // Final recommendation (with 50-unit safety minimum)
        int finalReorder = calculateDynamicReorderLevel(product.getCode(), totalCapacity);
        
        System.out.println("📊 Raw Smart Analysis: " + rawSmartReorder + " units (based on sales velocity)");
        System.out.println("📋 Safety Minimum: " + DEFAULT_REORDER_LEVEL + " units (business requirement)");
        System.out.println("🎯 Final Recommendation: " + finalReorder + " units");
        
        if (finalReorder > rawSmartReorder) {
            System.out.println("� Using safety minimum - slow moving product protected");
        } else {
            System.out.println("� Using smart calculation - fast moving product optimized");
        }
        System.out.println();
    }
    
    /**
     * Check current stock levels and create reorder alerts in the database
     * This utilizes the reorder_alert table from the database schema
     */
    private void checkAndCreateReorderAlert(Connection conn, String productCode) {
        try {
            // Get product ID and location ID for shelf
            String productSql = "SELECT p.product_id FROM product p WHERE p.product_code = ?";
            String locationSql = "SELECT location_id FROM inventory_location WHERE location_code = 'SHELF'";
            
            int productId = 0;
            int shelfLocationId = 0;
            
            // Get product ID
            try (PreparedStatement stmt = conn.prepareStatement(productSql)) {
                stmt.setString(1, productCode);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    productId = rs.getInt("product_id");
                }
            }
            
            // Get shelf location ID
            try (PreparedStatement stmt = conn.prepareStatement(locationSql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    shelfLocationId = rs.getInt("location_id");
                }
            }
            
            if (productId == 0 || shelfLocationId == 0) {
                return; // Product or location not found
            }
            
            // Get current stock level and reorder threshold
            int currentStock = getTotalStock(productCode);
            int totalCapacity = getTotalCapacity(productCode);
            int reorderLevel = calculateDynamicReorderLevel(productCode, totalCapacity);
            String velocityCategory = getSalesVelocityCategory(productCode);
            
            // Determine if reorder alert is needed
            boolean needsAlert = false;
            String alertType = "";
            
            if (currentStock < DEFAULT_REORDER_LEVEL) {
                // Below safety minimum - always needs alert
                needsAlert = true;
                alertType = "SHELF_RESTOCK";
            } else if (velocityCategory.equals("FAST") && currentStock <= reorderLevel) {
                // Fast-moving products need aggressive restocking
                needsAlert = true;
                alertType = "NEW_BATCH_ORDER";
            }
            
            if (needsAlert) {
                // Check if alert already exists (avoid duplicates)
                String checkAlertSql = "SELECT COUNT(*) FROM reorder_alert " +
                                     "WHERE product_id = ? AND location_id = ? " +
                                     "AND DATE(created_at) = DATE('now')";
                
                boolean alertExists = false;
                try (PreparedStatement stmt = conn.prepareStatement(checkAlertSql)) {
                    stmt.setInt(1, productId);
                    stmt.setInt(2, shelfLocationId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        alertExists = true;
                    }
                }
                
                // Create new alert if doesn't exist
                if (!alertExists) {
                    String insertAlertSql = "INSERT INTO reorder_alert " +
                                          "(product_id, location_id, current_quantity, alert_type) " +
                                          "VALUES (?, ?, ?, ?)";
                    
                    try (PreparedStatement stmt = conn.prepareStatement(insertAlertSql)) {
                        stmt.setInt(1, productId);
                        stmt.setInt(2, shelfLocationId);
                        stmt.setInt(3, currentStock);
                        stmt.setString(4, alertType);
                        
                        int rowsInserted = stmt.executeUpdate();
                        if (rowsInserted > 0) {
                            System.out.println("🚨 REORDER ALERT CREATED:");
                            System.out.println("   Product: " + productCode);
                            System.out.println("   Current Stock: " + currentStock + " units");
                            System.out.println("   Velocity: " + velocityCategory);
                            System.out.println("   Alert Type: " + alertType);
                            System.out.println("   Reorder Level: " + reorderLevel + " units");
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error creating reorder alert: " + e.getMessage());
        }
    }
    
    /**
     * Check if shelf needs restocking and perform automatic transfer from warehouse using proper reorder thresholds
     */
    private void checkAndSuggestShelfRestock(Connection conn, String productCode) {
        try {
            // Get shelf inventory details with proper min_threshold and location_capacity
            String shelfInfoSQL = "SELECT pi.current_quantity, pi.min_threshold, pi.location_capacity " +
                                "FROM physical_inventory pi " +
                                "JOIN batch b ON pi.batch_id = b.batch_id " +
                                "JOIN product p ON b.product_id = p.product_id " +
                                "JOIN inventory_location il ON pi.location_id = il.location_id " +
                                "WHERE p.product_code = ? AND il.location_code = 'SHELF' " +
                                "LIMIT 1";
            
            int shelfStock = 0;
            int minThreshold = 0;
            int shelfCapacity = 0;
            
            try (PreparedStatement stmt = conn.prepareStatement(shelfInfoSQL)) {
                stmt.setString(1, productCode);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    shelfStock = rs.getInt("current_quantity");
                    minThreshold = rs.getInt("min_threshold");
                    shelfCapacity = rs.getInt("location_capacity");
                }
            }
            
            // Get warehouse stock
            int warehouseStock = getWarehouseStock(productCode);
            
            // Check if shelf stock is below its min_threshold and warehouse has stock
            if (shelfStock <= minThreshold && warehouseStock > 0) {
                // Calculate transfer amount to reach shelf capacity (not exceeding it)
                int transferAmount = Math.min(shelfCapacity - shelfStock, warehouseStock);
                
                if (transferAmount > 0) {
                    performWarehouseToShelfTransfer(conn, productCode, transferAmount);
                    
                    System.out.println("📦 SHELF RESTOCKING PERFORMED:");
                    System.out.printf("   Product: %s%n", productCode);
                    System.out.printf("   Shelf was below minimum threshold (%d ≤ %d)%n", shelfStock, minThreshold);
                    System.out.printf("   Transferred: %d units from warehouse to shelf%n", transferAmount);
                    System.out.printf("   New shelf stock: %d units (Capacity: %d units)%n", 
                        shelfStock + transferAmount, shelfCapacity);
                }
            }
        } catch (Exception e) {
            System.err.println("Error during shelf restock check: " + e.getMessage());
        }
    }
    
    /**
     * Perform warehouse to shelf transfer
     */
    private void performWarehouseToShelfTransfer(Connection conn, String productCode, int transferAmount) throws SQLException {
        // Get product ID
        String getProductIdSql = "SELECT product_id FROM product WHERE product_code = ?";
        int productId;
        
        try (PreparedStatement stmt = conn.prepareStatement(getProductIdSql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Product not found: " + productCode);
            }
            productId = rs.getInt("product_id");
        }
        
        // Get location IDs
        String getLocationsSql = "SELECT location_id, location_code FROM inventory_location WHERE location_code IN ('SHELF', 'WAREHOUSE')";
        int shelfLocationId = -1, warehouseLocationId = -1;
        
        try (PreparedStatement stmt = conn.prepareStatement(getLocationsSql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String code = rs.getString("location_code");
                int locationId = rs.getInt("location_id");
                if ("SHELF".equals(code)) {
                    shelfLocationId = locationId;
                } else if ("WAREHOUSE".equals(code)) {
                    warehouseLocationId = locationId;
                }
            }
        }
        
        // Transfer stock from warehouse to shelf (FIFO - oldest expiry first)
        String transferSql = "SELECT pi_warehouse.inventory_id as warehouse_inventory_id, " +
                            "pi_shelf.inventory_id as shelf_inventory_id, " +
                            "pi_warehouse.batch_id, " +
                            "pi_warehouse.current_quantity as warehouse_qty, " +
                            "COALESCE(pi_shelf.current_quantity, 0) as shelf_qty, " +
                            "b.expiry_date " +
                            "FROM physical_inventory pi_warehouse " +
                            "JOIN batch b ON pi_warehouse.batch_id = b.batch_id " +
                            "LEFT JOIN physical_inventory pi_shelf ON b.batch_id = pi_shelf.batch_id AND pi_shelf.location_id = ? " +
                            "WHERE b.product_id = ? AND pi_warehouse.location_id = ? " +
                            "AND pi_warehouse.current_quantity > 0 " +
                            "AND b.expiry_date > DATE('now') " +
                            "ORDER BY b.expiry_date ASC, b.purchase_date ASC";
        
        int remainingToTransfer = transferAmount;
        
        try (PreparedStatement stmt = conn.prepareStatement(transferSql)) {
            stmt.setInt(1, shelfLocationId);
            stmt.setInt(2, productId);
            stmt.setInt(3, warehouseLocationId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next() && remainingToTransfer > 0) {
                int warehouseInventoryId = rs.getInt("warehouse_inventory_id");
                int shelfInventoryId = rs.getInt("shelf_inventory_id");
                int batchId = rs.getInt("batch_id");
                int warehouseQty = rs.getInt("warehouse_qty");
                int shelfQty = rs.getInt("shelf_qty");
                
                int transferFromThisBatch = Math.min(remainingToTransfer, warehouseQty);
                
                // Update warehouse stock
                String updateWarehouseSql = "UPDATE physical_inventory SET current_quantity = ? WHERE inventory_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateWarehouseSql)) {
                    updateStmt.setInt(1, warehouseQty - transferFromThisBatch);
                    updateStmt.setInt(2, warehouseInventoryId);
                    updateStmt.executeUpdate();
                }
                
                // Update or insert shelf stock
                if (shelfInventoryId > 0) {
                    // Update existing shelf inventory
                    String updateShelfSql = "UPDATE physical_inventory SET current_quantity = ? WHERE inventory_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateShelfSql)) {
                        updateStmt.setInt(1, shelfQty + transferFromThisBatch);
                        updateStmt.setInt(2, shelfInventoryId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Insert new shelf inventory record
                    String insertShelfSql = "INSERT INTO physical_inventory (batch_id, location_id, current_quantity, location_capacity) " +
                                          "VALUES (?, ?, ?, 100)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertShelfSql)) {
                        insertStmt.setInt(1, batchId);
                        insertStmt.setInt(2, shelfLocationId);
                        insertStmt.setInt(3, transferFromThisBatch);
                        insertStmt.executeUpdate();
                    }
                }
                
                // Log the stock movement to stock_movement table
                String stockMovementSql = "INSERT INTO stock_movement (batch_id, from_location_id, to_location_id, " +
                                        "movement_type, quantity, moved_by, notes) " +
                                        "VALUES (?, ?, ?, 'WAREHOUSE_TO_SHELF', ?, ?, ?)";
                try (PreparedStatement movementStmt = conn.prepareStatement(stockMovementSql)) {
                    movementStmt.setInt(1, batchId);
                    movementStmt.setInt(2, warehouseLocationId);
                    movementStmt.setInt(3, shelfLocationId);
                    movementStmt.setInt(4, transferFromThisBatch);
                    movementStmt.setInt(5, currentUser.getId().intValue()); // Using the currentUser from class field
                    movementStmt.setString(6, "Automatic warehouse-to-shelf restock");
                    movementStmt.executeUpdate();
                }
                
                remainingToTransfer -= transferFromThisBatch;
            }
        }
    }
    
    /**
     * Clean up resolved reorder alerts when stock levels have improved above reorder threshold
     */
    private void cleanupResolvedAlerts(Connection conn) throws SQLException {
        String cleanupSql = "DELETE FROM reorder_alert " +
                           "WHERE product_id IN (" +
                           "  SELECT p.product_id " +
                           "  FROM product p " +
                           "  LEFT JOIN batch b ON p.product_id = b.product_id " +
                           "  LEFT JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                           "  JOIN inventory_location il ON pi.location_id = il.location_id " +
                           "  WHERE il.location_code IN ('SHELF', 'WAREHOUSE') " +
                           "  AND b.expiry_date > DATE('now') " +
                           "  GROUP BY p.product_id " +
                           "  HAVING COALESCE(SUM(pi.current_quantity), 0) >= " + DEFAULT_REORDER_LEVEL + " " +
                           ") " +
                           "AND created_at <= DATETIME('now', '-1 hour')";
        
        try (PreparedStatement stmt = conn.prepareStatement(cleanupSql)) {
            int deletedCount = stmt.executeUpdate();
            if (deletedCount > 0) {
                System.out.println("🧹 Cleaned up " + deletedCount + " resolved reorder alert(s)");
            }
        }
    }
    
    /**
     * Display all active reorder alerts from the database
     */
    public void displayReorderAlerts() {
        System.out.println("\n🚨 ACTIVE REORDER ALERTS 🚨");
        System.out.println("╔" + "═".repeat(95) + "╗");
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Clean up resolved alerts first
                cleanupResolvedAlerts(conn);
                String alertSql = "SELECT ra.alert_id, p.product_code, p.product_name, " +
                                "ra.current_quantity, ra.alert_type, ra.created_at, " +
                                "il.location_name " +
                                "FROM reorder_alert ra " +
                                "JOIN product p ON ra.product_id = p.product_id " +
                                "JOIN inventory_location il ON ra.location_id = il.location_id " +
                                "WHERE DATE(ra.created_at) >= DATE('now', '-7 days') " +
                                "ORDER BY ra.created_at DESC";
                
                try (PreparedStatement stmt = conn.prepareStatement(alertSql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    boolean hasAlerts = false;
                    
                    // Print table header with more compact layout
                    System.out.printf("║ %-12s ║ %-15s ║ %-12s ║ %6s ║ %6s ║ %6s ║ %-10s ║ %-9s ║%n",
                        "Product Code", "Product Name", "Location", "Alert@", "Stock", "Speed", "Date", "Status");
                    System.out.println("╠═════════════╬════════════════╬═════════════╬═══════╬═══════╬═══════╬═══════════╬══════════╣");
                    
                    while (rs.next()) {
                        hasAlerts = true;
                        
                        String productCode = rs.getString("product_code");
                        String productName = rs.getString("product_name");
                        int alertQty = rs.getInt("current_quantity");
                        String createdAt = rs.getString("created_at");
                        String location = rs.getString("location_name");
                        
                        // Get current stock for updated info
                        int currentStock = getTotalStock(productCode);
                        String velocityCategory = getSalesVelocityCategory(productCode);
                        
                        // Truncate long names for table display
                        String shortProductName = productName.length() > 15 ? 
                            productName.substring(0, 12) + "..." : productName;
                        String shortLocation = location.length() > 12 ? 
                            location.substring(0, 9) + "..." : location;
                        String shortDate = createdAt.substring(5, 10); // Show MM-DD only
                        String shortVelocity = velocityCategory.substring(0, Math.min(6, velocityCategory.length()));
                        
                        // Determine status
                        String status;
                        if (currentStock < DEFAULT_REORDER_LEVEL) {
                            status = "🔴 URGENT";
                        } else if (velocityCategory.equals("FAST") && currentStock < 80) {
                            status = "🟡 CHECK";
                        } else {
                            status = "✅ GOOD";
                        }
                        
                        // Print table row with compact layout
                        System.out.printf("║ %-12s ║ %-15s ║ %-12s ║ %6d ║ %6d ║ %6s ║ %-10s ║ %-9s ║%n",
                            productCode, shortProductName, shortLocation, 
                            alertQty, currentStock, shortVelocity, shortDate, status);
                    }
                    
                    if (!hasAlerts) {
                        System.out.println("║" + " ".repeat(95) + "║");
                        System.out.println("║" + centerText("✅ No active reorder alerts in the last 7 days!", 95) + "║");
                        System.out.println("║" + centerText("All products are maintaining adequate stock levels.", 95) + "║");
                        System.out.println("║" + " ".repeat(95) + "║");
                    } else {
                        System.out.println("╠" + "═".repeat(95) + "╣");
                        System.out.println("║ Legend: 🔴 URGENT (<50) │ 🟡 CHECK (Fast <80) │ ✅ GOOD (Above threshold)" + " ".repeat(16) + "║");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error displaying reorder alerts: " + e.getMessage());
        }
        
        System.out.println("╚" + "═".repeat(95) + "╝");
    }
    
    /**
     * Center text within a given width
     */
    private String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        String leftPad = " ".repeat(padding);
        String rightPad = " ".repeat(width - text.length() - padding);
        return leftPad + text + rightPad;
    }
    
    /**
     * Inner classes for data structures
     */
    
    private static class Product {
        private String code, name, brand, category;
        private BigDecimal basePrice, finalPrice, discountPercent;
        
        public Product(String code, String name, String brand, String category, 
                      BigDecimal basePrice, BigDecimal finalPrice, BigDecimal discountPercent) {
            this.code = code;
            this.name = name;
            this.brand = brand;
            this.category = category;
            this.basePrice = basePrice;
            this.finalPrice = finalPrice;
            this.discountPercent = discountPercent;
        }
        
        // Getters
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getBrand() { return brand; }
        public String getCategory() { return category; }
        public BigDecimal getBasePrice() { return basePrice; }
        public BigDecimal getFinalPrice() { return finalPrice; }
        public BigDecimal getDiscountPercent() { return discountPercent; }
    }
    
    private static class CartItem {
        private String productCode, productName, brand;
        private int quantity;
        private BigDecimal basePrice, finalPrice, discountPercent;
        
        public CartItem(String productCode, String productName, String brand, int quantity,
                       BigDecimal basePrice, BigDecimal finalPrice, BigDecimal discountPercent) {
            this.productCode = productCode;
            this.productName = productName;
            this.brand = brand;
            this.quantity = quantity;
            this.basePrice = basePrice;
            this.finalPrice = finalPrice;
            this.discountPercent = discountPercent;
        }
        
        // Getters and setters
        public String getProductCode() { return productCode; }
        public String getProductName() { return productName; }
        public String getBrand() { return brand; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getBasePrice() { return basePrice; }
        public BigDecimal getFinalPrice() { return finalPrice; }
        public BigDecimal getDiscountPercent() { return discountPercent; }
    }

    // Generate reorder alerts based on TestBetterReorderLogic
    public void generateReorderAlerts() {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                // Clear existing product-level alerts (keep location-based ones)
                String clearSql = "DELETE FROM reorder_alert WHERE alert_type LIKE 'PRODUCT_%'";
                conn.createStatement().executeUpdate(clearSql);
                
                // Get all unique products that have physical inventory
                String productSql = "SELECT DISTINCT p.product_code FROM physical_inventory pi " +
                                  "JOIN batch b ON pi.batch_id = b.batch_id " +
                                  "JOIN product p ON b.product_id = p.product_id";
                ResultSet productRs = conn.createStatement().executeQuery(productSql);
                
                int alertsGenerated = 0;
                
                while (productRs.next()) {
                    String productCode = productRs.getString("product_code");
                    int totalStock = getTotalStock(productCode);
                    String velocity = getSalesVelocityCategory(productCode);
                    
                    String alertType = null;
                    
                    // Apply TestBetterReorderLogic rules
                    if (totalStock < DEFAULT_REORDER_LEVEL) {
                        // Critical: Always alert for products below 50 units
                        alertType = "PRODUCT_CRITICAL";
                        alertsGenerated++;
                    } else if ("FAST".equals(velocity) && totalStock < 80) {
                        // Fast-moving products between 50-80 units need consideration
                        alertType = "PRODUCT_CONSIDER";
                        alertsGenerated++;
                    }
                    
                    // Insert alert if criteria met
                    if (alertType != null) {
                        // Get the Physical Display Shelf location ID
                        String getLocationSql = "SELECT location_id FROM inventory_location WHERE location_code = 'SHELF'";
                        int shelfLocationId = 15; // Default fallback
                        
                        try (PreparedStatement locationStmt = conn.prepareStatement(getLocationSql)) {
                            ResultSet locationRs = locationStmt.executeQuery();
                            if (locationRs.next()) {
                                shelfLocationId = locationRs.getInt("location_id");
                            }
                        }
                        
                        String insertSql = "INSERT INTO reorder_alert " +
                            "(product_code, current_quantity, threshold_quantity, alert_type, status, product_id, location_id, created_at) " +
                            "VALUES (?, ?, ?, ?, 'ACTIVE', (SELECT product_id FROM product WHERE product_code = ?), ?, datetime('now'))";
                        
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, productCode);
                            insertStmt.setInt(2, totalStock);
                            insertStmt.setInt(3, "FAST".equals(velocity) ? 80 : DEFAULT_REORDER_LEVEL);
                            insertStmt.setString(4, alertType);
                            insertStmt.setString(5, productCode); // For the subquery
                            insertStmt.setInt(6, shelfLocationId); // Use correct location_id
                            insertStmt.executeUpdate();
                            
                            System.out.println("🔔 Generated " + alertType + " alert for " + productCode + 
                                             " (" + velocity + " velocity, " + totalStock + " units)");
                        }
                    }
                }
                
                System.out.println("\n✅ Generated " + alertsGenerated + " reorder alerts based on product velocity and stock levels");
            }
            
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error generating reorder alerts: " + e.getMessage());
        }
    }
}