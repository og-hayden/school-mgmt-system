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
 * Panel for students to view courses they are enrolled in.
 */
public class EnrolledCoursesPanel extends JPanel {

    private JTable enrolledCoursesTable;
    private DefaultTableModel tableModel;
    private JButton sendMessageButton;
    // Future: Add Unenroll Button
    private JButton backButton;
    private JTextField searchField; // Added search field
    private TableRowSorter<DefaultTableModel> sorter; // Sorter for filtering

    // Define column names
    private final String[] columnNames = {"ID", "Code", "Name", "Teacher"};
    private final int COURSE_ID_COL = 0;

    public EnrolledCoursesPanel() {
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
                 if (columnIndex == COURSE_ID_COL) {
                    return Integer.class;
                 }
                 return String.class;
            }
        };
        enrolledCoursesTable = new JTable(tableModel);

        // Hide the ID column visually
        enrolledCoursesTable.getColumnModel().getColumn(COURSE_ID_COL).setMinWidth(0);
        enrolledCoursesTable.getColumnModel().getColumn(COURSE_ID_COL).setMaxWidth(0);
        enrolledCoursesTable.getColumnModel().getColumn(COURSE_ID_COL).setWidth(0);

        // Allow single row selection
        enrolledCoursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Initialize the sorter
        sorter = new TableRowSorter<>(tableModel);
        enrolledCoursesTable.setRowSorter(sorter);

        sendMessageButton = new JButton("Send Message to Teacher");
        sendMessageButton.setEnabled(false); // Disabled until a row is selected

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
        JLabel titleLabel = new JLabel("My Enrolled Courses", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // Top Panel for Search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search Courses:"));
        topPanel.add(searchField);
        add(topPanel, BorderLayout.NORTH); // Place search above the table

        // Table Panel
        JScrollPane scrollPane = new JScrollPane(enrolledCoursesTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel (South)
        JPanel southPanel = new JPanel(new BorderLayout(10, 5));
        
        // Action buttons
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionButtonPanel.add(sendMessageButton);
        // Add Unenroll button here later
        southPanel.add(actionButtonPanel, BorderLayout.CENTER);

        // Back button
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.add(backButton);
        southPanel.add(navigationPanel, BorderLayout.WEST);

        add(southPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void setupListeners() {
        // Enable/disable Send Message button based on table selection
        enrolledCoursesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                sendMessageButton.setEnabled(enrolledCoursesTable.getSelectedRow() >= 0);
            }
        });
    }

    // --- Data Handling ---

    /**
     * Populates the enrolled courses table.
     *
     * @param courses The list of enrolled Course objects.
     * @param teacherNamesMap Map of Teacher UserID -> "FirstName LastName".
     */
    public void displayEnrolledCourses(List<Course> courses, Map<Integer, String> teacherNamesMap) {
        tableModel.setRowCount(0); // Clear existing data
        if (courses != null) {
            for (Course course : courses) {
                Integer teacherId = course.getTeacherUserID();
                String teacherDisplay = teacherId != null && teacherNamesMap != null && teacherNamesMap.containsKey(teacherId) 
                                        ? teacherNamesMap.get(teacherId) 
                                        : "(Not Assigned)";

                Object[] rowData = {
                        course.getCourseID(),
                        course.getCourseCode(),
                        course.getName(),
                        teacherDisplay
                };
                tableModel.addRow(rowData);
            }
        }
        // After loading, ensure button state is correct
        sendMessageButton.setEnabled(enrolledCoursesTable.getSelectedRow() >= 0);
    }

    /**
     * Filters the enrolled courses table based on the text in the search field.
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
        int selectedRow = enrolledCoursesTable.getSelectedRow();
        if (selectedRow >= 0) {
             Object idObj = enrolledCoursesTable.getModel().getValueAt(enrolledCoursesTable.convertRowIndexToModel(selectedRow), COURSE_ID_COL);
            if (idObj instanceof Integer) {
                return (Integer) idObj;
            }
        }
        return -1; 
    }

    // --- Action Listeners ---

    public void addSendMessageListener(ActionListener listener) {
        sendMessageButton.addActionListener(listener);
        sendMessageButton.setActionCommand("Send Message");
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
            JFrame frame = new JFrame("Enrolled Courses Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            EnrolledCoursesPanel enrolledCoursesPanel = new EnrolledCoursesPanel();

            // Add Dummy Data for Testing
             List<Course> dummyCourses = List.of(
                 new Course(1, "CS101", "Intro to Programming", 50, 5, null, null),
                 new Course(3, "EN101", "English Composition", 25, null, null, null) // Unassigned
             );
             Map<Integer, String> dummyTeachers = Map.of(
                 5, "Alice Smith"
             );
            
             enrolledCoursesPanel.displayEnrolledCourses(dummyCourses, dummyTeachers);

            // Add simple listeners
             enrolledCoursesPanel.addSendMessageListener(e -> System.out.println("Send Message clicked for Course ID: " + enrolledCoursesPanel.getSelectedCourseId()));
             enrolledCoursesPanel.addBackListener(e -> System.out.println("Back button clicked"));

            frame.getContentPane().add(enrolledCoursesPanel.getPanel());
            frame.setSize(700, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
} 