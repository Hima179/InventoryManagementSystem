package test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SQLiteCrudIntegrationTest {

    // Using an in-memory SQLite database ensures a pristine state for each test.
    private final String dbUrl = "jdbc:sqlite::memory:";
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        // 1. Establish database connection
        connection = DriverManager.getConnection(dbUrl);
        
        // 2. Disable auto-commit to take manual control of transactions
        connection.setAutoCommit(false); 

        // 3. Initialize the schema
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE products (" +
                         "id TEXT PRIMARY KEY, " +
                         "name TEXT, " +
                         "quantity INTEGER)");
        }
        
        // Commit the schema creation
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
    public void testPipelinedCrudLifecycleWithExplicitTransactions() throws Exception {
        
        final String targetId = "TXN-001";
        final String selectQuery = "SELECT name, quantity FROM products WHERE id = ?";

        // ====================================================================
        // PHASE 1: CREATE (INSERT)
        // ====================================================================
        String insertSql = "INSERT INTO products (id, name, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setString(1, targetId);
            insertStmt.setString(2, "Test Widget");
            insertStmt.setInt(3, 10);
            
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
                assertEquals("Test Widget", rs.getString("name"));
                assertEquals(10, rs.getInt("quantity"));
                
                // Guarantee there are no duplicates
                assertFalse("There should be exactly one matching row", rs.next());
            }
        }

        // ====================================================================
        // PHASE 3: UPDATE
        // ====================================================================
        String updateSql = "UPDATE products SET quantity = ? WHERE id = ?";
        try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
            // Adjusting quantity from 10 to 15
            updateStmt.setInt(1, 15); 
            updateStmt.setString(2, targetId);
            
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
                
                // Verify the mutated value
                assertEquals("Quantity MUST reflect the updated value", 15, rs.getInt("quantity"));
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
