package com.syos.shared.patterns.strategy;

import java.math.BigDecimal;

/**
 * Bulk discount pricing strategy for large quantity purchases.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class BulkDiscountPricingStrategy implements PricingStrategy {
    
    private final int minimumQuantity;
    private final BigDecimal discountPercentage;
    
    /**
     * Constructor
     * 
     * @param minimumQuantity Minimum quantity to qualify for bulk discount
     * @param discountPercentage Discount percentage (e.g., 10 for 10%)
     */
    public BulkDiscountPricingStrategy(int minimumQuantity, BigDecimal discountPercentage) {
        this.minimumQuantity = minimumQuantity;
        this.discountPercentage = discountPercentage;
    }
    
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, int quantity, String customerType) {
        BigDecimal totalPrice = basePrice.multiply(new BigDecimal(quantity));
        
        if (quantity >= minimumQuantity) {
            BigDecimal discount = totalPrice.multiply(discountPercentage).divide(new BigDecimal("100"));
            totalPrice = totalPrice.subtract(discount);
        }
        
        return totalPrice;
    }
    
    @Override
    public String getStrategyName() {
        return String.format("Bulk Discount (%.1f%% for %d+ items)", discountPercentage, minimumQuantity);
    }
    
    @Override
    public boolean isApplicable(BigDecimal basePrice, int quantity, String customerType) {
        return quantity >= minimumQuantity;
    }
}