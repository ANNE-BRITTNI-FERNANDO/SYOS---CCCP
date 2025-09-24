package com.syos.domain.repositories;

import com.syos.domain.entities.Product;
import com.syos.shared.valueobjects.ProductCode;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entities.
 * 
 * This interface follows the Repository pattern and defines the contract
 * for Product persistence operations. It belongs to the domain layer
 * and follows the Dependency Inversion Principle.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public interface ProductRepository {
    
    /**
     * Saves a product to the repository.
     * If the product already exists, it will be updated.
     * 
     * @param product the product to save
     * @return the saved product
     * @throws IllegalArgumentException if product is null
     */
    Product save(Product product);
    
    /**
     * Finds a product by its product code.
     * 
     * @param productCode the product code
     * @return an Optional containing the product if found, empty otherwise
     * @throws IllegalArgumentException if productCode is null
     */
    Optional<Product> findByProductCode(ProductCode productCode);
    
    /**
     * Finds a product by its product code string.
     * 
     * @param productCode the product code as string
     * @return an Optional containing the product if found, empty otherwise
     * @throws IllegalArgumentException if productCode is null or empty
     */
    Optional<Product> findByProductCode(String productCode);
    
    /**
     * Finds all products in the repository.
     * 
     * @return a list of all products
     */
    List<Product> findAll();
    
    /**
     * Finds all active products.
     * 
     * @return a list of active products
     */
    List<Product> findAllActive();
    
    /**
     * Finds products by category.
     * 
     * @param category the category name
     * @return a list of products in the specified category
     * @throws IllegalArgumentException if category is null or empty
     */
    List<Product> findByCategory(String category);
    
    /**
     * Finds products by brand.
     * 
     * @param brand the brand name
     * @return a list of products from the specified brand
     * @throws IllegalArgumentException if brand is null or empty
     */
    List<Product> findByBrand(String brand);
    
    /**
     * Finds products by name (case-insensitive partial match).
     * 
     * @param name the product name or partial name
     * @return a list of products matching the name
     * @throws IllegalArgumentException if name is null or empty
     */
    List<Product> findByNameContaining(String name);
    
    /**
     * Finds all unique categories.
     * 
     * @return a list of all product categories
     */
    List<String> findAllCategories();
    
    /**
     * Finds all unique brands.
     * 
     * @return a list of all product brands
     */
    List<String> findAllBrands();
    
    /**
     * Checks if a product exists with the given product code.
     * 
     * @param productCode the product code
     * @return true if a product exists with the given code
     * @throws IllegalArgumentException if productCode is null
     */
    boolean existsByProductCode(ProductCode productCode);
    
    /**
     * Checks if a product exists with the given product code string.
     * 
     * @param productCode the product code as string
     * @return true if a product exists with the given code
     * @throws IllegalArgumentException if productCode is null or empty
     */
    boolean existsByProductCode(String productCode);
    
    /**
     * Deletes a product from the repository.
     * Note: This should be used carefully as it permanently removes the product.
     * Consider deactivating products instead of deleting them.
     * 
     * @param product the product to delete
     * @throws IllegalArgumentException if product is null
     */
    void delete(Product product);
    
    /**
     * Deletes a product by its product code.
     * Note: This should be used carefully as it permanently removes the product.
     * Consider deactivating products instead of deleting them.
     * 
     * @param productCode the product code
     * @throws IllegalArgumentException if productCode is null
     */
    void deleteByProductCode(ProductCode productCode);
    
    /**
     * Counts the total number of products.
     * 
     * @return the total number of products
     */
    long count();
    
    /**
     * Counts the number of active products.
     * 
     * @return the number of active products
     */
    long countActive();
    
    /**
     * Counts the number of products in a specific category.
     * 
     * @param category the category name
     * @return the number of products in the category
     * @throws IllegalArgumentException if category is null or empty
     */
    long countByCategory(String category);
}