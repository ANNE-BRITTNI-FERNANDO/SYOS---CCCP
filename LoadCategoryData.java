import java.sql.*;

public class LoadCategoryData {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:data/syos_inventory.db");
            
            System.out.println("Loading categories and subcategories...");
            
            // Insert categories
            String categorySQL = "INSERT OR IGNORE INTO category (category_code, category_name, description, is_active, created_at) VALUES " +
                    "('LA', 'Laundry Products', 'Cleaning and laundry products', 1, CURRENT_TIMESTAMP)," +
                    "('FO', 'Food & Beverages', 'Food items and beverages', 1, CURRENT_TIMESTAMP)," +
                    "('EL', 'Electronics', 'Electronic devices and accessories', 1, CURRENT_TIMESTAMP)";
            
            PreparedStatement stmt = conn.prepareStatement(categorySQL);
            stmt.execute();
            System.out.println("Categories inserted");
            
            // Insert subcategories
            String subcategorySQL = "INSERT OR IGNORE INTO subcategory (category_id, subcategory_code, subcategory_name, description, default_shelf_capacity, is_active) VALUES " +
                    "(1, 'LA-SO', 'Soap', 'Bar soaps and liquid soaps', 100, 1)," +
                    "(1, 'LA-DE', 'Detergent', 'Washing detergents and powders', 80, 1)," +
                    "(1, 'LA-FA', 'Fabric Softener', 'Fabric softeners and conditioners', 60, 1)," +
                    "(2, 'FO-SN', 'Snacks', 'Cookies, crackers, and snack foods', 150, 1)," +
                    "(2, 'FO-DR', 'Drinks', 'Beverages and soft drinks', 120, 1)," +
                    "(2, 'FO-DA', 'Dairy', 'Milk, yogurt, and dairy products', 80, 1)," +
                    "(3, 'EL-PH', 'Mobile Phones', 'Smartphones and mobile devices', 50, 1)," +
                    "(3, 'EL-LA', 'Laptops', 'Laptop computers and accessories', 30, 1)," +
                    "(3, 'EL-AC', 'Accessories', 'Electronic accessories and cables', 200, 1)";
            
            stmt = conn.prepareStatement(subcategorySQL);
            stmt.execute();
            System.out.println("Subcategories inserted");
            
            // Verify insertion
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM category");
            ResultSet rs = stmt.executeQuery();
            rs.next();
            System.out.println("Total categories: " + rs.getInt(1));
            
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM subcategory");
            rs = stmt.executeQuery();
            rs.next();
            System.out.println("Total subcategories: " + rs.getInt(1));
            
            conn.close();
            System.out.println("Category data loaded successfully!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}