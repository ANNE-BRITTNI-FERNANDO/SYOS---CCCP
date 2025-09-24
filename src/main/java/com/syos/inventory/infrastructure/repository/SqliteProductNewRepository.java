package com.syos.inventory.infrastructure.repository;

import com.syos.inventory.domain.entity.ProductNew;
import com.syos.inventory.application.repository.ProductNewRepository;
import com.syos.inventory.infrastructure.database.DatabaseManager;


import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SQLite implementation of ProductNewRepository
 */
public class SqliteProductNewRepository implements ProductNewRepository {
    
    private static final Logger LOGGER = Logger.getLogger(SqliteProductNewRepository.class.getName());
    
    private final DatabaseManager databaseManager;
    
    public SqliteProductNewRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    @Override
    public ProductNew save(ProductNew product) {
        String sql = "INSERT INTO product (product_code, product_name, description, brand, base_price, " +
                     "unit_of_measure, subcategory_id, discount_percentage, discount_amount, " +
                     "final_price, is_active, created_at, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, product.getProductCode());
            statement.setString(2, product.getProductName());
            statement.setString(3, product.getDescription());
            statement.setString(4, product.getBrand());
            statement.setBigDecimal(5, product.getBasePrice());
            statement.setString(6, product.getUnitOfMeasure());
            statement.setLong(7, product.getSubcategoryId());
            statement.setBigDecimal(8, product.getDiscountPercentage());
            statement.setBigDecimal(9, product.getDiscountAmount());
            statement.setBigDecimal(10, product.getFinalPrice());
            statement.setBoolean(11, product.isActive());
            statement.setTimestamp(12, Timestamp.valueOf(product.getCreatedAt()));
            statement.setLong(13, product.getCreatedBy());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Creating product failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setProductId(generatedKeys.getLong(1));
                } else {
                    throw new RuntimeException("Creating product failed, no ID obtained.");
                }
            }
            
            LOGGER.info("Product saved successfully: " + product.getProductCode());
            return product;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving product: " + product.getProductCode(), e);
            throw new RuntimeException("Failed to save product: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ProductNew update(ProductNew product) {
        String sql = "UPDATE product SET product_name = ?, description = ?, brand = ?, base_price = ?, " +
                     "unit_of_measure = ?, discount_percentage = ?, discount_amount = ?, " +
                     "final_price = ?, is_active = ? " +
                     "WHERE product_id = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, product.getProductName());
            statement.setString(2, product.getDescription());
            statement.setString(3, product.getBrand());
            statement.setBigDecimal(4, product.getBasePrice());
            statement.setString(5, product.getUnitOfMeasure());
            statement.setBigDecimal(6, product.getDiscountPercentage());
            statement.setBigDecimal(7, product.getDiscountAmount());
            statement.setBigDecimal(8, product.getFinalPrice());
            statement.setBoolean(9, product.isActive());
            statement.setLong(10, product.getProductId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Updating product failed, no rows affected.");
            }
            
            LOGGER.info("Product updated successfully: " + product.getProductCode());
            return product;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating product: " + product.getProductCode(), e);
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<ProductNew> findById(Long productId) {
        String sql = "SELECT product_id, product_code, product_name, description, brand, base_price, " +
                     "unit_of_measure, subcategory_id, discount_percentage, discount_amount, " +
                     "final_price, is_active, created_at, created_by " +
                     "FROM product WHERE product_id = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, productId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToProduct(resultSet));
                }
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding product by ID: " + productId, e);
            throw new RuntimeException("Failed to find product: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<ProductNew> findByCode(String productCode) {
        String sql = "SELECT product_id, product_code, product_name, description, brand, base_price, " +
                     "unit_of_measure, subcategory_id, discount_percentage, discount_amount, " +
                     "final_price, is_active, created_at, created_by " +
                     "FROM product WHERE product_code = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, productCode);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToProduct(resultSet));
                }
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding product by code: " + productCode, e);
            throw new RuntimeException("Failed to find product: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<ProductNew> findAll() {
        String sql = "SELECT product_id, product_code, product_name, description, brand, base_price, " +
                     "unit_of_measure, subcategory_id, discount_percentage, discount_amount, " +
                     "final_price, is_active, created_at, created_by " +
                     "FROM product ORDER BY product_name";
        
        return executeQuery(sql);
    }
    
    @Override
    public List<ProductNew> findAllActive() {
        String sql = "SELECT product_id, product_code, product_name, description, brand, base_price, " +
                     "unit_of_measure, subcategory_id, discount_percentage, discount_amount, " +
                     "final_price, is_active, created_at, created_by " +
                     "FROM product WHERE is_active = 1 ORDER BY product_name";
        
        return executeQuery(sql);
    }
    
    @Override
    public List<ProductNew> findBySubcategory(Long subcategoryId) {
        String sql = "SELECT product_id, product_code, product_name, description, brand, base_price, " +
                     "unit_of_measure, subcategory_id, discount_percentage, discount_amount, " +
                     "final_price, is_active, created_at, created_by " +
                     "FROM product WHERE subcategory_id = ? ORDER BY product_name";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, subcategoryId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ProductNew> products = new ArrayList<>();
                while (resultSet.next()) {
                    products.add(mapResultSetToProduct(resultSet));
                }
                return products;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding products by subcategory: " + subcategoryId, e);
            throw new RuntimeException("Failed to find products: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<ProductNew> findWithDiscounts() {
        String sql = "SELECT product_id, product_code, product_name, description, brand, base_price, " +
                     "unit_of_measure, subcategory_id, discount_percentage, discount_amount, " +
                     "final_price, is_active, created_at, created_by " +
                     "FROM product " +
                     "WHERE (discount_percentage > 0 OR discount_amount > 0) AND is_active = 1 " +
                     "ORDER BY product_name";
        
        return executeQuery(sql);
    }
    
    @Override
    public List<ProductNew> searchByNameOrDescription(String searchTerm) {
        String sql = "SELECT product_id, product_code, product_name, description, brand, base_price, " +
                     "unit_of_measure, subcategory_id, discount_percentage, discount_amount, " +
                     "final_price, is_active, created_at, created_by " +
                     "FROM product " +
                     "WHERE (LOWER(product_name) LIKE ? OR LOWER(description) LIKE ?) AND is_active = 1 " +
                     "ORDER BY product_name";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ProductNew> products = new ArrayList<>();
                while (resultSet.next()) {
                    products.add(mapResultSetToProduct(resultSet));
                }
                return products;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching products: " + searchTerm, e);
            throw new RuntimeException("Failed to search products: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean deleteById(Long productId) {
        String sql = "DELETE FROM product WHERE product_id = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, productId);
            int affectedRows = statement.executeUpdate();
            
            LOGGER.info("Product deleted: " + productId);
            return affectedRows > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting product: " + productId, e);
            throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean existsByCode(String productCode) {
        String sql = "SELECT COUNT(*) FROM product WHERE product_code = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, productCode);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking product code existence: " + productCode, e);
            throw new RuntimeException("Failed to check product code: " + e.getMessage(), e);
        }
    }
    
    @Override
    public long getTotalCount() {
        return getCount("SELECT COUNT(*) FROM product");
    }
    
    @Override
    public long getActiveCount() {
        return getCount("SELECT COUNT(*) FROM product WHERE is_active = 1");
    }
    
    @Override
    public long getDiscountedCount() {
        return getCount("SELECT COUNT(*) FROM product WHERE (discount_percentage > 0 OR discount_amount > 0) AND is_active = 1");
    }
    
    // Helper methods
    private List<ProductNew> executeQuery(String sql) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            List<ProductNew> products = new ArrayList<>();
            while (resultSet.next()) {
                products.add(mapResultSetToProduct(resultSet));
            }
            return products;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing query: " + sql, e);
            throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
        }
    }
    
    private long getCount(String sql) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting count: " + sql, e);
            throw new RuntimeException("Failed to get count: " + e.getMessage(), e);
        }
    }
    
    private ProductNew mapResultSetToProduct(ResultSet resultSet) throws SQLException {
        return new ProductNew(
            resultSet.getLong("product_id"),
            resultSet.getString("product_code"),
            resultSet.getString("product_name"),
            resultSet.getString("description"),
            resultSet.getString("brand"),
            resultSet.getBigDecimal("base_price"),
            resultSet.getString("unit_of_measure"),
            resultSet.getLong("subcategory_id"),
            resultSet.getBigDecimal("discount_percentage"),
            resultSet.getBigDecimal("discount_amount"),
            resultSet.getBigDecimal("final_price"),
            resultSet.getBoolean("is_active"),
            resultSet.getTimestamp("created_at").toLocalDateTime(),
            resultSet.getLong("created_by")
        );
    }
}