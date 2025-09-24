package com.syos.inventory.domain.value;

import com.syos.inventory.domain.exception.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value object representing money with currency
 */
public class Money {
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("PHP"); // Philippine Peso
    private static final int SCALE = 2; // 2 decimal places for currency
    
    private final BigDecimal amount;
    private final Currency currency;
    
    /**
     * Create money with default currency (PHP)
     * @param amount Amount
     */
    public Money(BigDecimal amount) {
        this(amount, DEFAULT_CURRENCY);
    }
    
    /**
     * Create money with default currency (PHP)
     * @param amount Amount as double
     */
    public Money(double amount) {
        this(BigDecimal.valueOf(amount), DEFAULT_CURRENCY);
    }
    
    /**
     * Create money with specified currency
     * @param amount Amount
     * @param currency Currency
     */
    public Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new ValidationException("Amount cannot be null");
        }
        if (currency == null) {
            throw new ValidationException("Currency cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Amount cannot be negative");
        }
        
        this.amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
        this.currency = currency;
    }
    
    /**
     * Create zero money
     * @return Zero money with default currency
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }
    
    /**
     * Create money from string amount
     * @param amount Amount as string
     * @return Money object
     */
    public static Money of(String amount) {
        try {
            return new Money(new BigDecimal(amount));
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid money amount: " + amount);
        }
    }
    
    /**
     * Add money
     * @param other Money to add
     * @return New money object with sum
     * @throws ValidationException if currencies don't match
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    /**
     * Subtract money
     * @param other Money to subtract
     * @return New money object with difference
     * @throws ValidationException if currencies don't match or result is negative
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Cannot subtract more than available amount");
        }
        return new Money(result, this.currency);
    }
    
    /**
     * Multiply by quantity
     * @param quantity Quantity to multiply by
     * @return New money object with product
     */
    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new ValidationException("Quantity cannot be negative");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }
    
    /**
     * Multiply by factor
     * @param factor Factor to multiply by
     * @return New money object with product
     */
    public Money multiply(BigDecimal factor) {
        if (factor == null) {
            throw new ValidationException("Factor cannot be null");
        }
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Factor cannot be negative");
        }
        return new Money(this.amount.multiply(factor), this.currency);
    }
    
    /**
     * Check if this money is greater than other
     * @param other Money to compare
     * @return true if this money is greater
     */
    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }
    
    /**
     * Check if this money is less than other
     * @param other Money to compare
     * @return true if this money is less
     */
    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }
    
    /**
     * Check if this money is zero
     * @return true if amount is zero
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Check if this money is positive
     * @return true if amount is positive
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Get the amount
     * @return Amount as BigDecimal
     */
    public BigDecimal getAmount() {
        return amount;
    }
    
    /**
     * Get the currency
     * @return Currency
     */
    public Currency getCurrency() {
        return currency;
    }
    
    /**
     * Get currency code
     * @return Currency code (e.g., "PHP")
     */
    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }
    
    /**
     * Format money as string
     * @return Formatted money string
     */
    public String format() {
        return currency.getSymbol() + " " + amount.toString();
    }
    
    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new ValidationException("Currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Money money = (Money) obj;
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
    
    @Override
    public String toString() {
        return format();
    }
}