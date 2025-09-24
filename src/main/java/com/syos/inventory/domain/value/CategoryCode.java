package com.syos.inventory.domain.value;

import com.syos.inventory.domain.exception.DomainException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a category code (2 letters)
 */
public class CategoryCode {
    private static final Pattern CATEGORY_CODE_PATTERN = Pattern.compile("^[A-Z]{2}$");
    private final String value;

    public CategoryCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException("Category code cannot be empty");
        }
        
        String trimmedValue = value.trim().toUpperCase();
        if (!CATEGORY_CODE_PATTERN.matcher(trimmedValue).matches()) {
            throw new DomainException("Invalid category code format. Expected: XX (e.g., LA, FO, EL)");
        }
        
        this.value = trimmedValue;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryCode that = (CategoryCode) o;
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