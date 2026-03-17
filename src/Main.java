import service.InventoryManager;
import model.Product;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String dataDir = "data";
        java.io.File dir = new java.io.File(dataDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        
        String dbUrl = "jdbc:sqlite:" + dataDir + "/inventory.db";
        InventoryManager manager = new InventoryManager(dbUrl);
        
        System.out.println("=== Inventory Management System Database Test ===");
        
        // 1. Fetch and print existing inventory
        List<Product> existingProducts = manager.getAllProducts();
        System.out.println("\n[Existing Products in Database: " + existingProducts.size() + "]");
        for (Product p : existingProducts) {
            System.out.println(" - " + p);
        }
        
        // 2. Add a new row representing this run
        String uniqueId = "SAMP-" + System.currentTimeMillis();
        System.out.println("\n[Adding Sample Product: " + uniqueId + "]");
        manager.addProduct(new Product(uniqueId, "Sample Product Run", "Diagnostics", 1, 99.99));
        
        // 3. Print the new inventory
        System.out.println("\n[Updated Products in Database]");
        manager.getAllProducts().forEach(p -> System.out.println(" - " + p));
        
        System.out.println("\nExiting. Run this application again to verify the products persisted!");
    }
}
