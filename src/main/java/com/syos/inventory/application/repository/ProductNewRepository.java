package com.syos.inventory.application.repository;

import com.syos.inventory.domain.entity.ProductNew;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProductNew entity
 */
public interface ProductNewRepository {
    
    /**
     * Save a new product
     * @param product The product to save
     * @return The saved product with ID
     */
    ProductNew save(ProductNew product);
    
    /**
     * Update an existing product
     * @param product The product to update
     * @return The updated product
     */
    ProductNew update(ProductNew product);
    
    /**
     * Find product by ID
     * @param productId The product ID
     * @return Optional containing the product if found
     */
    Optional<ProductNew> findById(Long productId);
    
    /**
     * Find product by code
     * @param productCode The product code
     * @return Optional containing the product if found
     */
    Optional<ProductNew> findByCode(String productCode);
    
    /**
     * Find all products
     * @return List of all products
     */
    List<ProductNew> findAll();
    
    /**
     * Find all active products
     * @return List of active products
     */
    List<ProductNew> findAllActive();
    
    /**
     * Find products by subcategory
     * @param subcategoryId The subcategory ID
     * @return List of products in the subcategory
     */
    List<ProductNew> findBySubcategory(Long subcategoryId);
    
    /**
     * Find products with discounts
     * @return List of products that have discounts
     */
    List<ProductNew> findWithDiscounts();
    
    /**
     * Search products by name or description
     * @param searchTerm The search term
     * @return List of matching products
     */
    List<ProductNew> searchByNameOrDescription(String searchTerm);
    
    /**
     * Delete product by ID
     * @param productId The product ID
     * @return true if deleted successfully
     */
    boolean deleteById(Long productId);
    
    /**
     * Check if product code exists
     * @param productCode The product code
     * @return true if code exists
     */
    boolean existsByCode(String productCode);
    
    /**
     * Get total product count
     * @return Total number of products
     */
    long getTotalCount();
    
    /**
     * Get active product count
     * @return Number of active products
     */
    long getActiveCount();
    
    /**
     * Get product count with discounts
     * @return Number of products with discounts
     */
    long getDiscountedCount();
}