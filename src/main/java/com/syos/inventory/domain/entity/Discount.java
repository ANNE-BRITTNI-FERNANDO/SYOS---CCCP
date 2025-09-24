package com.syos.inventory.domain.entity;

import com.syos.inventory.domain.value.Money;
import com.syos.inventory.domain.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Discount entity representing price reductions that can be applied to products
 */
public class Discount {
    private Long id;
    private DiscountType type;
    private BigDecimal value;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum DiscountType {
        FIXED_AMOUNT,    // Fixed LKR amount off
        PERCENTAGE       // Percentage off
    }

    // Constructor for new discount
    public Discount(DiscountType type, BigDecimal value, String description) {
        validateDiscount(type, value);
        this.type = type;
        this.value = value;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor for loading from database
    public Discount(Long id, DiscountType type, BigDecimal value, String description,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private void validateDiscount(DiscountType type, BigDecimal value) {
        if (type == null) {
            throw new DomainException("Discount type cannot be null");
        }
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Discount value must be positive");
        }
        if (type == DiscountType.PERCENTAGE && value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new DomainException("Percentage discount cannot exceed 100%");
        }
    }

    public Money applyTo(Money originalPrice) {
        if (originalPrice == null) {
            throw new DomainException("Original price cannot be null");
        }

        BigDecimal originalAmount = originalPrice.getAmount();
        BigDecimal discountAmount;

        switch (type) {
            case FIXED_AMOUNT:
                discountAmount = value;
                // Ensure discount doesn't exceed original price
                if (discountAmount.compareTo(originalAmount) > 0) {
                    discountAmount = originalAmount;
                }
                break;
            case PERCENTAGE:
                discountAmount = originalAmount.multiply(value)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                break;
            default:
                throw new DomainException("Unknown discount type: " + type);
        }

        BigDecimal finalAmount = originalAmount.subtract(discountAmount);
        return new Money(finalAmount.max(BigDecimal.ZERO)); // Ensure non-negative result
    }

    public Money getDiscountAmount(Money originalPrice) {
        if (originalPrice == null) {
            return new Money(BigDecimal.ZERO);
        }
        
        Money finalPrice = applyTo(originalPrice);
        return originalPrice.subtract(finalPrice);
    }

    public void updateDiscount(DiscountType type, BigDecimal value, String description) {
        validateDiscount(type, value);
        this.type = type;
        this.value = value;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDisplayText() {
        switch (type) {
            case FIXED_AMOUNT:
                return String.format("LKR %.2f off", value);
            case PERCENTAGE:
                return String.format("%.1f%% off", value);
            default:
                return "Discount";
        }
    }

    public String getDisplayTextWithSavings(Money originalPrice) {
        Money savings = getDiscountAmount(originalPrice);
        switch (type) {
            case FIXED_AMOUNT:
                return String.format("LKR %.2f off", value);
            case PERCENTAGE:
                return String.format("%.1f%% off (Save LKR %.2f)", value, savings.getAmount());
            default:
                return "Discount";
        }
    }

    // Getters
    public Long getId() { return id; }
    public DiscountType getType() { return type; }
    public BigDecimal getValue() { return value; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Discount discount = (Discount) o;
        return Objects.equals(id, discount.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Discount{type=%s, value=%s, description='%s'}", 
                type, value, description);
    }
}