package com.school.management.view.admin;

import com.school.management.model.entities.User;
import com.school.management.model.entities.UserRole;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.*;

/**
 * A JDialog for selecting a teacher to assign to a course.
 */
public class AssignTeacherDialog extends JDialog {

    private JComboBox<UserComboBoxItem> teacherComboBox;
    private JButton assignButton;
    private JButton cancelButton;

    private List<User> teacherList;

    /**
     * Represents an item in the teacher combo box.
     */
    private static class UserComboBoxItem {
        private int id;
        private String displayName;

        public UserComboBoxItem(int id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return displayName; // This is what JComboBox displays
        }
    }

    /**
     * Constructs the AssignTeacherDialog.
     *
     * @param owner The parent frame.
     * @param courseId The ID of the course to assign a teacher to (for context).
     * @param teachers A list of User objects representing available teachers.
     */
    public AssignTeacherDialog(Frame owner, int courseId, List<User> teachers) {
        super(owner, "Assign Teacher to Course ID: " + courseId, true); // Modal dialog
        this.teacherList = teachers;

        initComponents();
        layoutComponents();
        populateTeacherComboBox(teachers);
        setupListeners();

        pack();
        setMinimumSize(getPreferredSize());
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        teacherComboBox = new JComboBox<>();
        assignButton = new JButton("Assign");
        cancelButton = new JButton("Cancel");
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel for label and combo box
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Teacher:"));
        topPanel.add(teacherComboBox);
        mainPanel.add(topPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(assignButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    private void populateTeacherComboBox(List<User> teachers) {
        Vector<UserComboBoxItem> comboBoxItems = new Vector<>();
        if (teachers != null) {
            for (User teacher : teachers) {
                if (teacher != null && teacher.getRole() == UserRole.TEACHER) { // Ensure it's a teacher
                    String displayName = teacher.getFirstName() + " " + teacher.getLastName() + " (ID: " + teacher.getUserID() + ")";
                    comboBoxItems.add(new UserComboBoxItem(teacher.getUserID(), displayName));
                }
            }
        }
        teacherComboBox.setModel(new DefaultComboBoxModel<>(comboBoxItems));
        if (!comboBoxItems.isEmpty()) {
             teacherComboBox.setSelectedIndex(0);
        }
    }

    private void setupListeners() {
        // Default cancel action
        cancelButton.addActionListener(e -> dispose()); // Close the dialog
    }

    /**
     * Gets the User ID of the selected teacher.
     *
     * @return The selected teacher's User ID, or -1 if no teacher is selected or the list was empty.
     */
    public int getSelectedTeacherId() {
        Object selectedItem = teacherComboBox.getSelectedItem();
        if (selectedItem instanceof UserComboBoxItem) {
            return ((UserComboBoxItem) selectedItem).getId();
        }
        return -1; // Indicate no selection or error
    }

    /**
     * Adds an ActionListener to the Assign button.
     * @param listener The ActionListener to add.
     */
    public void addAssignListener(ActionListener listener) {
        assignButton.addActionListener(listener);
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

        // Create dummy teacher data for testing
        List<User> dummyTeachers = new ArrayList<>();
        // User(int userID, String firstName, String lastName, String email, String passwordHash, String salt, UserRole role, String department, String profilePicturePath, String passwordResetToken, Date passwordResetExpiry, Timestamp createdAt, Timestamp updatedAt)
        // Simplified for testing dialog
        dummyTeachers.add(new User(5, "Alice", "Smith", "alice@test.com", "hash1", "salt1", UserRole.TEACHER, "Math", null, null, null, null, null));
        dummyTeachers.add(new User(8, "Bob", "Jones", "bob@test.com", "hash2", "salt2", UserRole.TEACHER, "Science", null, null, null, null, null));
        dummyTeachers.add(new User(12, "Charlie", "Brown", "charlie@test.com", "hash3", "salt3", UserRole.TEACHER, "History", null, null, null, null, null));

        SwingUtilities.invokeLater(() -> {
            AssignTeacherDialog dialog = new AssignTeacherDialog(null, 101, dummyTeachers);
            dialog.addAssignListener(e -> {
                int selectedId = dialog.getSelectedTeacherId();
                System.out.println("Assign clicked. Selected Teacher ID: " + selectedId);
                if (selectedId != -1) {
                    dialog.teacherList.stream()
                        .filter(t -> t.getUserID() == selectedId)
                        .findFirst()
                        .ifPresent(t -> System.out.println("  Teacher Name: " + t.getFirstName() + " " + t.getLastName()));
                }
                dialog.dispose();
            });
            dialog.setVisible(true);
        });
    }
} 