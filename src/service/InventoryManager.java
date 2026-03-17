package service;

import model.Product;
import util.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryManager {
    private DatabaseHelper dbHelper;

    public InventoryManager(String dbUrl) {
        this.dbHelper = new DatabaseHelper(dbUrl);
    }

    // CREATE
    public void addProduct(Product product) {
        if (getProduct(product.getId()) != null) {
            System.out.println("Product with ID " + product.getId() + " already exists.");
            return;
        }

        String sql = "INSERT INTO products (id, name, category, quantity, price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getId());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getCategory());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setDouble(5, product.getPrice());
            pstmt.executeUpdate();
            System.out.println("Product added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
    }

    // READ
    public Product getProduct(String id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToProduct(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting product: " + e.getMessage());
        }
        return null;
    }

    public List<Product> getAllProducts() {
        return getProductsList("SELECT * FROM products");
    }

    // UPDATE
    public void updateProduct(String id, String name, String category, int quantity, double price) {
        Product existing = getProduct(id);
        if (existing == null) {
            System.out.println("Product with ID " + id + " not found.");
            return;
        }

        String sql = "UPDATE products SET name = ?, category = ?, quantity = ?, price = ? WHERE id = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Keep existing values if new ones match skip criteria
            pstmt.setString(1, (name != null && !name.isEmpty()) ? name : existing.getName());
            pstmt.setString(2, (category != null && !category.isEmpty()) ? category : existing.getCategory());
            pstmt.setInt(3, quantity >= 0 ? quantity : existing.getQuantity());
            pstmt.setDouble(4, price >= 0 ? price : existing.getPrice());
            pstmt.setString(5, id);
            
            pstmt.executeUpdate();
            System.out.println("Product updated successfully!");
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
    }

    // DELETE
    public void removeProduct(String id) {
        if (getProduct(id) == null) {
            System.out.println("Product with ID " + id + " not found.");
            return;
        }

        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            System.out.println("Product removed successfully!");
        } catch (SQLException e) {
            System.err.println("Error removing product: " + e.getMessage());
        }
    }

    // SEARCH & FILTER
    public List<Product> searchByName(String keyword) {
        String sql = "SELECT * FROM products WHERE LOWER(name) LIKE ?";
        List<Product> products = new ArrayList<>();
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword.toLowerCase() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching products: " + e.getMessage());
        }
        return products;
    }

    public List<Product> filterByCategory(String category) {
        String sql = "SELECT * FROM products WHERE LOWER(category) = LOWER(?)";
        List<Product> products = new ArrayList<>();
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering products: " + e.getMessage());
        }
        return products;
    }

    // REPORTING
    public List<Product> getLowStockProducts(int threshold) {
        String sql = "SELECT * FROM products WHERE quantity <= ?";
        List<Product> products = new ArrayList<>();
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, threshold);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting low stock products: " + e.getMessage());
        }
        return products;
    }

    // Helper Methods
    private List<Product> getProductsList(String sql) {
        List<Product> products = new ArrayList<>();
        try (Connection conn = dbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
        }
        return products;
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getDouble("price")
        );
    }
}
