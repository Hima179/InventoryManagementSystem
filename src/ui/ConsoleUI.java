package ui;

import model.Product;
import service.InventoryManager;

import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private InventoryManager manager;
    private Scanner scanner;

    public ConsoleUI(InventoryManager manager) {
        this.manager = manager;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=== Inventory Management System ===");
            System.out.println("1. Add Product");
            System.out.println("2. View All Products");
            System.out.println("3. Update Product");
            System.out.println("4. Remove Product");
            System.out.println("5. Search Product by Name");
            System.out.println("6. Filter Products by Category");
            System.out.println("7. Low Stock Report");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");

            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    addProduct();
                    break;
                case 2:
                    viewAllProducts();
                    break;
                case 3:
                    updateProduct();
                    break;
                case 4:
                    removeProduct();
                    break;
                case 5:
                    searchProduct();
                    break;
                case 6:
                    filterCategory();
                    break;
                case 7:
                    lowStockReport();
                    break;
                case 8:
                    exit = true;
                    System.out.println("Exiting system. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void addProduct() {
        System.out.print("Enter Product ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Category: ");
        String category = scanner.nextLine();
        
        System.out.print("Enter Quantity: ");
        int quantity = 0;
        try {
            quantity = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity.");
            return;
        }

        System.out.print("Enter Price: ");
        double price = 0;
        try {
            price = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid price.");
            return;
        }

        Product product = new Product(id, name, category, quantity, price);
        manager.addProduct(product);
    }

    private void viewAllProducts() {
        List<Product> products = manager.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("No products in inventory.");
        } else {
            System.out.println("\n--- All Products ---");
            products.forEach(System.out::println);
        }
    }

    private void updateProduct() {
        System.out.print("Enter ID of Product to update: ");
        String id = scanner.nextLine();
        
        System.out.print("Enter new Name (leave blank to skip): ");
        String name = scanner.nextLine();
        
        System.out.print("Enter new Category (leave blank to skip): ");
        String category = scanner.nextLine();
        
        System.out.print("Enter new Quantity (-1 to skip): ");
        int quantity = -1;
        try {
            String qStr = scanner.nextLine();
            if (!qStr.isEmpty()) quantity = Integer.parseInt(qStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity format. Skipping update for quantity.");
        }
        
        System.out.print("Enter new Price (-1 to skip): ");
        double price = -1;
        try {
            String pStr = scanner.nextLine();
            if (!pStr.isEmpty()) price = Double.parseDouble(pStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid price format. Skipping update for price.");
        }
        
        manager.updateProduct(id, name, category, quantity, price);
    }

    private void removeProduct() {
        System.out.print("Enter ID of Product to remove: ");
        String id = scanner.nextLine();
        manager.removeProduct(id);
    }

    private void searchProduct() {
        System.out.print("Enter keyword to search: ");
        String keyword = scanner.nextLine();
        List<Product> results = manager.searchByName(keyword);
        displayProductList(results, "Search Results");
    }

    private void filterCategory() {
        System.out.print("Enter category to filter: ");
        String category = scanner.nextLine();
        List<Product> results = manager.filterByCategory(category);
        displayProductList(results, "Filtered Results for " + category);
    }

    private void lowStockReport() {
        System.out.print("Enter low stock threshold: ");
        try {
            int threshold = Integer.parseInt(scanner.nextLine());
            List<Product> results = manager.getLowStockProducts(threshold);
            displayProductList(results, "Low Stock Report (<= " + threshold + ")");
        } catch (NumberFormatException e) {
            System.out.println("Invalid threshold value.");
        }
    }

    private void displayProductList(List<Product> list, String title) {
        if (list.isEmpty()) {
            System.out.println("No products found.");
        } else {
            System.out.println("\n--- " + title + " ---");
            list.forEach(System.out::println);
        }
    }
}
