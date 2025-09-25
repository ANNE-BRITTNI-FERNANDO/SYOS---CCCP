/**
 * 🎯 COMPREHENSIVE BATCH MANAGEMENT LOGIC FOR SYOS
 * ==============================================
 * 
 * BUSINESS RULES:
 * 
 * 1. PRODUCT CREATION (First Time):
 *    ├─ Always create initial batch
 *    ├─ User chooses: Perishable vs Non-perishable
 *    ├─ Batch format: B-{PRODUCT_CODE}-{YYYYMMDDHHMI}
 *    └─ Inventory distribution: 80% Warehouse, 20% Shelf
 * 
 * 2. EXPIRY DATE RULES:
 *    ├─ NULL = Non-perishable (no expiry tracking)
 *    ├─ Valid Date = Perishable (FIFO sales required)
 *    └─ Alerts: 30, 14, 7, 1 days before expiry
 * 
 * 3. BATCH CREATION TRIGGERS:
 *    ├─ New Product → Always create initial batch
 *    ├─ New Purchase → Always create new batch (different dates/prices)
 *    ├─ Different Expiry → Mandatory new batch
 *    └─ Same Expiry → Business decision (recommended: new batch)
 * 
 * 4. SALES LOGIC (Future Implementation):
 *    ├─ Perishable: FIFO (oldest expiry first)
 *    ├─ Non-perishable: Any batch available
 *    ├─ Stock deduction: Shelf → Warehouse → Online
 *    └─ Cross-location transfers when needed
 * 
 * 5. INVENTORY MANAGEMENT:
 *    ├─ Each batch tracks inventory separately
 *    ├─ Multiple locations per batch possible
 *    ├─ Restock alerts based on total product quantity
 *    └─ Expiry alerts based on individual batches
 * 
 * EXAMPLES:
 * 
 * Scenario 1: Fresh Milk (Perishable)
 * ├─ Product: PRD-FRMI0001
 * ├─ Batch 1: B-PRD-FRMI0001-202509251200 (expires 2025-10-02)
 * ├─ Batch 2: B-PRD-FRMI0001-202509281200 (expires 2025-10-05)
 * └─ Sales Priority: Batch 1 first (FIFO)
 * 
 * Scenario 2: Steel Nails (Non-perishable)
 * ├─ Product: PRD-STNA0001  
 * ├─ Batch 1: B-PRD-STNA0001-202509251200 (no expiry)
 * ├─ Batch 2: B-PRD-STNA0001-202510151200 (no expiry)
 * └─ Sales Priority: Any batch available
 * 
 * WORKFLOW FOR DIFFERENT SCENARIOS:
 * 
 * 📱 UI Flow for Product Creation:
 * 1. Enter product details (name, brand, price, etc.)
 * 2. Choose product type:
 *    ├─ "Perishable" → Ask for expiry date (required)
 *    └─ "Non-perishable" → Skip expiry (set NULL)
 * 3. Enter initial batch details:
 *    ├─ Quantity (required)
 *    ├─ Purchase date (default: today)
 *    └─ Selling price for this batch
 * 4. Confirm and create → Auto-generate batch and distribute inventory
 * 
 * 🔄 Restock Workflow (Existing Products):
 * 1. Select existing product
 * 2. Enter new batch details:
 *    ├─ New quantity
 *    ├─ New purchase date
 *    ├─ New expiry date (if perishable)
 *    └─ New selling price
 * 3. Create new batch → Apply same distribution rules
 * 
 * 💰 Sales Workflow (Future):
 * 1. Customer buys product
 * 2. System finds best batch:
 *    ├─ Perishable: Oldest expiry date first
 *    └─ Non-perishable: Highest quantity available
 * 3. Check inventory availability:
 *    ├─ Physical Shelf (priority)
 *    ├─ Warehouse (if shelf insufficient)
 *    └─ Transfer stock if needed
 * 4. Deduct from specific batch and location
 * 5. Update inventory tables and create movement record
 */