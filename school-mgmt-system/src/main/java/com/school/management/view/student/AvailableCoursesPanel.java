package com.school.management.view.student;

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
 * Panel for students to view and enroll in available courses.
 */
public class AvailableCoursesPanel extends JPanel {

    private JTable availableCoursesTable;
    private DefaultTableModel tableModel;
    private JButton enrollButton;
    private JButton backButton;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    // Define column names
    private final String[] columnNames = {"ID", "Code", "Name", "Teacher", "Capacity", "Enrolled"};
    private final int COURSE_ID_COL = 0;

    public AvailableCoursesPanel() {
        initComponents();
        layoutComponents();
        setupListeners();
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
                 switch (columnIndex) {
                    case COURSE_ID_COL: return Integer.class;
                    case 4: return Integer.class; // Capacity
                    case 5: return Integer.class; // Enrolled
                    default: return String.class;
                 }
            }
        };
        availableCoursesTable = new JTable(tableModel);

        // Hide the ID column visually
        availableCoursesTable.getColumnModel().getColumn(COURSE_ID_COL).setMinWidth(0);
        availableCoursesTable.getColumnModel().getColumn(COURSE_ID_COL).setMaxWidth(0);
        availableCoursesTable.getColumnModel().getColumn(COURSE_ID_COL).setWidth(0);

        // Allow single row selection
        availableCoursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Initialize the sorter
        sorter = new TableRowSorter<>(tableModel);
        availableCoursesTable.setRowSorter(sorter);

        enrollButton = new JButton("Enroll in Selected Course");
        enrollButton.setEnabled(false); // Disabled until a row is selected

        backButton = new JButton("Back to Dashboard");
        searchField = new JTextField(20); // Initialize search field

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
        JLabel titleLabel = new JLabel("Available Courses", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // Top Panel for Search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search Courses:"));
        topPanel.add(searchField);
        add(topPanel, BorderLayout.NORTH); // Place search above the table

        // Table Panel
        JScrollPane scrollPane = new JScrollPane(availableCoursesTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel (South)
        JPanel southPanel = new JPanel(new BorderLayout(10, 5));
        
        // Enroll button
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionButtonPanel.add(enrollButton);
        southPanel.add(actionButtonPanel, BorderLayout.CENTER);

        // Back button
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.add(backButton);
        southPanel.add(navigationPanel, BorderLayout.WEST);

        add(southPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void setupListeners() {
        // Enable/disable Enroll button based on table selection
        availableCoursesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                enrollButton.setEnabled(availableCoursesTable.getSelectedRow() >= 0);
            }
        });
    }

    // --- Data Handling ---

    /**
     * Populates the available courses table.
     *
     * @param courses The list of available Course objects.
     * @param teacherNamesMap Map of Teacher UserID -> "FirstName LastName".
     * @param enrollmentCountsMap Map of CourseID -> current enrollment count.
     */
    public void displayAvailableCourses(List<Course> courses, Map<Integer, String> teacherNamesMap, Map<Integer, Integer> enrollmentCountsMap) {
        tableModel.setRowCount(0); // Clear existing data
        if (courses != null) {
            for (Course course : courses) {
                Integer teacherId = course.getTeacherUserID();
                String teacherDisplay = teacherId != null && teacherNamesMap != null && teacherNamesMap.containsKey(teacherId) 
                                        ? teacherNamesMap.get(teacherId) 
                                        : "(Not Assigned)";
                
                int enrolledCount = enrollmentCountsMap != null && enrollmentCountsMap.containsKey(course.getCourseID()) 
                                    ? enrollmentCountsMap.get(course.getCourseID()) 
                                    : 0;

                Object[] rowData = {
                        course.getCourseID(),
                        course.getCourseCode(),
                        course.getName(),
                        teacherDisplay,
                        course.getMaximumCapacity(),
                        enrolledCount
                };
                tableModel.addRow(rowData);
            }
        }
        // After loading, ensure button state is correct
        enrollButton.setEnabled(availableCoursesTable.getSelectedRow() >= 0);
    }

    /**
     * Filters the available courses table based on the text in the search field.
     * Searches across Course Code, Name, and Teacher columns.
     */
    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null); // No filter
        } else {
            // Case-insensitive search across Code (1), Name (2), Teacher (3)
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1, 2, 3)); 
        }
    }

    /**
     * Gets the CourseID of the currently selected row in the table.
     *
     * @return The selected CourseID, or -1 if no row is selected.
     */
    public int getSelectedCourseId() {
        int selectedRow = availableCoursesTable.getSelectedRow();
        if (selectedRow >= 0) {
             Object idObj = availableCoursesTable.getModel().getValueAt(availableCoursesTable.convertRowIndexToModel(selectedRow), COURSE_ID_COL);
            if (idObj instanceof Integer) {
                return (Integer) idObj;
            }
        }
        return -1; // No selection or ID not found/invalid
    }

    // --- Action Listeners ---

    public void addEnrollListener(ActionListener listener) {
        enrollButton.addActionListener(listener);
        enrollButton.setActionCommand("Enroll");
    }

    public void addBackListener(ActionListener listener) {
        backButton.addActionListener(listener);
        backButton.setActionCommand("Back To Student Dashboard");
    }
    
     // --- Getters for Controller --- 
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
            JFrame frame = new JFrame("Available Courses Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            AvailableCoursesPanel availableCoursesPanel = new AvailableCoursesPanel();

            // Add Dummy Data for Testing
             List<Course> dummyCourses = List.of(
                 new Course(1, "CS101", "Intro to Programming", 50, 5, null, null),
                 new Course(2, "MA201", "Calculus I", 30, 8, null, null),
                 new Course(4, "PY101", "Intro to Psychology", 60, null, null, null) // Unassigned Teacher
             );
             Map<Integer, String> dummyTeachers = Map.of(
                 5, "Alice Smith", 
                 8, "Bob Jones"
             );
             Map<Integer, Integer> dummyEnrollments = Map.of(
                 1, 45, // CS101 almost full
                 2, 15  // MA201 half full
                 // PY101 has 0 enrolled (not in map)
             );
            
             availableCoursesPanel.displayAvailableCourses(dummyCourses, dummyTeachers, dummyEnrollments);

            // Add simple listeners
             availableCoursesPanel.addEnrollListener(e -> System.out.println("Enroll clicked for Course ID: " + availableCoursesPanel.getSelectedCourseId()));
             availableCoursesPanel.addBackListener(e -> System.out.println("Back button clicked"));

            frame.getContentPane().add(availableCoursesPanel.getPanel());
            frame.setSize(700, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
} 