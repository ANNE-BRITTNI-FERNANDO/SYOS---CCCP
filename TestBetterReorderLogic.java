import java.sql.*;

/**
 * Test to show the problem with current logic and demonstrate better logic
 * Current Problem: Even slow-moving products get restocked above 50 units
 * Better Logic: Fast-moving = restock even above 50, Slow-moving = only restock below 50
 */
public class TestBetterReorderLogic {
    
    public static void main(String[] args) {
        System.out.println("=== CURRENT LOGIC PROBLEMS & BETTER SOLUTION ===\n");
        
        // Show the problem scenarios
        System.out.println("🚨 CURRENT LOGIC PROBLEMS:");
        testCurrentLogicProblems();
        
        System.out.println("\n" + "=".repeat(70) + "\n");
        
        // Show the better logic
        System.out.println("✅ BETTER LOGIC SOLUTION:");
        testBetterLogic();
    }
    
    private static void testCurrentLogicProblems() {
        System.out.println("❌ Problem 1: Slow product with 70 units still triggers restock");
        System.out.println("   Product: PRD-SLOW, Current: 70 units, Sales: 2 transactions/month");
        System.out.println("   Current Logic: 70 > 50 → Still says 'RESTOCK NEEDED' (Wrong!)");
        System.out.println("   Why Wrong: Slow sales, already above safety minimum, no rush needed");
        
        System.out.println("\n❌ Problem 2: Fast product with 60 units uses same logic as slow");
        System.out.println("   Product: PRD-FAST, Current: 60 units, Sales: 20 transactions/month");
        System.out.println("   Current Logic: Treats same as slow product (Wrong!)");
        System.out.println("   Why Wrong: Fast sales need aggressive restocking even above 50");
    }
    
    private static void testBetterLogic() {
        // Test scenarios with better logic
        testScenario("Slow + Below 50", 25, "slow", 2, 8);
        testScenario("Slow + Above 50", 70, "slow", 2, 8);
        testScenario("Fast + Below 50", 25, "fast", 15, 80);
        testScenario("Fast + Above 50", 60, "fast", 15, 80);
        testScenario("Medium + Above 50", 65, "medium", 5, 25);
    }
    
    private static void testScenario(String scenario, int currentStock, String velocity, 
                                    int transactions, int unitsSold) {
        System.out.println("\n📦 " + scenario.toUpperCase());
        System.out.println("   Current Stock: " + currentStock + " units");
        System.out.println("   Sales Pattern: " + transactions + " transactions, " + unitsSold + " units/month");
        
        // Better Logic Implementation
        boolean needsRestock = false;
        String reason = "";
        int recommendedOrder = 0;
        
        if (currentStock < 50) {
            // Always restock if below safety minimum
            needsRestock = true;
            reason = "Below safety minimum (50)";
            recommendedOrder = calculateOrderAmount(currentStock, velocity, 50);
        } else {
            // Above 50: Only restock if moving fast
            if (velocity.equals("fast")) {
                // Fast moving: aggressive restocking even above 50
                int fastReorderLevel = 80; // 40% of 200 capacity example
                if (currentStock <= fastReorderLevel) {
                    needsRestock = true;
                    reason = "Fast-moving product needs aggressive restocking";
                    recommendedOrder = calculateOrderAmount(currentStock, velocity, fastReorderLevel);
                } else {
                    needsRestock = false;
                    reason = "Fast-moving but stock level adequate (" + currentStock + " > " + fastReorderLevel + ")";
                }
            } else {
                // Slow/Medium moving: no restocking above 50
                needsRestock = false;
                reason = "Above safety minimum and not fast-moving";
            }
        }
        
        System.out.println("   Decision: " + (needsRestock ? "❗ RESTOCK NEEDED" : "✅ NO RESTOCK"));
        System.out.println("   Logic: " + reason);
        if (needsRestock) {
            System.out.println("   Order: " + recommendedOrder + " units");
        }
    }
    
    private static int calculateOrderAmount(int currentStock, String velocity, int targetLevel) {
        switch (velocity) {
            case "fast":
                return Math.max(targetLevel - currentStock + 50, 30); // Larger buffer for fast
            case "medium":
                return Math.max(targetLevel - currentStock + 25, 25); // Medium buffer
            case "slow":
                return Math.max(targetLevel - currentStock + 15, 20); // Smaller buffer
            default:
                return Math.max(targetLevel - currentStock + 20, 25);
        }
    }
}