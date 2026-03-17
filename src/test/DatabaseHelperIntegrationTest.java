package test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import util.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseHelperIntegrationTest {

    // Target the real or test file-backed database as requested: inventory.db
    private final String dbUrl = "jdbc:sqlite:data/inventory.db";
    private DatabaseHelper dbHelper;
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        // 1. Initialize the DatabaseHelper pointing to inventory.db
        // (This will also call initializeDatabase() internally to create the schema if missing)
        dbHelper = new DatabaseHelper(dbUrl);
        
        // 2. Obtain direct connection
        connection = dbHelper.getConnection();
        
        // 3. Take manual control of transactions
        connection.setAutoCommit(false); 
        
        // 4. Ensure a clean slate for the target test ID to avoid constraint violations
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM products WHERE id = 'TEST-001'");
        }
        connection.commit();
    }

    @After
    public void tearDown() throws Exception {
        // Ensure resources are safely released
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testFullCrudLifecycleWithDatabaseHelper() throws Exception {
        
        final String targetId = "TEST-001";
        final String selectQuery = "SELECT name, quantity, category, price FROM products WHERE id = ?";

        // ====================================================================
        // PHASE 1: CREATE (INSERT)
        // ====================================================================
        String insertSql = "INSERT INTO products (id, name, category, quantity, price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setString(1, targetId);
            insertStmt.setString(2, "Test Database Item");
            insertStmt.setString(3, "Hardware");
            insertStmt.setInt(4, 5);
            insertStmt.setDouble(5, 99.99);
            
            int rowsAffected = insertStmt.executeUpdate();
            assertEquals("Insert should affect exactly 1 row", 1, rowsAffected);
        }
        
        // Explicitly commit the Insert
        connection.commit();

        // ====================================================================
        // PHASE 2: READ (SELECT) -> Verify Insert
        // ====================================================================
        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
            selectStmt.setString(1, targetId);
            
            try (ResultSet rs = selectStmt.executeQuery()) {
                // Guarantee the data is present using rs.next()
                assertTrue("The product MUST exist in the database after insert commit", rs.next());
                
                // Verify the contents
                assertEquals("Test Database Item", rs.getString("name"));
                assertEquals("Hardware", rs.getString("category"));
                assertEquals(5, rs.getInt("quantity"));
                assertEquals(99.99, rs.getDouble("price"), 0.001);
                
                // Guarantee there are no rogue duplicates
                assertFalse("There should be exactly one matching row", rs.next());
            }
        }

        // ====================================================================
        // PHASE 3: UPDATE
        // ====================================================================
        String updateSql = "UPDATE products SET quantity = ?, price = ? WHERE id = ?";
        try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
            // Adjusting quantity from 5 to 10, price from 99.99 to 80.50
            updateStmt.setInt(1, 10); 
            updateStmt.setDouble(2, 80.50);
            updateStmt.setString(3, targetId);
            
            int rowsAffected = updateStmt.executeUpdate();
            assertEquals("Update should affect exactly 1 row", 1, rowsAffected);
        }
        
        // Explicitly commit the Update
        connection.commit();

        // ====================================================================
        // PHASE 4: READ (SELECT) -> Verify Update
        // ====================================================================
        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
            selectStmt.setString(1, targetId);
            
            try (ResultSet rs = selectStmt.executeQuery()) {
                // Guarantee the data still exists
                assertTrue("The product MUST still exist after the update commit", rs.next());
                
                // Verify the mutated values
                assertEquals("Quantity MUST reflect the updated value", 10, rs.getInt("quantity"));
                assertEquals("Price MUST reflect the updated value", 80.50, rs.getDouble("price"), 0.001);
            }
        }

        // ====================================================================
        // PHASE 5: DELETE
        // ====================================================================
        String deleteSql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
            deleteStmt.setString(1, targetId);
            
            int rowsAffected = deleteStmt.executeUpdate();
            assertEquals("Delete should affect exactly 1 row", 1, rowsAffected);
        }
        
        // Explicitly commit the Delete
        connection.commit();

        // ====================================================================
        // PHASE 6: READ (SELECT) -> Verify Delete
        // ====================================================================
        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
            selectStmt.setString(1, targetId);
            
            try (ResultSet rs = selectStmt.executeQuery()) {
                // Guarantee the data is completely absent using assertFalse(rs.next())
                assertFalse("The ResultSet MUST be completely empty after the deletion commit", rs.next());
            }
        }
    }
}
