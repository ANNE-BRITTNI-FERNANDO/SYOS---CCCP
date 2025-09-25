package com.syos.shared.patterns.strategy;

import java.util.Arrays;
import java.util.List;

/**
 * Context class for inventory type strategy pattern.
 * 
 * Manages different inventory type strategies and provides a unified interface
 * for inventory configuration. Follows the Strategy Pattern to allow different
 * inventory handling approaches without changing client code.
 * 
 * Supports Single Responsibility Principle by focusing only on strategy management
 * and Open/Closed Principle by allowing new strategies without modification.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class InventoryTypeContext {
    
    private InventoryTypeStrategy strategy;
    
    // Available strategies
    private static final List<InventoryTypeStrategy> AVAILABLE_STRATEGIES = Arrays.asList(
        new PhysicalInventoryStrategy(),
        new OnlineOnlyInventoryStrategy(),
        new HybridInventoryStrategy()
    );
    
    /**
     * Set the inventory type strategy
     */
    public void setStrategy(InventoryTypeStrategy strategy) {
        this.strategy = strategy;
    }
    
    /**
     * Set strategy by index (1-based for UI compatibility)
     */
    public void setStrategyByIndex(int index) {
        if (index >= 1 && index <= AVAILABLE_STRATEGIES.size()) {
            this.strategy = AVAILABLE_STRATEGIES.get(index - 1);
        } else {
            throw new IllegalArgumentException("Invalid strategy index: " + index);
        }
    }
    
    /**
     * Configure inventory using current strategy
     */
    public InventoryTypeStrategy.InventoryConfiguration configureInventory(int totalQuantity) {
        if (strategy == null) {
            throw new IllegalStateException("No inventory strategy set");
        }
        return strategy.configureInventory(totalQuantity);
    }
    
    /**
     * Get current strategy
     */
    public InventoryTypeStrategy getCurrentStrategy() {
        return strategy;
    }
    
    /**
     * Get all available strategies for display
     */
    public static List<InventoryTypeStrategy> getAvailableStrategies() {
        return AVAILABLE_STRATEGIES;
    }
    
    /**
     * Get strategy by index (1-based)
     */
    public static InventoryTypeStrategy getStrategyByIndex(int index) {
        if (index >= 1 && index <= AVAILABLE_STRATEGIES.size()) {
            return AVAILABLE_STRATEGIES.get(index - 1);
        }
        return null;
    }
    
    /**
     * Display all available inventory type options
     */
    public static void displayInventoryTypeOptions() {
        System.out.println();
        System.out.println("INVENTORY TYPE SELECTION");
        System.out.println("==================================================");
        System.out.println("Choose how you want to manage this product's inventory:");
        System.out.println();
        
        for (int i = 0; i < AVAILABLE_STRATEGIES.size(); i++) {
            InventoryTypeStrategy strategy = AVAILABLE_STRATEGIES.get(i);
            System.out.printf("%d. %s%n", i + 1, strategy.getTypeName());
            System.out.printf("   %s%n", strategy.getDescription());
            System.out.println();
        }
    }
    
    /**
     * Validate inventory type choice
     */
    public static boolean isValidInventoryTypeChoice(int choice) {
        return choice >= 1 && choice <= AVAILABLE_STRATEGIES.size();
    }
}