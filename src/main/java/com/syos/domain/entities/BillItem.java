package com.syos.domain.entities;

import com.syos.shared.valueobjects.Money;
import com.syos.shared.valueobjects.ProductCode;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * BillItem entity representing an individual item on a bill.
 * 
 * This class follows Domain-Driven Design principles and encapsulates
 * business logic for line items on sales bills.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class BillItem {
    
    private final ProductCode productCode;
    private final String productName;
    private final Money unitPrice;
    private BigDecimal quantity;
    private Money lineTotal;
    private String notes;
    
    // Business rule constants
    private static final BigDecimal MIN_QUANTITY = BigDecimal.valueOf(0.01);
    private static final BigDecimal MAX_QUANTITY = BigDecimal.valueOf(9999.99);
    private static final int MAX_NOTES_LENGTH = 200;
    
    /**
     * Creates a new BillItem.
     * 
     * @param productCode the product code
     * @param productName the product name (for display)
     * @param unitPrice the unit price at time of sale
     * @param quantity the quantity sold
     * @param notes optional notes for this line item
     * @throws IllegalArgumentException if any parameter violates business rules
     */
    public BillItem(ProductCode productCode, String productName, Money unitPrice, 
                    BigDecimal quantity, String notes) {
        this.productCode = Objects.requireNonNull(productCode, "Product code cannot be null");
        this.productName = validateProductName(productName);
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price cannot be null");
        
        setQuantity(quantity);
        setNotes(notes);
        calculateLineTotal();
    }
    
    /**
     * Creates a new BillItem without notes.
     * 
     * @param productCode the product code
     * @param productName the product name (for display)
     * @param unitPrice the unit price at time of sale
     * @param quantity the quantity sold
     * @throws IllegalArgumentException if any parameter violates business rules
     */
    public BillItem(ProductCode productCode, String productName, Money unitPrice, BigDecimal quantity) {
        this(productCode, productName, unitPrice, quantity, null);
    }
    
    /**
     * Gets the product code.
     * 
     * @return the product code
     */
    public ProductCode getProductCode() {
        return productCode;
    }
    
    /**
     * Gets the product name.
     * 
     * @return the product name
     */
    public String getProductName() {
        return productName;
    }
    
    /**
     * Gets the unit price.
     * 
     * @return the unit price
     */
    public Money getUnitPrice() {
        return unitPrice;
    }
    
    /**
     * Gets the quantity.
     * 
     * @return the quantity
     */
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    /**
     * Sets the quantity and recalculates the line total.
     * 
     * @param quantity the quantity
     * @throws IllegalArgumentException if quantity violates business rules
     */
    public void setQuantity(BigDecimal quantity) {
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        
        if (quantity.compareTo(MIN_QUANTITY) < 0) {
            throw new IllegalArgumentException(
                String.format("Quantity must be at least %s", MIN_QUANTITY)
            );
        }
        
        if (quantity.compareTo(MAX_QUANTITY) > 0) {
            throw new IllegalArgumentException(
                String.format("Quantity cannot exceed %s", MAX_QUANTITY)
            );
        }
        
        this.quantity = quantity;
        calculateLineTotal();
    }
    
    /**
     * Gets the line total.
     * 
     * @return the line total (unit price × quantity)
     */
    public Money getLineTotal() {
        return lineTotal;
    }
    
    /**
     * Gets the notes.
     * 
     * @return the notes (can be null)
     */
    public String getNotes() {
        return notes;
    }
    
    /**
     * Sets the notes for this line item.
     * 
     * @param notes the notes (can be null)
     * @throws IllegalArgumentException if notes exceed maximum length
     */
    public void setNotes(String notes) {
        if (notes != null) {
            String trimmedNotes = notes.trim();
            if (trimmedNotes.length() > MAX_NOTES_LENGTH) {
                throw new IllegalArgumentException(
                    String.format("Notes cannot exceed %d characters", MAX_NOTES_LENGTH)
                );
            }
            this.notes = trimmedNotes.isEmpty() ? null : trimmedNotes;
        } else {
            this.notes = null;
        }
    }
    
    /**
     * Calculates the line total (unit price × quantity).
     * This method is called automatically when quantity changes.
     */
    private void calculateLineTotal() {
        this.lineTotal = unitPrice.multiply(quantity);
    }
    
    /**
     * Validates the product name.
     * 
     * @param productName the product name to validate
     * @return the validated product name
     * @throws IllegalArgumentException if product name is invalid
     */
    private String validateProductName(String productName) {
        Objects.requireNonNull(productName, "Product name cannot be null");
        
        String trimmedName = productName.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        
        return trimmedName;
    }
    
    /**
     * Increases the quantity by the specified amount.
     * 
     * @param additionalQuantity the amount to add to the current quantity
     * @throws IllegalArgumentException if the resulting quantity violates business rules
     */
    public void increaseQuantity(BigDecimal additionalQuantity) {
        Objects.requireNonNull(additionalQuantity, "Additional quantity cannot be null");
        
        if (additionalQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Additional quantity must be positive");
        }
        
        setQuantity(this.quantity.add(additionalQuantity));
    }
    
    /**
     * Decreases the quantity by the specified amount.
     * 
     * @param reductionQuantity the amount to subtract from the current quantity
     * @throws IllegalArgumentException if the resulting quantity violates business rules
     */
    public void decreaseQuantity(BigDecimal reductionQuantity) {
        Objects.requireNonNull(reductionQuantity, "Reduction quantity cannot be null");
        
        if (reductionQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Reduction quantity must be positive");
        }
        
        BigDecimal newQuantity = this.quantity.subtract(reductionQuantity);
        setQuantity(newQuantity);
    }
    
    /**
     * Checks if this line item has any notes.
     * 
     * @return true if notes are present
     */
    public boolean hasNotes() {
        return notes != null && !notes.isEmpty();
    }
    
    /**
     * Gets a display string for this line item.
     * 
     * @return formatted display string
     */
    public String getDisplayString() {
        return String.format("%s × %s @ %s = %s", 
            productName, quantity, unitPrice, lineTotal);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillItem billItem = (BillItem) o;
        return Objects.equals(productCode, billItem.productCode) &&
               Objects.equals(unitPrice, billItem.unitPrice) &&
               Objects.equals(quantity, billItem.quantity);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(productCode, unitPrice, quantity);
    }
    
    @Override
    public String toString() {
        return String.format("BillItem{product=%s, quantity=%s, unitPrice=%s, total=%s}",
            productCode, quantity, unitPrice, lineTotal);
    }
}