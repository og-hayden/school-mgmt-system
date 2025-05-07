package com.school.management.view.teacher;

import com.school.management.model.entities.Course;
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
 * Main dashboard view for Teachers.
 * Displays assigned courses and allows viewing enrolled students.
 */
public class TeacherDashboardView extends JPanel {

    private JTable assignedCoursesTable;
    private DefaultTableModel tableModel;
    private JButton viewStudentsButton;
    private JButton logoutButton;
    private JButton messagesButton;
    private JButton profileButton;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    // Define column names
    private final String[] columnNames = {"ID", "Code", "Name", "Capacity"};
    private final int COURSE_ID_COL = 0;

    public TeacherDashboardView() {
        initComponents();
        layoutComponents();
        setupListeners(); // Basic listener setup like button enabling
    }

    private void initComponents() {
        // Non-editable table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                 if (columnIndex == COURSE_ID_COL || columnIndex == 3 /*Capacity*/) {
                    return Integer.class;
                 }
                 return String.class;
            }
        };
        assignedCoursesTable = new JTable(tableModel);

        // Hide the ID column visually
        assignedCoursesTable.getColumnModel().getColumn(COURSE_ID_COL).setMinWidth(0);
        assignedCoursesTable.getColumnModel().getColumn(COURSE_ID_COL).setMaxWidth(0);
        assignedCoursesTable.getColumnModel().getColumn(COURSE_ID_COL).setWidth(0);

        // Allow single row selection
        assignedCoursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Initialize the sorter
        sorter = new TableRowSorter<>(tableModel);
        assignedCoursesTable.setRowSorter(sorter);

        viewStudentsButton = new JButton("View Enrolled Students");
        viewStudentsButton.setEnabled(false); // Disabled until a row is selected

        messagesButton = new JButton("View Messages");

        profileButton = new JButton("View Profile");

        logoutButton = new JButton("Logout");
        searchField = new JTextField(20);

        // Add listener to search field
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // Title
        JLabel titleLabel = new JLabel("Teacher Dashboard - Assigned Courses", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // Top Panel for Search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search Courses:"));
        topPanel.add(searchField);
        add(topPanel, BorderLayout.NORTH);

        // Table Panel
        JScrollPane scrollPane = new JScrollPane(assignedCoursesTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel (South)
        JPanel southPanel = new JPanel(new BorderLayout(10, 5));
        
        // Action buttons
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionButtonPanel.add(viewStudentsButton);
        actionButtonPanel.add(messagesButton);
        actionButtonPanel.add(profileButton);
        southPanel.add(actionButtonPanel, BorderLayout.CENTER);

        // Logout button
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.add(logoutButton);
        southPanel.add(navigationPanel, BorderLayout.WEST);

        add(southPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void setupListeners() {
        // Enable/disable View Students button based on table selection
        assignedCoursesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Avoid handling intermediate events
                viewStudentsButton.setEnabled(assignedCoursesTable.getSelectedRow() >= 0);
            }
        });
    }

    // --- Data Handling ---

    /**
     * Populates the assigned courses table.
     *
     * @param courses The list of Course objects assigned to the teacher.
     */
    public void displayAssignedCourses(List<Course> courses) {
        tableModel.setRowCount(0); // Clear existing data
        if (courses != null) {
            for (Course course : courses) {
                Object[] rowData = {
                        course.getCourseID(),
                        course.getCourseCode(),
                        course.getName(),
                        course.getMaximumCapacity()
                };
                tableModel.addRow(rowData);
            }
        }
         viewStudentsButton.setEnabled(assignedCoursesTable.getSelectedRow() >= 0);
    }

    /**
     * Filters the assigned courses table based on the text in the search field.
     * Searches across Course Code and Name columns.
     */
    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null); // No filter
        } else {
            // Case-insensitive search across Code (1) and Name (2)
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1, 2)); 
        }
    }

    /**
     * Gets the CourseID of the currently selected row in the table.
     *
     * @return The selected CourseID, or -1 if no row is selected.
     */
    public int getSelectedCourseId() {
        int selectedRow = assignedCoursesTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Ensure the hidden ID column is accessed correctly
             Object idObj = assignedCoursesTable.getModel().getValueAt(assignedCoursesTable.convertRowIndexToModel(selectedRow), COURSE_ID_COL);
            if (idObj instanceof Integer) {
                return (Integer) idObj;
            }
        }
        return -1; // No selection or ID not found/invalid
    }

    // --- Action Listeners ---

    public void addViewStudentsListener(ActionListener listener) {
        viewStudentsButton.addActionListener(listener);
        // Set action command explicitly if needed, otherwise uses button text
        viewStudentsButton.setActionCommand("View Students"); 
    }

    public void addMessagesListener(ActionListener listener) {
        messagesButton.addActionListener(listener);
        messagesButton.setActionCommand("View Messages");
    }

    public void addProfileListener(ActionListener listener) {
        profileButton.addActionListener(listener);
        profileButton.setActionCommand("View Profile");
    }

    public void addLogoutListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
        logoutButton.setActionCommand("Logout"); // Consistent action command
    }
    
     // --- Getters for Controller --- 
     // Provides access to the panel itself for adding to the main frame
    public JPanel getPanel() {
         return this;
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
            JFrame frame = new JFrame("Teacher Dashboard View Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            TeacherDashboardView dashboard = new TeacherDashboardView();

            // Add Dummy Data for Testing
            List<Course> dummyCourses = List.of(
                new Course(1, "CS101", "Intro to Programming", 50, 5, null, null), // Assume teacher ID 5
                new Course(5, "DS303", "Data Structures", 40, 5, null, null) 
            );
            dashboard.displayAssignedCourses(dummyCourses);

            // Add simple listeners
            dashboard.addViewStudentsListener(e -> System.out.println("View Students clicked for Course ID: " + dashboard.getSelectedCourseId()));
            dashboard.addMessagesListener(e -> System.out.println("View Messages clicked"));
            dashboard.addProfileListener(e -> System.out.println("View Profile clicked"));
            dashboard.addLogoutListener(e -> System.out.println("Logout clicked"));

            frame.getContentPane().add(dashboard.getPanel()); // Add the panel
            frame.setSize(700, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
} 