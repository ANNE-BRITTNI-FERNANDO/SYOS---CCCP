package com.syos.inventory.application.repository;

import com.syos.inventory.domain.entity.Category;
import com.syos.inventory.domain.entity.Subcategory;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category and Subcategory entities
 */
public interface CategoryRepository {
    
    // Category methods
    /**
     * Save a new category
     * @param category The category to save
     * @return The saved category with ID
     */
    Category saveCategory(Category category);
    
    /**
     * Update an existing category
     * @param category The category to update
     * @return The updated category
     */
    Category updateCategory(Category category);
    
    /**
     * Find category by ID
     * @param categoryId The category ID
     * @return Optional containing the category if found
     */
    Optional<Category> findCategoryById(Long categoryId);
    
    /**
     * Find category by code
     * @param categoryCode The category code
     * @return Optional containing the category if found
     */
    Optional<Category> findCategoryByCode(String categoryCode);
    
    /**
     * Find all categories
     * @return List of all categories
     */
    List<Category> findAllCategories();
    
    /**
     * Find all active categories
     * @return List of active categories
     */
    List<Category> findAllActiveCategories();
    
    /**
     * Check if category code exists
     * @param categoryCode The category code
     * @return true if code exists
     */
    boolean categoryCodeExists(String categoryCode);
    
    // Subcategory methods
    /**
     * Save a new subcategory
     * @param subcategory The subcategory to save
     * @return The saved subcategory with ID
     */
    Subcategory saveSubcategory(Subcategory subcategory);
    
    /**
     * Update an existing subcategory
     * @param subcategory The subcategory to update
     * @return The updated subcategory
     */
    Subcategory updateSubcategory(Subcategory subcategory);
    
    /**
     * Find subcategory by ID
     * @param subcategoryId The subcategory ID
     * @return Optional containing the subcategory if found
     */
    Optional<Subcategory> findSubcategoryById(Long subcategoryId);
    
    /**
     * Find subcategory by code
     * @param subcategoryCode The subcategory code
     * @return Optional containing the subcategory if found
     */
    Optional<Subcategory> findSubcategoryByCode(String subcategoryCode);
    
    /**
     * Find subcategories by category ID
     * @param categoryId The category ID
     * @return List of subcategories in the category
     */
    List<Subcategory> findSubcategoriesByCategory(Long categoryId);
    
    /**
     * Find all subcategories
     * @return List of all subcategories
     */
    List<Subcategory> findAllSubcategories();
    
    /**
     * Find all active subcategories
     * @return List of active subcategories
     */
    List<Subcategory> findAllActiveSubcategories();
    
    /**
     * Check if subcategory code exists
     * @param subcategoryCode The subcategory code
     * @return true if code exists
     */
    boolean subcategoryCodeExists(String subcategoryCode);
    
    /**
     * Generate next subcategory code for a category
     * @param categoryCode The category code
     * @return Next available subcategory code
     */
    String generateNextSubcategoryCode(String categoryCode);
}