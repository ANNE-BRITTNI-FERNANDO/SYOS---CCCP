import java.sql.*;

public class ShowTables {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:data/syos_inventory.db";
            
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                
                System.out.println("All tables in database:");
                while (rs.next()) {
                    System.out.println("  " + rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}