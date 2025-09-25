package com.syos.shared.patterns.strategy;

/**
 * Strategy for handling physical inventory with shelf management.
 * 
 * This strategy distributes inventory between warehouse and shelf storage,
 * requiring shelf capacity configuration and physical location management.
 * 
 * Implements Strategy Pattern for physical inventory operations.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class PhysicalInventoryStrategy implements InventoryTypeStrategy {
    
    private static final double WAREHOUSE_RATIO = 0.8; // 80% to warehouse
    private static final double SHELF_RATIO = 0.2;     // 20% to shelf
    
    @Override
    public InventoryConfiguration configureInventory(int totalQuantity) {
        int physicalQty = (int) (totalQuantity * WAREHOUSE_RATIO);
        int shelfQty = totalQuantity - physicalQty;
        int onlineQty = 0; // Physical only
        
        String summary = String.format(
            "Physical Store Distribution:\n" +
            "- Warehouse Stock: %d units (%.0f%%)\n" +
            "- Shelf Display: %d units (%.0f%%)\n" +
            "- Online Availability: %d units\n" +
            "Note: Requires shelf capacity configuration",
            physicalQty, WAREHOUSE_RATIO * 100,
            shelfQty, SHELF_RATIO * 100,
            onlineQty
        );
        
        return new InventoryConfiguration(
            physicalQty, shelfQty, onlineQty, true, summary
        );
    }
    
    @Override
    public String getTypeName() {
        return "Physical Store Only";
    }
    
    @Override
    public String getDescription() {
        return "Product will be stored in physical locations with shelf management. " +
               "Requires shelf capacity setup and physical inventory tracking.";
    }
    
    @Override
    public boolean requiresPhysicalStorage() {
        return true;
    }
}