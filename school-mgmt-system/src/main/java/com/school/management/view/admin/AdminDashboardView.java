package com.school.management.view.admin;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * The main dashboard view for the Administrator role.
 * Contains navigation buttons to access different management panels.
 */
public class AdminDashboardView extends JPanel {

    private JButton userManagementButton;
    private JButton courseManagementButton;
    // private JButton profileButton; // Add later if needed
    private JButton logoutButton;

    public AdminDashboardView() {
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        userManagementButton = new JButton("Manage Users");
        courseManagementButton = new JButton("Manage Courses");
        logoutButton = new JButton("Logout");
        
        // Style buttons (optional)
        Dimension buttonSize = new Dimension(200, 40);
        userManagementButton.setPreferredSize(buttonSize);
        courseManagementButton.setPreferredSize(buttonSize);
        logoutButton.setPreferredSize(buttonSize);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10)); // Overall layout with padding
        
        // Title Label
        JLabel titleLabel = new JLabel("Administrator Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // Add padding
        add(titleLabel, BorderLayout.NORTH);

        // Button Panel (using GridBagLayout for centered column)
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE; // Place components vertically
        gbc.insets = new Insets(10, 10, 10, 10); // Padding between buttons
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        buttonPanel.add(userManagementButton, gbc);
        buttonPanel.add(courseManagementButton, gbc);
        // buttonPanel.add(profileButton, gbc);
        
        // Add logout button separately or at the bottom
        gbc.weighty = 1.0; // Push logout down if centered
        gbc.anchor = GridBagConstraints.PAGE_END; // Anchor logout towards bottom
        gbc.insets = new Insets(30, 10, 10, 10); // More top padding for logout
        //buttonPanel.add(logoutButton, gbc); // Adding to center panel

        // Adding center button panel
        add(buttonPanel, BorderLayout.CENTER);
        
        // Alternatively, place logout button at the bottom of the main panel
         JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         southPanel.add(logoutButton);
         southPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
         add(southPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the whole panel
    }

    // --- Getters for Controller (if needed, maybe not) ---
    // public JButton getUserManagementButton() { return userManagementButton; }
    // public JButton getCourseManagementButton() { return courseManagementButton; }
    // public JButton getLogoutButton() { return logoutButton; }
    
    // --- Action Listeners ---

    public void addUserManagementListener(ActionListener listener) {
        userManagementButton.addActionListener(listener);
    }

    public void addCourseManagementListener(ActionListener listener) {
        courseManagementButton.addActionListener(listener);
    }

    public void addLogoutListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
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
            JFrame frame = new JFrame("Admin Dashboard View Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            AdminDashboardView dashboardView = new AdminDashboardView();
            
            // Add simple listeners for testing
            dashboardView.addUserManagementListener(e -> System.out.println("Manage Users button clicked"));
            dashboardView.addCourseManagementListener(e -> System.out.println("Manage Courses button clicked"));
            dashboardView.addLogoutListener(e -> {
                System.out.println("Logout button clicked");
                // In real app, this would trigger controller logout
                // For test, maybe close the window
                // frame.dispose(); 
             });
             
            frame.getContentPane().add(dashboardView);
            frame.pack(); 
            frame.setMinimumSize(new Dimension(400, 300)); // Ensure minimum size
            frame.setLocationRelativeTo(null); 
            frame.setVisible(true);
        });
    }
} 