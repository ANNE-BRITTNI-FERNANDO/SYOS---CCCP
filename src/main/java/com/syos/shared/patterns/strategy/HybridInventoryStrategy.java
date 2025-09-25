package com.syos.shared.patterns.strategy;

/**
 * Strategy for handling hybrid inventory (both physical and online).
 * 
 * This strategy distributes inventory across physical and online channels,
 * allowing products to be sold both in-store and online with optimized
 * distribution ratios.
 * 
 * Implements Strategy Pattern for hybrid inventory operations.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class HybridInventoryStrategy implements InventoryTypeStrategy {
    
    private static final double PHYSICAL_RATIO = 0.6;  // 60% physical
    private static final double SHELF_RATIO = 0.25;    // 25% of physical goes to shelf
    
    @Override
    public InventoryConfiguration configureInventory(int totalQuantity) {
        int totalPhysical = (int) (totalQuantity * PHYSICAL_RATIO);
        int shelfQty = (int) (totalPhysical * SHELF_RATIO);
        int physicalQty = totalPhysical - shelfQty;
        int onlineQty = totalQuantity - totalPhysical;
        
        String summary = String.format(
            "Hybrid Store Distribution:\n" +
            "- Warehouse Stock: %d units (%.0f%%)\n" +
            "- Shelf Display: %d units (%.0f%%)\n" +
            "- Online Availability: %d units (%.0f%%)\n" +
            "Note: Requires shelf capacity configuration for physical portion",
            physicalQty, ((double) physicalQty / totalQuantity) * 100,
            shelfQty, ((double) shelfQty / totalQuantity) * 100,
            onlineQty, ((double) onlineQty / totalQuantity) * 100
        );
        
        return new InventoryConfiguration(
            physicalQty, shelfQty, onlineQty, true, summary
        );
    }
    
    @Override
    public String getTypeName() {
        return "Hybrid (Physical + Online)";
    }
    
    @Override
    public String getDescription() {
        return "Product will be available both in physical store and online. " +
               "Inventory is distributed across channels with shelf management for physical portion.";
    }
    
    @Override
    public boolean requiresPhysicalStorage() {
        return true;
    }
}