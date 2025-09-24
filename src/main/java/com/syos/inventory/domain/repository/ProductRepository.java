package com.syos.inventory.domain.repository;

import com.syos.inventory.domain.entity.Product;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity
 */
public interface ProductRepository {
    
    /**
     * Save a product (create or update)
     * @param product Product to save
     * @return Saved product with ID
     */
    Product save(Product product);
    
    /**
     * Find product by ID
     * @param id Product ID
     * @return Product if found
     */
    Optional<Product> findById(Long id);
    
    /**
     * Find product by SKU
     * @param sku Product SKU
     * @return Product if found
     */
    Optional<Product> findBySku(String sku);
    
    /**
     * Find all products
     * @return List of all products
     */
    List<Product> findAll();
    
    /**
     * Find active products
     * @param active Active status
     * @return List of products with specified active status
     */
    List<Product> findByActive(boolean active);
    
    /**
     * Find products by category
     * @param category Product category
     * @return List of products in specified category
     */
    List<Product> findByCategory(String category);
    
    /**
     * Find products with low stock
     * @return List of products with quantity <= minimum stock level
     */
    List<Product> findLowStockProducts();
    
    /**
     * Find products out of stock
     * @return List of products with quantity = 0
     */
    List<Product> findOutOfStockProducts();
    
    /**
     * Find products by name containing text
     * @param nameFragment Text to search in product name
     * @return List of products with name containing the text
     */
    List<Product> findByNameContaining(String nameFragment);
    
    /**
     * Find all product categories
     * @return List of distinct categories
     */
    List<String> findAllCategories();
    
    /**
     * Check if SKU exists
     * @param sku SKU to check
     * @return true if SKU exists
     */
    boolean existsBySku(String sku);
    
    /**
     * Delete product by ID
     * @param id Product ID
     * @return true if product was deleted
     */
    boolean deleteById(Long id);
    
    /**
     * Count total products
     * @return Total number of products
     */
    long count();
    
    /**
     * Count products by category
     * @param category Product category
     * @return Number of products in category
     */
    long countByCategory(String category);
    
    /**
     * Count low stock products
     * @return Number of products with low stock
     */
    long countLowStockProducts();
    
    /**
     * Count out of stock products
     * @return Number of products out of stock
     */
    long countOutOfStockProducts();
}