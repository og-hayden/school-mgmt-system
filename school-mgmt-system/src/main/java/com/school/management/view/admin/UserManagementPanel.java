package com.school.management.view.admin;

import com.school.management.model.entities.User;
import com.school.management.model.entities.UserRole;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * JPanel for managing users (viewing, adding, editing, deleting).
 * Contains a table to display users and buttons for actions.
 */
public class UserManagementPanel extends JPanel {

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JTextField searchField; // Search field added
    private TableRowSorter<DefaultTableModel> sorter; // Sorter for filtering
    private JButton backButton; // Button to go back to admin dashboard

    // Define column names
    private final String[] columnNames = {"ID", "First Name", "Last Name", "Email", "Role", "Department"};
    // Define hidden column index for UserID
    private final int USER_ID_COL = 0;

    public UserManagementPanel() {
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        // Non-editable table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        userTable = new JTable(tableModel);
        
        // Hide the ID column visually, but keep it in the model for retrieval
         userTable.getColumnModel().getColumn(USER_ID_COL).setMinWidth(0);
         userTable.getColumnModel().getColumn(USER_ID_COL).setMaxWidth(0);
         userTable.getColumnModel().getColumn(USER_ID_COL).setWidth(0);

        // Allow single row selection
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Initialize the sorter
        sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);

        addButton = new JButton("Add User");
        editButton = new JButton("Edit Selected User");
        deleteButton = new JButton("Delete Selected User");
        searchField = new JTextField(20); // Initialize search field
        backButton = new JButton("Back to Dashboard");

        // Add listener to search field
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTable(); // Plain text components do not fire this
            }
        });
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // Title
        JLabel titleLabel = new JLabel("User Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // Top Panel for Search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        add(topPanel, BorderLayout.NORTH); // Place search above the table

        // Table Panel
        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel (South)
        JPanel southPanel = new JPanel(new BorderLayout(10, 5));
        
        // Action buttons (Add, Edit, Delete)
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionButtonPanel.add(addButton);
        actionButtonPanel.add(editButton);
        actionButtonPanel.add(deleteButton);
        southPanel.add(actionButtonPanel, BorderLayout.CENTER);

        // Back button (West or East in south panel)
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.add(backButton);
        southPanel.add(navigationPanel, BorderLayout.WEST);
        

        add(southPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    // --- Data Handling ---

    /**
     * Populates the user table with a list of users.
     *
     * @param users The list of User objects to display.
     */
    public void displayUsers(List<User> users) {
        tableModel.setRowCount(0); // Clear existing data
        if (users != null) {
            for (User user : users) {
                Object[] rowData = {
                        user.getUserID(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole().name(), // Display role name
                        user.getDepartment() == null ? "" : user.getDepartment() // Handle null department
                };
                tableModel.addRow(rowData);
            }
        }
    }

    /**
     * Filters the table based on the text in the search field.
     * Searches across First Name, Last Name, Email, and Role columns.
     */
    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null); // No filter
        } else {
            // Case-insensitive search across multiple columns (index 1, 2, 3, 4)
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1, 2, 3, 4));
        }
    }

    /**
     * Gets the UserID of the currently selected row in the table.
     *
     * @return The selected UserID, or -1 if no row is selected.
     */
    public int getSelectedUserId() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Retrieve the ID from the hidden USER_ID_COL column in the model
            Object idObj = tableModel.getValueAt(selectedRow, USER_ID_COL);
            if (idObj instanceof Integer) {
                return (Integer) idObj;
            }
        }
        return -1; // No selection or ID not found/invalid
    }

    // --- Action Listeners ---

    public void addAddUserListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    public void addEditUserListener(ActionListener listener) {
        editButton.addActionListener(listener);
    }

    public void addDeleteUserListener(ActionListener listener) {
        deleteButton.addActionListener(listener);
    }
    
    public void addBackButtonListener(ActionListener listener) {
        backButton.addActionListener(listener);
    }

    // --- Main method for visual testing ---
    public static void main(String[] args) {
        // Set Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
             System.out.println("Nimbus L&F not found, using default.");
        }
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("User Management Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            UserManagementPanel userPanel = new UserManagementPanel();
            
            // Add Dummy Data for Testing
            List<User> dummyUsers = List.of(
                new User(1, "Admin", "Istrator", "admin@example.com", null, null, UserRole.ADMINISTRATOR, null, null, null, null, null, null),
                new User(2, "Teacher", "One", "teach1@example.com", null, null, UserRole.TEACHER, "CompSci", null, null, null, null, null),
                new User(3, "Student", "Alpha", "stu1@example.com", null, null, UserRole.STUDENT, null, null, null, null, null, null),
                 new User(4, "Student", "Beta", "stu2@example.com", null, null, UserRole.STUDENT, null, null, null, null, null, null)
            );
            userPanel.displayUsers(dummyUsers);
            
            // Add simple listeners
            userPanel.addAddUserListener(e -> System.out.println("Add User clicked"));
            userPanel.addEditUserListener(e -> System.out.println("Edit User clicked for ID: " + userPanel.getSelectedUserId()));
            userPanel.addDeleteUserListener(e -> System.out.println("Delete User clicked for ID: " + userPanel.getSelectedUserId()));
            userPanel.addBackButtonListener(e -> System.out.println("Back button clicked"));
             
            frame.getContentPane().add(userPanel);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null); 
            frame.setVisible(true);
        });
    }
} 