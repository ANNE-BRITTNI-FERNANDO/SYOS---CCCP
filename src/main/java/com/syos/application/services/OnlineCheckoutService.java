package com.syos.application.services;

import java.sql.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Online Checkout Service for processing customer orders.
 * 
 * Handles the complete checkout process including order validation,
 * inventory updates, and bill generation using existing database tables.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class OnlineCheckoutService {
    
    private static final Logger LOGGER = Logger.getLogger(OnlineCheckoutService.class.getName());
    private final String databaseUrl;
    
    public OnlineCheckoutService(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }
    
    /**
     * Customer information for checkout
     */
    public static class CustomerInfo {
        private final String name;
        private final String email;
        private final String phone;
        private final String address;
        
        public CustomerInfo(String name, String email, String phone, String address) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.address = address;
        }
        
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
        
        public boolean isValid() {
            return name != null && !name.trim().isEmpty() &&
                   email != null && !email.trim().isEmpty() &&
                   phone != null && !phone.trim().isEmpty();
        }
    }
    
    /**
     * Checkout result with success/failure information
     */
    public static class CheckoutResult {
        private final boolean success;
        private final String message;
        private final String billNumber;
        private final BigDecimal totalAmount;
        private final java.sql.Date orderDate;
        
        private CheckoutResult(boolean success, String message, String billNumber, 
                             BigDecimal totalAmount, java.sql.Date orderDate) {
            this.success = success;
            this.message = message;
            this.billNumber = billNumber;
            this.totalAmount = totalAmount;
            this.orderDate = orderDate;
        }
        
        public static CheckoutResult success(String message, String billNumber, 
                                          BigDecimal totalAmount, java.sql.Date orderDate) {
            return new CheckoutResult(true, message, billNumber, totalAmount, orderDate);
        }
        
        public static CheckoutResult failure(String message) {
            return new CheckoutResult(false, message, null, null, null);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getBillNumber() { return billNumber; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public java.sql.Date getOrderDate() { return orderDate; }
    }
    
    /**
     * Process checkout for online order
     */
    public CheckoutResult processCheckout(String sessionId, CustomerInfo customer) {
        Connection conn = null;
        
        try {
            conn = DriverManager.getConnection(databaseUrl);
            conn.setAutoCommit(false);
            
            // Get cart from shopping cart service
            OnlineInventoryService inventoryService = new OnlineInventoryService(databaseUrl);
            ShoppingCartService cartService = new ShoppingCartService(inventoryService);
            ShoppingCartService.Cart cart = cartService.getCart(sessionId);
            
            if (cart == null || cart.getItems().isEmpty()) {
                return CheckoutResult.failure("Cart is empty or not found");
            }
            
            // Validate inventory availability
            for (ShoppingCartService.CartItem item : cart.getItems()) {
                if (!validateInventory(conn, item)) {
                    return CheckoutResult.failure("Insufficient stock for: " + item.getProductName());
                }
            }
            
            // Generate bill number
            String billNumber = generateBillNumber();
            
            // Store final total before clearing cart
            BigDecimal finalTotal = cart.getFinalTotal();
            
            // Create bill record
            Long billId = createBill(conn, billNumber, customer, cart);
            
            // Create bill items and update inventory
            for (ShoppingCartService.CartItem item : cart.getItems()) {
                createBillItem(conn, billId, item);
                updateOnlineInventory(conn, item);
            }
            
            // Clear the cart after successful checkout
            cartService.clearCart(sessionId);
            
            conn.commit();
            
            return CheckoutResult.success(
                "Order processed successfully! Bill Number: " + billNumber,
                billNumber,
                finalTotal,
                new java.sql.Date(System.currentTimeMillis())
            );
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.severe(() -> "Error during rollback: " + rollbackEx.getMessage());
                }
            }
            LOGGER.severe(() -> "Database error during checkout: " + e.getMessage());
            return CheckoutResult.failure("Checkout failed due to database error. Please try again.");
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.severe(() -> "Error during rollback: " + rollbackEx.getMessage());
                }
            }
            LOGGER.severe(() -> "Error during checkout: " + e.getMessage());
            return CheckoutResult.failure("Checkout failed. Please try again.");
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.severe(() -> "Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Create bill record in database
     */
    private Long createBill(Connection conn, String billNumber, CustomerInfo customer, 
                          ShoppingCartService.Cart cart) throws SQLException {
        String sql = "INSERT INTO bill (bill_serial_number, sales_channel_id, employee_id, customer_id, " +
                    "subtotal, total_discount, final_total, delivery_address) " +
                    "VALUES (?, 2, 1, NULL, ?, 0.00, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, billNumber);
            stmt.setBigDecimal(2, cart.getSubtotal());
            stmt.setBigDecimal(3, cart.getFinalTotal());
            stmt.setString(4, customer.getAddress());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        
        throw new SQLException("Failed to create bill");
    }
    
    /**
     * Create bill item record in database
     */
    private void createBillItem(Connection conn, Long billId, ShoppingCartService.CartItem cartItem) throws SQLException {
        Long productId = getProductId(conn, cartItem.getProductCode());
        if (productId == null) {
            throw new SQLException("Product not found: " + cartItem.getProductCode());
        }
        
        // Get or create a batch for this product (online orders use virtual batch)
        Long batchId = getOrCreateOnlineBatch(conn, productId);
        
        String sql = "INSERT INTO bill_item (bill_id, product_id, batch_id, quantity, unit_price, " +
                    "discount_percentage, discount_amount, line_total) " +
                    "VALUES (?, ?, ?, ?, ?, 0.00, 0.00, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, billId);
            stmt.setLong(2, productId);
            stmt.setLong(3, batchId);
            stmt.setInt(4, cartItem.getQuantity());
            stmt.setBigDecimal(5, cartItem.getUnitPrice());
            stmt.setBigDecimal(6, cartItem.getLineTotal());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get product ID from product code
     */
    private Long getProductId(Connection conn, String productCode) throws SQLException {
        String sql = "SELECT product_id FROM product WHERE product_code = ? AND is_active = 1";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("product_id");
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get or create an online batch for the product
     */
    private Long getOrCreateOnlineBatch(Connection conn, Long productId) throws SQLException {
        // First, try to find an existing online batch for this product
        String selectSql = "SELECT b.batch_id FROM batch b " +
                          "INNER JOIN online_inventory oi ON b.batch_id = oi.batch_id " +
                          "WHERE b.product_id = ? AND oi.available_quantity > 0 " +
                          "ORDER BY b.batch_id DESC LIMIT 1";
        
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setLong(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("batch_id");
                }
            }
        }
        
        // If no existing batch, create a new one for online sales
        String insertSql = "INSERT INTO batch (product_id, batch_number, received_date, quantity_received) " +
                          "VALUES (?, 'ONLINE-' || datetime('now', 'localtime'), datetime('now'), 1000)";
        
        try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, productId);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Long batchId = rs.getLong(1);
                    
                    // Add to online inventory
                    String onlineInventorySql = "INSERT INTO online_inventory (batch_id, available_quantity, reserved_quantity) " +
                                              "VALUES (?, 1000, 0)";
                    try (PreparedStatement onlineStmt = conn.prepareStatement(onlineInventorySql)) {
                        onlineStmt.setLong(1, batchId);
                        onlineStmt.executeUpdate();
                    }
                    
                    return batchId;
                }
            }
        }
        
        throw new SQLException("Failed to create online batch for product: " + productId);
    }
    
    /**
     * Validate inventory availability
     */
    private boolean validateInventory(Connection conn, ShoppingCartService.CartItem item) throws SQLException {
        String sql = "SELECT SUM(oi.available_quantity) as total_available " +
                    "FROM batch b " +
                    "INNER JOIN online_inventory oi ON b.batch_id = oi.batch_id " +
                    "INNER JOIN product p ON b.product_id = p.product_id " +
                    "WHERE p.product_code = ? AND p.is_active = 1";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getProductCode());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int availableQuantity = rs.getInt("total_available");
                    return availableQuantity >= item.getQuantity();
                }
            }
        }
        
        return false;
    }
    
    /**
     * Update online inventory after purchase
     */
    private void updateOnlineInventory(Connection conn, ShoppingCartService.CartItem item) throws SQLException {
        String sql = "UPDATE online_inventory " +
                    "SET available_quantity = available_quantity - ? " +
                    "WHERE batch_id IN (" +
                    "  SELECT b.batch_id FROM batch b " +
                    "  INNER JOIN product p ON b.product_id = p.product_id " +
                    "  WHERE p.product_code = ? AND available_quantity > 0" +
                    ") " +
                    "AND available_quantity >= ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getQuantity());
            stmt.setString(2, item.getProductCode());
            stmt.setInt(3, item.getQuantity());
            
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Failed to update inventory for: " + item.getProductCode());
            }
        }
    }
    
    /**
     * Generate unique bill number
     */
    private String generateBillNumber() {
        return "ON-" + System.currentTimeMillis() + "-" + 
               String.format("%04d", new Random().nextInt(10000));
    }
}