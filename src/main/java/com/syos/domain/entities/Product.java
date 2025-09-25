package com.syos.domain.entities;

import com.syos.shared.valueobjects.Money;
import com.syos.shared.valueobjects.ProductCode;
import com.syos.shared.valueobjects.Timestamp;
import java.util.Objects;

/**
 * Product entity representing a product in the SYOS inventory system.
 * 
 * This class follows Domain-Driven Design principles and encapsulates
 * all product-related business logic and rules.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class Product {
    
    private final ProductCode productCode;
    private String name;
    private String description;
    private Money price;
    private String category;
    private String brand;
    private String unit; // e.g., "kg", "piece", "liter"
    private boolean isActive;
    private Integer customShelfCapacity; // Custom capacity for this product on shelves
    private Integer reorderLevel; // Minimum quantity before reorder alert (default: 50)
    private final Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Business rule constants
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_CATEGORY_LENGTH = 50;
    private static final int MAX_BRAND_LENGTH = 50;
    private static final int MAX_UNIT_LENGTH = 20;
    
    /**
     * Creates a new Product entity.
     * This constructor is used for creating new products.
     * 
     * @param productCode the unique product code
     * @param name the product name
     * @param description the product description
     * @param price the product price
     * @param category the product category
     * @param brand the product brand
     * @param unit the unit of measurement
     * @throws IllegalArgumentException if any parameter violates business rules
     */
    public Product(ProductCode productCode, String name, String description, 
                   Money price, String category, String brand, String unit) {
        this.productCode = Objects.requireNonNull(productCode, "Product code cannot be null");
        this.createdAt = Timestamp.now();
        this.updatedAt = this.createdAt;
        this.isActive = true; // New products are active by default
        this.reorderLevel = 50; // Default reorder level
        this.customShelfCapacity = null; // No custom capacity by default
        
        setName(name);
        setDescription(description);
        setPrice(price);
        setCategory(category);
        setBrand(brand);
        setUnit(unit);
    }
    
    /**
     * Creates a new Product entity with custom shelf capacity and reorder level.
     * 
     * @param productCode the unique product code
     * @param name the product name
     * @param description the product description
     * @param price the product price
     * @param category the product category
     * @param brand the product brand
     * @param unit the unit of measurement
     * @param customShelfCapacity custom shelf capacity for this product
     * @param reorderLevel minimum quantity before reorder alert
     * @throws IllegalArgumentException if any parameter violates business rules
     */
    public Product(ProductCode productCode, String name, String description, 
                   Money price, String category, String brand, String unit,
                   Integer customShelfCapacity, Integer reorderLevel) {
        this.productCode = Objects.requireNonNull(productCode, "Product code cannot be null");
        this.createdAt = Timestamp.now();
        this.updatedAt = this.createdAt;
        this.isActive = true; // New products are active by default
        
        setName(name);
        setDescription(description);
        setPrice(price);
        setCategory(category);
        setBrand(brand);
        setUnit(unit);
        setCustomShelfCapacity(customShelfCapacity);
        setReorderLevel(reorderLevel);
    }
    
    /**
     * Reconstitutes a Product entity from persistence.
     * This constructor is used when loading products from the database.
     * 
     * @param productCode the unique product code
     * @param name the product name
     * @param description the product description
     * @param price the product price
     * @param category the product category
     * @param brand the product brand
     * @param unit the unit of measurement
     * @param isActive the active status
     * @param customShelfCapacity custom shelf capacity for this product
     * @param reorderLevel minimum quantity before reorder alert
     * @param createdAt the creation timestamp
     * @param updatedAt the last update timestamp
     */
    public Product(ProductCode productCode, String name, String description, 
                   Money price, String category, String brand, String unit,
                   boolean isActive, Integer customShelfCapacity, Integer reorderLevel,
                   Timestamp createdAt, Timestamp updatedAt) {
        this.productCode = Objects.requireNonNull(productCode, "Product code cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
        this.isActive = isActive;
        this.customShelfCapacity = customShelfCapacity;
        this.reorderLevel = reorderLevel != null ? reorderLevel : 50; // Default to 50 if null
        
        setName(name);
        setDescription(description);
        setPrice(price);
        setCategory(category);
        setBrand(brand);
        setUnit(unit);
    }
    
    /**
     * Gets the product code.
     * 
     * @return the product code
     */
    public ProductCode getProductCode() {
        return productCode;
    }
    
    /**
     * Gets the product name.
     * 
     * @return the product name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the product name.
     * 
     * @param name the product name
     * @throws IllegalArgumentException if name violates business rules
     */
    public void setName(String name) {
        Objects.requireNonNull(name, "Product name cannot be null");
        
        String trimmedName = name.trim();
        if (trimmedName.length() < MIN_NAME_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Product name must be at least %d characters long", MIN_NAME_LENGTH)
            );
        }
        
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Product name cannot exceed %d characters", MAX_NAME_LENGTH)
            );
        }
        
        this.name = trimmedName;
        updateTimestamp();
    }
    
    /**
     * Gets the product description.
     * 
     * @return the product description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the product description.
     * 
     * @param description the product description
     * @throws IllegalArgumentException if description violates business rules
     */
    public void setDescription(String description) {
        if (description != null) {
            String trimmedDescription = description.trim();
            if (trimmedDescription.length() > MAX_DESCRIPTION_LENGTH) {
                throw new IllegalArgumentException(
                    String.format("Product description cannot exceed %d characters", MAX_DESCRIPTION_LENGTH)
                );
            }
            this.description = trimmedDescription.isEmpty() ? null : trimmedDescription;
        } else {
            this.description = null;
        }
        updateTimestamp();
    }
    
    /**
     * Gets the product price.
     * 
     * @return the product price
     */
    public Money getPrice() {
        return price;
    }
    
    /**
     * Sets the product price.
     * 
     * @param price the product price
     * @throws IllegalArgumentException if price is null or invalid
     */
    public void setPrice(Money price) {
        this.price = Objects.requireNonNull(price, "Product price cannot be null");
        
        if (price.isZero()) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        
        updateTimestamp();
    }
    
    /**
     * Gets the product category.
     * 
     * @return the product category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Sets the product category.
     * 
     * @param category the product category
     * @throws IllegalArgumentException if category violates business rules
     */
    public void setCategory(String category) {
        Objects.requireNonNull(category, "Product category cannot be null");
        
        String trimmedCategory = category.trim();
        if (trimmedCategory.isEmpty()) {
            throw new IllegalArgumentException("Product category cannot be empty");
        }
        
        if (trimmedCategory.length() > MAX_CATEGORY_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Product category cannot exceed %d characters", MAX_CATEGORY_LENGTH)
            );
        }
        
        this.category = trimmedCategory;
        updateTimestamp();
    }
    
    /**
     * Gets the product brand.
     * 
     * @return the product brand
     */
    public String getBrand() {
        return brand;
    }
    
    /**
     * Sets the product brand.
     * 
     * @param brand the product brand
     * @throws IllegalArgumentException if brand violates business rules
     */
    public void setBrand(String brand) {
        if (brand != null) {
            String trimmedBrand = brand.trim();
            if (trimmedBrand.length() > MAX_BRAND_LENGTH) {
                throw new IllegalArgumentException(
                    String.format("Product brand cannot exceed %d characters", MAX_BRAND_LENGTH)
                );
            }
            this.brand = trimmedBrand.isEmpty() ? null : trimmedBrand;
        } else {
            this.brand = null;
        }
        updateTimestamp();
    }
    
    /**
     * Gets the product unit.
     * 
     * @return the product unit
     */
    public String getUnit() {
        return unit;
    }
    
    /**
     * Sets the product unit.
     * 
     * @param unit the product unit
     * @throws IllegalArgumentException if unit violates business rules
     */
    public void setUnit(String unit) {
        Objects.requireNonNull(unit, "Product unit cannot be null");
        
        String trimmedUnit = unit.trim();
        if (trimmedUnit.isEmpty()) {
            throw new IllegalArgumentException("Product unit cannot be empty");
        }
        
        if (trimmedUnit.length() > MAX_UNIT_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Product unit cannot exceed %d characters", MAX_UNIT_LENGTH)
            );
        }
        
        this.unit = trimmedUnit;
        updateTimestamp();
    }
    
    /**
     * Checks if the product is active.
     * 
     * @return true if the product is active
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Activates the product.
     * Active products are available for sale and inventory operations.
     */
    public void activate() {
        if (!this.isActive) {
            this.isActive = true;
            updateTimestamp();
        }
    }
    
    /**
     * Deactivates the product.
     * Inactive products are not available for new sales but existing inventory remains.
     */
    public void deactivate() {
        if (this.isActive) {
            this.isActive = false;
            updateTimestamp();
        }
    }
    
    /**
     * Gets the creation timestamp.
     * 
     * @return the creation timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Gets the last update timestamp.
     * 
     * @return the last update timestamp
     */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Updates the last modification timestamp.
     * This method is called automatically when any property is changed.
     */
    private void updateTimestamp() {
        this.updatedAt = Timestamp.now();
    }
    
    /**
     * Checks if this product belongs to a specific category.
     * 
     * @param categoryName the category name to check
     * @return true if the product belongs to the specified category
     */
    public boolean belongsToCategory(String categoryName) {
        if (categoryName == null || this.category == null) {
            return false;
        }
        return this.category.equalsIgnoreCase(categoryName.trim());
    }
    
    /**
     * Checks if this product is from a specific brand.
     * 
     * @param brandName the brand name to check
     * @return true if the product is from the specified brand
     */
    public boolean isFromBrand(String brandName) {
        if (brandName == null || this.brand == null) {
            return false;
        }
        return this.brand.equalsIgnoreCase(brandName.trim());
    }
    
    /**
     * Validates if the product can be sold.
     * A product can be sold if it's active and has a valid price.
     * 
     * @return true if the product can be sold
     */
    public boolean canBeSold() {
        return isActive && price != null && !price.isZero();
    }
    
    /**
     * Gets the custom shelf capacity.
     * 
     * @return the custom shelf capacity, or null if using default capacity
     */
    public Integer getCustomShelfCapacity() {
        return customShelfCapacity;
    }
    
    /**
     * Sets the custom shelf capacity for this product.
     * 
     * @param customShelfCapacity the custom shelf capacity, or null to use default
     * @throws IllegalArgumentException if capacity is negative
     */
    public void setCustomShelfCapacity(Integer customShelfCapacity) {
        if (customShelfCapacity != null && customShelfCapacity < 0) {
            throw new IllegalArgumentException("Custom shelf capacity cannot be negative");
        }
        this.customShelfCapacity = customShelfCapacity;
        updateTimestamp();
    }
    
    /**
     * Gets the reorder level.
     * 
     * @return the reorder level
     */
    public Integer getReorderLevel() {
        return reorderLevel;
    }
    
    /**
     * Sets the reorder level for this product.
     * 
     * @param reorderLevel the minimum quantity before reorder alert
     * @throws IllegalArgumentException if reorder level is null or negative
     */
    public void setReorderLevel(Integer reorderLevel) {
        if (reorderLevel == null || reorderLevel < 0) {
            throw new IllegalArgumentException("Reorder level cannot be null or negative");
        }
        this.reorderLevel = reorderLevel;
        updateTimestamp();
    }
    
    /**
     * Checks if the product needs to be reordered based on current quantity.
     * 
     * @param currentQuantity the current total quantity in inventory
     * @return true if current quantity is at or below the reorder level
     */
    public boolean needsReorder(int currentQuantity) {
        return currentQuantity <= reorderLevel;
    }
    
    /**
     * Gets the effective shelf capacity for this product.
     * Returns custom capacity if set, otherwise a default based on category.
     * 
     * @return the effective shelf capacity
     */
    public int getEffectiveShelfCapacity() {
        if (customShelfCapacity != null) {
            return customShelfCapacity;
        }
        
        // Default capacities based on category
        if (category != null) {
            switch (category.toLowerCase()) {
                case "electronics":
                    return 20;
                case "food & beverages":
                    return 100;
                case "laundry products":
                    return 50;
                case "general items":
                    return 75;
                default:
                    return 30;
            }
        }
        return 30; // Default capacity
    }
    
    /**
     * Creates a display name for the product.
     * Format: "Name (Brand)" or just "Name" if no brand.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        if (brand != null && !brand.isEmpty()) {
            return String.format("%s (%s)", name, brand);
        }
        return name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productCode, product.productCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(productCode);
    }
    
    @Override
    public String toString() {
        return String.format("Product{code=%s, name='%s', category='%s', price=%s, active=%s}",
            productCode, name, category, price, isActive);
    }
}