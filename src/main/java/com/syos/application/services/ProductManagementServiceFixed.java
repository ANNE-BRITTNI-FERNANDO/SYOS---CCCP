package com.syos.application.services;

import com.syos.inventory.domain.entity.ProductNew;
import com.syos.inventory.domain.entity.Category;
import com.syos.inventory.domain.entity.Subcategory;
import com.syos.inventory.infrastructure.repository.SqliteProductNewRepositoryImpl;
import com.syos.inventory.infrastructure.database.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductManagementServiceFixed {
    private final SqliteProductNewRepositoryImpl productRepository;
    private final DatabaseManager databaseManager;

    public ProductManagementServiceFixed() {
        this.databaseManager = DatabaseManager.getInstance();
        this.productRepository = new SqliteProductNewRepositoryImpl(databaseManager);
    }

    /**
     * Get all products from the database
     */
    public List<ProductNew> getAllProducts() {
        try {
            return productRepository.findAllActive();
        } catch (Exception e) {
            System.err.println("Error fetching all products: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get all categories from the database
     */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM category WHERE is_active = 1 ORDER BY category_name";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Category category = new Category(
                    rs.getLong("category_id"),
                    rs.getString("category_code"),
                    rs.getString("category_name"),
                    rs.getString("description"),
                    rs.getBoolean("is_active"),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                );
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
        }
        
        return categories;
    }

    /**
     * Get subcategories for a specific category
     */
    public List<Subcategory> getSubCategoriesByCategory(Long categoryId) {
        List<Subcategory> subCategories = new ArrayList<>();
        String sql = "SELECT subcategory_id, category_id, subcategory_code, subcategory_name, " +
                    "description, default_shelf_capacity, is_active " +
                    "FROM subcategory WHERE category_id = ? AND is_active = 1 ORDER BY subcategory_name";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Subcategory subCategory = new Subcategory(
                    rs.getLong("subcategory_id"),
                    rs.getLong("category_id"),
                    rs.getString("subcategory_code"),
                    rs.getString("subcategory_name"),
                    rs.getString("description"),
                    rs.getInt("default_shelf_capacity"),
                    rs.getBoolean("is_active"),
                    LocalDateTime.now() // Use current time since table doesn't have created_at
                );
                subCategories.add(subCategory);
            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error fetching subcategories: " + e.getMessage());
        }
        
        return subCategories;
    }

    /**
     * Create a new product
     */
    public boolean createProduct(String productName, String description, Long subcategoryId, 
                               BigDecimal price, String unit, String brand, Long createdBy) {
        return createProduct(productName, description, subcategoryId, price, unit, brand, createdBy, 0, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public boolean createProduct(String productName, String description, Long subcategoryId, 
                               BigDecimal price, String unit, String brand, Long createdBy,
                               int discountType, BigDecimal discountValue1, BigDecimal discountValue2) {
        try {
            // Generate product code
            String productCode = generateProductCode(subcategoryId);
            
            ProductNew product = new ProductNew(
                productCode, productName, description, brand, price, unit, subcategoryId, createdBy
            );
            
            // Apply discount if specified
            if (discountType == 1) { // Fixed amount discount
                product.setFixedDiscount(discountValue1);
            } else if (discountType == 2) { // Percentage discount
                product.setPercentageDiscount(discountValue1);
            }
            
            ProductNew savedProduct = productRepository.save(product);
            return savedProduct != null && savedProduct.getProductId() != null;
        } catch (Exception e) {
            System.err.println("Error creating product: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update an existing product
     */
    public boolean updateProduct(String productCode, String productName, String description, 
                               BigDecimal price, String unit, String brand) {
        try {
            Optional<ProductNew> existingProductOpt = productRepository.findByCode(productCode);
            if (existingProductOpt.isPresent()) {
                ProductNew existingProduct = existingProductOpt.get();
                existingProduct.updateInfo(productName, description, brand, price, unit);
                
                ProductNew updatedProduct = productRepository.update(existingProduct);
                return updatedProduct != null;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete (deactivate) a product
     */
    public boolean deleteProduct(String productCode) {
        try {
            Optional<ProductNew> productOpt = productRepository.findByCode(productCode);
            if (productOpt.isPresent()) {
                ProductNew product = productOpt.get();
                product.deactivate();
                ProductNew updatedProduct = productRepository.update(product);
                return updatedProduct != null;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }

    /**
     * Find product by code
     */
    public ProductNew findProductByCode(String productCode) {
        try {
            Optional<ProductNew> productOpt = productRepository.findByCode(productCode);
            return productOpt.orElse(null);
        } catch (Exception e) {
            System.err.println("Error finding product by code: " + e.getMessage());
            return null;
        }
    }

    /**
     * Search products by name
     */
    public List<ProductNew> searchProductsByName(String searchTerm) {
        try {
            return productRepository.searchByNameOrDescription(searchTerm);
        } catch (Exception e) {
            System.err.println("Error searching products: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Generate product code based on subcategory
     */
    private String generateProductCode(Long subcategoryId) throws SQLException {
        // Get subcategory code
        String subcategoryCode = getSubcategoryCode(subcategoryId);
        
        // Get next sequence number for this subcategory
        int nextSeq = getNextSequenceNumber(subcategoryCode);
        
        return subcategoryCode + "-" + String.format("%03d", nextSeq);
    }

    private String getSubcategoryCode(Long subcategoryId) throws SQLException {
        String sql = "SELECT subcategory_code FROM subcategory WHERE subcategory_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, subcategoryId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String code = rs.getString("subcategory_code");
                rs.close();
                return code;
            }
            rs.close();
        }
        throw new SQLException("Subcategory not found with ID: " + subcategoryId);
    }

    private int getNextSequenceNumber(String subcategoryCode) throws SQLException {
        String pattern = subcategoryCode + "-%";
        String sql = "SELECT product_code FROM product WHERE product_code LIKE ? ORDER BY product_code";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, pattern);
            ResultSet rs = stmt.executeQuery();
            
            int maxSequence = 0;
            while (rs.next()) {
                String productCode = rs.getString("product_code");
                // Extract sequence number from code (e.g., "LA-SO-001" -> 1)
                String[] parts = productCode.split("-");
                if (parts.length >= 3) {
                    try {
                        int sequence = Integer.parseInt(parts[2]);
                        maxSequence = Math.max(maxSequence, sequence);
                    } catch (NumberFormatException e) {
                        // Skip invalid codes
                    }
                }
            }
            rs.close();
            return maxSequence + 1;
        }
    }

    /**
     * Get category name by ID
     */
    public String getCategoryName(Long categoryId) {
        String sql = "SELECT category_name FROM category WHERE category_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String name = rs.getString("category_name");
                rs.close();
                return name;
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting category name: " + e.getMessage());
        }
        return "Unknown";
    }

    /**
     * Get subcategory name by ID
     */
    public String getSubcategoryName(Long subcategoryId) {
        String sql = "SELECT subcategory_name FROM subcategory WHERE subcategory_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, subcategoryId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String name = rs.getString("subcategory_name");
                rs.close();
                return name;
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting subcategory name: " + e.getMessage());
        }
        return "Unknown";
    }

    /**
     * Get category by subcategory ID
     */
    public Category getCategoryBySubcategory(Long subcategoryId) {
        String sql = "SELECT c.* FROM category c JOIN subcategory s ON c.category_id = s.category_id WHERE s.subcategory_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, subcategoryId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Category category = new Category(
                    rs.getLong("category_id"),
                    rs.getString("category_code"),
                    rs.getString("category_name"),
                    rs.getString("description"),
                    rs.getBoolean("is_active"),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                );
                rs.close();
                return category;
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting category by subcategory: " + e.getMessage());
        }
        return null;
    }

    /**
     * Create a new main category
     */
    public boolean createCategory(String categoryCode, String categoryName, String description) {
        String sql = "INSERT INTO category (category_code, category_name, description, is_active, created_at) VALUES (?, ?, ?, 1, ?)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categoryCode);
            stmt.setString(2, categoryName);
            stmt.setString(3, description);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating category: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create a new subcategory
     */
    public boolean createSubcategory(String mainCategoryCode, String subcategoryCode, String subcategoryName, 
                                   int shelfCapacity, int minThreshold) {
        // First get the category ID for the main category
        Long categoryId = getCategoryIdByCode(mainCategoryCode);
        if (categoryId == null) {
            System.err.println("Main category not found: " + mainCategoryCode);
            return false;
        }
        
        String sql = "INSERT INTO subcategory (category_id, subcategory_code, subcategory_name, " +
                    "description, default_shelf_capacity, is_active) VALUES (?, ?, ?, ?, ?, 1)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, categoryId);
            stmt.setString(2, subcategoryCode);
            stmt.setString(3, subcategoryName);
            stmt.setString(4, ""); // Empty description for now
            stmt.setInt(5, shelfCapacity); // Use shelfCapacity as default_shelf_capacity
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating subcategory: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get category ID by category code
     */
    private Long getCategoryIdByCode(String categoryCode) {
        String sql = "SELECT category_id FROM category WHERE category_code = ? AND is_active = 1";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categoryCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Long categoryId = rs.getLong("category_id");
                rs.close();
                return categoryId;
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting category ID by code: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Set fixed discount for product
     */
    public boolean setFixedDiscount(String productCode, BigDecimal discountAmount) {
        try {
            Optional<ProductNew> productOpt = productRepository.findByCode(productCode);
            if (productOpt.isPresent()) {
                ProductNew product = productOpt.get();
                product.setFixedDiscount(discountAmount);
                ProductNew updated = productRepository.update(product);
                return updated != null;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error setting fixed discount: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Set percentage discount for product
     */
    public boolean setPercentageDiscount(String productCode, BigDecimal discountPercentage) {
        try {
            Optional<ProductNew> productOpt = productRepository.findByCode(productCode);
            if (productOpt.isPresent()) {
                ProductNew product = productOpt.get();
                product.setPercentageDiscount(discountPercentage);
                ProductNew updated = productRepository.update(product);
                return updated != null;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error setting percentage discount: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove discount from product
     */
    public boolean removeDiscount(String productCode) {
        try {
            Optional<ProductNew> productOpt = productRepository.findByCode(productCode);
            if (productOpt.isPresent()) {
                ProductNew product = productOpt.get();
                product.removeDiscount();
                ProductNew updated = productRepository.update(product);
                return updated != null;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error removing discount: " + e.getMessage());
            return false;
        }
    }
}