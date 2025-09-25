package com.syos.shared.patterns.strategy;

import java.math.BigDecimal;

/**
 * Regular pricing strategy with no special discounts.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class RegularPricingStrategy implements PricingStrategy {
    
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, int quantity, String customerType) {
        // Regular pricing: just multiply base price by quantity
        return basePrice.multiply(new BigDecimal(quantity));
    }
    
    @Override
    public String getStrategyName() {
        return "Regular Pricing";
    }
    
    @Override
    public boolean isApplicable(BigDecimal basePrice, int quantity, String customerType) {
        // Regular pricing is always applicable as fallback
        return true;
    }
}