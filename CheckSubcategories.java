import java.sql.*;

public class CheckSubcategories {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:data/syos_inventory.db");
            
            // Check subcategories table
            System.out.println("=== SUBCATEGORIES ===");
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM subcategory ORDER BY subcategory_id");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("ID: " + rs.getLong("subcategory_id") + 
                                 ", Code: " + rs.getString("subcategory_code") +
                                 ", Name: " + rs.getString("subcategory_name") +
                                 ", Category ID: " + rs.getLong("category_id") +
                                 ", Active: " + rs.getBoolean("is_active"));
            }
            rs.close();
            stmt.close();
            
            // Check categories table
            System.out.println("\n=== CATEGORIES ===");
            stmt = conn.prepareStatement("SELECT * FROM category ORDER BY category_id");
            rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("ID: " + rs.getLong("category_id") + 
                                 ", Code: " + rs.getString("category_code") +
                                 ", Name: " + rs.getString("category_name") +
                                 ", Active: " + rs.getBoolean("is_active"));
            }
            rs.close();
            stmt.close();
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}