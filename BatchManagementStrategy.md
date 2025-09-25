/**
 * SYOS Inventory System - Batch Management Strategy
 * =================================================
 * 
 * BUSINESS LOGIC FOR BATCH & EXPIRY MANAGEMENT:
 * 
 * 1. PRODUCT CREATION WORKFLOW:
 *    - When creating a product for the FIRST TIME:
 *      → Always create an initial batch immediately
 *      → User must provide: quantity, purchase_date, expiry_date (optional)
 *      → Generate batch_number automatically
 * 
 * 2. EXPIRY DATE LOGIC:
 *    - If expiry_date is NULL → Non-perishable product (old batch code logic)
 *    - If expiry_date is provided → Perishable product (new batch system)
 *    - Expiry alerts generated 30, 14, 7, and 1 days before expiry
 * 
 * 3. BATCH NUMBERING SYSTEM:
 *    - Format: B-{PRODUCT_CODE}-{YYYYMMDDHHMI}
 *    - Example: B-PRD-FODR0001-202509251045
 *    - Ensures unique batch identification
 * 
 * 4. INVENTORY DISTRIBUTION:
 *    - 80% to WAREHOUSE (location_id: 14)
 *    - 20% to PHYSICAL_SHELF (location_id: 15) 
 *    - 0% to ONLINE initially (manual transfer)
 * 
 * 5. SALES LOGIC (Future Implementation):
 *    - FIFO (First In, First Out) for perishable items
 *    - Automatic batch selection based on expiry_date
 *    - Stock deduction from physical_inventory first
 * 
 * 6. BATCH REPLENISHMENT:
 *    - New batches for existing products
 *    - Different expiry dates = different batches
 *    - Separate inventory tracking per batch
 */