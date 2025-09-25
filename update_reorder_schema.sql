-- Update reorder_alert table schema to support product-level alerts
-- This adds the missing columns needed for proper REORDER alert generation

-- Add product_code column to directly reference products
ALTER TABLE reorder_alert ADD COLUMN product_code VARCHAR(20);

-- Add threshold_quantity column to track what threshold triggered the alert
ALTER TABLE reorder_alert ADD COLUMN threshold_quantity INTEGER;

-- Add status column to track alert state (ACTIVE, RESOLVED, etc.)
ALTER TABLE reorder_alert ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';

-- Add alert_created column (rename from created_at for UI consistency)
ALTER TABLE reorder_alert ADD COLUMN alert_created DATETIME DEFAULT CURRENT_TIMESTAMP;

-- Update existing records to have proper product_code values
UPDATE reorder_alert SET product_code = (
    SELECT p.product_code 
    FROM product p 
    WHERE p.product_id = reorder_alert.product_id
) WHERE product_code IS NULL;

-- Update existing records to have status
UPDATE reorder_alert SET status = 'ACTIVE' WHERE status IS NULL;

-- Update existing records to have alert_created from created_at
UPDATE reorder_alert SET alert_created = created_at WHERE alert_created IS NULL;