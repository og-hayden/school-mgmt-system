package com.school.management.view.common;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * A dialog for users to change their password.
 */
public class ChangePasswordDialog extends JDialog {

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton saveButton;
    private JButton cancelButton;

    public ChangePasswordDialog(Frame owner) {
        super(owner, "Change Password", true); // Modal dialog
        initComponents();
        layoutComponents();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        currentPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        saveButton = new JButton("Save Changes");
        cancelButton = new JButton("Cancel");

        cancelButton.addActionListener(e -> dispose()); // Default cancel action
    }

    private void layoutComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Current Password
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(currentPasswordField, gbc);

        // New Password
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(newPasswordField, gbc);

        // Confirm New Password
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(confirmPasswordField, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0;
        gbc.insets = new Insets(15, 5, 5, 5); // Add top padding
        panel.add(buttonPanel, gbc);

        getContentPane().add(panel);
    }

    // --- Getters for Controller --- 

    public char[] getCurrentPassword() {
        return currentPasswordField.getPassword();
    }

    public char[] getNewPassword() {
        return newPasswordField.getPassword();
    }

    public char[] getConfirmPassword() {
        return confirmPasswordField.getPassword();
    }

    // --- Listeners --- 

    public void addSaveListener(ActionListener listener) {
        saveButton.addActionListener(listener);
        saveButton.setActionCommand("Save Password Change"); // Specific command
    }
    
    // --- Utility --- 

     public void displayError(String message) {
         JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
     }

     public void clearFields() {
         currentPasswordField.setText("");
         newPasswordField.setText("");
         confirmPasswordField.setText("");
     }
     
     public void closeDialog() {
         dispose();
     }
} 