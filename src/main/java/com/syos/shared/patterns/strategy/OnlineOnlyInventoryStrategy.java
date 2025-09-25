package com.syos.shared.patterns.strategy;

/**
 * Strategy for handling online-only inventory.
 * 
 * This strategy places all inventory in online storage, suitable for
 * digital products or products sold exclusively online without physical
 * shelf management requirements.
 * 
 * Implements Strategy Pattern for online inventory operations.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class OnlineOnlyInventoryStrategy implements InventoryTypeStrategy {
    
    @Override
    public InventoryConfiguration configureInventory(int totalQuantity) {
        int physicalQty = 0; // No physical storage
        int shelfQty = 0;    // No shelf display
        int onlineQty = totalQuantity; // All online
        
        String summary = String.format(
            "Online Store Distribution:\n" +
            "- Warehouse Stock: %d units\n" +
            "- Shelf Display: %d units\n" +
            "- Online Availability: %d units (100%%)\n" +
            "Note: No physical storage or shelf configuration required",
            physicalQty, shelfQty, onlineQty
        );
        
        return new InventoryConfiguration(
            physicalQty, shelfQty, onlineQty, false, summary
        );
    }
    
    @Override
    public String getTypeName() {
        return "Online Store Only";
    }
    
    @Override
    public String getDescription() {
        return "Product will be available exclusively online. " +
               "No physical storage or shelf management required.";
    }
    
    @Override
    public boolean requiresPhysicalStorage() {
        return false;
    }
}