import java.sql.*;

/**
 * Test the NEW SMART REORDER LOGIC:
 * - Below 50: Always restock (safety minimum)
 * - Above 50 + Fast-moving: Restock for aggressive inventory 
 * - Above 50 + Slow/Medium: NO restock needed (smart business logic!)
 */
public class TestNewSmartLogic {
    
    public static void main(String[] args) {
        System.out.println("=== NEW SMART REORDER LOGIC TEST ===\n");
        
        System.out.println("🎯 THE NEW SMART RULES:");
        System.out.println("1. Current Stock < 50 → ALWAYS RESTOCK (safety minimum)");
        System.out.println("2. Current Stock ≥ 50 + Fast-moving → RESTOCK (aggressive for fast sales)");
        System.out.println("3. Current Stock ≥ 50 + Slow/Medium → NO RESTOCK (no point over-stocking)");
        System.out.println("\n" + "=".repeat(70) + "\n");
        
        // Test all scenarios
        testNewLogic("Below 50 + Slow", 25, "SLOW", true, "Safety minimum protection");
        testNewLogic("Below 50 + Fast", 35, "FAST", true, "Safety minimum protection");
        testNewLogic("Above 50 + Slow", 70, "SLOW", false, "Above safety + slow sales = no rush");
        testNewLogic("Above 50 + Medium", 65, "MEDIUM", false, "Above safety + medium sales = no rush");
        testNewLogic("Above 50 + Fast", 60, "FAST", true, "Fast sales need aggressive restocking");
        testNewLogic("Above 50 + Fast (High Stock)", 95, "FAST", false, "Even fast items don't need restock when very high");
    }
    
    private static void testNewLogic(String scenario, int currentStock, String velocity, 
                                    boolean expectedRestock, String expectedReason) {
        
        System.out.println("📦 " + scenario.toUpperCase());
        System.out.println("   Current Stock: " + currentStock + " units");
        System.out.println("   Sales Velocity: " + velocity);
        
        // Apply the NEW SMART LOGIC
        boolean needsRestock;
        String reason;
        int orderAmount = 0;
        
        if (currentStock < 50) {
            // Rule 1: Below 50 = Always restock
            needsRestock = true;
            reason = "Below safety minimum (50 units)";
            orderAmount = 50 - currentStock + getVelocityBuffer(velocity);
        } else {
            // Above 50: Check velocity
            if (velocity.equals("FAST")) {
                // Rule 2: Fast + Above 50 = Check against higher threshold
                int fastThreshold = 80; // Example for fast-moving
                if (currentStock <= fastThreshold) {
                    needsRestock = true;
                    reason = "Fast-moving needs aggressive restocking";
                    orderAmount = fastThreshold - currentStock + 30;
                } else {
                    needsRestock = false;
                    reason = "Fast-moving but stock very high (" + currentStock + " > " + fastThreshold + ")";
                }
            } else {
                // Rule 3: Slow/Medium + Above 50 = NO restock
                needsRestock = false;
                reason = "Above safety minimum, no rush for " + velocity.toLowerCase() + "-moving items";
            }
        }
        
        System.out.println("   Decision: " + (needsRestock ? "❗ RESTOCK NEEDED" : "✅ NO RESTOCK"));
        System.out.println("   Logic: " + reason);
        if (needsRestock) {
            System.out.println("   Recommended Order: " + orderAmount + " units");
        }
        
        // Validate against expected result
        String validation = (needsRestock == expectedRestock) ? "✅ CORRECT" : "❌ WRONG LOGIC";
        System.out.println("   Validation: " + validation);
        System.out.println();
    }
    
    private static int getVelocityBuffer(String velocity) {
        switch (velocity) {
            case "FAST": return 30;
            case "MEDIUM": return 20;
            case "SLOW": return 15;
            default: return 20;
        }
    }
}