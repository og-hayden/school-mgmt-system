package com.school.management.view.auth;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * JPanel representing the login screen UI.
 * Contains fields for email, password, a login button, and an error message area.
 */
public class LoginView extends JPanel {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton forgotPasswordButton;
    private JLabel errorLabel;

    public LoginView() {
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        forgotPasswordButton = new JButton("Forgot Password?");
        forgotPasswordButton.setBorderPainted(false);
        forgotPasswordButton.setContentAreaFilled(false);
        forgotPasswordButton.setForeground(Color.BLUE);
        forgotPasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordButton.setHorizontalAlignment(SwingConstants.CENTER);

        errorLabel = new JLabel(" "); // Initialize with space to reserve height
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title (Optional, but good for context)
        JLabel titleLabel = new JLabel("School Management System Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 5, 20, 5); // More top/bottom padding
        add(titleLabel, gbc);
        
        gbc.gridwidth = 1; // Reset gridwidth
        gbc.weightx = 0.0; // Reset weightx
        gbc.insets = new Insets(5, 5, 5, 5); // Reset insets

        // Email Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Email:"), gbc);

        // Email Field
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(emailField, gbc);

        // Password Label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Password:"), gbc);

        // Password Field
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(passwordField, gbc);

        // Error Label
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(errorLabel, gbc);
        
        // Login Button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2; // Span across both columns
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE; 
        gbc.insets = new Insets(15, 5, 5, 5); // Reduced bottom padding
        add(loginButton, gbc);

        // Forgot Password Button
        gbc.gridx = 0;
        gbc.gridy = 5; // Place below Login button
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 5, 10, 5); // Padding top/bottom
        add(forgotPasswordButton, gbc);
    }

    // --- Getters ---

    public String getEmail() {
        return emailField.getText().trim();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    // --- Listener ---

    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
        loginButton.setActionCommand("Login"); // Ensure command is set
    }

    /**
     * Adds an ActionListener to the Forgot Password button.
     * @param listener The listener to add.
     */
    public void addForgotPasswordListener(ActionListener listener) {
        forgotPasswordButton.addActionListener(listener);
        forgotPasswordButton.setActionCommand("Forgot Password"); // Set command
    }

    // --- UI Control ---

    public void displayError(String message) {
        errorLabel.setText(message == null || message.trim().isEmpty() ? " " : message);
    }

    public void clearForm() {
        emailField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
    }
    
    public JButton getLoginButton() {
        return loginButton;
    }

    // --- Main method for testing ---
    public static void main(String[] args) {
        // Set a modern Look and Feel ( Nimbus ) if available
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
            JFrame frame = new JFrame("Login View Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            LoginView loginView = new LoginView();
            
            // Add a simple action listener for testing the button click
             loginView.addLoginListener(e -> {
                 String email = loginView.getEmail();
                 String password = new String(loginView.getPassword());
                 System.out.println("Login button clicked!");
                 System.out.println("Email entered: " + email);
                 System.out.println("Password entered: " + password);
                 if (email.isEmpty() || password.isEmpty()) {
                     loginView.displayError("Email and Password cannot be empty.");
                 } else if (!email.contains("@")) {
                     loginView.displayError("Invalid email format.");
                 }
                 else {
                     loginView.displayError(" "); // Clear error on valid (for test) input
                 }
             });
             
            frame.getContentPane().add(loginView);
            frame.pack(); // Adjust frame size to fit components
            frame.setLocationRelativeTo(null); // Center the frame
            frame.setVisible(true);
        });
    }
} 