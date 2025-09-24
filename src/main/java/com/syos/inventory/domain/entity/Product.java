package com.syos.inventory.domain.entity;

import com.syos.inventory.domain.value.Money;
import com.syos.inventory.domain.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Product entity representing an inventory item
 */
public class Product {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private String category;
    private Money price;
    private int quantityInStock;
    private int minimumStockLevel;
    private String unit;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Constructor for creating new product
     */
    public Product(String sku, String name, String description, String category, 
                   Money price, int quantityInStock, int minimumStockLevel, String unit) {
        setSku(sku);
        setName(name);
        setDescription(description);
        setCategory(category);
        setPrice(price);
        setQuantityInStock(quantityInStock);
        setMinimumStockLevel(minimumStockLevel);
        setUnit(unit);
        
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor for loading existing product from database
     */
    public Product(Long id, String sku, String name, String description, String category,
                   Money price, int quantityInStock, int minimumStockLevel, String unit,
                   boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.quantityInStock = quantityInStock;
        this.minimumStockLevel = minimumStockLevel;
        this.unit = unit;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Check if product is in stock
     * @return true if quantity > 0
     */
    public boolean isInStock() {
        return quantityInStock > 0;
    }
    
    /**
     * Check if product is low stock
     * @return true if quantity <= minimum stock level
     */
    public boolean isLowStock() {
        return quantityInStock <= minimumStockLevel;
    }
    
    /**
     * Check if sufficient quantity is available
     * @param requestedQuantity Requested quantity
     * @return true if enough stock available
     */
    public boolean hasSufficientStock(int requestedQuantity) {
        return quantityInStock >= requestedQuantity;
    }
    
    /**
     * Add stock to inventory
     * @param quantity Quantity to add
     */
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity to add must be positive");
        }
        this.quantityInStock += quantity;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Remove stock from inventory
     * @param quantity Quantity to remove
     * @throws ValidationException if insufficient stock
     */
    public void removeStock(int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity to remove must be positive");
        }
        if (quantity > quantityInStock) {
            throw new ValidationException("Insufficient stock. Available: " + quantityInStock + ", Requested: " + quantity);
        }
        this.quantityInStock -= quantity;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update product information
     * @param name New name
     * @param description New description
     * @param category New category
     * @param price New price
     * @param minimumStockLevel New minimum stock level
     * @param unit New unit
     */
    public void updateInfo(String name, String description, String category, 
                          Money price, int minimumStockLevel, String unit) {
        setName(name);
        setDescription(description);
        setCategory(category);
        setPrice(price);
        setMinimumStockLevel(minimumStockLevel);
        setUnit(unit);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Calculate total value of stock
     * @return Total value (price * quantity)
     */
    public Money getTotalStockValue() {
        return price.multiply(quantityInStock);
    }
    
    // Validation methods
    private void setSku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new ValidationException("SKU cannot be null or empty");
        }
        if (sku.trim().length() > 50) {
            throw new ValidationException("SKU cannot exceed 50 characters");
        }
        this.sku = sku.trim().toUpperCase();
    }
    
    private void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Product name cannot be null or empty");
        }
        if (name.trim().length() > 200) {
            throw new ValidationException("Product name cannot exceed 200 characters");
        }
        this.name = name.trim();
    }
    
    private void setDescription(String description) {
        if (description != null && description.trim().length() > 1000) {
            throw new ValidationException("Description cannot exceed 1000 characters");
        }
        this.description = description == null ? null : description.trim();
    }
    
    private void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Category cannot be null or empty");
        }
        if (category.trim().length() > 100) {
            throw new ValidationException("Category cannot exceed 100 characters");
        }
        this.category = category.trim();
    }
    
    private void setPrice(Money price) {
        if (price == null) {
            throw new ValidationException("Price cannot be null");
        }
        if (!price.isPositive()) {
            throw new ValidationException("Price must be positive");
        }
        this.price = price;
    }
    
    private void setQuantityInStock(int quantityInStock) {
        if (quantityInStock < 0) {
            throw new ValidationException("Quantity in stock cannot be negative");
        }
        this.quantityInStock = quantityInStock;
    }
    
    private void setMinimumStockLevel(int minimumStockLevel) {
        if (minimumStockLevel < 0) {
            throw new ValidationException("Minimum stock level cannot be negative");
        }
        this.minimumStockLevel = minimumStockLevel;
    }
    
    private void setUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            throw new ValidationException("Unit cannot be null or empty");
        }
        if (unit.trim().length() > 20) {
            throw new ValidationException("Unit cannot exceed 20 characters");
        }
        this.unit = unit.trim();
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSku() {
        return sku;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public Money getPrice() {
        return price;
    }
    
    public int getQuantityInStock() {
        return quantityInStock;
    }
    
    public int getMinimumStockLevel() {
        return minimumStockLevel;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return Objects.equals(id, product.id) && Objects.equals(sku, product.sku);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, sku);
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", quantityInStock=" + quantityInStock +
                ", minimumStockLevel=" + minimumStockLevel +
                ", unit='" + unit + '\'' +
                ", active=" + active +
                '}';
    }
}