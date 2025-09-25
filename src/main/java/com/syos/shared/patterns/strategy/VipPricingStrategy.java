package com.syos.shared.patterns.strategy;

import java.math.BigDecimal;

/**
 * VIP customer pricing strategy with special discounts.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class VipPricingStrategy implements PricingStrategy {
    
    private final BigDecimal vipDiscountPercentage;
    
    /**
     * Constructor
     * 
     * @param vipDiscountPercentage VIP discount percentage
     */
    public VipPricingStrategy(BigDecimal vipDiscountPercentage) {
        this.vipDiscountPercentage = vipDiscountPercentage;
    }
    
    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, int quantity, String customerType) {
        BigDecimal totalPrice = basePrice.multiply(new BigDecimal(quantity));
        
        if ("VIP".equals(customerType)) {
            BigDecimal discount = totalPrice.multiply(vipDiscountPercentage).divide(new BigDecimal("100"));
            totalPrice = totalPrice.subtract(discount);
        }
        
        return totalPrice;
    }
    
    @Override
    public String getStrategyName() {
        return String.format("VIP Pricing (%.1f%% discount)", vipDiscountPercentage);
    }
    
    @Override
    public boolean isApplicable(BigDecimal basePrice, int quantity, String customerType) {
        return "VIP".equals(customerType);
    }
}