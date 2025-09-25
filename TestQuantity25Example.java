import java.sql.*;
import java.math.BigDecimal;

/**
 * Test to demonstrate reorder logic when product quantity is 25
 * Shows how our Option A safety minimum logic works in practice
 */
public class TestQuantity25Example {
    private static final String DATABASE_PATH = "syos_inventory.db";
    private static final int DEFAULT_REORDER_LEVEL = 50;
    
    public static void main(String[] args) {
        System.out.println("=== REORDER LOGIC TEST: Product Quantity = 25 ===\n");
        
        // Test different scenarios with quantity 25
        testScenario1_SlowMoving();
        testScenario2_MediumMoving();
        testScenario3_FastMoving();
        testScenario4_NewProduct();
    }
    
    /**
     * Scenario 1: Slow-moving product with qty 25
     */
    private static void testScenario1_SlowMoving() {
        System.out.println("📦 SCENARIO 1: Slow-Moving Product");
        System.out.println("Current Stock: 25 units");
        System.out.println("Estimated Capacity: 100 units");
        System.out.println("Sales Pattern: 2 transactions, 8 units sold (30 days)");
        
        int currentStock = 25;
        int capacity = 100;
        int smartReorder = Math.max((int)(capacity * 0.15), 5); // 15% for slow moving
        int finalReorder = Math.max(smartReorder, DEFAULT_REORDER_LEVEL);
        
        System.out.println("\n🧮 Calculation:");
        System.out.println("Raw Smart Analysis: 15% of " + capacity + " = " + smartReorder + " units");
        System.out.println("Safety Minimum: " + DEFAULT_REORDER_LEVEL + " units");
        System.out.println("Final Reorder Level: Math.max(" + smartReorder + ", " + DEFAULT_REORDER_LEVEL + ") = " + finalReorder + " units");
        
        System.out.println("\n📊 Restocking Decision:");
        if (currentStock <= finalReorder) {
            System.out.println("❗ RESTOCK NEEDED: " + currentStock + " ≤ " + finalReorder);
            System.out.println("Recommended Order: " + (finalReorder - currentStock + 25) + " units");
            System.out.println("Logic: Safety minimum protects against stockouts for slow-moving items");
        } else {
            System.out.println("✅ STOCK OK: " + currentStock + " > " + finalReorder);
        }
        System.out.println("\n" + "=".repeat(60) + "\n");
    }
    
    /**
     * Scenario 2: Medium-moving product with qty 25
     */
    private static void testScenario2_MediumMoving() {
        System.out.println("📦 SCENARIO 2: Medium-Moving Product");
        System.out.println("Current Stock: 25 units");
        System.out.println("Estimated Capacity: 120 units");
        System.out.println("Sales Pattern: 5 transactions, 25 units sold (30 days)");
        
        int currentStock = 25;
        int capacity = 120;
        int smartReorder = Math.max((int)(capacity * 0.25), 10); // 25% for medium moving
        int finalReorder = Math.max(smartReorder, DEFAULT_REORDER_LEVEL);
        
        System.out.println("\n🧮 Calculation:");
        System.out.println("Raw Smart Analysis: 25% of " + capacity + " = " + smartReorder + " units");
        System.out.println("Safety Minimum: " + DEFAULT_REORDER_LEVEL + " units");
        System.out.println("Final Reorder Level: Math.max(" + smartReorder + ", " + DEFAULT_REORDER_LEVEL + ") = " + finalReorder + " units");
        
        System.out.println("\n📊 Restocking Decision:");
        if (currentStock <= finalReorder) {
            System.out.println("❗ RESTOCK NEEDED: " + currentStock + " ≤ " + finalReorder);
            System.out.println("Recommended Order: " + (finalReorder - currentStock + 25) + " units");
            System.out.println("Logic: Safety minimum applied since smart (" + smartReorder + ") < 50");
        } else {
            System.out.println("✅ STOCK OK: " + currentStock + " > " + finalReorder);
        }
        System.out.println("\n" + "=".repeat(60) + "\n");
    }
    
    /**
     * Scenario 3: Fast-moving product with qty 25 (large capacity)
     */
    private static void testScenario3_FastMoving() {
        System.out.println("📦 SCENARIO 3: Fast-Moving Product (Large Capacity)");
        System.out.println("Current Stock: 25 units");
        System.out.println("Estimated Capacity: 200 units");
        System.out.println("Sales Pattern: 15 transactions, 80 units sold (30 days)");
        
        int currentStock = 25;
        int capacity = 200;
        int smartReorder = Math.max((int)(capacity * 0.40), 20); // 40% for fast moving
        int finalReorder = Math.max(smartReorder, DEFAULT_REORDER_LEVEL);
        
        System.out.println("\n🧮 Calculation:");
        System.out.println("Raw Smart Analysis: 40% of " + capacity + " = " + smartReorder + " units");
        System.out.println("Safety Minimum: " + DEFAULT_REORDER_LEVEL + " units");
        System.out.println("Final Reorder Level: Math.max(" + smartReorder + ", " + DEFAULT_REORDER_LEVEL + ") = " + finalReorder + " units");
        
        System.out.println("\n📊 Restocking Decision:");
        if (currentStock <= finalReorder) {
            System.out.println("❗ RESTOCK NEEDED: " + currentStock + " ≤ " + finalReorder);
            System.out.println("Recommended Order: " + (finalReorder - currentStock + 50) + " units");
            System.out.println("Logic: Smart calculation (" + smartReorder + ") > 50, so using optimized level");
        } else {
            System.out.println("✅ STOCK OK: " + currentStock + " > " + finalReorder);
        }
        System.out.println("\n" + "=".repeat(60) + "\n");
    }
    
    /**
     * Scenario 4: New product with qty 25
     */
    private static void testScenario4_NewProduct() {
        System.out.println("📦 SCENARIO 4: New Product (No Sales History)");
        System.out.println("Current Stock: 25 units");
        System.out.println("Estimated Capacity: 80 units");
        System.out.println("Sales Pattern: No sales yet");
        
        int currentStock = 25;
        int capacity = 80;
        int smartReorder = Math.max((int)(capacity * 0.20), 10); // 20% for new products
        int finalReorder = Math.max(smartReorder, DEFAULT_REORDER_LEVEL);
        
        System.out.println("\n🧮 Calculation:");
        System.out.println("Raw Smart Analysis: 20% of " + capacity + " = " + smartReorder + " units (new product)");
        System.out.println("Safety Minimum: " + DEFAULT_REORDER_LEVEL + " units");
        System.out.println("Final Reorder Level: Math.max(" + smartReorder + ", " + DEFAULT_REORDER_LEVEL + ") = " + finalReorder + " units");
        
        System.out.println("\n📊 Restocking Decision:");
        if (currentStock <= finalReorder) {
            System.out.println("❗ RESTOCK NEEDED: " + currentStock + " ≤ " + finalReorder);
            System.out.println("Recommended Order: " + (finalReorder - currentStock + 25) + " units");
            System.out.println("Logic: Safety minimum protects new products with unknown demand");
        } else {
            System.out.println("✅ STOCK OK: " + currentStock + " > " + finalReorder);
        }
        System.out.println("\n" + "=".repeat(60) + "\n");
    }
}