package com.syos.inventory.application.service;

import com.syos.inventory.domain.entity.Product;
import com.syos.inventory.domain.repository.ProductRepository;
import com.syos.inventory.domain.value.Money;
import com.syos.inventory.domain.exception.BusinessException;
import com.syos.inventory.domain.exception.EntityNotFoundException;
import com.syos.inventory.domain.exception.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Application service for managing products and inventory
 */
public class ProductService {
    private static final Logger logger = Logger.getLogger(ProductService.class.getName());
    
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * Create a new product
     * @param sku Product SKU
     * @param name Product name
     * @param description Product description
     * @param category Product category
     * @param price Product price
     * @param quantityInStock Initial stock quantity
     * @param minimumStockLevel Minimum stock level
     * @param unit Unit of measurement
     * @return Created product
     * @throws BusinessException if SKU already exists
     */
    public Product createProduct(String sku, String name, String description, 
                               String category, Money price, int quantityInStock, 
                               int minimumStockLevel, String unit) {
        try {
            // Check if SKU already exists
            if (productRepository.existsBySku(sku)) {
                throw new BusinessException("Product already exists with SKU: " + sku);
            }
            
            Product product = new Product(sku, name, description, category, price, 
                                        quantityInStock, minimumStockLevel, unit);
            
            Product savedProduct = productRepository.save(product);
            logger.info(() -> "Product created successfully: " + sku);
            
            return savedProduct;
            
        } catch (ValidationException | BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.severe(() -> "Error creating product " + sku + ": " + e.getMessage());
            throw new BusinessException("Failed to create product", e);
        }
    }
    
    /**
     * Get product by ID
     * @param productId Product ID
     * @return Product
     * @throws EntityNotFoundException if product not found
     */
    public Product getProductById(Long productId) {
        Optional<Product> product = productRepository.findById(productId);
        if (!product.isPresent()) {
            throw new EntityNotFoundException("Product not found with ID: " + productId);
        }
        return product.get();
    }
    
    /**
     * Get product by SKU
     * @param sku Product SKU
     * @return Product
     * @throws EntityNotFoundException if product not found
     */
    public Product getProductBySku(String sku) {
        Optional<Product> product = productRepository.findBySku(sku);
        if (!product.isPresent()) {
            throw new EntityNotFoundException("Product not found with SKU: " + sku);
        }
        return product.get();
    }
    
    /**
     * Get all products
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        try {
            return productRepository.findAll();
        } catch (Exception e) {
            logger.severe(() -> "Error retrieving all products: " + e.getMessage());
            throw new BusinessException("Failed to retrieve products", e);
        }
    }
    
    /**
     * Get active products
     * @return List of active products
     */
    public List<Product> getActiveProducts() {
        try {
            return productRepository.findByActive(true);
        } catch (Exception e) {
            logger.severe(() -> "Error retrieving active products: " + e.getMessage());
            throw new BusinessException("Failed to retrieve active products", e);
        }
    }
    
    /**
     * Get products by category
     * @param category Product category
     * @return List of products in category
     */
    public List<Product> getProductsByCategory(String category) {
        try {
            return productRepository.findByCategory(category);
        } catch (Exception e) {
            logger.severe(() -> "Error retrieving products by category " + category + ": " + e.getMessage());
            throw new BusinessException("Failed to retrieve products by category", e);
        }
    }
    
    /**
     * Search products by name
     * @param nameFragment Text to search in product name
     * @return List of matching products
     */
    public List<Product> searchProductsByName(String nameFragment) {
        if (nameFragment == null || nameFragment.trim().isEmpty()) {
            throw new ValidationException("Search text cannot be empty");
        }
        
        try {
            return productRepository.findByNameContaining(nameFragment.trim());
        } catch (Exception e) {
            logger.severe(() -> "Error searching products by name: " + e.getMessage());
            throw new BusinessException("Failed to search products", e);
        }
    }
    
    /**
     * Update product information
     * @param productId Product ID
     * @param name New name
     * @param description New description
     * @param category New category
     * @param price New price
     * @param minimumStockLevel New minimum stock level
     * @param unit New unit
     * @return Updated product
     * @throws EntityNotFoundException if product not found
     */
    public Product updateProduct(Long productId, String name, String description, 
                               String category, Money price, int minimumStockLevel, String unit) {
        try {
            Product product = getProductById(productId);
            product.updateInfo(name, description, category, price, minimumStockLevel, unit);
            
            Product updatedProduct = productRepository.save(product);
            logger.info(() -> "Product updated successfully: " + product.getSku());
            
            return updatedProduct;
            
        } catch (EntityNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.severe(() -> "Error updating product " + productId + ": " + e.getMessage());
            throw new BusinessException("Failed to update product", e);
        }
    }
    
    /**
     * Add stock to product
     * @param productId Product ID
     * @param quantity Quantity to add
     * @return Updated product
     * @throws EntityNotFoundException if product not found
     */
    public Product addStock(Long productId, int quantity) {
        try {
            Product product = getProductById(productId);
            product.addStock(quantity);
            
            Product updatedProduct = productRepository.save(product);
            logger.info(() -> "Stock added to product " + product.getSku() + ": +" + quantity);
            
            return updatedProduct;
            
        } catch (EntityNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.severe(() -> "Error adding stock to product " + productId + ": " + e.getMessage());
            throw new BusinessException("Failed to add stock", e);
        }
    }
    
