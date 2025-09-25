package com.syos.application.services;

import java.sql.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Service for managing online inventory and product catalog.
 * 
 * Provides functionality for online customers to browse products,
 * check availability, and manage shopping cart operations.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class OnlineInventoryService {
    
    private static final Logger LOGGER = Logger.getLogger(OnlineInventoryService.class.getName());
    private final String databaseUrl;
    
    public OnlineInventoryService(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }
    
    /**
     * Product information for online catalog
     */
    public static class OnlineProduct {
        private final Long productId;
        private final String productCode;
        private final String productName;
        private final String description;
        private final String brand;
        private final String category;
        private final BigDecimal basePrice;
        private final BigDecimal finalPrice;
        private final String unitOfMeasure;
        private final int availableQuantity;
        private final boolean hasDiscount;
        private final String discountDescription;
        private final java.util.Date expiryDate; // Earliest expiry date from batches
        
        public OnlineProduct(Long productId, String productCode, String productName, 
                           String description, String brand, String category,
                           BigDecimal basePrice, BigDecimal finalPrice, String unitOfMeasure,
                           int availableQuantity, boolean hasDiscount, String discountDescription,
                           java.util.Date expiryDate) {
            this.productId = productId;
            this.productCode = productCode;
            this.productName = productName;
            this.description = description;
            this.brand = brand;
            this.category = category;
            this.basePrice = basePrice;
            this.finalPrice = finalPrice;
            this.unitOfMeasure = unitOfMeasure;
            this.availableQuantity = availableQuantity;
            this.hasDiscount = hasDiscount;
            this.discountDescription = discountDescription;
            this.expiryDate = expiryDate;
        }
        
        // Getters
        public Long getProductId() { return productId; }
        public String getProductCode() { return productCode; }
        public String getProductName() { return productName; }
        public String getDescription() { return description; }
        public String getBrand() { return brand; }
        public String getCategory() { return category; }
        public BigDecimal getBasePrice() { return basePrice; }
        public BigDecimal getFinalPrice() { return finalPrice; }
        public String getUnitOfMeasure() { return unitOfMeasure; }
        public int getAvailableQuantity() { return availableQuantity; }
        public boolean hasDiscount() { return hasDiscount; }
        public String getDiscountDescription() { return discountDescription; }
        public java.util.Date getExpiryDate() { return expiryDate; }
        
        public boolean isAvailable() {
            return availableQuantity > 0;
        }
        
        public boolean hasStock(int requestedQuantity) {
            return availableQuantity >= requestedQuantity;
        }
    }
    
    /**
     * Search products by name or description
     */
    public List<OnlineProduct> searchProducts(String searchTerm) {
        List<OnlineProduct> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_code, p.product_name, p.description, p.brand, " +
            "c.category_name, p.base_price, p.final_price, p.unit_of_measure, " +
            "COALESCE(batch_info.total_quantity, 0) as available_quantity, " +
            "(p.discount_percentage > 0 OR p.discount_amount > 0) as has_discount, " +
            "CASE " +
            "    WHEN p.discount_percentage > 0 THEN 'Save ' || ROUND(p.discount_percentage, 1) || '%' " +
            "    WHEN p.discount_amount > 0 THEN 'Save LKR ' || p.discount_amount " +
            "    ELSE 'No discount' " +
            "END as discount_description, " +
            "batch_info.earliest_expiry_date " +
            "FROM product p " +
            "INNER JOIN subcategory s ON p.subcategory_id = s.subcategory_id " +
            "INNER JOIN category c ON s.category_id = c.category_id " +
            "LEFT JOIN ( " +
            "    SELECT b.product_id, " +
            "           SUM(COALESCE(oi.available_quantity, b.quantity_received)) as total_quantity, " +
            "           MIN(CASE WHEN b.expiry_date IS NOT NULL THEN b.expiry_date END) as earliest_expiry_date " +
            "    FROM batch b " +
            "    LEFT JOIN online_inventory oi ON b.batch_id = oi.batch_id " +
            "    WHERE b.expiry_date IS NULL OR b.expiry_date > date('now') " +
            "    GROUP BY b.product_id " +
            ") batch_info ON p.product_id = batch_info.product_id " +
            "WHERE p.is_active = 1 " +
            "AND (LOWER(p.product_name) LIKE LOWER(?) " +
            "     OR LOWER(p.description) LIKE LOWER(?) " +
            "     OR LOWER(p.brand) LIKE LOWER(?)) " +
            "AND COALESCE(batch_info.total_quantity, 0) > 0 " +
            "ORDER BY p.product_name";
        
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(createOnlineProductFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error searching products: " + e.getMessage());
            throw new RuntimeException("Failed to search products", e);
        }
        
        return products;
    }
    
    /**
     * Get all products by category
     */
    public List<OnlineProduct> getProductsByCategory(String categoryName) {
        List<OnlineProduct> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_code, p.product_name, p.description, p.brand, " +
            "c.category_name, p.base_price, p.final_price, p.unit_of_measure, " +
            "COALESCE(batch_info.total_quantity, 0) as available_quantity, " +
            "(p.discount_percentage > 0 OR p.discount_amount > 0) as has_discount, " +
            "CASE " +
            "    WHEN p.discount_percentage > 0 THEN 'Save ' || ROUND(p.discount_percentage, 1) || '%' " +
            "    WHEN p.discount_amount > 0 THEN 'Save LKR ' || p.discount_amount " +
            "    ELSE 'No discount' " +
            "END as discount_description, " +
            "batch_info.earliest_expiry_date " +
            "FROM product p " +
            "INNER JOIN subcategory s ON p.subcategory_id = s.subcategory_id " +
            "INNER JOIN category c ON s.category_id = c.category_id " +
            "LEFT JOIN ( " +
            "    SELECT b.product_id, " +
            "           SUM(COALESCE(oi.available_quantity, b.quantity_received)) as total_quantity, " +
            "           MIN(CASE WHEN b.expiry_date IS NOT NULL THEN b.expiry_date END) as earliest_expiry_date " +
            "    FROM batch b " +
            "    LEFT JOIN online_inventory oi ON b.batch_id = oi.batch_id " +
            "    WHERE b.expiry_date IS NULL OR b.expiry_date > date('now') " +
            "    GROUP BY b.product_id " +
            ") batch_info ON p.product_id = batch_info.product_id " +
            "WHERE p.is_active = 1 " +
            "AND LOWER(c.category_name) = LOWER(?) " +
            "AND COALESCE(batch_info.total_quantity, 0) > 0 " +
            "ORDER BY p.product_name";
        
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categoryName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(createOnlineProductFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error getting products by category: " + e.getMessage());
            throw new RuntimeException("Failed to get products by category", e);
        }
        
        return products;
    }
    
    /**
     * Get product by code
     */
    public Optional<OnlineProduct> getProductByCode(String productCode) {
        String sql = "SELECT p.product_id, p.product_code, p.product_name, p.description, p.brand, " +
            "c.category_name, p.base_price, p.final_price, p.unit_of_measure, " +
            "COALESCE(oi.available_quantity, 0) as available_quantity, " +
            "(p.discount_percentage > 0 OR p.discount_amount > 0) as has_discount, " +
            "CASE " +
            "    WHEN p.discount_percentage > 0 THEN 'Save ' || ROUND(p.discount_percentage, 1) || '%' " +
            "    WHEN p.discount_amount > 0 THEN 'Save LKR ' || p.discount_amount " +
            "    ELSE 'No discount' " +
            "END as discount_description, " +
            "oi.earliest_expiry_date " +
            "FROM product p " +
            "INNER JOIN subcategory s ON p.subcategory_id = s.subcategory_id " +
            "INNER JOIN category c ON s.category_id = c.category_id " +
            "LEFT JOIN ( " +
            "    SELECT b.product_id, SUM(oi.available_quantity) as available_quantity, " +
            "           MIN(CASE WHEN b.expiry_date IS NOT NULL THEN b.expiry_date END) as earliest_expiry_date " +
            "    FROM batch b " +
            "    INNER JOIN online_inventory oi ON b.batch_id = oi.batch_id " +
            "    WHERE b.expiry_date IS NULL OR b.expiry_date > date('now') " +
            "    GROUP BY b.product_id " +
            ") oi ON p.product_id = oi.product_id " +
            "WHERE p.is_active = 1 AND UPPER(p.product_code) = UPPER(?)";
        
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, productCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(createOnlineProductFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error getting product by code: " + e.getMessage());
            throw new RuntimeException("Failed to get product by code", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Get all available categories
     */
    public List<String> getAvailableCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT c.category_name " +
            "FROM category c " +
            "INNER JOIN subcategory s ON c.category_id = s.category_id " +
            "INNER JOIN product p ON s.subcategory_id = p.subcategory_id " +
            "WHERE p.is_active = 1 AND c.is_active = 1 " +
            "ORDER BY c.category_name";
        
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error getting categories: " + e.getMessage());
            throw new RuntimeException("Failed to get categories", e);
        }
        
        return categories;
    }
    
    /**
     * Check product availability for specific quantity
     */
    public boolean checkAvailability(String productCode, int quantity) {
        Optional<OnlineProduct> product = getProductByCode(productCode);
        return product.map(p -> p.hasStock(quantity)).orElse(false);
    }
    
    /**
     * Get featured/popular products
     */
    public List<OnlineProduct> getFeaturedProducts(int limit) {
        List<OnlineProduct> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_code, p.product_name, p.description, p.brand, " +
            "c.category_name, p.base_price, p.final_price, p.unit_of_measure, " +
            "COALESCE(batch_info.total_quantity, 0) as available_quantity, " +
            "(p.discount_percentage > 0 OR p.discount_amount > 0) as has_discount, " +
            "CASE " +
            "    WHEN p.discount_percentage > 0 THEN 'Save ' || ROUND(p.discount_percentage, 1) || '%' " +
            "    WHEN p.discount_amount > 0 THEN 'Save LKR ' || p.discount_amount " +
            "    ELSE 'No discount' " +
            "END as discount_description, " +
            "batch_info.earliest_expiry_date " +
            "FROM product p " +
            "INNER JOIN subcategory s ON p.subcategory_id = s.subcategory_id " +
            "INNER JOIN category c ON s.category_id = c.category_id " +
            "LEFT JOIN ( " +
            "    SELECT b.product_id, " +
            "           SUM(COALESCE(oi.available_quantity, b.quantity_received)) as total_quantity, " +
            "           MIN(CASE WHEN b.expiry_date IS NOT NULL THEN b.expiry_date END) as earliest_expiry_date " +
            "    FROM batch b " +
            "    LEFT JOIN online_inventory oi ON b.batch_id = oi.batch_id " +
            "    WHERE b.expiry_date IS NULL OR b.expiry_date > date('now') " +
            "    GROUP BY b.product_id " +
            ") batch_info ON p.product_id = batch_info.product_id " +
            "WHERE p.is_active = 1 " +
            "AND COALESCE(batch_info.total_quantity, 0) > 0 " +
            "ORDER BY (p.discount_percentage + p.discount_amount) DESC, p.product_name " +
            "LIMIT ?";
        
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(createOnlineProductFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error getting featured products: " + e.getMessage());
            throw new RuntimeException("Failed to get featured products", e);
        }
        
        return products;
    }
    
    /**
     * Create OnlineProduct from ResultSet
     */
    private OnlineProduct createOnlineProductFromResultSet(ResultSet rs) throws SQLException {
        // Handle expiry date parsing manually to avoid SQLite date format issues
        java.util.Date expiryDate = null;
        String expiryDateStr = rs.getString("earliest_expiry_date");
        if (expiryDateStr != null && !expiryDateStr.isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                expiryDate = sdf.parse(expiryDateStr);
            } catch (java.text.ParseException e) {
                LOGGER.warning("Failed to parse expiry date: " + expiryDateStr);
            }
        }
        
        return new OnlineProduct(
            rs.getLong("product_id"),
            rs.getString("product_code"),
            rs.getString("product_name"),
            rs.getString("description"),
            rs.getString("brand"),
            rs.getString("category_name"),
            rs.getBigDecimal("base_price"),
            rs.getBigDecimal("final_price"),
            rs.getString("unit_of_measure"),
            rs.getInt("available_quantity"),
            rs.getBoolean("has_discount"),
            rs.getString("discount_description"),
            expiryDate
        );
    }
}