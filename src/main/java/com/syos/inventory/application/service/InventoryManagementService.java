package com.syos.inventory.application.service;

import com.syos.domain.entities.Product;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InventoryManagementService handles all inventory operations including
 * physical inventory (warehouse/shelf), online inventory, and batch tracking.
 * 
 * This service properly utilizes the physical_inventory, online_inventory,
 * and inventory_location tables to track stock across multiple locations.
 */
public class InventoryManagementService {
    
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    /**
     * Inventory location types (matching database schema constraints)
     */
    public enum LocationType {
        WAREHOUSE,        // Warehouse storage (maps to database WAREHOUSE type)
        PHYSICAL_SHELF,   // Retail display shelf (maps to database PHYSICAL_SHELF type)
        ONLINE_INVENTORY  // Online store inventory (maps to database ONLINE_INVENTORY type)
    }
    
    /**
     * Movement types for inventory transactions
     */
    public enum MovementType {
        STOCK_IN,
        STOCK_OUT,
        TRANSFER,
        ADJUSTMENT
    }
    
    /**
     * Creates initial inventory records for a new product
     */
    public boolean createProductInventory(Product product, int initialPhysicalQty, 
                                        int initialShelfQty, int initialOnlineQty, String expiryDate) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                conn.setAutoCommit(false);
                
                // Create batch record first
                String batchCode = generateBatchCode(product.getProductCode().getValue());
                Long batchId = createBatch(conn, product.getProductCode().getValue(), batchCode, 
                                         initialPhysicalQty + initialShelfQty + initialOnlineQty, expiryDate);
                
                if (batchId == null) {
                    conn.rollback();
                    return false;
                }
                
                // Get location IDs for the 3 locations
                Long warehouseLocationId = getLocationId(conn, "WAREHOUSE");
                Long shelfLocationId = getLocationId(conn, "SHELF");  
                Long onlineLocationId = getLocationId(conn, "ONLINE");
                
                // Create warehouse inventory
                if (initialPhysicalQty > 0) {
                    createPhysicalInventory(conn, product.getProductCode().getValue(), 
                                          warehouseLocationId, initialPhysicalQty, batchId);
                }
                
                // Create shelf inventory 
                if (initialShelfQty > 0) {
                    createPhysicalInventory(conn, product.getProductCode().getValue(), 
                                          shelfLocationId, initialShelfQty, batchId);
                }
                
                // Create online inventory
                if (initialOnlineQty > 0) {
                    createOnlineInventory(conn, product.getProductCode().getValue(), 
                                        onlineLocationId, initialOnlineQty, batchId);
                }
                
