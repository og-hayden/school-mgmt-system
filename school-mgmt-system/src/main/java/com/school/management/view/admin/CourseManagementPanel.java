package com.school.management.view.admin;

import com.school.management.model.entities.Course;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * JPanel for managing courses (viewing, adding, editing, deleting, assigning teachers).
 * Contains a table to display courses and buttons for actions.
 */
public class CourseManagementPanel extends JPanel {

    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton assignTeacherButton;
    private JButton unassignTeacherButton;
    private JButton backButton;
    private JTextField searchField; // Placeholder
    private TableRowSorter<DefaultTableModel> sorter; // Sorter for filtering

    // Define column names
    private final String[] columnNames = {"ID", "Code", "Name", "Capacity", "Teacher Name"};
    private final int COURSE_ID_COL = 0;

    public CourseManagementPanel() {
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

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                 if (columnIndex == COURSE_ID_COL /* || columnIndex == TEACHER_ID_COL removed */) {
                     return Integer.class; // Treat ID columns as numbers
                 } else if (columnIndex == 3) { // Capacity
                     return Integer.class;
                 }
                 return String.class; // Default
            }
        };
        courseTable = new JTable(tableModel);

        // Hide the ID column visually
        courseTable.getColumnModel().getColumn(COURSE_ID_COL).setMinWidth(0);
        courseTable.getColumnModel().getColumn(COURSE_ID_COL).setMaxWidth(0);
        courseTable.getColumnModel().getColumn(COURSE_ID_COL).setWidth(0);

        // Allow single row selection
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Initialize the sorter
        sorter = new TableRowSorter<>(tableModel);
        courseTable.setRowSorter(sorter);

        addButton = new JButton("Add Course");
        editButton = new JButton("Edit Selected Course");
        deleteButton = new JButton("Delete Selected Course");
        assignTeacherButton = new JButton("Assign Teacher");
        unassignTeacherButton = new JButton("Unassign Teacher");
        backButton = new JButton("Back to Dashboard");
        searchField = new JTextField(20); // Placeholder

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
                filterTable();
            }
        });
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // Title
        JLabel titleLabel = new JLabel("Course Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // Top Panel for Search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        add(topPanel, BorderLayout.NORTH); // Place search above the table

        // Table Panel
        JScrollPane scrollPane = new JScrollPane(courseTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel (South)
        JPanel southPanel = new JPanel(new BorderLayout(10, 5));
        
        // Action buttons (Add, Edit, Delete, Assign, Unassign)
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionButtonPanel.add(addButton);
        actionButtonPanel.add(editButton);
        actionButtonPanel.add(deleteButton);
        actionButtonPanel.add(assignTeacherButton);
        actionButtonPanel.add(unassignTeacherButton);
        southPanel.add(actionButtonPanel, BorderLayout.CENTER);

        // Back button
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.add(backButton);
        southPanel.add(navigationPanel, BorderLayout.WEST);

        add(southPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    // --- Data Handling ---

    /**
     * Populates the course table.
     *
     * @param courses The list of Course objects to display.
     * @param teacherNamesMap A map of Teacher UserID -> "FirstName LastName". Can be null.
     */
    public void displayCourses(List<Course> courses, Map<Integer, String> teacherNamesMap) {
        tableModel.setRowCount(0); // Clear existing data
        if (courses != null) {
            for (Course course : courses) {
                Integer teacherId = course.getTeacherUserID();
                String teacherDisplay = "(Unassigned)"; // Default display text

                if (teacherId != null && teacherNamesMap != null && teacherNamesMap.containsKey(teacherId)) {
                    teacherDisplay = teacherNamesMap.get(teacherId);
                }

                Object[] rowData = {
                        course.getCourseID(),
                        course.getCourseCode(),
                        course.getName(),
                        course.getMaximumCapacity(),
                        teacherDisplay // Display the name (or "Unassigned")
                };
                tableModel.addRow(rowData);
            }
        }
    }

    /**
     * Filters the course table based on the text in the search field.
     * Searches across Course Code, Name, and Teacher Name columns.
     */
    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null); // No filter
        } else {
            // Case-insensitive search across Code (1), Name (2), Teacher (4)
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1, 2, 4)); 
        }
    }

    /**
     * Gets the CourseID of the currently selected row in the table.
     *
     * @return The selected CourseID, or -1 if no row is selected.
     */
    public int getSelectedCourseId() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow >= 0) {
            Object idObj = tableModel.getValueAt(selectedRow, COURSE_ID_COL);
            if (idObj instanceof Integer) {
                return (Integer) idObj;
            }
        }
        return -1; 
    }
    

    // --- Action Listeners ---

    public void addAddCourseListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    public void addEditCourseListener(ActionListener listener) {
        editButton.addActionListener(listener);
    }

    public void addDeleteCourseListener(ActionListener listener) {
        deleteButton.addActionListener(listener);
    }

    public void addAssignTeacherListener(ActionListener listener) {
        assignTeacherButton.addActionListener(listener);
    }

    public void addUnassignTeacherListener(ActionListener listener) {
        unassignTeacherButton.addActionListener(listener);
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
            JFrame frame = new JFrame("Course Management Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            CourseManagementPanel coursePanel = new CourseManagementPanel();

            // Add Dummy Data for Testing
            List<Course> dummyCourses = List.of(
                new Course(1, "CS101", "Intro to Programming", 50, 2, null, null),
                new Course(2, "MA201", "Calculus I", 30, 2, null, null),
                new Course(3, "EN101", "English Composition", 25, null, null, null) // Unassigned
            );

            // Create a dummy teacher name map for testing
            Map<Integer, String> dummyTeacherNames = Map.of(2, "Prof. Davison");

            // Pass the dummy data and the map
            coursePanel.displayCourses(dummyCourses, dummyTeacherNames); 

            // Add simple listeners
            coursePanel.addAddCourseListener(e -> System.out.println("Add Course clicked"));
            coursePanel.addEditCourseListener(e -> System.out.println("Edit Course clicked for ID: " + coursePanel.getSelectedCourseId()));
            coursePanel.addDeleteCourseListener(e -> System.out.println("Delete Course clicked for ID: " + coursePanel.getSelectedCourseId()));
            coursePanel.addAssignTeacherListener(e -> System.out.println("Assign Teacher clicked for Course ID: " + coursePanel.getSelectedCourseId()));
            // Remove getSelectedCourseTeacherId() call from test as it's less reliable now
            coursePanel.addUnassignTeacherListener(e -> System.out.println("Unassign Teacher clicked for Course ID: " + coursePanel.getSelectedCourseId()));
            coursePanel.addBackButtonListener(e -> System.out.println("Back button clicked"));

            frame.getContentPane().add(coursePanel);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
} 