    /**
     * Remove stock from product
     * @param productId Product ID
     * @param quantity Quantity to remove
     * @return Updated product
     * @throws EntityNotFoundException if product not found
     * @throws ValidationException if insufficient stock
     */
    public Product removeStock(Long productId, int quantity) {
        try {
            Product product = getProductById(productId);
            product.removeStock(quantity);
            
            Product updatedProduct = productRepository.save(product);
            logger.info(() -> "Stock removed from product " + product.getSku() + ": -" + quantity);
            
            return updatedProduct;
            
        } catch (EntityNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.severe(() -> "Error removing stock from product " + productId + ": " + e.getMessage());
            throw new BusinessException("Failed to remove stock", e);
        }
    }
    
    /**
     * Check if product has sufficient stock
     * @param productId Product ID
     * @param requestedQuantity Requested quantity
     * @return true if sufficient stock available
     * @throws EntityNotFoundException if product not found
     */
    public boolean hasSufficientStock(Long productId, int requestedQuantity) {
        Product product = getProductById(productId);
        return product.hasSufficientStock(requestedQuantity);
    }
    
    /**
     * Get low stock products
     * @return List of products with low stock
     */
    public List<Product> getLowStockProducts() {
        try {
            return productRepository.findLowStockProducts();
        } catch (Exception e) {
            logger.severe(() -> "Error retrieving low stock products: " + e.getMessage());
            throw new BusinessException("Failed to retrieve low stock products", e);
        }
    }
    
    /**
     * Get out of stock products
     * @return List of products out of stock
     */
    public List<Product> getOutOfStockProducts() {
        try {
            return productRepository.findOutOfStockProducts();
        } catch (Exception e) {
            logger.severe(() -> "Error retrieving out of stock products: " + e.getMessage());
            throw new BusinessException("Failed to retrieve out of stock products", e);
        }
    }
    
    /**
     * Get all product categories
     * @return List of categories
     */
    public List<String> getAllCategories() {
        try {
            return productRepository.findAllCategories();
        } catch (Exception e) {
            logger.severe(() -> "Error retrieving categories: " + e.getMessage());
            throw new BusinessException("Failed to retrieve categories", e);
        }
    }
    
    /**
     * Activate or deactivate product
     * @param productId Product ID
     * @param active New active status
     * @return Updated product
     * @throws EntityNotFoundException if product not found
     */
    public Product setProductActive(Long productId, boolean active) {
        try {
            Product product = getProductById(productId);
            product.setActive(active);
            
            Product updatedProduct = productRepository.save(product);
            logger.info(() -> "Product " + (active ? "activated" : "deactivated") + ": " + product.getSku());
            
            return updatedProduct;
            
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.severe(() -> "Error setting product active status " + productId + ": " + e.getMessage());
            throw new BusinessException("Failed to update product status", e);
        }
    }
    
    /**
     * Delete product
     * @param productId Product ID
     * @return true if product was deleted
     * @throws EntityNotFoundException if product not found
     */
    public boolean deleteProduct(Long productId) {
        try {
            // Check if product exists
            Product product = getProductById(productId);
            
            boolean deleted = productRepository.deleteById(productId);
            if (deleted) {
                logger.info(() -> "Product deleted: " + product.getSku());
            }
            
            return deleted;
            
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.severe(() -> "Error deleting product " + productId + ": " + e.getMessage());
            throw new BusinessException("Failed to delete product", e);
        }
    }
    
    /**
     * Get product statistics
     * @return Product statistics
     */
    public ProductStatistics getProductStatistics() {
        try {
            long totalProducts = productRepository.count();
            long lowStockCount = productRepository.countLowStockProducts();
            long outOfStockCount = productRepository.countOutOfStockProducts();
            
            return new ProductStatistics(totalProducts, lowStockCount, outOfStockCount);
            
        } catch (Exception e) {
            logger.severe(() -> "Error retrieving product statistics: " + e.getMessage());
            throw new BusinessException("Failed to retrieve product statistics", e);
        }
    }
    
    /**
     * Inner class for product statistics
     */
    public static class ProductStatistics {
        private final long totalProducts;
        private final long lowStockProducts;
        private final long outOfStockProducts;
        
        public ProductStatistics(long totalProducts, long lowStockProducts, long outOfStockProducts) {
            this.totalProducts = totalProducts;
            this.lowStockProducts = lowStockProducts;
            this.outOfStockProducts = outOfStockProducts;
        }
        
        public long getTotalProducts() {
            return totalProducts;
        }
        
        public long getLowStockProducts() {
            return lowStockProducts;
        }
        
        public long getOutOfStockProducts() {
            return outOfStockProducts;
        }
        
        public long getInStockProducts() {
            return totalProducts - outOfStockProducts;
        }
        
        @Override
        public String toString() {
            return "ProductStatistics{" +
                    "totalProducts=" + totalProducts +
                    ", lowStockProducts=" + lowStockProducts +
                    ", outOfStockProducts=" + outOfStockProducts +
                    ", inStockProducts=" + getInStockProducts() +
                    '}';
        }
    }
}