package com.syos.shared.patterns.builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating Bill instances with complex configuration.
 * 
 * Implements the Builder Pattern for constructing Bill objects
 * step by step with validation and complex business logic.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class BillBuilder {
    
    // Bill properties
    private String billSerialNumber;
    private LocalDateTime billDate;
    private Long salesChannelId;
    private Long employeeId;
    private Long customerId;
    private String deliveryAddress;
    private BigDecimal cashTendered;
    
    // Bill items
    private List<BillItemData> items;
    
    // Calculated totals
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal finalTotal;
    private BigDecimal changeAmount;
    
    /**
     * Inner class to hold bill item data during construction
     */
    public static class BillItemData {
        private final Long productId;
        private final Long batchId;
        private final Integer quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal discountPercentage;
        private final BigDecimal discountAmount;
        private final BigDecimal lineTotal;
        
        public BillItemData(Long productId, Long batchId, Integer quantity, 
                           BigDecimal unitPrice, BigDecimal discountPercentage, 
                           BigDecimal discountAmount, BigDecimal lineTotal) {
            this.productId = productId;
            this.batchId = batchId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.discountPercentage = discountPercentage;
            this.discountAmount = discountAmount;
            this.lineTotal = lineTotal;
        }
        
        // Getters
        public Long getProductId() { return productId; }
        public Long getBatchId() { return batchId; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getDiscountPercentage() { return discountPercentage; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public BigDecimal getLineTotal() { return lineTotal; }
    }
    
    /**
     * Constructor - Initialize builder
     */
    public BillBuilder() {
        this.items = new ArrayList<>();
        this.billDate = LocalDateTime.now();
        this.subtotal = BigDecimal.ZERO;
        this.totalDiscount = BigDecimal.ZERO;
        this.finalTotal = BigDecimal.ZERO;
        this.changeAmount = BigDecimal.ZERO;
    }
    
    /**
     * Set bill serial number
     */
    public BillBuilder withBillSerialNumber(String billSerialNumber) {
        if (billSerialNumber == null || billSerialNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Bill serial number cannot be null or empty");
        }
        this.billSerialNumber = billSerialNumber.trim();
        return this;
    }
    
    /**
     * Set bill date
     */
    public BillBuilder withBillDate(LocalDateTime billDate) {
        if (billDate == null) {
            throw new IllegalArgumentException("Bill date cannot be null");
        }
        this.billDate = billDate;
        return this;
    }
    
    /**
     * Set sales channel
     */
    public BillBuilder withSalesChannel(Long salesChannelId) {
        if (salesChannelId == null || salesChannelId <= 0) {
            throw new IllegalArgumentException("Sales channel ID must be valid");
        }
        this.salesChannelId = salesChannelId;
        return this;
    }
    
    /**
     * Set employee (cashier)
     */
    public BillBuilder withEmployee(Long employeeId) {
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be valid");
        }
        this.employeeId = employeeId;
        return this;
    }
    
    /**
     * Set customer (optional)
     */
    public BillBuilder withCustomer(Long customerId) {
        this.customerId = customerId;
        return this;
    }
    
    /**
     * Set delivery address for online orders
     */
    public BillBuilder withDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
        return this;
    }
    
    /**
     * Set cash tendered
     */
    public BillBuilder withCashTendered(BigDecimal cashTendered) {
        if (cashTendered != null && cashTendered.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cash tendered cannot be negative");
        }
        this.cashTendered = cashTendered;
        return this;
    }
    
    /**
     * Add item to bill
     */
    public BillBuilder addItem(Long productId, Long batchId, Integer quantity, BigDecimal unitPrice) {
        return addItem(productId, batchId, quantity, unitPrice, BigDecimal.ZERO, BigDecimal.ZERO);
    }
    
    /**
     * Add item to bill with percentage discount
     */
    public BillBuilder addItemWithPercentageDiscount(Long productId, Long batchId, Integer quantity, 
                                                    BigDecimal unitPrice, BigDecimal discountPercentage) {
        if (discountPercentage == null) discountPercentage = BigDecimal.ZERO;
        
        BigDecimal itemSubtotal = unitPrice.multiply(new BigDecimal(quantity));
        BigDecimal discountAmount = itemSubtotal.multiply(discountPercentage).divide(new BigDecimal("100"));
        
        return addItem(productId, batchId, quantity, unitPrice, discountPercentage, discountAmount);
    }
    
    /**
     * Add item to bill with fixed discount
     */
    public BillBuilder addItemWithFixedDiscount(Long productId, Long batchId, Integer quantity, 
                                               BigDecimal unitPrice, BigDecimal discountAmount) {
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        
        BigDecimal itemSubtotal = unitPrice.multiply(new BigDecimal(quantity));
        BigDecimal discountPercentage = BigDecimal.ZERO;
        
        if (itemSubtotal.compareTo(BigDecimal.ZERO) > 0 && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            discountPercentage = discountAmount.multiply(new BigDecimal("100")).divide(itemSubtotal, 2, java.math.RoundingMode.HALF_UP);
        }
        
        return addItem(productId, batchId, quantity, unitPrice, discountPercentage, discountAmount);
    }
    
    /**
     * Add item to bill with discount details
     */
    private BillBuilder addItem(Long productId, Long batchId, Integer quantity, BigDecimal unitPrice, 
                               BigDecimal discountPercentage, BigDecimal discountAmount) {
        
        validateItemParameters(productId, batchId, quantity, unitPrice);
        
        BigDecimal itemSubtotal = unitPrice.multiply(new BigDecimal(quantity));
        BigDecimal lineTotal = itemSubtotal.subtract(discountAmount);
        
        BillItemData item = new BillItemData(productId, batchId, quantity, unitPrice, 
                                           discountPercentage, discountAmount, lineTotal);
        this.items.add(item);
        
        // Update totals
        recalculateTotals();
        
        return this;
    }
    
    /**
     * Remove last added item
     */
    public BillBuilder removeLastItem() {
        if (!items.isEmpty()) {
            items.remove(items.size() - 1);
            recalculateTotals();
        }
        return this;
    }
    
    /**
     * Clear all items
     */
    public BillBuilder clearItems() {
        this.items.clear();
        recalculateTotals();
        return this;
    }
    
    /**
     * Apply global bill discount percentage
     */
    public BillBuilder withGlobalDiscountPercentage(BigDecimal discountPercentage) {
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal additionalDiscount = subtotal.multiply(discountPercentage).divide(new BigDecimal("100"));
            this.totalDiscount = this.totalDiscount.add(additionalDiscount);
            this.finalTotal = subtotal.subtract(totalDiscount);
        }
        return this;
    }
    
    /**
     * Apply global bill discount amount
     */
    public BillBuilder withGlobalDiscountAmount(BigDecimal discountAmount) {
        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalDiscount = this.totalDiscount.add(discountAmount);
            this.finalTotal = subtotal.subtract(totalDiscount);
        }
        return this;
    }
    
    /**
     * Build the bill data structure
     */
    public BillData build() {
        validateBillData();
        
        // Calculate change if cash tendered is provided
        if (cashTendered != null) {
            this.changeAmount = cashTendered.subtract(finalTotal);
            if (changeAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("Cash tendered is less than the bill total");
            }
        }
        
        return new BillData(billSerialNumber, billDate, salesChannelId, employeeId, customerId,
                          subtotal, totalDiscount, finalTotal, cashTendered, changeAmount,
                          deliveryAddress, new ArrayList<>(items));
    }
    
    /**
     * Recalculate bill totals based on items
     */
    private void recalculateTotals() {
        this.subtotal = BigDecimal.ZERO;
        this.totalDiscount = BigDecimal.ZERO;
        
        for (BillItemData item : items) {
            BigDecimal itemSubtotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            this.subtotal = this.subtotal.add(itemSubtotal);
            this.totalDiscount = this.totalDiscount.add(item.getDiscountAmount());
        }
        
        this.finalTotal = this.subtotal.subtract(this.totalDiscount);
    }
    
    /**
     * Validate item parameters
     */
    private void validateItemParameters(Long productId, Long batchId, Integer quantity, BigDecimal unitPrice) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be valid");
        }
        if (batchId == null || batchId <= 0) {
            throw new IllegalArgumentException("Batch ID must be valid");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }
    
    /**
     * Validate bill data before building
     */
    private void validateBillData() {
        if (billSerialNumber == null || billSerialNumber.trim().isEmpty()) {
            throw new IllegalStateException("Bill serial number is required");
        }
        if (salesChannelId == null) {
            throw new IllegalStateException("Sales channel is required");
        }
        if (employeeId == null) {
            throw new IllegalStateException("Employee is required");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("At least one item is required");
        }
    }
    
    /**
     * Data class to hold the built bill information
     */
    public static class BillData {
        private final String billSerialNumber;
        private final LocalDateTime billDate;
        private final Long salesChannelId;
        private final Long employeeId;
        private final Long customerId;
        private final BigDecimal subtotal;
        private final BigDecimal totalDiscount;
        private final BigDecimal finalTotal;
        private final BigDecimal cashTendered;
        private final BigDecimal changeAmount;
        private final String deliveryAddress;
        private final List<BillItemData> items;
        
        public BillData(String billSerialNumber, LocalDateTime billDate, Long salesChannelId,
                       Long employeeId, Long customerId, BigDecimal subtotal, BigDecimal totalDiscount,
                       BigDecimal finalTotal, BigDecimal cashTendered, BigDecimal changeAmount,
                       String deliveryAddress, List<BillItemData> items) {
            this.billSerialNumber = billSerialNumber;
            this.billDate = billDate;
            this.salesChannelId = salesChannelId;
            this.employeeId = employeeId;
            this.customerId = customerId;
            this.subtotal = subtotal;
            this.totalDiscount = totalDiscount;
            this.finalTotal = finalTotal;
            this.cashTendered = cashTendered;
            this.changeAmount = changeAmount;
            this.deliveryAddress = deliveryAddress;
            this.items = items;
        }
        
        // Getters
        public String getBillSerialNumber() { return billSerialNumber; }
        public LocalDateTime getBillDate() { return billDate; }
        public Long getSalesChannelId() { return salesChannelId; }
        public Long getEmployeeId() { return employeeId; }
        public Long getCustomerId() { return customerId; }
        public BigDecimal getSubtotal() { return subtotal; }
        public BigDecimal getTotalDiscount() { return totalDiscount; }
        public BigDecimal getFinalTotal() { return finalTotal; }
        public BigDecimal getCashTendered() { return cashTendered; }
        public BigDecimal getChangeAmount() { return changeAmount; }
        public String getDeliveryAddress() { return deliveryAddress; }
        public List<BillItemData> getItems() { return items; }
    }
}