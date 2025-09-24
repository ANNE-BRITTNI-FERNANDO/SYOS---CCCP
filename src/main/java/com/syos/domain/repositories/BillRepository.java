package com.syos.domain.repositories;

import com.syos.domain.entities.Bill;
import com.syos.shared.valueobjects.UserCode;
import com.syos.shared.valueobjects.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Bill entities.
 * 
 * This interface follows the Repository pattern and defines the contract
 * for Bill persistence operations. It belongs to the domain layer
 * and follows the Dependency Inversion Principle.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public interface BillRepository {
    
    /**
     * Saves a bill to the repository.
     * If the bill already exists, it will be updated.
     * 
     * @param bill the bill to save
     * @return the saved bill
     * @throws IllegalArgumentException if bill is null
     */
    Bill save(Bill bill);
    
    /**
     * Finds a bill by its bill number.
     * 
     * @param billNumber the bill number
     * @return an Optional containing the bill if found, empty otherwise
     * @throws IllegalArgumentException if billNumber is null or empty
     */
    Optional<Bill> findByBillNumber(String billNumber);
    
    /**
     * Finds all bills in the repository.
     * 
     * @return a list of all bills
     */
    List<Bill> findAll();
    
    /**
     * Finds bills by status.
     * 
     * @param status the bill status
     * @return a list of bills with the specified status
     * @throws IllegalArgumentException if status is null
     */
    List<Bill> findByStatus(Bill.Status status);
    
    /**
     * Finds bills by cashier code.
     * 
     * @param cashierCode the cashier code
     * @return a list of bills created by the specified cashier
     * @throws IllegalArgumentException if cashierCode is null
     */
    List<Bill> findByCashierCode(UserCode cashierCode);
    
    /**
     * Finds bills by cashier code string.
     * 
     * @param cashierCode the cashier code as string
     * @return a list of bills created by the specified cashier
     * @throws IllegalArgumentException if cashierCode is null or empty
     */
    List<Bill> findByCashierCode(String cashierCode);
    
    /**
     * Finds bills created within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return a list of bills created within the specified date range
     * @throws IllegalArgumentException if startDate or endDate is null, or startDate is after endDate
     */
    List<Bill> findByDateRange(Timestamp startDate, Timestamp endDate);
    
    /**
     * Finds bills by payment method.
     * 
     * @param paymentMethod the payment method
     * @return a list of bills paid with the specified payment method
     * @throws IllegalArgumentException if paymentMethod is null
     */
    List<Bill> findByPaymentMethod(Bill.PaymentMethod paymentMethod);
    
    /**
     * Finds bills by customer name (case-insensitive partial match).
     * 
     * @param customerName the customer name or partial name
     * @return a list of bills matching the customer name
     * @throws IllegalArgumentException if customerName is null or empty
     */
    List<Bill> findByCustomerNameContaining(String customerName);
    
    /**
     * Finds bills by customer phone.
     * 
     * @param customerPhone the customer phone number
     * @return a list of bills for the specified customer phone
     * @throws IllegalArgumentException if customerPhone is null or empty
     */
    List<Bill> findByCustomerPhone(String customerPhone);
    
    /**
     * Finds draft bills (bills that can still be modified).
     * 
     * @return a list of draft bills
     */
    List<Bill> findDraftBills();
    
    /**
     * Finds finalized bills (bills ready for payment).
     * 
     * @return a list of finalized bills
     */
    List<Bill> findFinalizedBills();
    
    /**
     * Finds paid bills.
     * 
     * @return a list of paid bills
     */
    List<Bill> findPaidBills();
    
    /**
     * Finds cancelled bills.
     * 
     * @return a list of cancelled bills
     */
    List<Bill> findCancelledBills();
    
    /**
     * Finds bills created today.
     * 
     * @return a list of bills created today
     */
    List<Bill> findTodaysBills();
    
    /**
     * Finds bills created by a specific cashier today.
     * 
     * @param cashierCode the cashier code
     * @return a list of bills created by the cashier today
     * @throws IllegalArgumentException if cashierCode is null
     */
    List<Bill> findTodaysBillsByCashier(UserCode cashierCode);
    
    /**
     * Checks if a bill exists with the given bill number.
     * 
     * @param billNumber the bill number
     * @return true if a bill exists with the given number
     * @throws IllegalArgumentException if billNumber is null or empty
     */
    boolean existsByBillNumber(String billNumber);
    
    /**
     * Deletes a bill from the repository.
     * Note: This should be used carefully as it permanently removes the bill.
     * Consider cancelling bills instead of deleting them.
     * 
     * @param bill the bill to delete
     * @throws IllegalArgumentException if bill is null
     */
    void delete(Bill bill);
    
    /**
     * Deletes a bill by its bill number.
     * Note: This should be used carefully as it permanently removes the bill.
     * Consider cancelling bills instead of deleting them.
     * 
     * @param billNumber the bill number
     * @throws IllegalArgumentException if billNumber is null or empty
     */
    void deleteByBillNumber(String billNumber);
    
    /**
     * Counts the total number of bills.
     * 
     * @return the total number of bills
     */
    long count();
    
    /**
     * Counts the number of bills with a specific status.
     * 
     * @param status the bill status
     * @return the number of bills with the specified status
     * @throws IllegalArgumentException if status is null
     */
    long countByStatus(Bill.Status status);
    
    /**
     * Counts the number of bills created by a specific cashier.
     * 
     * @param cashierCode the cashier code
     * @return the number of bills created by the specified cashier
     * @throws IllegalArgumentException if cashierCode is null
     */
    long countByCashierCode(UserCode cashierCode);
    
    /**
     * Counts the number of bills created within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return the number of bills created within the specified date range
     * @throws IllegalArgumentException if startDate or endDate is null, or startDate is after endDate
     */
    long countByDateRange(Timestamp startDate, Timestamp endDate);
    
    /**
     * Counts the number of bills created today.
     * 
     * @return the number of bills created today
     */
    long countTodaysBills();
    
    /**
     * Generates the next available bill number.
     * This method should ensure uniqueness and follow the business rules
     * for bill number generation.
     * 
     * @return the next available bill number
     */
    String generateNextBillNumber();
}