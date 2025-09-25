package com.syos.application.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Shopping Cart Service for managing customer cart operations.
 * 
 * Provides functionality for adding/removing items, quantity management,
 * discount application, and cart persistence across sessions.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class ShoppingCartService {
    
    private static final Logger LOGGER = Logger.getLogger(ShoppingCartService.class.getName());
    // Make cart storage static to persist across service instances
    private static final Map<String, Cart> userCarts = new ConcurrentHashMap<>();
    private final OnlineInventoryService inventoryService;
    
    public ShoppingCartService(OnlineInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    /**
     * Shopping Cart Item representation
     */
    public static class CartItem {
        private final String productCode;
        private final String productName;
        private final BigDecimal unitPrice;
        private int quantity;
        private final String unitOfMeasure;
        private BigDecimal lineTotal;
        
        public CartItem(String productCode, String productName, BigDecimal unitPrice, 
                       int quantity, String unitOfMeasure) {
            this.productCode = productCode;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.unitOfMeasure = unitOfMeasure;
            calculateLineTotal();
        }
        
        public void setQuantity(int quantity) {
            this.quantity = quantity;
            calculateLineTotal();
        }
        
        private void calculateLineTotal() {
            this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
        }
        
        // Getters
        public String getProductCode() { return productCode; }
        public String getProductName() { return productName; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
        public String getUnitOfMeasure() { return unitOfMeasure; }
        public BigDecimal getLineTotal() { return lineTotal; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CartItem)) return false;
            CartItem cartItem = (CartItem) o;
            return Objects.equals(productCode, cartItem.productCode);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(productCode);
        }
    }
    
    /**
     * Shopping Cart representation
     */
    public static class Cart {
        private final String sessionId;
        private final Map<String, CartItem> items = new LinkedHashMap<>();
        private BigDecimal subtotal = BigDecimal.ZERO;
        private BigDecimal totalDiscount = BigDecimal.ZERO;
        private BigDecimal finalTotal = BigDecimal.ZERO;
        private Date lastModified = new Date();
        
        public Cart(String sessionId) {
            this.sessionId = sessionId;
        }
        
        public void addItem(CartItem item) {
            CartItem existing = items.get(item.getProductCode());
            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
            } else {
                items.put(item.getProductCode(), item);
            }
            lastModified = new Date();
            recalculateTotal();
        }
        
        public void updateItemQuantity(String productCode, int newQuantity) {
            CartItem item = items.get(productCode);
            if (item != null) {
                if (newQuantity <= 0) {
                    items.remove(productCode);
                } else {
                    item.setQuantity(newQuantity);
                }
                lastModified = new Date();
                recalculateTotal();
            }
        }
        
        public void removeItem(String productCode) {
            if (items.remove(productCode) != null) {
                lastModified = new Date();
                recalculateTotal();
            }
        }
        
        public void clearCart() {
            items.clear();
            lastModified = new Date();
            recalculateTotal();
        }
        
        private void recalculateTotal() {
            subtotal = items.values().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Apply any cart-level discounts here if needed
            totalDiscount = BigDecimal.ZERO;
            finalTotal = subtotal.subtract(totalDiscount);
        }
        
        public boolean isEmpty() {
            return items.isEmpty();
        }
        
        public int getTotalItemCount() {
            return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        }
        
        // Getters
        public String getSessionId() { return sessionId; }
        public Collection<CartItem> getItems() { return items.values(); }
        public CartItem getItem(String productCode) { return items.get(productCode); }
        public BigDecimal getSubtotal() { return subtotal; }
        public BigDecimal getTotalDiscount() { return totalDiscount; }
        public BigDecimal getFinalTotal() { return finalTotal; }
        public Date getLastModified() { return lastModified; }
        public int getItemCount() { return items.size(); }
    }
    
    /**
     * Cart operation result
     */
    public static class CartOperationResult {
        private final boolean success;
        private final String message;
        private final Cart cart;
        
        public CartOperationResult(boolean success, String message, Cart cart) {
            this.success = success;
            this.message = message;
            this.cart = cart;
        }
        
        public static CartOperationResult success(String message, Cart cart) {
            return new CartOperationResult(true, message, cart);
        }
        
        public static CartOperationResult failure(String message) {
            return new CartOperationResult(false, message, null);
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Cart getCart() { return cart; }
    }
    
    /**
     * Get or create cart for session
     */
    public Cart getCart(String sessionId) {
        return userCarts.computeIfAbsent(sessionId, Cart::new);
    }
    
    /**
     * Add product to cart
     */
    public CartOperationResult addToCart(String sessionId, String productCode, int quantity) {
        try {
            // Validate product exists and has stock
            Optional<OnlineInventoryService.OnlineProduct> productOpt = 
                inventoryService.getProductByCode(productCode);
            
            if (productOpt.isEmpty()) {
                return CartOperationResult.failure("Product not found: " + productCode);
            }
            
            OnlineInventoryService.OnlineProduct product = productOpt.get();
            
            if (!product.isAvailable()) {
                return CartOperationResult.failure("Product is out of stock: " + product.getProductName());
            }
            
            if (!product.hasStock(quantity)) {
                return CartOperationResult.failure(
                    String.format("Insufficient stock. Available: %d, Requested: %d", 
                        product.getAvailableQuantity(), quantity));
            }
            
            Cart cart = getCart(sessionId);
            
            // Check if adding this quantity would exceed available stock
            CartItem existingItem = cart.getItem(productCode);
            int totalRequestedQuantity = quantity;
            if (existingItem != null) {
                totalRequestedQuantity += existingItem.getQuantity();
            }
            
            if (!product.hasStock(totalRequestedQuantity)) {
                return CartOperationResult.failure(
                    String.format("Cannot add %d items. Total would be %d, but only %d available", 
                        quantity, totalRequestedQuantity, product.getAvailableQuantity()));
            }
            
            CartItem newItem = new CartItem(
                product.getProductCode(),
                product.getProductName(),
                product.getFinalPrice(),
                quantity,
                product.getUnitOfMeasure()
            );
            
            cart.addItem(newItem);
            
            return CartOperationResult.success(
                String.format("Added %d x %s to cart", quantity, product.getProductName()), cart);
                
        } catch (Exception e) {
            LOGGER.severe("Error adding to cart: " + e.getMessage());
            return CartOperationResult.failure("Failed to add item to cart");
        }
    }
    
    /**
     * Update item quantity in cart
     */
    public CartOperationResult updateCartItem(String sessionId, String productCode, int newQuantity) {
        try {
            Cart cart = getCart(sessionId);
            CartItem item = cart.getItem(productCode);
            
            if (item == null) {
                return CartOperationResult.failure("Item not found in cart");
            }
            
            if (newQuantity <= 0) {
                cart.removeItem(productCode);
                return CartOperationResult.success("Item removed from cart", cart);
            }
            
            // Check stock availability
            Optional<OnlineInventoryService.OnlineProduct> productOpt = 
                inventoryService.getProductByCode(productCode);
            
            if (productOpt.isEmpty()) {
                return CartOperationResult.failure("Product no longer available");
            }
            
            OnlineInventoryService.OnlineProduct product = productOpt.get();
            if (!product.hasStock(newQuantity)) {
                return CartOperationResult.failure(
                    String.format("Insufficient stock. Available: %d, Requested: %d", 
                        product.getAvailableQuantity(), newQuantity));
            }
            
            cart.updateItemQuantity(productCode, newQuantity);
            return CartOperationResult.success("Cart updated successfully", cart);
            
        } catch (Exception e) {
            LOGGER.severe("Error updating cart: " + e.getMessage());
            return CartOperationResult.failure("Failed to update cart");
        }
    }
    
    /**
     * Remove item from cart
     */
    public CartOperationResult removeFromCart(String sessionId, String productCode) {
        try {
            Cart cart = getCart(sessionId);
            CartItem item = cart.getItem(productCode);
            
            if (item == null) {
                return CartOperationResult.failure("Item not found in cart");
            }
            
            cart.removeItem(productCode);
            return CartOperationResult.success(
                String.format("Removed %s from cart", item.getProductName()), cart);
                
        } catch (Exception e) {
            LOGGER.severe("Error removing from cart: " + e.getMessage());
            return CartOperationResult.failure("Failed to remove item from cart");
        }
    }
    
    /**
     * Clear entire cart
     */
    public CartOperationResult clearCart(String sessionId) {
        try {
            Cart cart = getCart(sessionId);
            cart.clearCart();
            return CartOperationResult.success("Cart cleared successfully", cart);
            
        } catch (Exception e) {
            LOGGER.severe("Error clearing cart: " + e.getMessage());
            return CartOperationResult.failure("Failed to clear cart");
        }
    }
    
    /**
     * Validate cart before checkout
     */
    public CartOperationResult validateCart(String sessionId) {
        try {
            Cart cart = getCart(sessionId);
            
            if (cart.isEmpty()) {
                return CartOperationResult.failure("Cart is empty");
            }
            
            List<String> issues = new ArrayList<>();
            
            for (CartItem item : cart.getItems()) {
                Optional<OnlineInventoryService.OnlineProduct> productOpt = 
                    inventoryService.getProductByCode(item.getProductCode());
                
                if (productOpt.isEmpty()) {
                    issues.add(item.getProductName() + " is no longer available");
                    continue;
                }
                
                OnlineInventoryService.OnlineProduct product = productOpt.get();
                if (!product.hasStock(item.getQuantity())) {
                    issues.add(String.format("%s: only %d available, but %d in cart", 
                        product.getProductName(), product.getAvailableQuantity(), item.getQuantity()));
                }
            }
            
            if (!issues.isEmpty()) {
                return CartOperationResult.failure("Cart validation failed: " + String.join(", ", issues));
            }
            
            return CartOperationResult.success("Cart is valid for checkout", cart);
            
        } catch (Exception e) {
            LOGGER.severe("Error validating cart: " + e.getMessage());
            return CartOperationResult.failure("Failed to validate cart");
        }
    }
    
    /**
     * Clean up old carts (call periodically)
     */
    public void cleanupOldCarts(long maxAgeMillis) {
        long currentTime = System.currentTimeMillis();
        userCarts.entrySet().removeIf(entry -> {
            Cart cart = entry.getValue();
            return (currentTime - cart.getLastModified().getTime()) > maxAgeMillis;
        });
    }
}