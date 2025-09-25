package com.syos.shared.patterns.factory;

import com.syos.inventory.domain.entity.ProductNew;
import java.math.BigDecimal;

/**
 * Factory for creating ProductNew instances with different configurations.
 * 
 * Implements the Factory Pattern to encapsulate product creation logic
 * and provide a clean interface for creating products with validation.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class ProductFactory {
    
    /**
     * Creates a basic product with essential information.
     * 
     * @param productCode Unique product code
     * @param productName Product name
     * @param description Product description
     * @param brand Product brand
     * @param basePrice Base price of the product
     * @param unitOfMeasure Unit of measurement
     * @param subcategoryId Subcategory identifier
     * @param createdBy User who created the product
     * @return New ProductNew instance
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static ProductNew createBasicProduct(
            String productCode,
            String productName,
            String description,
            String brand,
            BigDecimal basePrice,
            String unitOfMeasure,
            Long subcategoryId,
            Long createdBy) {
        
        validateProductParameters(productCode, productName, basePrice, unitOfMeasure, subcategoryId, createdBy);
        
        return new ProductNew(productCode, productName, description, brand,
                basePrice, unitOfMeasure, subcategoryId, createdBy);
    }
    
    /**
     * Creates a product with discount information.
     * 
     * @param productCode Unique product code
     * @param productName Product name
     * @param description Product description
     * @param brand Product brand
     * @param basePrice Base price of the product
     * @param unitOfMeasure Unit of measurement
     * @param subcategoryId Subcategory identifier
     * @param discountPercentage Discount percentage (0-100)
     * @param createdBy User who created the product
     * @return New ProductNew instance with discount applied
     */
    public static ProductNew createDiscountedProduct(
            String productCode,
            String productName,
            String description,
            String brand,
            BigDecimal basePrice,
            String unitOfMeasure,
            Long subcategoryId,
            BigDecimal discountPercentage,
            Long createdBy) {
        
        ProductNew product = createBasicProduct(productCode, productName, description, brand, 
                basePrice, unitOfMeasure, subcategoryId, createdBy);
        
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            product.setPercentageDiscount(discountPercentage);
        }
        
        return product;
    }
    
    /**
     * Creates a product with fixed discount amount.
     * 
     * @param productCode Unique product code
     * @param productName Product name
     * @param description Product description
     * @param brand Product brand
     * @param basePrice Base price of the product
     * @param unitOfMeasure Unit of measurement
     * @param subcategoryId Subcategory identifier
     * @param discountAmount Fixed discount amount
     * @param createdBy User who created the product
     * @return New ProductNew instance with fixed discount
     */
    public static ProductNew createFixedDiscountProduct(
            String productCode,
            String productName,
            String description,
            String brand,
            BigDecimal basePrice,
            String unitOfMeasure,
            Long subcategoryId,
            BigDecimal discountAmount,
            Long createdBy) {
        
        ProductNew product = createBasicProduct(productCode, productName, description, brand, 
                basePrice, unitOfMeasure, subcategoryId, createdBy);
        
        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            product.setFixedDiscount(discountAmount);
        }
        
        return product;
    }
    
    /**
     * Creates a copy of an existing product with a new product code.
     * Useful for creating product variants.
     * 
     * @param originalProduct Original product to copy
     * @param newProductCode New unique product code
     * @param createdBy User who created the copy
     * @return New ProductNew instance copied from original
     */
    public static ProductNew createProductCopy(ProductNew originalProduct, String newProductCode, Long createdBy) {
        if (originalProduct == null) {
            throw new IllegalArgumentException("Original product cannot be null");
        }
        
        return createDiscountedProduct(
                newProductCode,
                originalProduct.getProductName(),
                originalProduct.getDescription(),
                originalProduct.getBrand(),
                originalProduct.getBasePrice(),
                originalProduct.getUnitOfMeasure(),
                originalProduct.getSubcategoryId(),
                originalProduct.getDiscountPercentage(),
                createdBy
        );
    }
    
    /**
     * Validates common product parameters.
     * 
     * @param productCode Product code to validate
     * @param productName Product name to validate
     * @param basePrice Base price to validate
     * @param unitOfMeasure Unit of measure to validate
     * @param subcategoryId Subcategory ID to validate
     * @param createdBy Created by user ID to validate
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private static void validateProductParameters(
            String productCode,
            String productName,
            BigDecimal basePrice,
            String unitOfMeasure,
            Long subcategoryId,
            Long createdBy) {
        
        if (productCode == null || productCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Product code cannot be null or empty");
        }
        
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Base price must be greater than zero");
        }
        
        if (unitOfMeasure == null || unitOfMeasure.trim().isEmpty()) {
            throw new IllegalArgumentException("Unit of measure cannot be null or empty");
        }
        
        if (subcategoryId == null || subcategoryId <= 0) {
            throw new IllegalArgumentException("Subcategory ID must be a positive number");
        }
        
        if (createdBy == null || createdBy <= 0) {
            throw new IllegalArgumentException("Created by user ID must be a positive number");
        }
    }
}