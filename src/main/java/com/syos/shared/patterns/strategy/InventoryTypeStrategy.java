package com.syos.shared.patterns.strategy;

/**
 * Strategy interface for handling different inventory type operations.
 * 
 * Implements the Strategy Pattern to handle different inventory types:
 * - Physical inventory (requires shelf capacity, location management)
 * - Online inventory (digital stock, no physical constraints)
 * - Hybrid inventory (combination of both)
 * 
 * This follows the Open/Closed Principle by allowing new inventory types
 * to be added without modifying existing code.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public interface InventoryTypeStrategy {
    
    /**
     * Configuration result for inventory setup
     */
    class InventoryConfiguration {
        private final int physicalQuantity;
        private final int shelfQuantity;
        private final int onlineQuantity;
        private final boolean requiresShelfConfig;
        private final String configurationSummary;
        
        public InventoryConfiguration(int physicalQuantity, int shelfQuantity, 
                                   int onlineQuantity, boolean requiresShelfConfig, 
                                   String configurationSummary) {
            this.physicalQuantity = physicalQuantity;
            this.shelfQuantity = shelfQuantity;
            this.onlineQuantity = onlineQuantity;
            this.requiresShelfConfig = requiresShelfConfig;
            this.configurationSummary = configurationSummary;
        }
        
        public int getPhysicalQuantity() { return physicalQuantity; }
        public int getShelfQuantity() { return shelfQuantity; }
        public int getOnlineQuantity() { return onlineQuantity; }
        public boolean requiresShelfConfig() { return requiresShelfConfig; }
        public String getConfigurationSummary() { return configurationSummary; }
    }
    
    /**
     * Configure inventory distribution for the given total quantity
     * 
     * @param totalQuantity Total quantity to distribute
     * @return InventoryConfiguration with distribution details
     */
    InventoryConfiguration configureInventory(int totalQuantity);
    
    /**
     * Get the display name for this inventory type
     */
    String getTypeName();
    
    /**
     * Get description of how inventory will be handled
     */
    String getDescription();
    
    /**
     * Check if this inventory type requires physical storage setup
     */
    boolean requiresPhysicalStorage();
}