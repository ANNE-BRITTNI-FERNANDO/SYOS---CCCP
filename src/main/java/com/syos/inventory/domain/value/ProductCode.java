package com.syos.inventory.domain.value;

import com.syos.inventory.domain.exception.DomainException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a product code in the format XX-YY-NNN
 * where XX is category, YY is subcategory, and NNN is sequence number
 */
public class ProductCode {
    private static final Pattern PRODUCT_CODE_PATTERN = Pattern.compile("^[A-Z]{2}-[A-Z]{2}-\\d{3}$");
    private final String value;

    public ProductCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException("Product code cannot be empty");
        }
        
        String trimmedValue = value.trim().toUpperCase();
        if (!PRODUCT_CODE_PATTERN.matcher(trimmedValue).matches()) {
            throw new DomainException("Invalid product code format. Expected: XX-YY-NNN (e.g., LA-SO-001)");
        }
        
        this.value = trimmedValue;
    }

    public static ProductCode generate(CategoryCode categoryCode, CategoryCode subcategoryCode, int sequenceNumber) {
        if (sequenceNumber < 1 || sequenceNumber > 999) {
            throw new DomainException("Sequence number must be between 1 and 999");
        }
        
        String code = String.format("%s-%s-%03d", 
                categoryCode.getValue(), 
                subcategoryCode.getValue(), 
                sequenceNumber);
        return new ProductCode(code);
    }

    public CategoryCode getCategoryCode() {
        return new CategoryCode(value.substring(0, 2));
    }

    public CategoryCode getSubcategoryCode() {
        return new CategoryCode(value.substring(3, 5));
    }

    public int getSequenceNumber() {
        return Integer.parseInt(value.substring(6, 9));
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCode that = (ProductCode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}