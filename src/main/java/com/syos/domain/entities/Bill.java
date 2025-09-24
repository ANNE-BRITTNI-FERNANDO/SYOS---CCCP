package com.syos.domain.entities;

import com.syos.shared.valueobjects.Money;
import com.syos.shared.valueobjects.UserCode;
import com.syos.shared.valueobjects.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Bill entity representing a sales transaction in the SYOS inventory system.
 * 
 * This class follows Domain-Driven Design principles and encapsulates
 * all bill-related business logic and rules using the Aggregate pattern.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class Bill {
    
    /**
     * Enumeration for bill status.
     */
    public enum Status {
        DRAFT("Draft", "Bill is being prepared"),
        FINALIZED("Finalized", "Bill is completed and cannot be modified"),
        PAID("Paid", "Bill has been paid"),
        CANCELLED("Cancelled", "Bill has been cancelled");
        
        private final String displayName;
        private final String description;
        
        Status(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Enumeration for payment method.
     */
    public enum PaymentMethod {
        CASH("Cash", "Payment made in cash"),
        CARD("Card", "Payment made by card"),
        DIGITAL("Digital", "Payment made through digital wallet"),
        CREDIT("Credit", "Payment on credit");
        
        private final String displayName;
        private final String description;
        
        PaymentMethod(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    private final String billNumber;
    private final UserCode cashierCode;
    private final String cashierName;
    private final List<BillItem> items;
    private Status status;
    private PaymentMethod paymentMethod;
    private Money subtotal;
    private Money taxAmount;
    private Money discountAmount;
    private Money totalAmount;
    private String customerName;
    private String customerPhone;
    private String notes;
    private final Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp finalizedAt;
    private Timestamp paidAt;
    
    // Business rule constants
    private static final int MAX_CUSTOMER_NAME_LENGTH = 100;
    private static final int MAX_CUSTOMER_PHONE_LENGTH = 20;
    private static final int MAX_NOTES_LENGTH = 500;
    private static final int MAX_ITEMS_PER_BILL = 100;
    
    /**
     * Creates a new Bill entity.
     * 
     * @param billNumber the unique bill number
     * @param cashierCode the code of the cashier creating this bill
     * @param cashierName the name of the cashier
     * @throws IllegalArgumentException if any parameter violates business rules
     */
    public Bill(String billNumber, UserCode cashierCode, String cashierName) {
        this.billNumber = validateBillNumber(billNumber);
        this.cashierCode = Objects.requireNonNull(cashierCode, "Cashier code cannot be null");
        this.cashierName = validateCashierName(cashierName);
        this.items = new ArrayList<>();
        this.status = Status.DRAFT;
        this.subtotal = Money.zero();
        this.taxAmount = Money.zero();
        this.discountAmount = Money.zero();
        this.totalAmount = Money.zero();
        this.createdAt = Timestamp.now();
        this.updatedAt = this.createdAt;
    }
    
    /**
     * Reconstitutes a Bill entity from persistence.
     * 
     * @param billNumber the bill number
     * @param cashierCode the cashier code
     * @param cashierName the cashier name
     * @param items the bill items
     * @param status the bill status
     * @param paymentMethod the payment method
     * @param subtotal the subtotal
     * @param taxAmount the tax amount
     * @param discountAmount the discount amount
     * @param totalAmount the total amount
     * @param customerName the customer name
     * @param customerPhone the customer phone
     * @param notes the notes
     * @param createdAt the creation timestamp
     * @param updatedAt the update timestamp
     * @param finalizedAt the finalized timestamp
     * @param paidAt the paid timestamp
     */
    public Bill(String billNumber, UserCode cashierCode, String cashierName,
                List<BillItem> items, Status status, PaymentMethod paymentMethod,
                Money subtotal, Money taxAmount, Money discountAmount, Money totalAmount,
                String customerName, String customerPhone, String notes,
                Timestamp createdAt, Timestamp updatedAt, Timestamp finalizedAt, Timestamp paidAt) {
        this.billNumber = validateBillNumber(billNumber);
        this.cashierCode = Objects.requireNonNull(cashierCode, "Cashier code cannot be null");
        this.cashierName = validateCashierName(cashierName);
        this.items = new ArrayList<>(Objects.requireNonNull(items, "Items cannot be null"));
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.paymentMethod = paymentMethod;
        this.subtotal = Objects.requireNonNull(subtotal, "Subtotal cannot be null");
        this.taxAmount = Objects.requireNonNull(taxAmount, "Tax amount cannot be null");
        this.discountAmount = Objects.requireNonNull(discountAmount, "Discount amount cannot be null");
        this.totalAmount = Objects.requireNonNull(totalAmount, "Total amount cannot be null");
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.notes = notes;
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
        this.finalizedAt = finalizedAt;
        this.paidAt = paidAt;
    }
    
    /**
     * Gets the bill number.
     * 
     * @return the bill number
     */
    public String getBillNumber() {
        return billNumber;
    }
    
    /**
     * Gets the cashier code.
     * 
     * @return the cashier code
     */
    public UserCode getCashierCode() {
        return cashierCode;
    }
    
    /**
     * Gets the cashier name.
     * 
     * @return the cashier name
     */
    public String getCashierName() {
        return cashierName;
    }
    
    /**
     * Gets an immutable list of bill items.
     * 
     * @return the bill items
     */
    public List<BillItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    /**
     * Gets the bill status.
     * 
     * @return the bill status
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Gets the payment method.
     * 
     * @return the payment method (can be null if not set)
     */
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    /**
     * Sets the payment method.
     * 
     * @param paymentMethod the payment method
     * @throws IllegalStateException if bill is not in FINALIZED status
     */
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        if (status != Status.FINALIZED) {
            throw new IllegalStateException("Payment method can only be set on finalized bills");
        }
        
        this.paymentMethod = paymentMethod;
        updateTimestamp();
    }
    
    /**
     * Gets the subtotal.
     * 
     * @return the subtotal
     */
    public Money getSubtotal() {
        return subtotal;
    }
    
    /**
     * Gets the tax amount.
     * 
     * @return the tax amount
     */
    public Money getTaxAmount() {
        return taxAmount;
    }
    
    /**
     * Gets the discount amount.
     * 
     * @return the discount amount
     */
    public Money getDiscountAmount() {
        return discountAmount;
    }
    
    /**
     * Gets the total amount.
     * 
     * @return the total amount
     */
    public Money getTotalAmount() {
        return totalAmount;
    }
    
    /**
     * Gets the customer name.
     * 
     * @return the customer name (can be null)
     */
    public String getCustomerName() {
        return customerName;
    }
    
    /**
     * Sets the customer name.
     * 
     * @param customerName the customer name
     * @throws IllegalArgumentException if customer name violates business rules
     * @throws IllegalStateException if bill is not in DRAFT status
     */
    public void setCustomerName(String customerName) {
        ensureDraftStatus("set customer name");
        
        if (customerName != null) {
            String trimmedName = customerName.trim();
            if (trimmedName.length() > MAX_CUSTOMER_NAME_LENGTH) {
                throw new IllegalArgumentException(
                    String.format("Customer name cannot exceed %d characters", MAX_CUSTOMER_NAME_LENGTH)
                );
            }
            this.customerName = trimmedName.isEmpty() ? null : trimmedName;
        } else {
            this.customerName = null;
        }
        updateTimestamp();
    }
    
    /**
     * Gets the customer phone.
     * 
     * @return the customer phone (can be null)
     */
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    /**
     * Sets the customer phone.
     * 
     * @param customerPhone the customer phone
     * @throws IllegalArgumentException if customer phone violates business rules
     * @throws IllegalStateException if bill is not in DRAFT status
     */
    public void setCustomerPhone(String customerPhone) {
        ensureDraftStatus("set customer phone");
        
        if (customerPhone != null) {
            String trimmedPhone = customerPhone.trim();
            if (trimmedPhone.length() > MAX_CUSTOMER_PHONE_LENGTH) {
                throw new IllegalArgumentException(
                    String.format("Customer phone cannot exceed %d characters", MAX_CUSTOMER_PHONE_LENGTH)
                );
            }
            this.customerPhone = trimmedPhone.isEmpty() ? null : trimmedPhone;
        } else {
            this.customerPhone = null;
        }
        updateTimestamp();
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
     * Sets the notes.
     * 
     * @param notes the notes
     * @throws IllegalArgumentException if notes violate business rules
     * @throws IllegalStateException if bill is not in DRAFT status
     */
    public void setNotes(String notes) {
        ensureDraftStatus("set notes");
        
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
        updateTimestamp();
    }
    
    /**
     * Adds an item to the bill.
     * 
     * @param item the item to add
     * @throws IllegalArgumentException if item is null or bill already has maximum items
     * @throws IllegalStateException if bill is not in DRAFT status
     */
    public void addItem(BillItem item) {
        ensureDraftStatus("add item");
        Objects.requireNonNull(item, "Item cannot be null");
        
        if (items.size() >= MAX_ITEMS_PER_BILL) {
            throw new IllegalArgumentException(
                String.format("Cannot add more than %d items to a bill", MAX_ITEMS_PER_BILL)
            );
        }
        
        items.add(item);
        recalculateTotals();
        updateTimestamp();
    }
    
    /**
     * Removes an item from the bill.
     * 
     * @param item the item to remove
     * @throws IllegalArgumentException if item is null or not found
     * @throws IllegalStateException if bill is not in DRAFT status
     */
    public void removeItem(BillItem item) {
        ensureDraftStatus("remove item");
        Objects.requireNonNull(item, "Item cannot be null");
        
        if (!items.remove(item)) {
            throw new IllegalArgumentException("Item not found in bill");
        }
        
        recalculateTotals();
        updateTimestamp();
    }
    
    /**
     * Finds an item by product code.
     * 
     * @param productCode the product code to search for
     * @return the bill item if found
     */
    public Optional<BillItem> findItemByProductCode(String productCode) {
        return items.stream()
            .filter(item -> item.getProductCode().getValue().equals(productCode))
            .findFirst();
    }
    
    /**
     * Applies a discount amount.
     * 
     * @param discountAmount the discount amount
     * @throws IllegalArgumentException if discount amount is invalid
     * @throws IllegalStateException if bill is not in DRAFT status
     */
    public void applyDiscount(Money discountAmount) {
        ensureDraftStatus("apply discount");
        Objects.requireNonNull(discountAmount, "Discount amount cannot be null");
        
        if (discountAmount.isGreaterThan(subtotal)) {
            throw new IllegalArgumentException("Discount cannot be greater than subtotal");
        }
        
        this.discountAmount = discountAmount;
        recalculateTotals();
        updateTimestamp();
    }
    
    /**
     * Applies tax amount.
     * 
     * @param taxAmount the tax amount
     * @throws IllegalArgumentException if tax amount is invalid
     * @throws IllegalStateException if bill is not in DRAFT status
     */
    public void applyTax(Money taxAmount) {
        ensureDraftStatus("apply tax");
        Objects.requireNonNull(taxAmount, "Tax amount cannot be null");
        
        this.taxAmount = taxAmount;
        recalculateTotals();
        updateTimestamp();
    }
    
    /**
     * Finalizes the bill, making it immutable.
     * 
     * @throws IllegalStateException if bill is not in DRAFT status or has no items
     */
    public void finalizeBill() {
        if (status != Status.DRAFT) {
            throw new IllegalStateException("Only draft bills can be finalized");
        }
        
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot finalize bill with no items");
        }
        
        this.status = Status.FINALIZED;
        this.finalizedAt = Timestamp.now();
        updateTimestamp();
    }
    
    /**
     * Marks the bill as paid.
     * 
     * @param paymentMethod the payment method used
     * @throws IllegalArgumentException if payment method is null
     * @throws IllegalStateException if bill is not finalized
     */
    public void markAsPaid(PaymentMethod paymentMethod) {
        if (status != Status.FINALIZED) {
            throw new IllegalStateException("Only finalized bills can be marked as paid");
        }
        
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "Payment method cannot be null");
        this.status = Status.PAID;
        this.paidAt = Timestamp.now();
        updateTimestamp();
    }
    
    /**
     * Cancels the bill.
     * 
     * @throws IllegalStateException if bill is already paid
     */
    public void cancel() {
        if (status == Status.PAID) {
            throw new IllegalStateException("Cannot cancel paid bills");
        }
        
        this.status = Status.CANCELLED;
        updateTimestamp();
    }
    
    /**
     * Recalculates all totals based on current items, discount, and tax.
     */
    private void recalculateTotals() {
        this.subtotal = items.stream()
            .map(BillItem::getLineTotal)
            .reduce(Money.zero(), Money::add);
        
        this.totalAmount = subtotal.subtract(discountAmount).add(taxAmount);
    }
    
    /**
     * Ensures the bill is in DRAFT status for modifications.
     * 
     * @param operation the operation being attempted
     * @throws IllegalStateException if bill is not in DRAFT status
     */
    private void ensureDraftStatus(String operation) {
        if (status != Status.DRAFT) {
            throw new IllegalStateException(
                String.format("Cannot %s on %s bill", operation, status.getDisplayName().toLowerCase())
            );
        }
    }
    
    /**
     * Updates the last modification timestamp.
     */
    private void updateTimestamp() {
        this.updatedAt = Timestamp.now();
    }
    
    /**
     * Validates the bill number.
     * 
     * @param billNumber the bill number to validate
     * @return the validated bill number
     * @throws IllegalArgumentException if bill number is invalid
     */
    private String validateBillNumber(String billNumber) {
        Objects.requireNonNull(billNumber, "Bill number cannot be null");
        
        String trimmedBillNumber = billNumber.trim();
        if (trimmedBillNumber.isEmpty()) {
            throw new IllegalArgumentException("Bill number cannot be empty");
        }
        
        return trimmedBillNumber;
    }
    
    /**
     * Validates the cashier name.
     * 
     * @param cashierName the cashier name to validate
     * @return the validated cashier name
     * @throws IllegalArgumentException if cashier name is invalid
     */
    private String validateCashierName(String cashierName) {
        Objects.requireNonNull(cashierName, "Cashier name cannot be null");
        
        String trimmedName = cashierName.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Cashier name cannot be empty");
        }
        
        return trimmedName;
    }
    
    /**
     * Gets the creation timestamp.
     * 
     * @return the creation timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Gets the last update timestamp.
     * 
     * @return the last update timestamp
     */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Gets the finalized timestamp.
     * 
     * @return the finalized timestamp (can be null)
     */
    public Timestamp getFinalizedAt() {
        return finalizedAt;
    }
    
    /**
     * Gets the paid timestamp.
     * 
     * @return the paid timestamp (can be null)
     */
    public Timestamp getPaidAt() {
        return paidAt;
    }
    
    /**
     * Gets the total number of items.
     * 
     * @return the number of items
     */
    public int getItemCount() {
        return items.size();
    }
    
    /**
     * Checks if the bill is empty.
     * 
     * @return true if the bill has no items
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    /**
     * Checks if the bill can be modified.
     * 
     * @return true if the bill is in DRAFT status
     */
    public boolean canBeModified() {
        return status == Status.DRAFT;
    }
    
    /**
     * Checks if the bill can be finalized.
     * 
     * @return true if the bill is in DRAFT status and has items
     */
    public boolean canBeFinalized() {
        return status == Status.DRAFT && !items.isEmpty();
    }
    
    /**
     * Checks if the bill can be paid.
     * 
     * @return true if the bill is in FINALIZED status
     */
    public boolean canBePaid() {
        return status == Status.FINALIZED;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bill bill = (Bill) o;
        return Objects.equals(billNumber, bill.billNumber);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(billNumber);
    }
    
    @Override
    public String toString() {
        return String.format("Bill{number='%s', cashier=%s, items=%d, total=%s, status=%s}",
            billNumber, cashierName, items.size(), totalAmount, status);
    }
}