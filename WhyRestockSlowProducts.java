/**
 * Analysis: Why restock slow-moving products below 50 units?
 * This demonstrates the business risks of NOT restocking slow items
 */
public class WhyRestockSlowProducts {
    
    public static void main(String[] args) {
        System.out.println("=== WHY RESTOCK SLOW-MOVING PRODUCTS? ===\n");
        
        System.out.println("🐌 SLOW PRODUCT SCENARIO:");
        System.out.println("Product: Organic Quinoa Crackers");
        System.out.println("Current Stock: 25 units");
        System.out.println("Sales Pattern: 2 transactions/month, 8 units/month");
        System.out.println("Monthly Sales Rate: ~8 units");
        System.out.println();
        
        // Scenario 1: DON'T restock slow products
        System.out.println("❌ OPTION A: Don't Restock (Your Question)");
        analyzeNoRestockScenario();
        
        System.out.println("\n" + "=".repeat(60) + "\n");
        
        // Scenario 2: DO restock slow products  
        System.out.println("✅ OPTION B: Restock to Safety Minimum (Current Logic)");
        analyzeRestockScenario();
        
        System.out.println("\n" + "=".repeat(60) + "\n");
        
        System.out.println("🎯 BUSINESS CONCLUSION:");
        showBusinessConclusion();
    }
    
    private static void analyzeNoRestockScenario() {
        System.out.println("Decision: Don't restock because sales are slow");
        System.out.println("Logic: 'Why waste money on slow-moving inventory?'");
        System.out.println();
        
        System.out.println("📅 TIMELINE PROJECTION:");
        int currentStock = 25;
        int monthlyUsage = 8;
        
        for (int month = 1; month <= 5; month++) {
            currentStock -= monthlyUsage;
            System.out.println("Month " + month + ": " + Math.max(currentStock, 0) + " units remaining");
            
            if (currentStock <= 0) {
                System.out.println("❌ STOCKOUT in Month " + month + "!");
                break;
            }
        }
        
        System.out.println();
        System.out.println("💔 PROBLEMS:");
        System.out.println("1. Stockout in ~3 months (25 ÷ 8 = 3.1 months)");
        System.out.println("2. Customer disappointment: 'Why don't you have this?'");
        System.out.println("3. Lost sales: Even 2 customers/month = lost revenue");
        System.out.println("4. Customer trust: 'This store never has what I need'");
        System.out.println("5. Emergency reorders: Rush shipping costs more");
    }
    
    private static void analyzeRestockScenario() {
        System.out.println("Decision: Restock to 50 units (safety minimum)");
        System.out.println("Logic: 'Ensure consistent availability'");
        System.out.println();
        
        System.out.println("📅 TIMELINE PROJECTION:");
        int targetStock = 50;
        int orderAmount = 40; // To reach 50 from 25
        int monthlyUsage = 8;
        
        System.out.println("Order: " + orderAmount + " units → Stock becomes " + targetStock);
        
        int currentStock = targetStock;
        for (int month = 1; month <= 8; month++) {
            currentStock -= monthlyUsage;
            System.out.println("Month " + month + ": " + currentStock + " units remaining");
            
            if (currentStock <= 15) {
                System.out.println("⚠️  Time to reorder again (before stockout)");
                break;
            }
        }
        
        System.out.println();
        System.out.println("✅ BENEFITS:");
        System.out.println("1. 6+ months of stock coverage (50 ÷ 8 = 6.25 months)");
        System.out.println("2. Happy customers: Product always available");
        System.out.println("3. No lost sales: Every customer finds what they need");
        System.out.println("4. Brand reliability: 'This store always has stock'");
        System.out.println("5. Planned reorders: Better pricing, no rush fees");
    }
    
    private static void showBusinessConclusion() {
        System.out.println("🎯 THE REAL BUSINESS LOGIC:");
        System.out.println();
        System.out.println("1. CUSTOMER SATISFACTION:");
        System.out.println("   - Even slow products have loyal customers");
        System.out.println("   - Stockouts destroy customer trust");
        System.out.println("   - 'Always available' = competitive advantage");
        System.out.println();
        
        System.out.println("2. COST ANALYSIS:");
        System.out.println("   - Cost of 40 extra units: ~$20-100 (one-time)");
        System.out.println("   - Cost of lost customers: $1000+ (lifetime value)");
        System.out.println("   - Cost of emergency orders: 2-3x normal price");
        System.out.println();
        
        System.out.println("3. RISK MANAGEMENT:");
        System.out.println("   - Supply chain delays happen");
        System.out.println("   - Seasonal demand spikes occur");
        System.out.println("   - Safety stock protects against unknowns");
        System.out.println();
        
        System.out.println("💡 SMARTER APPROACH:");
        System.out.println("   Instead of 'Don't restock slow items'");
        System.out.println("   Think: 'Restock smart amounts for slow items'");
        System.out.println("   - Fast items: Order 100+ units aggressively");
        System.out.println("   - Slow items: Order just 25-40 units (safety buffer)");
    }
}