package util;

import model.Product;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    public static List<Product> loadProducts(String filePath) {
        List<Product> products = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return products;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    try {
                        String id = parts[0];
                        String name = parts[1];
                        String category = parts[2];
                        int quantity = Integer.parseInt(parts[3]);
                        double price = Double.parseDouble(parts[4]);
                        products.add(new Product(id, name, category, quantity, price));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid product data: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        }
        return products;
    }

    public static void saveProducts(String filePath, List<Product> products) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Product p : products) {
                // Using basic concatenation to avoid locale-specific decimal formats (e.g. 10,00 vs 10.00)
                writer.write(p.getId() + "," + p.getName() + "," + p.getCategory() + "," + 
                             p.getQuantity() + "," + p.getPrice() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
