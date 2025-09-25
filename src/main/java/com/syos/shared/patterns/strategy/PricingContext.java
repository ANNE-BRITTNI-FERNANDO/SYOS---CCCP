package com.syos.shared.patterns.strategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

/**
 * Context class for pricing strategies.
 * Manages and applies different pricing strategies.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class PricingContext {
    
    private final List<PricingStrategy> strategies;
    private PricingStrategy defaultStrategy;
    
    /**
     * Constructor
     */
    public PricingContext() {
        this.strategies = new ArrayList<>();
        this.defaultStrategy = new RegularPricingStrategy();
        
        // Add default strategies
        addStrategy(defaultStrategy);
        addStrategy(new BulkDiscountPricingStrategy(10, new BigDecimal("5")));  // 5% for 10+ items
        addStrategy(new BulkDiscountPricingStrategy(25, new BigDecimal("10"))); // 10% for 25+ items
        addStrategy(new VipPricingStrategy(new BigDecimal("15")));              // 15% VIP discount
    }
    
    /**
     * Add a pricing strategy
     */
    public void addStrategy(PricingStrategy strategy) {
        if (strategy != null && !strategies.contains(strategy)) {
            strategies.add(strategy);
        }
    }
    
    /**
     * Remove a pricing strategy
     */
    public void removeStrategy(PricingStrategy strategy) {
        strategies.remove(strategy);
    }
    
    /**
     * Calculate price using the best applicable strategy
     */
    public PricingResult calculateBestPrice(BigDecimal basePrice, int quantity, String customerType) {
        PricingStrategy bestStrategy = defaultStrategy;
        BigDecimal bestPrice = basePrice.multiply(new BigDecimal(quantity));
        
        for (PricingStrategy strategy : strategies) {
            if (strategy.isApplicable(basePrice, quantity, customerType)) {
                BigDecimal price = strategy.calculatePrice(basePrice, quantity, customerType);
                
                // Choose strategy that gives the best (lowest) price for customer
                if (price.compareTo(bestPrice) < 0) {
                    bestPrice = price;
                    bestStrategy = strategy;
                }
            }
        }
        
        BigDecimal originalPrice = basePrice.multiply(new BigDecimal(quantity));
        BigDecimal savings = originalPrice.subtract(bestPrice);
        
        return new PricingResult(bestStrategy, bestPrice, originalPrice, savings);
    }
    
    /**
     * Calculate price using a specific strategy
     */
    public BigDecimal calculatePrice(PricingStrategy strategy, BigDecimal basePrice, int quantity, String customerType) {
        if (strategy.isApplicable(basePrice, quantity, customerType)) {
            return strategy.calculatePrice(basePrice, quantity, customerType);
        }
        return defaultStrategy.calculatePrice(basePrice, quantity, customerType);
    }
    
    /**
     * Get all available strategies
     */
    public List<PricingStrategy> getAvailableStrategies() {
        return new ArrayList<>(strategies);
    }
    
    /**
     * Set default strategy
     */
    public void setDefaultStrategy(PricingStrategy strategy) {
        if (strategy != null) {
            this.defaultStrategy = strategy;
        }
    }
    
    /**
     * Result class containing pricing information
     */
    public static class PricingResult {
        private final PricingStrategy strategy;
        private final BigDecimal finalPrice;
        private final BigDecimal originalPrice;
        private final BigDecimal savings;
        
        public PricingResult(PricingStrategy strategy, BigDecimal finalPrice, BigDecimal originalPrice, BigDecimal savings) {
            this.strategy = strategy;
            this.finalPrice = finalPrice;
            this.originalPrice = originalPrice;
            this.savings = savings;
        }
        
        public PricingStrategy getStrategy() { return strategy; }
        public BigDecimal getFinalPrice() { return finalPrice; }
        public BigDecimal getOriginalPrice() { return originalPrice; }
        public BigDecimal getSavings() { return savings; }
        
        public boolean hasSavings() {
            return savings.compareTo(BigDecimal.ZERO) > 0;
        }
        
        public String getSavingsDescription() {
            if (hasSavings()) {
                BigDecimal savingsPercentage = savings.multiply(new BigDecimal("100")).divide(originalPrice, 1, java.math.RoundingMode.HALF_UP);
                return String.format("You save LKR %.2f (%.1f%%)", savings, savingsPercentage);
            }
            return "No discount applied";
        }
    }
}