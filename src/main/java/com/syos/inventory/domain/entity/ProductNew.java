package com.syos.inventory.domain.entity;

import com.syos.inventory.domain.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Product domain entity matching the database schema
 */
public class ProductNew {
    private Long productId;
    private String productCode;
    private String productName;
    private String description;
    private String brand;
    private BigDecimal basePrice;
    private String unitOfMeasure;
    private Long subcategoryId;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;
    private boolean active;
    private LocalDateTime createdAt;
    private Long createdBy;
    
    /**
     * Constructor for creating new product
     */
    public ProductNew(String productCode, String productName, String description, String brand,
                     BigDecimal basePrice, String unitOfMeasure, Long subcategoryId, Long createdBy) {
        // Initialize discount fields first
        this.discountPercentage = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        
        // Now set fields that may call recalculateFinalPrice()
        setProductCode(productCode);
        setProductName(productName);
        setDescription(description);
        setBrand(brand);
        setUnitOfMeasure(unitOfMeasure);
        setSubcategoryId(subcategoryId);
        setCreatedBy(createdBy);
        setBasePrice(basePrice); // This calls recalculateFinalPrice, so discount fields must be initialized
    }
    
    /**
     * Constructor for loading existing product from database
     */
    public ProductNew(Long productId, String productCode, String productName, String description,
                     String brand, BigDecimal basePrice, String unitOfMeasure, Long subcategoryId,
                     BigDecimal discountPercentage, BigDecimal discountAmount, BigDecimal finalPrice,
                     boolean active, LocalDateTime createdAt, Long createdBy) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.description = description;
        this.brand = brand;
        this.basePrice = basePrice;
        this.unitOfMeasure = unitOfMeasure;
        this.subcategoryId = subcategoryId;
        this.discountPercentage = discountPercentage;
        this.discountAmount = discountAmount;
        this.finalPrice = finalPrice;
        this.active = active;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }
    
    /**
     * Update product information
     */
    public void updateInfo(String productName, String description, String brand,
                          BigDecimal basePrice, String unitOfMeasure) {
        setProductName(productName);
        setDescription(description);
        setBrand(brand);
        setBasePrice(basePrice);
        setUnitOfMeasure(unitOfMeasure);
        recalculateFinalPrice();
    }
    
    /**
     * Set percentage discount
     */
    public void setPercentageDiscount(BigDecimal percentage) {
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) < 0 || 
            percentage.compareTo(new BigDecimal("100")) > 0) {
            throw new ValidationException("Discount percentage must be between 0 and 100");
        }
        this.discountPercentage = percentage;
        this.discountAmount = BigDecimal.ZERO;
        recalculateFinalPrice();
    }
    
    /**
     * Set fixed amount discount
     */
    public void setFixedDiscount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0 || 
            amount.compareTo(basePrice) > 0) {
            throw new ValidationException("Discount amount must be between 0 and base price");
        }
        this.discountAmount = amount;
        this.discountPercentage = BigDecimal.ZERO;
        recalculateFinalPrice();
    }
    
    /**
     * Remove discount
     */
    public void removeDiscount() {
        this.discountPercentage = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.finalPrice = this.basePrice;
    }
    
    /**
     * Recalculate final price based on discount
     */
    private void recalculateFinalPrice() {
        if (discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = basePrice.multiply(discountPercentage).divide(new BigDecimal("100"));
            this.finalPrice = basePrice.subtract(discount);
        } else if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.finalPrice = basePrice.subtract(discountAmount);
        } else {
            this.finalPrice = basePrice;
        }
    }
    
    /**
     * Check if product has discount
     */
    public boolean hasDiscount() {
        return discountPercentage.compareTo(BigDecimal.ZERO) > 0 || 
               discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Get discount description
     */
    public String getDiscountDescription() {
        if (discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savings = basePrice.subtract(finalPrice);
            return String.format("%.1f%% off (Save LKR %.2f)", discountPercentage, savings);
        } else if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            return String.format("LKR %.2f off", discountAmount);
        }
        return "No discount";
    }
    
    /**
     * Activate product
     */
    public void activate() {
        this.active = true;
    }
    
    /**
     * Deactivate product
     */
    public void deactivate() {
        this.active = false;
    }
    
    // Validation methods
    private void setProductCode(String productCode) {
        if (productCode == null || productCode.trim().isEmpty()) {
            throw new ValidationException("Product code cannot be null or empty");
        }
        if (productCode.trim().length() > 30) {
            throw new ValidationException("Product code cannot exceed 30 characters");
        }
        this.productCode = productCode.trim().toUpperCase();
    }
    
    private void setProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new ValidationException("Product name cannot be null or empty");
        }
        if (productName.trim().length() > 255) {
            throw new ValidationException("Product name cannot exceed 255 characters");
        }
        this.productName = productName.trim();
    }
    
    private void setDescription(String description) {
        if (description != null && description.trim().length() > 1000) {
            throw new ValidationException("Description cannot exceed 1000 characters");
        }
        this.description = description == null ? null : description.trim();
    }
    
    private void setBrand(String brand) {
        if (brand != null && brand.trim().length() > 100) {
            throw new ValidationException("Brand cannot exceed 100 characters");
        }
        this.brand = brand == null ? null : brand.trim();
    }
    
    private void setBasePrice(BigDecimal basePrice) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Base price must be positive");
        }
        this.basePrice = basePrice;
        recalculateFinalPrice();
    }
    
    private void setUnitOfMeasure(String unitOfMeasure) {
        if (unitOfMeasure == null || unitOfMeasure.trim().isEmpty()) {
            throw new ValidationException("Unit of measure cannot be null or empty");
        }
        if (unitOfMeasure.trim().length() > 20) {
            throw new ValidationException("Unit of measure cannot exceed 20 characters");
        }
        this.unitOfMeasure = unitOfMeasure.trim();
    }
    
    private void setSubcategoryId(Long subcategoryId) {
        if (subcategoryId == null || subcategoryId <= 0) {
            throw new ValidationException("Subcategory ID must be valid");
        }
        this.subcategoryId = subcategoryId;
    }
    
    private void setCreatedBy(Long createdBy) {
        if (createdBy == null || createdBy <= 0) {
            throw new ValidationException("Created by user ID must be valid");
        }
        this.createdBy = createdBy;
    }
    
    // Getters and setters
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getProductCode() {
        return productCode;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }
    
    public Long getSubcategoryId() {
        return subcategoryId;
    }
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public BigDecimal getFinalPrice() {
        return finalPrice;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProductNew that = (ProductNew) obj;
        return Objects.equals(productId, that.productId) && 
               Objects.equals(productCode, that.productCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(productId, productCode);
    }
    
    @Override
    public String toString() {
        return "ProductNew{" +
                "productId=" + productId +
                ", productCode='" + productCode + '\'' +
                ", productName='" + productName + '\'' +
                ", basePrice=" + basePrice +
                ", finalPrice=" + finalPrice +
                ", active=" + active +
                '}';
    }
}