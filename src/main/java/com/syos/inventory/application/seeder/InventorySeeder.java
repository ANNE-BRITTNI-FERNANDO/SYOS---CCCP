package com.syos.inventory.application.seeder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * InventorySeeder is responsible for creating default inventory locations
 * and setting up the basic inventory infrastructure when the application starts.
 */
public class InventorySeeder {
    
    private static final Logger logger = Logger.getLogger(InventorySeeder.class.getName());
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    /**
     * Seeds the default inventory locations if they don't exist
     */
    public void seedInventoryLocations() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Connect to the database
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            Connection conn = DriverManager.getConnection(url);
            
            logger.info("Checking for default inventory locations...");
            
            // Check if locations already exist
            Statement checkStmt = conn.createStatement();
            ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM inventory_location");
            rs.next();
            int locationCount = rs.getInt(1);
            
            if (locationCount > 0) {
                logger.info("Inventory locations already exist - skipping creation");
                conn.close();
                return;
            }
            
            // Create default inventory locations
            String insertLocationSQL = "INSERT INTO inventory_location (location_code, location_name, location_type, is_active) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertLocationSQL);
            
            // 1. Main Warehouse
            pstmt.setString(1, "WH-MAIN");
            pstmt.setString(2, "Main Warehouse");
            pstmt.setString(3, "WAREHOUSE");
            pstmt.setBoolean(4, true);
            pstmt.executeUpdate();
            
            // 2. Physical Store Shelf
            pstmt.setString(1, "SHELF-01");
            pstmt.setString(2, "Store Shelf Section 1");
            pstmt.setString(3, "PHYSICAL_SHELF");
            pstmt.setBoolean(4, true);
            pstmt.executeUpdate();
            
            // 3. Physical Store Shelf 2
            pstmt.setString(1, "SHELF-02");
            pstmt.setString(2, "Store Shelf Section 2");
            pstmt.setString(3, "PHYSICAL_SHELF");
            pstmt.setBoolean(4, true);
            pstmt.executeUpdate();
            
            // 4. Online Inventory Location
            pstmt.setString(1, "ONLINE-01");
            pstmt.setString(2, "Online Store Inventory");
            pstmt.setString(3, "ONLINE_INVENTORY");
            pstmt.setBoolean(4, true);
            pstmt.executeUpdate();
            
            logger.info("Default inventory locations created successfully:");
            logger.info("- WH-MAIN: Main Warehouse");
            logger.info("- SHELF-01: Store Shelf Section 1");
            logger.info("- SHELF-02: Store Shelf Section 2");
            logger.info("- ONLINE-01: Online Store Inventory");
            
            conn.close();
            
        } catch (Exception e) {
            logger.severe("Failed to seed inventory locations: " + e.getMessage());
            throw new RuntimeException("Critical error: Unable to create default inventory locations", e);
        }
    }
    
    /**
     * Seeds initial sales channels if they don't exist
     */
    public void seedSalesChannels() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Connect to the database
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            Connection conn = DriverManager.getConnection(url);
            
            logger.info("Checking for default sales channels...");
            
            // Check if sales channels already exist
            Statement checkStmt = conn.createStatement();
            ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM sales_channel");
            rs.next();
            int channelCount = rs.getInt(1);
            
            if (channelCount > 0) {
                logger.info("Sales channels already exist - skipping creation");
                conn.close();
                return;
            }
            
            // Create default sales channels
            String insertChannelSQL = "INSERT INTO sales_channel (channel_code, channel_name, channel_type, is_active) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertChannelSQL);
            
            // 1. POS System
            pstmt.setString(1, "POS-01");
            pstmt.setString(2, "Main POS Terminal");
            pstmt.setString(3, "POS");
            pstmt.setBoolean(4, true);
            pstmt.executeUpdate();
            
            // 2. Online Store
            pstmt.setString(1, "WEB-01");
            pstmt.setString(2, "Online Web Store");
            pstmt.setString(3, "ONLINE");
            pstmt.setBoolean(4, true);
            pstmt.executeUpdate();
            
            logger.info("Default sales channels created successfully:");
            logger.info("- POS-01: Main POS Terminal");
            logger.info("- WEB-01: Online Web Store");
            
            conn.close();
            
        } catch (Exception e) {
            logger.severe("Failed to seed sales channels: " + e.getMessage());
            throw new RuntimeException("Critical error: Unable to create default sales channels", e);
        }
    }
}