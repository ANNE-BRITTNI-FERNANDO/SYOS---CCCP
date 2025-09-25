package com.syos.shared.patterns.observer;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Event class representing inventory-related events in the system.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class InventoryEvent {
    
    public enum EventType {
        STOCK_LOW,
        STOCK_OUT,
        STOCK_RESTOCKED,
        PRODUCT_SOLD,
        BATCH_EXPIRED,
        BATCH_NEAR_EXPIRY,
        PRICE_CHANGED,
        PRODUCT_CREATED,
        PRODUCT_DEACTIVATED
    }
    
    private final EventType eventType;
    private final Long productId;
    private final String productCode;
    private final String productName;
    private final Long batchId;
    private final String locationName;
    private final Integer oldQuantity;
    private final Integer newQuantity;
    private final BigDecimal oldPrice;
    private final BigDecimal newPrice;
    private final String message;
    private final LocalDateTime timestamp;
    private final Long userId;
    
    /**
     * Constructor for creating inventory events
     */
    private InventoryEvent(Builder builder) {
        this.eventType = builder.eventType;
        this.productId = builder.productId;
        this.productCode = builder.productCode;
        this.productName = builder.productName;
        this.batchId = builder.batchId;
        this.locationName = builder.locationName;
        this.oldQuantity = builder.oldQuantity;
        this.newQuantity = builder.newQuantity;
        this.oldPrice = builder.oldPrice;
        this.newPrice = builder.newPrice;
        this.message = builder.message;
        this.timestamp = LocalDateTime.now();
        this.userId = builder.userId;
    }
    
    // Getters
    public EventType getEventType() { return eventType; }
    public Long getProductId() { return productId; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public Long getBatchId() { return batchId; }
    public String getLocationName() { return locationName; }
    public Integer getOldQuantity() { return oldQuantity; }
    public Integer getNewQuantity() { return newQuantity; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public BigDecimal getNewPrice() { return newPrice; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Long getUserId() { return userId; }
    
    /**
     * Get formatted event description
     */
    public String getDescription() {
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }
        
        switch (eventType) {
            case STOCK_LOW:
                return String.format("Low stock alert for %s (%s) - Only %d units remaining at %s", 
                    productName, productCode, newQuantity, locationName);
            
            case STOCK_OUT:
                return String.format("Out of stock: %s (%s) at %s", 
                    productName, productCode, locationName);
            
            case STOCK_RESTOCKED:
                return String.format("Stock restocked: %s (%s) - %d units added at %s", 
                    productName, productCode, (newQuantity - oldQuantity), locationName);
            
            case PRODUCT_SOLD:
                return String.format("Product sold: %s (%s) - %d units sold from %s", 
                    productName, productCode, (oldQuantity - newQuantity), locationName);
            
            case BATCH_EXPIRED:
                return String.format("Batch expired: %s (%s) - Batch ID %d", 
                    productName, productCode, batchId);
            
            case BATCH_NEAR_EXPIRY:
                return String.format("Batch near expiry: %s (%s) - Batch ID %d", 
                    productName, productCode, batchId);
            
            case PRICE_CHANGED:
                return String.format("Price changed for %s (%s) - From LKR %.2f to LKR %.2f", 
                    productName, productCode, oldPrice, newPrice);
            
            case PRODUCT_CREATED:
                return String.format("New product created: %s (%s)", 
                    productName, productCode);
            
            case PRODUCT_DEACTIVATED:
                return String.format("Product deactivated: %s (%s)", 
                    productName, productCode);
            
            default:
                return String.format("Event occurred for %s (%s)", productName, productCode);
        }
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s", timestamp, eventType, getDescription());
    }
    
    /**
     * Builder class for creating InventoryEvent instances
     */
    public static class Builder {
        private EventType eventType;
        private Long productId;
        private String productCode;
        private String productName;
        private Long batchId;
        private String locationName;
        private Integer oldQuantity;
        private Integer newQuantity;
        private BigDecimal oldPrice;
        private BigDecimal newPrice;
        private String message;
        private Long userId;
        
        public Builder(EventType eventType) {
            this.eventType = eventType;
        }
        
        public Builder withProduct(Long productId, String productCode, String productName) {
            this.productId = productId;
            this.productCode = productCode;
            this.productName = productName;
            return this;
        }
        
        public Builder withBatch(Long batchId) {
            this.batchId = batchId;
            return this;
        }
        
        public Builder withLocation(String locationName) {
            this.locationName = locationName;
            return this;
        }
        
        public Builder withQuantityChange(Integer oldQuantity, Integer newQuantity) {
            this.oldQuantity = oldQuantity;
            this.newQuantity = newQuantity;
            return this;
        }
        
        public Builder withPriceChange(BigDecimal oldPrice, BigDecimal newPrice) {
            this.oldPrice = oldPrice;
            this.newPrice = newPrice;
            return this;
        }
        
        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }
        
        public Builder withUser(Long userId) {
            this.userId = userId;
            return this;
        }
        
        public InventoryEvent build() {
            return new InventoryEvent(this);
        }
    }
}