                conn.commit();
                return true;
                
            }
        } catch (Exception e) {
            System.err.println("Error creating product inventory: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets total inventory quantity for a product across all locations
     */
    public int getTotalInventoryQuantity(String productCode) {
        int total = 0;
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                Long productId = getProductId(conn, productCode);
                if (productId == null) return 0;
                
                // Get physical inventory via batch table
                String physicalSql = "SELECT SUM(pi.current_quantity) FROM physical_inventory pi " +
                                   "JOIN batch b ON pi.batch_id = b.batch_id " +
                                   "WHERE b.product_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(physicalSql)) {
                    stmt.setLong(1, productId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        total += rs.getInt(1);
                    }
                }
                
                // Get online inventory via batch table
                String onlineSql = "SELECT SUM(oi.available_quantity) FROM online_inventory oi " +
                                 "JOIN batch b ON oi.batch_id = b.batch_id " +
                                 "WHERE b.product_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(onlineSql)) {
                    stmt.setLong(1, productId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        total += rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting total inventory: " + e.getMessage());
        }
        
        return total;
    }
    
    /**
     * Gets inventory breakdown by location type
     */
    public Map<LocationType, Integer> getInventoryByLocation(String productCode) {
        Map<LocationType, Integer> inventory = new HashMap<>();
        inventory.put(LocationType.WAREHOUSE, 0);
        inventory.put(LocationType.PHYSICAL_SHELF, 0);
        inventory.put(LocationType.ONLINE_INVENTORY, 0);
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                Long productId = getProductId(conn, productCode);
                if (productId == null) return inventory;
                
                String sql = "SELECT il.location_type, " +
                           "COALESCE(SUM(pi.current_quantity), 0) as physical_qty, " +
                           "COALESCE(SUM(oi.available_quantity), 0) as online_qty " +
                           "FROM inventory_location il " +
                           "LEFT JOIN physical_inventory pi ON il.location_id = pi.location_id " +
                           "LEFT JOIN batch b1 ON pi.batch_id = b1.batch_id AND b1.product_id = ? " +
                           "LEFT JOIN online_inventory oi ON il.location_type = 'ONLINE_INVENTORY' " +
                           "LEFT JOIN batch b2 ON oi.batch_id = b2.batch_id AND b2.product_id = ? " +
                           "WHERE il.is_active = 1 " +
                           "GROUP BY il.location_type";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, productId);
                    stmt.setLong(2, productId);
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        String locationType = rs.getString("location_type");
                        int physicalQty = rs.getInt("physical_qty");
                        int onlineQty = rs.getInt("online_qty");
                        
                        LocationType type = LocationType.valueOf(locationType);
                        inventory.put(type, physicalQty + onlineQty);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting inventory by location: " + e.getMessage());
        }
        
        return inventory;
    }
    
    /**
     * Moves inventory between locations
     */
    public boolean transferInventory(String productCode, String fromLocationCode, 
                                   String toLocationCode, int quantity, String reason) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                conn.setAutoCommit(false);
                
                // Get location IDs
                Long fromLocationId = getLocationId(conn, fromLocationCode);
                Long toLocationId = getLocationId(conn, toLocationCode);
                
                if (fromLocationId == null || toLocationId == null) {
                    System.err.println("Invalid location codes");
                    return false;
                }
                
                // Check available quantity at source
                int availableQty = getPhysicalInventoryQuantity(conn, productCode, fromLocationId);
                if (availableQty < quantity) {
                    System.err.println("Insufficient quantity for transfer");
                    conn.rollback();
                    return false;
                }
                
                // Reduce quantity at source
                reducePhysicalInventory(conn, productCode, fromLocationId, quantity);
                
                // Add quantity at destination
                addPhysicalInventory(conn, productCode, toLocationId, quantity);
                
                // Log the transfer (if inventory_movement table exists)
                logInventoryMovement(conn, productCode, quantity, MovementType.TRANSFER, 
                                   "Transfer from " + fromLocationCode + " to " + toLocationCode + ": " + reason);
                
                conn.commit();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error transferring inventory: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Adjusts inventory quantity (for corrections, damage, etc.)
     */
    public boolean adjustInventory(String productCode, String locationCode, 
                                 int adjustmentQuantity, String reason) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                conn.setAutoCommit(false);
                
                Long locationId = getLocationId(conn, locationCode);
                if (locationId == null) {
                    System.err.println("Invalid location code: " + locationCode);
                    return false;
                }
                
                // Apply adjustment
                if (adjustmentQuantity > 0) {
                    addPhysicalInventory(conn, productCode, locationId, adjustmentQuantity);
                } else {
                    int currentQty = getPhysicalInventoryQuantity(conn, productCode, locationId);
                    int reduceBy = Math.abs(adjustmentQuantity);
                    
                    if (currentQty < reduceBy) {
                        System.err.println("Cannot reduce inventory below zero");
                        conn.rollback();
                        return false;
                    }
                    
                    reducePhysicalInventory(conn, productCode, locationId, reduceBy);
                }
                
                // Log the adjustment
                logInventoryMovement(conn, productCode, adjustmentQuantity, MovementType.ADJUSTMENT, 
                                   "Inventory adjustment at " + locationCode + ": " + reason);
                
                conn.commit();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error adjusting inventory: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets products that need reordering based on reorder levels
     */
    public List<Map<String, Object>> getReorderAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT p.product_code, p.product_name, " +
                           "COALESCE(SUM(pi.current_quantity), 0) as total_quantity " +
                           "FROM product p " +
                           "LEFT JOIN batch b ON p.product_id = b.product_id " +
                           "LEFT JOIN physical_inventory pi ON b.batch_id = pi.batch_id " +
                           "JOIN inventory_location il ON pi.location_id = il.location_id " +
                           "WHERE p.is_active = 1 AND il.location_code = 'SHELF' " +
                           "GROUP BY p.product_code, p.product_name " +
                           "HAVING total_quantity <= 10 " +
                           "ORDER BY total_quantity ASC";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        Map<String, Object> alert = new HashMap<>();
                        alert.put("productCode", rs.getString("product_code"));
                        alert.put("productName", rs.getString("product_name"));
                        alert.put("currentQuantity", rs.getInt("total_quantity"));
                        alert.put("reorderLevel", 10); // Default threshold
                        alerts.add(alert);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting reorder alerts: " + e.getMessage());
        }
        
        return alerts;
    }
    
    // Private helper methods
    
    private Long getProductId(Connection conn, String productCode) throws Exception {
        String sql = "SELECT product_id FROM product WHERE product_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("product_id");
            }
        }
        return null;
    }
    
    private double getProductFinalPrice(Connection conn, Long productId) throws Exception {
        String sql = "SELECT final_price FROM product WHERE product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("final_price");
            }
        }
        return 0.0;
    }
    
    private Long createBatch(Connection conn, String productCode, String batchCode, int totalQuantity, String expiryDate) {
        try {
            // First get product_id from product_code
            Long productId = getProductId(conn, productCode);
            if (productId == null) {
                System.err.println("Product not found: " + productCode);
                return null;
            }
            
            // Get the product's final price (after discount) for the batch selling price
            double productPrice = getProductFinalPrice(conn, productId);
            
            String sql = "INSERT INTO batch (product_id, batch_number, purchase_date, expiry_date, quantity_received, selling_price) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, productId);
                stmt.setString(2, batchCode);
                stmt.setString(3, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                
                // Use provided expiry date or default to 2 years if null/N/A
                String finalExpiryDate;
                if (expiryDate != null && !expiryDate.trim().isEmpty() && 
                    !expiryDate.trim().equalsIgnoreCase("N/A") && !expiryDate.trim().equalsIgnoreCase("null")) {
                    finalExpiryDate = expiryDate.trim();
                } else {
                    finalExpiryDate = LocalDateTime.now().plusYears(2).format(DateTimeFormatter.ISO_LOCAL_DATE);
                }
                
                stmt.setString(4, finalExpiryDate);
                stmt.setInt(5, totalQuantity);
                stmt.setDouble(6, productPrice); // Use the product's final price
                
                int affected = stmt.executeUpdate();
                if (affected > 0) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating batch: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    private String generateBatchCode(String productCode) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return "B-" + productCode + "-" + timestamp;
    }
    
    private void createPhysicalInventory(Connection conn, String productCode, Long locationId, 
                                       int quantity, Long batchId) throws Exception {
        // Physical_inventory table schema: batch_id, location_id, current_quantity, min_threshold, location_capacity
        String sql = "INSERT INTO physical_inventory (batch_id, location_id, current_quantity, min_threshold, location_capacity) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, batchId);
            stmt.setLong(2, locationId);
            stmt.setInt(3, quantity);
            stmt.setInt(4, 5);  // Default min threshold
            stmt.setInt(5, 20); // Default location capacity
            stmt.executeUpdate();
        }
    }
    
    private void createOnlineInventory(Connection conn, String productCode, Long locationId, 
                                     int quantity, Long batchId) throws Exception {
        // Online_inventory table schema: batch_id, available_quantity, reserved_quantity
        String sql = "INSERT INTO online_inventory (batch_id, available_quantity, reserved_quantity) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, batchId);
            stmt.setInt(2, quantity);
            stmt.setInt(3, 0); // Default reserved quantity
            stmt.executeUpdate();
        }
    }
    

    
    private Long getLocationId(Connection conn, String locationCode) throws Exception {
        String sql = "SELECT location_id FROM inventory_location WHERE location_code = ? AND is_active = 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, locationCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("location_id");
            }
        }
        return null;
    }
    
    private int getPhysicalInventoryQuantity(Connection conn, String productCode, Long locationId) throws Exception {
        Long productId = getProductId(conn, productCode);
        if (productId == null) return 0;
        
        String sql = "SELECT SUM(pi.current_quantity) FROM physical_inventory pi " +
                    "JOIN batch b ON pi.batch_id = b.batch_id " +
                    "WHERE b.product_id = ? AND pi.location_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            stmt.setLong(2, locationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    private void addPhysicalInventory(Connection conn, String productCode, Long locationId, int quantity) throws Exception {
        Long productId = getProductId(conn, productCode);
        if (productId == null) return;
        
        // Check if record exists for this product/location via batch
        String checkSql = "SELECT pi.inventory_id, pi.batch_id FROM physical_inventory pi " +
                         "JOIN batch b ON pi.batch_id = b.batch_id " +
                         "WHERE b.product_id = ? AND pi.location_id = ? LIMIT 1";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setLong(1, productId);
            checkStmt.setLong(2, locationId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Update existing record
                String updateSql = "UPDATE physical_inventory SET current_quantity = current_quantity + ? WHERE inventory_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, quantity);
                    updateStmt.setLong(2, rs.getLong("inventory_id"));
                    updateStmt.executeUpdate();
                }
            } else {
                // Create new record (need batch_id)
                Long batchId = getOrCreateBatchForProduct(conn, productCode);
                createPhysicalInventory(conn, productCode, locationId, quantity, batchId);
            }
        }
    }
    
    private void reducePhysicalInventory(Connection conn, String productCode, Long locationId, int quantity) throws Exception {
        Long productId = getProductId(conn, productCode);
        if (productId == null) return;
        
        String sql = "UPDATE physical_inventory SET current_quantity = current_quantity - ? " +
                    "WHERE batch_id IN (SELECT batch_id FROM batch WHERE product_id = ?) " +
                    "AND location_id = ? AND current_quantity >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setLong(2, productId);
            stmt.setLong(3, locationId);
            stmt.setInt(4, quantity);
            stmt.executeUpdate();
        }
    }
    
    private Long getOrCreateBatchForProduct(Connection conn, String productCode) throws Exception {
        Long productId = getProductId(conn, productCode);
        if (productId == null) return null;
        
        // Try to get existing batch
        String getBatchSql = "SELECT batch_id FROM batch WHERE product_id = ? ORDER BY batch_id DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(getBatchSql)) {
            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("batch_id");
            }
        }
        
        // Create new batch if none exists
        return createBatch(conn, productCode, generateBatchCode(productCode), 0, null);
    }
    
    private void logInventoryMovement(Connection conn, String productCode, int quantity, 
                                    MovementType movementType, String notes) {
        try {
            // Check if inventory_movement table exists (it might not be in schema)
            String checkTableSql = "SELECT name FROM sqlite_master WHERE type='table' AND name='inventory_movement'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkTableSql)) {
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    // Table doesn't exist, skip logging
                    return;
                }
            }
            
            String sql = "INSERT INTO inventory_movement (product_code, movement_type, quantity, movement_date, notes) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, productCode);
                stmt.setString(2, movementType.name());
                stmt.setInt(3, quantity);
                stmt.setString(4, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                stmt.setString(5, notes);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            // Log movement failed, but don't fail the whole operation
            System.err.println("Warning: Could not log inventory movement: " + e.getMessage());
        }
    }
}