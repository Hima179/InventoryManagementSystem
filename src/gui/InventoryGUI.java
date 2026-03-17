package gui;

import model.Product;
import service.InventoryManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class InventoryGUI extends JFrame {
    private InventoryManager manager;
    private JTable table;
    private DefaultTableModel tableModel;

    public InventoryGUI(InventoryManager manager) {
        this.manager = manager;
        setTitle("Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        initUI();
        refreshTable();
    }

    private void initUI() {
        // Table setup
        String[] columns = {"ID", "Name", "Category", "Quantity", "Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Control panel setup
        JPanel controlPanel = new JPanel();
        JButton btnAdd = new JButton("Add Product");
        JButton btnUpdate = new JButton("Update Product");
        JButton btnDelete = new JButton("Delete Product");
        JButton btnRefresh = new JButton("Refresh");

        controlPanel.add(btnAdd);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);
        controlPanel.add(btnRefresh);

        // Action Listeners
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnRefresh.addActionListener(e -> refreshTable());

        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(controlPanel, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        tableModel.setRowCount(0); // clear existing data
        List<Product> products = manager.getAllProducts();
        for (Product p : products) {
            tableModel.addRow(new Object[]{p.getId(), p.getName(), p.getCategory(), p.getQuantity(), p.getPrice()});
        }
    }

    private void addProduct() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        
        String[] categories = {"Electronics", "Furniture", "Hardware", "Software", "Office Supplies", "Other"};
        JComboBox<String> categoryField = new JComboBox<>(categories);
        
        Integer[] quantities = new Integer[100];
        for (int i = 0; i < 100; i++) {
            quantities[i] = i + 1;
        }
        JComboBox<Integer> quantityField = new JComboBox<>(quantities);
        
        JTextField priceField = new JTextField();

        Object[] message = {
                "ID:", idField,
                "Name:", nameField,
                "Category:", categoryField,
                "Quantity:", quantityField,
                "Price:", priceField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                String category = (String) categoryField.getSelectedItem();
                int quantity = (Integer) quantityField.getSelectedItem();
                double price = Double.parseDouble(priceField.getText().trim());

                if (manager.getProduct(id) != null) {
                    JOptionPane.showMessageDialog(this, "Product with ID " + id + " already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Product p = new Product(id, name, category, quantity, price);
                manager.addProduct(p);
                refreshTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid price format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to update.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String id = (String) tableModel.getValueAt(selectedRow, 0);
        Product p = manager.getProduct(id);
        if (p == null) return;

        JTextField nameField = new JTextField(p.getName());
        
        String[] categories = {"Electronics", "Furniture", "Hardware", "Software", "Office Supplies", "Other"};
        JComboBox<String> categoryField = new JComboBox<>(categories);
        categoryField.setSelectedItem(p.getCategory());
        
        Integer[] quantities = new Integer[100];
        for (int i = 0; i < 100; i++) {
            quantities[i] = i + 1;
        }
        JComboBox<Integer> quantityField = new JComboBox<>(quantities);
        quantityField.setSelectedItem(p.getQuantity());
        
        JTextField priceField = new JTextField(String.valueOf(p.getPrice()));

        Object[] message = {
                "Name:", nameField,
                "Category:", categoryField,
                "Quantity:", quantityField,
                "Price:", priceField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Update Product (ID: " + id + ")", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String category = (String) categoryField.getSelectedItem();
                int quantity = (Integer) quantityField.getSelectedItem();
                double price = Double.parseDouble(priceField.getText().trim());

                manager.updateProduct(id, name, category, quantity, price);
                refreshTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid price format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String id = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete product " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.removeProduct(id);
            refreshTable();
        }
    }

    public static void main(String[] args) {
        String dataDir = "data";
        java.io.File dir = new java.io.File(dataDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        
        String dbUrl = "jdbc:sqlite:" + dataDir + "/inventory.db";
        InventoryManager manager = new InventoryManager(dbUrl);
        
        // Launch GUI correctly on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            InventoryGUI gui = new InventoryGUI(manager);
            gui.setVisible(true);
        });
    }
}
