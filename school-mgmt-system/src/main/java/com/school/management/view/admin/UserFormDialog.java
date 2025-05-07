package com.school.management.view.admin;

import com.school.management.model.entities.User;
import com.school.management.model.entities.UserRole;
import com.school.management.util.validation.InputValidator;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * A JDialog for adding or editing user information.
 */
public class UserFormDialog extends JDialog {

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JComboBox<UserRole> roleComboBox;
    private JTextField departmentField;
    private JLabel departmentLabel;
    private JButton saveButton;
    private JButton cancelButton;

    private boolean isEditMode;
    private User userToEdit; // Store the user being edited

    /**
     * Constructor for Add mode.
     * @param owner The parent frame.
     */
    public UserFormDialog(Frame owner) {
        this(owner, false, null);
    }

    /**
     * Constructor for Edit mode.
     * @param owner The parent frame.
     * @param userToEdit The user whose details are to be edited.
     */
    public UserFormDialog(Frame owner, User userToEdit) {
        this(owner, true, userToEdit);
    }

    private UserFormDialog(Frame owner, boolean isEditMode, User userToEdit) {
        super(owner, true); // Modal dialog
        this.isEditMode = isEditMode;
        this.userToEdit = userToEdit;
        
        setTitle(isEditMode ? "Edit User" : "Add New User");
        initComponents();
        layoutComponents();
        setupListeners();

        if (isEditMode && userToEdit != null) {
            populateFields(userToEdit);
        } else {
             // Set initial state for Add mode (e.g., hide department initially)
             updateDepartmentVisibility();
        }
        
        pack();
        setMinimumSize(getPreferredSize()); // Prevent resizing too small
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        emailField = new JTextField(20);
        departmentField = new JTextField(20);
        departmentLabel = new JLabel("Department:");

        // Populate role combo box
        roleComboBox = new JComboBox<>(UserRole.values());
        
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
    }

    private void layoutComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_END;

        // First Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(firstNameField, gbc);
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;

        // Last Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.weightx = 1.0;
        formPanel.add(lastNameField, gbc);
         gbc.weightx = 0.0;
         gbc.fill = GridBagConstraints.NONE;

        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
         gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
         gbc.anchor = GridBagConstraints.LINE_START;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.weightx = 1.0;
        formPanel.add(emailField, gbc);
         gbc.weightx = 0.0;
         gbc.fill = GridBagConstraints.NONE;

        // Role
        gbc.gridx = 0;
        gbc.gridy = 3;
         gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
         gbc.anchor = GridBagConstraints.LINE_START;
        formPanel.add(roleComboBox, gbc);

        // Department (Label and Field)
        gbc.gridx = 0;
        gbc.gridy = 4;
         gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(departmentLabel, gbc);
        gbc.gridx = 1;
         gbc.anchor = GridBagConstraints.LINE_START;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.weightx = 1.0;
        formPanel.add(departmentField, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add panels to dialog
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(formPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        // Add padding around the dialog content
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void setupListeners() {
        // Listener to show/hide Department based on Role selection
        roleComboBox.addItemListener(e -> updateDepartmentVisibility());
        
        // Default cancel action
         cancelButton.addActionListener(e -> dispose()); // Close the dialog
    }

    /**
     * Shows or hides the Department field based on the selected role.
     */
    private void updateDepartmentVisibility() {
        UserRole selectedRole = (UserRole) roleComboBox.getSelectedItem();
        boolean isTeacher = (selectedRole == UserRole.TEACHER);
        departmentLabel.setVisible(isTeacher);
        departmentField.setVisible(isTeacher);
    }

    /**
     * Populates the form fields with data from an existing user (for edit mode).
     *
     * @param user The user whose data should be displayed.
     */
    private void populateFields(User user) {
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        roleComboBox.setSelectedItem(user.getRole());
        departmentField.setText(user.getDepartment() != null ? user.getDepartment() : "");
        updateDepartmentVisibility(); // Ensure correct visibility on load
    }

    /**
     * Retrieves the user details entered into the form.
     * For edit mode, it updates the userToEdit object.
     * For add mode, it creates a new User object.
     * Note: Password and Salt are not handled by this form.
     *
     * @return A User object containing the form data, or null if validation fails.
     */
    public User getUserDetails() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        UserRole selectedRole = (UserRole) roleComboBox.getSelectedItem();
        String department = (selectedRole == UserRole.TEACHER) ? departmentField.getText().trim() : null;

        // --- Validation --- 
        if (!InputValidator.isNotEmpty(firstName)) {
            showError("First Name cannot be empty.");
            firstNameField.requestFocus(); // Focus the field with error
            return null;
        }
        if (!InputValidator.isNotEmpty(lastName)) {
            showError("Last Name cannot be empty.");
            lastNameField.requestFocus();
            return null;
        }
        if (!InputValidator.isNotEmpty(email)) {
            showError("Email cannot be empty.");
            emailField.requestFocus();
            return null;
        }
        if (!InputValidator.isValidEmail(email)) {
            showError("Invalid email format.");
            emailField.requestFocus();
            return null;
        }
         if (selectedRole == UserRole.TEACHER && !InputValidator.isNotEmpty(department)) {
            showError("Department cannot be empty for Teachers.");
            departmentField.requestFocus();
            return null;
        }

        // --- Create/Update User Object --- 
        User user = isEditMode ? userToEdit : new User();
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRole(selectedRole);
        user.setDepartment(department);
        
        return user;
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
            UserFormDialog addDialog = new UserFormDialog(null);
            addDialog.setTitle("Test Add User");
            addDialog.addSaveListener(e -> {
                 System.out.println("Save clicked (Add Mode)");
                 User addedUser = addDialog.getUserDetails();
                 System.out.println("  User Data: " + addedUser.getFirstName() + ", " + addedUser.getLastName() + ", " + addedUser.getEmail() + ", " + addedUser.getRole() + ", Dept: " + addedUser.getDepartment());
                 addDialog.dispose();
            });
            addDialog.setVisible(true);
        });
        
         // Test Edit Mode (requires dummy user)
         SwingUtilities.invokeLater(() -> {
             // Create a dummy user to edit
             User dummyUser = new User(5, "Edit", "Me", "edit@test.com", "dummyhash", "dummysalt", UserRole.TEACHER, "Physics", null, null, null, null, null);
             
             UserFormDialog editDialog = new UserFormDialog(null, dummyUser);
             editDialog.setTitle("Test Edit User");
             editDialog.addSaveListener(e -> {
                  System.out.println("Save clicked (Edit Mode)");
                  User editedUser = editDialog.getUserDetails();
                  System.out.println("  User Data: ID:" + editedUser.getUserID() + ", " + editedUser.getFirstName() + ", " + editedUser.getLastName() + ", " + editedUser.getEmail() + ", " + editedUser.getRole() + ", Dept: " + editedUser.getDepartment());
                  editDialog.dispose();
             });
             editDialog.setVisible(true);
         });
    }
} 