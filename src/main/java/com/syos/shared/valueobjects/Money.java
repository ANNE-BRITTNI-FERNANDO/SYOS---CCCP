package com.syos.shared.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Currency;

/**
 * Value object representing monetary amounts.
 * 
 * This class implements the Money pattern, ensuring proper handling of
 * monetary calculations with precision and currency awareness.
 * It follows immutability principles and provides type safety for money operations.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public final class Money implements Comparable<Money> {
    
    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("USD");
    
    private final BigDecimal amount;
    private final Currency currency;
    
    /**
     * Private constructor for creating Money instances.
     * 
     * @param amount the monetary amount
     * @param currency the currency
     */
    private Money(BigDecimal amount, Currency currency) {
        this.amount = amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
    }
    
    /**
     * Creates a Money instance from a BigDecimal amount.
     * 
     * @param amount the monetary amount
     * @return new Money instance
     * @throws IllegalArgumentException if amount is null or negative
     */
    public static Money of(BigDecimal amount) {
        return of(amount, DEFAULT_CURRENCY);
    }
    
    /**
     * Creates a Money instance from a BigDecimal amount and currency.
     * 
     * @param amount the monetary amount
     * @param currency the currency
     * @return new Money instance
     * @throws IllegalArgumentException if amount is null or negative, or currency is null
     */
    public static Money of(BigDecimal amount, Currency currency) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        return new Money(amount, currency);
    }
    
    /**
     * Creates a Money instance from a double amount.
     * 
     * @param amount the monetary amount
     * @return new Money instance
     * @throws IllegalArgumentException if amount is negative
     */
    public static Money of(double amount) {
        return of(BigDecimal.valueOf(amount), DEFAULT_CURRENCY);
    }
    
    /**
     * Creates a Money instance from a double amount and currency.
     * 
     * @param amount the monetary amount
     * @param currency the currency
     * @return new Money instance
     * @throws IllegalArgumentException if amount is negative or currency is null
     */
    public static Money of(double amount, Currency currency) {
        return of(BigDecimal.valueOf(amount), currency);
    }
    
    /**
     * Creates a zero Money instance.
     * 
     * @return zero Money instance
     */
    public static Money zero() {
        return of(BigDecimal.ZERO);
    }
    
    /**
     * Creates a zero Money instance with specified currency.
     * 
     * @param currency the currency
     * @return zero Money instance
     */
    public static Money zero(Currency currency) {
        return of(BigDecimal.ZERO, currency);
    }
    
    /**
     * Adds another Money amount to this one.
     * 
     * @param other the other Money to add
     * @return new Money instance with the sum
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    /**
     * Subtracts another Money amount from this one.
     * 
     * @param other the other Money to subtract
     * @return new Money instance with the difference
     * @throws IllegalArgumentException if currencies don't match or result would be negative
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtraction result cannot be negative");
        }
        return new Money(result, this.currency);
    }
    
    /**
     * Multiplies this Money by a factor.
     * 
     * @param factor the multiplication factor
     * @return new Money instance with the product
     * @throws IllegalArgumentException if factor is negative
     */
    public Money multiply(BigDecimal factor) {
        Objects.requireNonNull(factor, "Factor cannot be null");
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Factor cannot be negative");
        }
        return new Money(this.amount.multiply(factor), this.currency);
    }
    
    /**
     * Multiplies this Money by a factor.
     * 
     * @param factor the multiplication factor
     * @return new Money instance with the product
     * @throws IllegalArgumentException if factor is negative
     */
    public Money multiply(double factor) {
        return multiply(BigDecimal.valueOf(factor));
    }
    
    /**
     * Divides this Money by a divisor.
     * 
     * @param divisor the division divisor
     * @return new Money instance with the quotient
     * @throws IllegalArgumentException if divisor is zero or negative
     */
    public Money divide(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "Divisor cannot be null");
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Divisor must be positive");
        }
        return new Money(this.amount.divide(divisor, DEFAULT_SCALE, DEFAULT_ROUNDING), this.currency);
    }
    
    /**
     * Divides this Money by a divisor.
     * 
     * @param divisor the division divisor
     * @return new Money instance with the quotient
     * @throws IllegalArgumentException if divisor is zero or negative
     */
    public Money divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }
    
    /**
     * Checks if this Money is greater than another.
     * 
     * @param other the other Money to compare
     * @return true if this Money is greater than the other
     * @throws IllegalArgumentException if currencies don't match
     */
    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }
    
    /**
     * Checks if this Money is greater than or equal to another.
     * 
     * @param other the other Money to compare
     * @return true if this Money is greater than or equal to the other
     * @throws IllegalArgumentException if currencies don't match
     */
    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }
    
    /**
     * Checks if this Money is less than another.
     * 
     * @param other the other Money to compare
     * @return true if this Money is less than the other
     * @throws IllegalArgumentException if currencies don't match
     */
    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }
    
    /**
     * Checks if this Money is less than or equal to another.
     * 
     * @param other the other Money to compare
     * @return true if this Money is less than or equal to the other
     * @throws IllegalArgumentException if currencies don't match
     */
    public boolean isLessThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0;
    }
    
    /**
     * Checks if this Money is zero.
     * 
     * @return true if the amount is zero
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Checks if this Money is positive.
     * 
     * @return true if the amount is greater than zero
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Gets the amount as BigDecimal.
     * 
     * @return the amount
     */
    public BigDecimal getAmount() {
        return amount;
    }
    
    /**
     * Gets the currency.
     * 
     * @return the currency
     */
    public Currency getCurrency() {
        return currency;
    }
    
    /**
     * Validates that another Money has the same currency.
     * 
     * @param other the other Money to validate
     * @throws IllegalArgumentException if currencies don't match
     */
    private void validateSameCurrency(Money other) {
        Objects.requireNonNull(other, "Other money cannot be null");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Currency mismatch: %s vs %s", 
                    this.currency.getCurrencyCode(), 
                    other.currency.getCurrencyCode()));
        }
    }
    
    @Override
    public int compareTo(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Money money = (Money) obj;
        return Objects.equals(amount, money.amount) && 
               Objects.equals(currency, money.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
    
    @Override
    public String toString() {
        return String.format("%s %s", currency.getCurrencyCode(), amount.toString());
    }
}