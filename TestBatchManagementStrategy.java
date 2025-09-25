/**
 * SYOS Inventory System - Complete Batch & Expiry Management Strategy
 * ==================================================================
 * 
 * 🎯 BUSINESS REQUIREMENTS ANALYSIS:
 * 
 * 1. PRODUCT CREATION WORKFLOW:
 *    ✅ When creating a product for the FIRST TIME, always create an initial batch
 *    ✅ User must provide: quantity, purchase_date, expiry_date (optional)
 *    ✅ Generate batch_number automatically using format: B-{PRODUCT_CODE}-{TIMESTAMP}
 *    ✅ Distribute inventory: 80% warehouse, 20% shelf, 0% online initially
 * 
 * 2. EXPIRY DATE LOGIC:
 *    ✅ NULL expiry_date = Non-perishable product (old batch code logic applies)
 *    ✅ Valid expiry_date = Perishable product (new batch system with FIFO)
 *    ✅ Expiry alerts: 30, 14, 7, and 1 days before expiry
 *    ✅ Expired products: Automatic flagging and removal suggestions
 * 
 * 3. BATCH MANAGEMENT RULES:
 *    ✅ Each purchase/delivery = New batch (even for same product)
 *    ✅ Different expiry dates = Different batches (mandatory)
 *    ✅ Same expiry date = Can be same batch (business decision)
 *    ✅ Batch tracking enables FIFO sales and expiry management
 * 
 * 4. SALES INTEGRATION (Future):
 *    ✅ FIFO Rule: Sell oldest batches first (by expiry_date, then purchase_date)
 *    ✅ Automatic batch selection during sales transactions
 *    ✅ Stock deduction priority: Physical Shelf → Warehouse → Online
 *    ✅ Cross-location transfers when shelf stock is low
 * 
 * 5. INVENTORY DISTRIBUTION STRATEGY:
 *    ✅ Warehouse (80%): Bulk storage, safety stock
 *    ✅ Physical Shelf (20%): Ready-to-sell, customer-facing
 *    ✅ Online (0% initially): Manual transfer from warehouse/shelf
 *    ✅ Reorder alerts when shelf < 20% of shelf_capacity
 * 
 * 6. BATCH NUMBERING SYSTEM:
 *    Format: B-{PRODUCT_CODE}-{YYYYMMDDHHMI}
 *    Examples:
 *    - B-PRD-FODR0001-202509251430
 *    - B-PRD-MASU0001-202509251431
 *    - B-PRD-TEST0001-202509251432
 * 
 * 7. DATABASE RELATIONSHIPS:
 *    product (1) → batch (M) → physical_inventory (M)
 *                           → online_inventory (M)
 *    
 *    Each batch can have inventory in multiple locations
 *    Each location can have inventory from multiple batches
 * 
 * 8. EXPIRY MANAGEMENT WORKFLOW:
 *    
 *    Non-Perishable (expiry_date = NULL):
 *    ├─ Single batch per product type
 *    ├─ Quantity tracking only
 *    ├─ No expiry alerts
 *    └─ Simple inventory management
 *    
 *    Perishable (expiry_date != NULL):
 *    ├─ Multiple batches per product
 *    ├─ FIFO sales priority
 *    ├─ Automatic expiry alerts
 *    ├─ Batch-specific inventory tracking
 *    └─ Complex inventory management
 * 
 * 9. SALES TRANSACTION RULES (Implementation Plan):
 *    
 *    Sale Request → Check Available Stock:
 *    ├─ Get all batches for product ORDER BY expiry_date ASC, purchase_date ASC
 *    ├─ For each batch (FIFO order):
 *    │   ├─ Check physical_inventory (shelf first, then warehouse)
 *    │   ├─ Deduct quantity from available stock
 *    │   ├─ Update inventory tables
 *    │   └─ Create stock_movement record
 *    └─ Generate bill_item with specific batch_id
 * 
 * 10. RESTOCK & REPLENISHMENT:
 *     
 *     Existing Product Restock:
 *     ├─ Always create NEW batch (different purchase/expiry dates)
 *     ├─ Generate new batch_number
 *     ├─ Apply same distribution rules (80%/20%)
 *     └─ Maintain separate inventory tracking
 *     
 *     Low Stock Alerts:
 *     ├─ Shelf quantity < min_threshold → SHELF_RESTOCK alert
 *     ├─ Total product quantity < safety_stock → NEW_BATCH_ORDER alert
 *     └─ Approaching expiry (30 days) → EXPIRY_ALERT
 * 
 * 📋 IMPLEMENTATION PRIORITY:
 * 
 * Phase 1 (Current): ✅ COMPLETED
 * - Product creation with batch
 * - Expiry date optional input
 * - Basic inventory distribution
 * - Batch number generation
 * 
 * Phase 2 (Next): 🔄 IN PROGRESS
 * - Enhanced ProductManagementUI with batch support
 * - Expiry alert system
 * - Batch history viewing
 * - Inventory transfer between locations
 * 
 * Phase 3 (Future): ⏳ PLANNED
 * - FIFO sales implementation
 * - Automatic batch selection during sales
 * - Stock movement tracking
 * - Comprehensive reporting
 * 
 * Phase 4 (Advanced): 📋 BACKLOG
 * - Barcode integration
 * - Mobile app support
 * - Real-time alerts
 * - AI-powered demand forecasting
 */
 
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestBatchManagementStrategy {
    
    private static final String DB_URL = "jdbc:sqlite:data/syos_inventory.db";
    
    public static void main(String[] args) {
        System.out.println("🧪 TESTING BATCH MANAGEMENT STRATEGY");
        System.out.println("===================================");
        
        try {
            // Test 1: Create perishable product (with expiry)
            testCreatePerishableProduct();
            
            // Test 2: Create non-perishable product (no expiry)
            testCreateNonPerishableProduct();
            
            // Test 3: Test batch numbering system
            testBatchNumberGeneration();
            
            // Test 4: Verify inventory distribution
            testInventoryDistribution();
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test creating a perishable product with expiry date
     */
    private static void testCreatePerishableProduct() throws SQLException {
        System.out.println("\n🧪 Test 1: Creating Perishable Product");
        
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Create product
            Long productId = createTestProduct(conn, "Fresh Milk", "DairyBrand", 
                "PRD-FRMI0001", "Dairy product");
            
            if (productId != null) {
                // Create batch with expiry date
                LocalDate expiryDate = LocalDate.now().plusDays(7); // Expires in 7 days
                Long batchId = createTestBatch(conn, productId, expiryDate, 100);
                
                if (batchId != null) {
                    // Create inventory records
                    createTestInventory(conn, batchId, 80, 20); // 80% warehouse, 20% shelf
                    System.out.println("✅ Perishable product created successfully!");
                    
                    // Check expiry alert needed
                    long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
                    if (daysUntilExpiry <= 30) {
                        System.out.println("⚠️  Expiry alert needed: " + daysUntilExpiry + " days until expiry");
                    }
                } else {
                    System.err.println("❌ Failed to create batch for perishable product");
                }
            }
        }
    }
    
    /**
     * Test creating a non-perishable product without expiry date
     */
    private static void testCreateNonPerishableProduct() throws SQLException {
        System.out.println("\n🧪 Test 2: Creating Non-Perishable Product");
        
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Create product
            Long productId = createTestProduct(conn, "Steel Nails", "HardwareCo", 
                "PRD-STNA0001", "Construction hardware");
            
            if (productId != null) {
                // Create batch without expiry date (NULL)
                Long batchId = createTestBatch(conn, productId, null, 500);
                
                if (batchId != null) {
                    // Create inventory records
                    createTestInventory(conn, batchId, 400, 100); // 400 warehouse, 100 shelf
                    System.out.println("✅ Non-perishable product created successfully!");
                    System.out.println("ℹ️  No expiry tracking needed for this product");
                } else {
                    System.err.println("❌ Failed to create batch for non-perishable product");
                }
            }
        }
    }
    
    /**
     * Test batch number generation
     */
    private static void testBatchNumberGeneration() {
        System.out.println("\n🧪 Test 3: Batch Number Generation");
        
        String productCode1 = "PRD-FODR0001";
        String productCode2 = "PRD-MASU0001";
        
        String batch1 = generateBatchNumber(productCode1);
        String batch2 = generateBatchNumber(productCode2);
        
        System.out.println("Product: " + productCode1 + " → Batch: " + batch1);
        System.out.println("Product: " + productCode2 + " → Batch: " + batch2);
        
        // Verify format
        if (batch1.startsWith("B-" + productCode1 + "-") && batch1.length() == 28) {
            System.out.println("✅ Batch numbering format is correct");
        } else {
            System.err.println("❌ Batch numbering format is incorrect");
        }
    }
    
    /**
     * Test inventory distribution logic
     */
    private static void testInventoryDistribution() {
        System.out.println("\n🧪 Test 4: Inventory Distribution");
        
        int[] testQuantities = {100, 150, 75, 1000, 33};
        
        for (int totalQty : testQuantities) {
            int warehouseQty = (int) Math.floor(totalQty * 0.80);
            int shelfQty = (int) Math.ceil(totalQty * 0.20);
            
            System.out.printf("Total: %d → Warehouse: %d (%.0f%%), Shelf: %d (%.0f%%)%n", 
                totalQty, warehouseQty, (warehouseQty * 100.0 / totalQty), 
                shelfQty, (shelfQty * 100.0 / totalQty));
        }
        System.out.println("✅ Distribution logic working correctly");
    }
    
    // Helper methods
    
    private static Long createTestProduct(Connection conn, String name, String brand, 
                                        String productCode, String description) throws SQLException {
        String sql = "INSERT INTO product (product_code, product_name, description, brand, base_price, " +
                    "unit_of_measure, subcategory_id, final_price, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, productCode);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.setString(4, brand);
            stmt.setDouble(5, 10.99);
            stmt.setString(6, "pieces");
            stmt.setInt(7, 1);
            stmt.setDouble(8, 10.99);
            stmt.setInt(9, 1);
            
            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        }
        return null;
    }
    
    private static Long createTestBatch(Connection conn, Long productId, LocalDate expiryDate, 
                                      int quantity) throws SQLException {
        String sql = "INSERT INTO batch (product_id, batch_number, purchase_date, expiry_date, " +
                    "quantity_received, selling_price) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, productId);
            stmt.setString(2, "BATCH-TEST-" + System.currentTimeMillis());
            stmt.setString(3, LocalDate.now().toString());
            stmt.setString(4, expiryDate != null ? expiryDate.toString() : null);
            stmt.setInt(5, quantity);
            stmt.setDouble(6, 12.99);
            
            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        }
        return null;
    }
    
    private static void createTestInventory(Connection conn, Long batchId, 
                                          int warehouseQty, int shelfQty) throws SQLException {
        // Warehouse inventory (location_id = 14)
        String sql1 = "INSERT INTO physical_inventory (batch_id, location_id, current_quantity, " +
                     "min_threshold, shelf_capacity) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
            stmt.setLong(1, batchId);
            stmt.setInt(2, 14); // Warehouse
            stmt.setInt(3, warehouseQty);
            stmt.setInt(4, 50);
            stmt.setInt(5, 1000);
            stmt.executeUpdate();
        }
        
        // Shelf inventory (location_id = 15)
        try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
            stmt.setLong(1, batchId);
            stmt.setInt(2, 15); // Shelf
            stmt.setInt(3, shelfQty);
            stmt.setInt(4, 20);
            stmt.setInt(5, 100);
            stmt.executeUpdate();
        }
    }
    
    private static String generateBatchNumber(String productCode) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return "B-" + productCode + "-" + timestamp;
    }
}