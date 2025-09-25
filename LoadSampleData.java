import com.syos.inventory.infrastructure.database.DatabaseInitializer;

public class LoadSampleData {
    public static void main(String[] args) {
        try {
            System.out.println("Loading sample data...");
            boolean success = DatabaseInitializer.loadSampleData();
            if (success) {
                System.out.println("Sample data loaded successfully!");
            } else {
                System.out.println("Failed to load sample data.");
            }
        } catch (Exception e) {
            System.err.println("Error loading sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}