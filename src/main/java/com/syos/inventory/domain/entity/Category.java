package com.syos.inventory.domain.entity;

import com.syos.inventory.domain.exception.ValidationException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Category domain entity representing a main product category
 */
public class Category {
    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    
    /**
     * Constructor for creating new category
     */
    public Category(String categoryCode, String categoryName, String description) {
        setCategoryCode(categoryCode);
        setCategoryName(categoryName);
        setDescription(description);
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Constructor for loading existing category from database
     */
    public Category(Long categoryId, String categoryCode, String categoryName, 
                   String description, boolean active, LocalDateTime createdAt) {
        this.categoryId = categoryId;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
    }
    
    /**
     * Update category information
     */
    public void updateInfo(String categoryName, String description) {
        setCategoryName(categoryName);
        setDescription(description);
    }
    
    /**
     * Activate category
     */
    public void activate() {
        this.active = true;
    }
    
    /**
     * Deactivate category
     */
    public void deactivate() {
        this.active = false;
    }
    
    // Validation methods
    private void setCategoryCode(String categoryCode) {
        if (categoryCode == null || categoryCode.trim().isEmpty()) {
            throw new ValidationException("Category code cannot be null or empty");
        }
        if (categoryCode.trim().length() > 10) {
            throw new ValidationException("Category code cannot exceed 10 characters");
        }
        this.categoryCode = categoryCode.trim().toUpperCase();
    }
    
    private void setCategoryName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new ValidationException("Category name cannot be null or empty");
        }
        if (categoryName.trim().length() > 100) {
            throw new ValidationException("Category name cannot exceed 100 characters");
        }
        this.categoryName = categoryName.trim();
    }
    
    private void setDescription(String description) {
        if (description != null && description.trim().length() > 1000) {
            throw new ValidationException("Description cannot exceed 1000 characters");
        }
        this.description = description == null ? null : description.trim();
    }
    
    // Getters
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryCode() {
        return categoryCode;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public String getDescription() {
        return description;
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
        Category category = (Category) obj;
        return Objects.equals(categoryId, category.categoryId) && 
               Objects.equals(categoryCode, category.categoryCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(categoryId, categoryCode);
    }
    
    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", categoryCode='" + categoryCode + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", active=" + active +
                '}';
    }
}
