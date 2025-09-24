package com.syos.inventory.domain.entity;

import com.syos.inventory.domain.exception.ValidationException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Subcategory domain entity representing a product subcategory
 */
public class Subcategory {
    private Long subcategoryId;
    private Long categoryId;
    private String subcategoryCode;
    private String subcategoryName;
    private String description;
    private int defaultShelfCapacity;
    private boolean active;
    private LocalDateTime createdAt;
    
    /**
     * Constructor for creating new subcategory
     */
    public Subcategory(Long categoryId, String subcategoryCode, String subcategoryName, 
                      String description, int defaultShelfCapacity) {
        setCategoryId(categoryId);
        setSubcategoryCode(subcategoryCode);
        setSubcategoryName(subcategoryName);
        setDescription(description);
        setDefaultShelfCapacity(defaultShelfCapacity);
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Constructor for loading existing subcategory from database
     */
    public Subcategory(Long subcategoryId, Long categoryId, String subcategoryCode, 
                      String subcategoryName, String description, int defaultShelfCapacity,
                      boolean active, LocalDateTime createdAt) {
        this.subcategoryId = subcategoryId;
        this.categoryId = categoryId;
        this.subcategoryCode = subcategoryCode;
        this.subcategoryName = subcategoryName;
        this.description = description;
        this.defaultShelfCapacity = defaultShelfCapacity;
        this.active = active;
        this.createdAt = createdAt;
    }
    
    /**
     * Update subcategory information
     */
    public void updateInfo(String subcategoryName, String description, int defaultShelfCapacity) {
        setSubcategoryName(subcategoryName);
        setDescription(description);
        setDefaultShelfCapacity(defaultShelfCapacity);
    }
    
    /**
     * Generate full subcategory code (category-subcategory format)
     */
    public String getFullCode() {
        return subcategoryCode; // Already includes category prefix
    }
    
    /**
     * Activate subcategory
     */
    public void activate() {
        this.active = true;
    }
    
    /**
     * Deactivate subcategory
     */
    public void deactivate() {
        this.active = false;
    }
    
    // Validation methods
    private void setCategoryId(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new ValidationException("Category ID must be valid");
        }
        this.categoryId = categoryId;
    }
    
    private void setSubcategoryCode(String subcategoryCode) {
        if (subcategoryCode == null || subcategoryCode.trim().isEmpty()) {
            throw new ValidationException("Subcategory code cannot be null or empty");
        }
        if (subcategoryCode.trim().length() > 20) {
            throw new ValidationException("Subcategory code cannot exceed 20 characters");
        }
        this.subcategoryCode = subcategoryCode.trim().toUpperCase();
    }
    
    private void setSubcategoryName(String subcategoryName) {
        if (subcategoryName == null || subcategoryName.trim().isEmpty()) {
            throw new ValidationException("Subcategory name cannot be null or empty");
        }
        if (subcategoryName.trim().length() > 100) {
            throw new ValidationException("Subcategory name cannot exceed 100 characters");
        }
        this.subcategoryName = subcategoryName.trim();
    }
    
    private void setDescription(String description) {
        if (description != null && description.trim().length() > 1000) {
            throw new ValidationException("Description cannot exceed 1000 characters");
        }
        this.description = description == null ? null : description.trim();
    }
    
    private void setDefaultShelfCapacity(int defaultShelfCapacity) {
        if (defaultShelfCapacity < 1) {
            throw new ValidationException("Default shelf capacity must be at least 1");
        }
        this.defaultShelfCapacity = defaultShelfCapacity;
    }
    
    // Getters
    public Long getSubcategoryId() {
        return subcategoryId;
    }
    
    public void setSubcategoryId(Long subcategoryId) {
        this.subcategoryId = subcategoryId;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public String getSubcategoryCode() {
        return subcategoryCode;
    }
    
    public String getSubcategoryName() {
        return subcategoryName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getDefaultShelfCapacity() {
        return defaultShelfCapacity;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Subcategory that = (Subcategory) obj;
        return Objects.equals(subcategoryId, that.subcategoryId) && 
               Objects.equals(subcategoryCode, that.subcategoryCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(subcategoryId, subcategoryCode);
    }
    
    @Override
    public String toString() {
        return "Subcategory{" +
                "subcategoryId=" + subcategoryId +
                ", subcategoryCode='" + subcategoryCode + '\'' +
                ", subcategoryName='" + subcategoryName + '\'' +
                ", defaultShelfCapacity=" + defaultShelfCapacity +
                ", active=" + active +
                '}';
    }
}
