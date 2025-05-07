package com.school.management.view.admin;

import com.school.management.model.entities.Course;
import com.school.management.util.validation.InputValidator;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * A JDialog for adding or editing course information.
 */
public class CourseFormDialog extends JDialog {

    private JTextField courseCodeField;
    private JTextField courseNameField;
    private JSpinner capacitySpinner;
    private JButton saveButton;
    private JButton cancelButton;

    private boolean isEditMode;
    private Course courseToEdit; // Store the course being edited

    /**
     * Constructor for Add mode.
     * @param owner The parent frame.
     */
    public CourseFormDialog(Frame owner) {
        this(owner, false, null);
    }

    /**
     * Constructor for Edit mode.
     * @param owner The parent frame.
     * @param courseToEdit The course whose details are to be edited.
     */
    public CourseFormDialog(Frame owner, Course courseToEdit) {
        this(owner, true, courseToEdit);
    }

    private CourseFormDialog(Frame owner, boolean isEditMode, Course courseToEdit) {
        super(owner, true); // Modal dialog
        this.isEditMode = isEditMode;
        this.courseToEdit = courseToEdit;
        
        setTitle(isEditMode ? "Edit Course" : "Add New Course");
        initComponents();
        layoutComponents();
        setupListeners();

        if (isEditMode && courseToEdit != null) {
            populateFields(courseToEdit);
        }
        
        pack();
        setMinimumSize(getPreferredSize());
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        courseCodeField = new JTextField(15);
        courseNameField = new JTextField(25);
        // Spinner for capacity: Start at 1, min 1, max high, step 1
        SpinnerModel capacityModel = new SpinnerNumberModel(30, 1, 500, 1);
        capacitySpinner = new JSpinner(capacityModel);
        // Prevent text editing in spinner, force use of arrows or direct number input
        if (capacitySpinner.getEditor() instanceof JSpinner.DefaultEditor) {
             JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) capacitySpinner.getEditor();
             editor.getTextField().setEditable(true); // Allow editing but validate on save
         }
        
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
    }

    private void layoutComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_END;

        // Course Code
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(courseCodeField, gbc);
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;

        // Course Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(new JLabel("Course Name:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(courseNameField, gbc);
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;

        // Capacity
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(new JLabel("Max Capacity:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        // Don't fill horizontally for spinner
        formPanel.add(capacitySpinner, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add panels to dialog
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(formPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void setupListeners() {
        // Default cancel action
        cancelButton.addActionListener(e -> dispose()); // Close the dialog
    }

    /**
     * Populates the form fields with data from an existing course (for edit mode).
     *
     * @param course The course whose data should be displayed.
     */
    private void populateFields(Course course) {
        courseCodeField.setText(course.getCourseCode());
        courseNameField.setText(course.getName());
        capacitySpinner.setValue(course.getMaximumCapacity());
        // Course Code might be non-editable in edit mode
        // courseCodeField.setEditable(false); 
    }

    /**
     * Retrieves the course details entered into the form.
     * For edit mode, it updates the courseToEdit object.
     * For add mode, it creates a new Course object.
     *
     * @return A Course object containing the form data, or null if validation fails.
     */
    public Course getCourseDetails() {
        String courseCode = courseCodeField.getText().trim();
        String courseName = courseNameField.getText().trim();
        Integer capacity = (Integer) capacitySpinner.getValue(); // Already validated by SpinnerModel >= 1
        
         // --- Validation --- 
         if (!InputValidator.isNotEmpty(courseCode)) {
             showError("Course Code cannot be empty.");
             courseCodeField.requestFocus();
             return null;
         }
         if (!InputValidator.isNotEmpty(courseName)) {
             showError("Course Name cannot be empty.");
             courseNameField.requestFocus();
             return null;
         }
         // Capacity is inherently validated by the JSpinner's model (min 1)
         
        // --- Create/Update Course Object --- 
        Course course = isEditMode ? courseToEdit : new Course();
        
        course.setCourseCode(courseCode);
        course.setName(courseName);
        course.setMaximumCapacity(capacity); 
        
        return course;
    }
    
    /**
     * Helper method to display an error message in a dialog.
     * @param message The error message to display.
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Adds an ActionListener to the Save button.
     * @param listener The ActionListener to add.
     */
    public void addSaveListener(ActionListener listener) {
        saveButton.addActionListener(listener);
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
        
        // Test Add Mode
        SwingUtilities.invokeLater(() -> {
            CourseFormDialog addDialog = new CourseFormDialog(null);
            addDialog.setTitle("Test Add Course");
            addDialog.addSaveListener(e -> {
                 System.out.println("Save clicked (Add Mode)");
                 Course addedCourse = addDialog.getCourseDetails();
                 System.out.println("  Course Data: Code:" + addedCourse.getCourseCode() + ", Name:" + addedCourse.getName() + ", Capacity:" + addedCourse.getMaximumCapacity());
                 addDialog.dispose();
            });
            addDialog.setVisible(true);
        });
        
         // Test Edit Mode
         SwingUtilities.invokeLater(() -> {
             // Create a dummy course to edit
             Course dummyCourse = new Course(10, "EDIT101", "Editable Course", 15, null, null, null);
             
             CourseFormDialog editDialog = new CourseFormDialog(null, dummyCourse);
             editDialog.setTitle("Test Edit Course");
             editDialog.addSaveListener(e -> {
                  System.out.println("Save clicked (Edit Mode)");
                  Course editedCourse = editDialog.getCourseDetails();
                  System.out.println("  Course Data: ID:" + editedCourse.getCourseID() + ", Code:" + editedCourse.getCourseCode() + ", Name:" + editedCourse.getName() + ", Capacity:" + editedCourse.getMaximumCapacity());
                  editDialog.dispose();
             });
             editDialog.setVisible(true);
         });
    }
} 