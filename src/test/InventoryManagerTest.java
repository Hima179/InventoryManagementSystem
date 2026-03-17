package test;

import model.Product;
import service.InventoryManager;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class InventoryManagerTest {
    private InventoryManager manager;
    private final String testDbUrl = "jdbc:sqlite:test_inventory.db";

    @Before
    public void setUp() {
        // Ensure clean state
        File dbFile = new File("test_inventory.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        
        manager = new InventoryManager(testDbUrl);
        
        Product p1 = new Product("T001", "Test Product 1", "Category A", 10, 100.0);
        Product p2 = new Product("T002", "Test Product 2", "Category B", 5, 50.0);
        manager.addProduct(p1);
        manager.addProduct(p2);
    }

    @After
    public void tearDown() {
        File dbFile = new File("test_inventory.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    public void testAddProduct() {
        manager.addProduct(new Product("T003", "Test Product 3", "Category C", 1, 10.0));
        assertEquals(3, manager.getAllProducts().size());
        
        // Duplicate shouldn't add
        manager.addProduct(new Product("T001", "Duplicate Test", "Category A", 5, 20.0));
        assertEquals(3, manager.getAllProducts().size());
    }

    @Test
    public void testGetProduct() {
        Product fetched = manager.getProduct("T001");
        assertNotNull(fetched);
        assertEquals("Test Product 1", fetched.getName());
    }

    @Test
    public void testUpdateProduct() {
        manager.updateProduct("T001", "Updated Name", "", 20, 150.0);
        Product updated = manager.getProduct("T001");
        assertEquals("Updated Name", updated.getName());
        assertEquals("Category A", updated.getCategory()); // Unchanged
        assertEquals(20, updated.getQuantity());
        assertEquals(150.0, updated.getPrice(), 0.001);
    }

    @Test
    public void testSearchAndFilter() {
        List<Product> searchResults = manager.searchByName("Product 1");
        assertEquals(1, searchResults.size());

        List<Product> filterResults = manager.filterByCategory("Category B");
        assertEquals(1, filterResults.size());
    }

    @Test
    public void testLowStock() {
        List<Product> lowStock = manager.getLowStockProducts(5);
        assertEquals(1, lowStock.size());
        assertEquals("T002", lowStock.get(0).getId());
    }

    @Test
    public void testRemoveProduct() {
        manager.removeProduct("T001");
        assertEquals(1, manager.getAllProducts().size());
        assertNull(manager.getProduct("T001"));
    }
}
