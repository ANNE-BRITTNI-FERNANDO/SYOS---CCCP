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

/**
 * SQLite implementation of ProductNewRepository
 */
public class SqliteProductNewRepositoryImpl implements ProductNewRepository {
    private final DatabaseManager databaseManager;

    public SqliteProductNewRepositoryImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public ProductNew save(ProductNew product) {
        String sql = "INSERT INTO product (product_code, product_name, description, brand, base_price, " +
                    "unit_of_measure, subcategory_id, discount_percentage, discount_amount, " +
                    "final_price, is_active, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getProductCode());
            stmt.setString(2, product.getProductName());
            stmt.setString(3, product.getDescription());
            stmt.setString(4, product.getBrand());
            stmt.setBigDecimal(5, product.getBasePrice());
            stmt.setString(6, product.getUnitOfMeasure());
            stmt.setLong(7, product.getSubcategoryId());
            stmt.setBigDecimal(8, product.getDiscountPercentage());
            stmt.setBigDecimal(9, product.getDiscountAmount());
            stmt.setBigDecimal(10, product.getFinalPrice());
            stmt.setBoolean(11, product.isActive());
            stmt.setLong(12, product.getCreatedBy());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setProductId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating product failed, no ID obtained.");
                }
            }

            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving product: " + e.getMessage(), e);
        }
    }

    @Override
    public ProductNew update(ProductNew product) {
        String sql = "UPDATE product SET product_name = ?, description = ?, brand = ?, base_price = ?, " +
                    "unit_of_measure = ?, discount_percentage = ?, discount_amount = ?, " +
                    "final_price = ?, is_active = ? WHERE product_id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getDescription());
            stmt.setString(3, product.getBrand());
            stmt.setBigDecimal(4, product.getBasePrice());
            stmt.setString(5, product.getUnitOfMeasure());
            stmt.setBigDecimal(6, product.getDiscountPercentage());
            stmt.setBigDecimal(7, product.getDiscountAmount());
            stmt.setBigDecimal(8, product.getFinalPrice());
            stmt.setBoolean(9, product.isActive());
            stmt.setLong(10, product.getProductId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating product failed, no rows affected.");
            }

            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating product: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ProductNew> findById(Long productId) {
        String sql = "SELECT * FROM product WHERE product_id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ProductNew product = mapResultSetToProduct(rs);
                rs.close();
                return Optional.of(product);
            }
            rs.close();
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding product by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ProductNew> findByCode(String productCode) {
        String sql = "SELECT * FROM product WHERE product_code = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ProductNew product = mapResultSetToProduct(rs);
                rs.close();
                return Optional.of(product);
            }
            rs.close();
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding product by code: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductNew> findAll() {
        String sql = "SELECT * FROM product ORDER BY product_name";
        return executeQuery(sql);
    }

    @Override
    public List<ProductNew> findAllActive() {
        String sql = "SELECT * FROM product WHERE is_active = 1 ORDER BY product_name";
        return executeQuery(sql);
    }

    @Override
    public List<ProductNew> findBySubcategory(Long subcategoryId) {
        String sql = "SELECT * FROM product WHERE subcategory_id = ? AND is_active = 1 ORDER BY product_name";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, subcategoryId);
            return executeQueryWithStatement(stmt);

        } catch (SQLException e) {
            throw new RuntimeException("Error finding products by subcategory: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductNew> findWithDiscounts() {
        String sql = "SELECT * FROM product WHERE is_active = 1 AND (discount_percentage > 0 OR discount_amount > 0) ORDER BY product_name";
        return executeQuery(sql);
    }

    @Override
    public List<ProductNew> searchByNameOrDescription(String searchTerm) {
        String sql = "SELECT * FROM product WHERE is_active = 1 AND (product_name LIKE ? OR description LIKE ?) ORDER BY product_name";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            return executeQueryWithStatement(stmt);

        } catch (SQLException e) {
            throw new RuntimeException("Error searching products: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Long productId) {
        String sql = "DELETE FROM product WHERE product_id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, productId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting product: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByCode(String productCode) {
        String sql = "SELECT COUNT(*) FROM product WHERE product_code = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();

            boolean exists = rs.next() && rs.getInt(1) > 0;
            rs.close();
            return exists;

        } catch (SQLException e) {
            throw new RuntimeException("Error checking product code existence: " + e.getMessage(), e);
        }
    }

    @Override
    public long getTotalCount() {
        String sql = "SELECT COUNT(*) FROM product";
        return executeCountQuery(sql);
    }

    @Override
    public long getActiveCount() {
        String sql = "SELECT COUNT(*) FROM product WHERE is_active = 1";
        return executeCountQuery(sql);
    }

    @Override
    public long getDiscountedCount() {
        String sql = "SELECT COUNT(*) FROM product WHERE is_active = 1 AND (discount_percentage > 0 OR discount_amount > 0)";
        return executeCountQuery(sql);
    }

    private List<ProductNew> executeQuery(String sql) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            return executeQueryWithStatement(stmt);

        } catch (SQLException e) {
            throw new RuntimeException("Error executing query: " + e.getMessage(), e);
        }
    }

    private List<ProductNew> executeQueryWithStatement(PreparedStatement stmt) throws SQLException {
        List<ProductNew> products = new ArrayList<>();
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            products.add(mapResultSetToProduct(rs));
        }
        rs.close();
        return products;
    }

    private long executeCountQuery(String sql) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getLong(1) : 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error executing count query: " + e.getMessage(), e);
        }
    }

    private ProductNew mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new ProductNew(
                rs.getLong("product_id"),
                rs.getString("product_code"),
                rs.getString("product_name"),
                rs.getString("description"),
                rs.getString("brand"),
                rs.getBigDecimal("base_price"),
                rs.getString("unit_of_measure"),
                rs.getLong("subcategory_id"),
                rs.getBigDecimal("discount_percentage"),
                rs.getBigDecimal("discount_amount"),
                rs.getBigDecimal("final_price"),
                rs.getBoolean("is_active"),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getLong("created_by")
        );
    }
}