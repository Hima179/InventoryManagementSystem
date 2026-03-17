# Inventory Management System (SQLite + JUnit)

This is a robust, Java-based Inventory Management System designed with OOP principles, backed by a persistent **SQLite Database**, and fully tested using **JUnit**. 

It features both a Command-Line Interface (CLI) demonstration and a full-featured **Java Swing GUI** for seamless CRUD operations, searching, filtering, and low-stock reporting.

## Project Structure
- `src/model/` - Contains the core `Product` data model.
- `src/util/` - Contains `DatabaseHelper.java` for SQLite connection management.
- `src/service/` - Contains `InventoryManager.java`, executing raw SQL CRUD queries.
- `src/gui/` - Contains the Java Swing GUI components.
- `src/test/` - Contains JUnit integration tests proving database transactional integrity.
- `src/Main.java` - The primary entry point demonstrating persistence.

## 1. Automated Build Setup (Maven)
This project is configured using **Maven** (`pom.xml`) which will automatically download the required `sqlite-jdbc` and `junit` dependencies.

To verify the installation and run all automated **JUnit tests** (including `DatabaseHelperIntegrationTest` and `InventoryManagerTest`), open your terminal and run:
```bash
mvn clean test
```
*If all tests pass, the SQLite database layer is functioning perfectly.*

## 2. Running the Main Demonstration (CLI Persistence)
The `Main.java` class is designed to prove that SQLite persistence works across application restarts. 
When run, the application will connect to `data/inventory.db`, print all stored records, insert a new distinct `SAMP-[timestamp]` record, and exit.

**To run via Maven:**
```bash
mvn compile exec:java
```
**Verification:**
Run the command twice. On the second execution, you will see the `SAMP-xxx` product from the *first* execution still present in the database!

## 3. Running the Graphical User Interface (GUI)
The project includes a fully functional Swing GUI backing directly onto the SQLite database. To launch the GUI instead of the CLI script, you can temporarily update the `Main.java` to launch `InventoryGUI`:

```java
// Inside Main.java

// Launch GUI
javax.swing.SwingUtilities.invokeLater(() -> {
    gui.InventoryGUI gui = new gui.InventoryGUI(manager);
    gui.setVisible(true);
});
```
Then execute the program using:
```bash
mvn compile exec:java
```

### GUI Features:
- **Add Product:** Inserts securely into SQLite. Prevents duplicate IDs.
- **Update Product:** Select a row to modify names, quantities, or prices.
- **Delete Product:** Permanently destroys the row in the database.
- **Live Search/Filter:** Filter products dynamically without losing data.
- **Low Stock Report:** Identify hardware/software falling below quantity thresholds.

## Requirements
- Java 11 or higher
- Apache Maven (to resolve `pom.xml` dependencies naturally)
