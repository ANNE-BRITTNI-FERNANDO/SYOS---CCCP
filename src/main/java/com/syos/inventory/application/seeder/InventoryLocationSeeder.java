package com.syos.inventory.application.seeder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

/**
 * InventoryLocationSeeder is responsible for creating default inventory locations
 * when the application starts up if they don't already exist.
 * 
 * Creates warehouse, shelf, and online storage locations for inventory tracking.
 */
public class InventoryLocationSeeder {
    
    private static final Logger logger = Logger.getLogger(InventoryLocationSeeder.class.getName());
    
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    /**
     * Seeds the default inventory locations if they don't exist
     */
    public static void seedInventoryLocations() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Connect to the database
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            Connection conn = DriverManager.getConnection(url);
            
            logger.info("Seeding inventory locations...");
            
            // Check if locations already exist
            String checkSql = "SELECT COUNT(*) FROM inventory_location";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                logger.info("Inventory locations already exist - skipping creation");
                conn.close();
                return;
            }
            
            // Create default inventory locations (using database schema constraint values)
            String insertSql = "INSERT INTO inventory_location (location_code, location_name, location_type, is_active) VALUES (?, ?, ?, 1)";
            PreparedStatement stmt = conn.prepareStatement(insertSql);
            
            // Warehouse location (physical storage)
            stmt.setString(1, "WAREHOUSE");
            stmt.setString(2, "Main Warehouse Storage");
            stmt.setString(3, "WAREHOUSE");
            stmt.executeUpdate();
            logger.info("Created warehouse location: WAREHOUSE");
            
            // Physical shelf location (retail display)
            stmt.setString(1, "SHELF");
            stmt.setString(2, "Retail Display Shelf");
            stmt.setString(3, "PHYSICAL_SHELF");
            stmt.executeUpdate();
            logger.info("Created shelf location: SHELF");
            
            // Online inventory location
            stmt.setString(1, "ONLINE");
            stmt.setString(2, "Online Store Inventory");
            stmt.setString(3, "ONLINE_INVENTORY");
            stmt.executeUpdate();
            logger.info("Created online location: ONLINE");
            
            conn.close();
            logger.info("Inventory location seeding completed successfully!");
            
        } catch (Exception e) {
            logger.severe("Failed to seed inventory locations: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get the warehouse location ID
     */
    public static Long getWarehouseLocationId() {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            Connection conn = DriverManager.getConnection(url);
            
            String sql = "SELECT location_id FROM inventory_location WHERE location_code = 'WAREHOUSE'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Long id = rs.getLong("location_id");
                conn.close();
                return id;
            }
            
            conn.close();
        } catch (Exception e) {
            System.err.println("Error getting warehouse location ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get the shelf location ID
     */
    public static Long getShelfLocationId() {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            Connection conn = DriverManager.getConnection(url);
            
            String sql = "SELECT location_id FROM inventory_location WHERE location_code = 'SHELF'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Long id = rs.getLong("location_id");
                conn.close();
                return id;
            }
            
            conn.close();
        } catch (Exception e) {
            System.err.println("Error getting shelf location ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get the online inventory location ID
     */
    public static Long getOnlineLocationId() {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            Connection conn = DriverManager.getConnection(url);
            
            String sql = "SELECT location_id FROM inventory_location WHERE location_code = 'ONLINE'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Long id = rs.getLong("location_id");
                conn.close();
                return id;
            }
            
            conn.close();
        } catch (Exception e) {
            System.err.println("Error getting online location ID: " + e.getMessage());
        }
        return null;
    }
}