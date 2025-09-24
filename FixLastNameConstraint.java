import java.sql.*;

public class FixLastNameConstraint {
    private static final String DATABASE_PATH = "data/syos_inventory.db";
    
    public static void main(String[] args) {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Connect to the database
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            Connection conn = DriverManager.getConnection(url);
            
            System.out.println("Connected to database: " + DATABASE_PATH);
            
            // SQLite doesn't support ALTER COLUMN directly, so we need to:
            // 1. Create a new table without the NOT NULL constraint
            // 2. Copy data from old table to new table
            // 3. Drop old table and rename new table
            
            Statement stmt = conn.createStatement();
            
            // Step 1: Create new user table without NOT NULL constraint on last_name
            String createNewTable = "CREATE TABLE user_new (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_code VARCHAR(20) NOT NULL UNIQUE," +
                "email VARCHAR(255) NOT NULL UNIQUE," +
                "password_hash VARCHAR(255) NOT NULL," +
                "password_salt VARCHAR(255) NOT NULL," +
                "first_name VARCHAR(100) NOT NULL," +
                "last_name VARCHAR(100)," +
                "phone VARCHAR(15)," +
                "address TEXT," +
                "role_id INTEGER NOT NULL," +
                "is_active BOOLEAN DEFAULT 1," +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "last_login DATETIME," +
                "FOREIGN KEY (role_id) REFERENCES role(role_id)" +
                ")";
            
            stmt.execute(createNewTable);
            System.out.println("Created new user table without NOT NULL constraint on last_name");
            
            // Step 2: Copy all data from old table to new table
            String copyData = "INSERT INTO user_new (" +
                "user_id, user_code, email, password_hash, password_salt, " +
                "first_name, last_name, phone, address, role_id, " +
                "is_active, created_at, last_login" +
                ") SELECT " +
                "user_id, user_code, email, password_hash, password_salt, " +
                "first_name, last_name, phone, address, role_id, " +
                "is_active, created_at, last_login " +
                "FROM user";
            
            int rowsCopied = stmt.executeUpdate(copyData);
            System.out.println("Copied " + rowsCopied + " rows to new table");
            
            // Step 3: Drop old table
            stmt.execute("DROP TABLE user");
            System.out.println("Dropped old user table");
            
            // Step 4: Rename new table to original name
            stmt.execute("ALTER TABLE user_new RENAME TO user");
            System.out.println("Renamed new table to 'user'");
            
            // Verify the change by checking table structure
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(user)");
            System.out.println("\nUpdated table structure:");
            while (rs.next()) {
                String columnName = rs.getString("name");
                String dataType = rs.getString("type");
                boolean notNull = rs.getBoolean("notnull");
                System.out.println(columnName + " " + dataType + " (NOT NULL: " + notNull + ")");
            }
            
            conn.close();
            System.out.println("\nDatabase migration completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}