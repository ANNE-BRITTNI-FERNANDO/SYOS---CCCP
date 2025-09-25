package com.syos.shared.patterns.strategy;

import java.math.BigDecimal;

/**
 * Strategy interface for different pricing algorithms.
 * 
 * Implements the Strategy Pattern to allow different pricing
 * strategies to be used interchangeably.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public interface PricingStrategy {
    
    /**
     * Calculate the final price based on the strategy.
     * 
     * @param basePrice The original price of the product
     * @param quantity The quantity being purchased
     * @param customerType Type of customer (REGULAR, VIP, WHOLESALE)
     * @return The calculated final price
     */
    BigDecimal calculatePrice(BigDecimal basePrice, int quantity, String customerType);
    
    /**
     * Get the strategy name for identification.
     * 
     * @return Strategy name
     */
    String getStrategyName();
    
    /**
     * Check if this strategy is applicable for the given parameters.
     * 
     * @param basePrice Base price
     * @param quantity Quantity
     * @param customerType Customer type
     * @return true if strategy is applicable
     */
    boolean isApplicable(BigDecimal basePrice, int quantity, String customerType);
}