import java.sql.*;

public class CheckDiscountIssue {
    public static void main(String[] args) {
        String dbPath = "data/syos_inventory.db";
        
        System.out.println("🔍 INVESTIGATING DISCOUNT STORAGE ISSUE");
        System.out.println("=" + "=".repeat(50));
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            
            // Check the latest product (PRD-FODR0002 - Coke)
            System.out.println("\n📊 LATEST PRODUCT DISCOUNT DATA:");
            System.out.println("-".repeat(80));
            
            String productQuery = "SELECT product_id, product_code, product_name, brand, " +
                "base_price, discount_percentage, discount_amount, final_price " +
                "FROM product WHERE product_code = 'PRD-FODR0002'";
            
            try (PreparedStatement stmt = conn.prepareStatement(productQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    System.out.printf("Product ID: %d%n", rs.getInt("product_id"));
                    System.out.printf("Product Code: %s%n", rs.getString("product_code"));
                    System.out.printf("Product Name: %s%n", rs.getString("product_name"));
                    System.out.printf("Brand: %s%n", rs.getString("brand"));
                    System.out.printf("Base Price: %.2f%n", rs.getDouble("base_price"));
                    System.out.printf("Discount Percentage: %.2f%%%n", rs.getDouble("discount_percentage"));
                    System.out.printf("Discount Amount: %.2f%n", rs.getDouble("discount_amount"));
                    System.out.printf("Final Price: %.2f%n", rs.getDouble("final_price"));
                    
                    // Calculate what the discount should be
                    double basePrice = rs.getDouble("base_price");
                    double discountPercentage = rs.getDouble("discount_percentage");
                    double expectedDiscountAmount = basePrice * discountPercentage / 100;
                    double expectedFinalPrice = basePrice - expectedDiscountAmount;
                    
                    System.out.println("\n📋 EXPECTED VALUES (if 12% discount was applied):");
                    System.out.printf("Expected Discount Amount: %.2f%n", expectedDiscountAmount);
                    System.out.printf("Expected Final Price: %.2f%n", expectedFinalPrice);
                    
                    System.out.println("\n⚠️  ISSUE ANALYSIS:");
                    if (rs.getDouble("discount_percentage") == 0.0) {
                        System.out.println("❌ Discount percentage is 0% - not stored properly");
                    }
                    if (rs.getDouble("discount_amount") == 0.0) {
                        System.out.println("❌ Discount amount is 0 - not calculated properly");
                    }
                    if (rs.getDouble("final_price") == rs.getDouble("base_price")) {
                        System.out.println("❌ Final price equals base price - discount not applied");
                    }
                } else {
                    System.out.println("❌ Product PRD-FODR0002 not found in database");
                }
            }
            
            // Check all products with discount issues
            System.out.println("\n📊 ALL PRODUCTS WITH POTENTIAL DISCOUNT ISSUES:");
            System.out.println("-".repeat(100));
            
            String allProductsQuery = "SELECT product_code, product_name, base_price, " +
                "discount_percentage, discount_amount, final_price " +
                "FROM product ORDER BY product_id DESC LIMIT 5";
            
            try (PreparedStatement stmt = conn.prepareStatement(allProductsQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                System.out.printf("%-15s %-20s %-10s %-8s %-8s %-10s%n",
                    "Code", "Name", "BasePrice", "Disc%", "DiscAmt", "FinalPrice");
                System.out.println("-".repeat(100));
                
                while (rs.next()) {
                    System.out.printf("%-15s %-20s %-10.2f %-8.2f %-8.2f %-10.2f%n",
                        rs.getString("product_code"),
                        rs.getString("product_name").substring(0, Math.min(20, rs.getString("product_name").length())),
                        rs.getDouble("base_price"),
                        rs.getDouble("discount_percentage"),
                        rs.getDouble("discount_amount"),
                        rs.getDouble("final_price"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}