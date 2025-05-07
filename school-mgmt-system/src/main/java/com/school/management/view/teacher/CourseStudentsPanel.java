package com.school.management.view.teacher;

import com.school.management.model.entities.Course;
import com.school.management.model.entities.User;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Panel for displaying students enrolled in a specific course.
 */
public class CourseStudentsPanel extends JPanel {

    private JLabel courseNameLabel;
    private JTable studentsTable;
    private DefaultTableModel tableModel;
    private JButton backButton;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    // Define column names
    private final String[] columnNames = {"Student ID", "First Name", "Last Name", "Email"};
    private final int STUDENT_ID_COL = 0;

    public CourseStudentsPanel() {
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        courseNameLabel = new JLabel("Enrolled Students for: [Course Name]", SwingConstants.CENTER);
        courseNameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Non-editable table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                 if (columnIndex == STUDENT_ID_COL) {
                    return Integer.class;
                 }
                 return String.class;
            }
        };
        studentsTable = new JTable(tableModel);

        // Allow single row selection (might be useful later)
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Initialize the sorter
        sorter = new TableRowSorter<>(tableModel);
        studentsTable.setRowSorter(sorter);

        backButton = new JButton("Back to Dashboard");
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
        
        // Top Panel: Title and Search
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.add(courseNameLabel, BorderLayout.CENTER);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Search Students:"));
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        // Table Panel
        JScrollPane scrollPane = new JScrollPane(studentsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Back Button Panel (South)
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);
        
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    // --- Data Handling ---

    /**
     * Populates the students table and updates the course name label.
     *
     * @param course The course whose students are being displayed.
     * @param students The list of enrolled students (User objects).
     */
    public void displayStudents(Course course, List<User> students) {
        if (course != null) {
            courseNameLabel.setText("Enrolled Students for: " + course.getName() + " (" + course.getCourseCode() + ")");
        } else {
            courseNameLabel.setText("Enrolled Students for: [Unknown Course]");
        }
        
        tableModel.setRowCount(0); // Clear existing data
        if (students != null) {
            for (User student : students) {
                 Object[] rowData = {
                         student.getUserID(),
                         student.getFirstName(),
                         student.getLastName(),
                         student.getEmail()
                 };
                 tableModel.addRow(rowData);
            }
        }
    }

    /**
     * Filters the students table based on the text in the search field.
     * Searches across First Name, Last Name, and Email columns.
     */
    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null); // No filter
        } else {
            // Case-insensitive search across First Name (1), Last Name (2), Email (3)
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1, 2, 3)); 
        }
    }

    // --- Action Listeners ---

    public void addBackListener(ActionListener listener) {
        backButton.addActionListener(listener);
        // Set action command explicitly for clarity in controller
        backButton.setActionCommand("Back To Teacher Dashboard"); 
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
            JFrame frame = new JFrame("Course Students Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            CourseStudentsPanel studentsPanel = new CourseStudentsPanel();

            // Add Dummy Data for Testing
             Course dummyCourse = new Course(1, "CS101", "Intro to Programming", 50, 5, null, null);
             List<User> dummyStudents = new ArrayList<>();
             // User(int userID, String firstName, String lastName, String email, String passwordHash, String salt, UserRole role, String department, String profilePicturePath, String passwordResetToken, Date passwordResetExpiry, Timestamp createdAt, Timestamp updatedAt)
             dummyStudents.add(new User(10, "Alice", "Wonder", "alice@test.com", null, null, null, null, null, null, null, null, null)); 
             dummyStudents.add(new User(15, "Bob", "Builder", "bob@test.com", null, null, null, null, null, null, null, null, null));
             dummyStudents.add(new User(20, "Charlie", "Chaplin", "charlie@test.com", null, null, null, null, null, null, null, null, null));
            
             studentsPanel.displayStudents(dummyCourse, dummyStudents);

            // Add simple listeners
             studentsPanel.addBackListener(e -> System.out.println("Back button clicked"));

            frame.getContentPane().add(studentsPanel.getPanel()); // Add the panel
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
} 