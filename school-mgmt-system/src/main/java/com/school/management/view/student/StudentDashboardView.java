package com.school.management.view.student;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * Main dashboard view for Students.
 * Provides navigation to available courses, enrolled courses, profile, and logout.
 */
public class StudentDashboardView extends JPanel {

    private JButton viewAvailableCoursesButton;
    private JButton viewEnrolledCoursesButton;
    private JButton profileButton;
    private JButton logoutButton;

    public StudentDashboardView() {
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        viewAvailableCoursesButton = new JButton("View Available Courses");
        viewEnrolledCoursesButton = new JButton("View My Enrolled Courses");
        profileButton = new JButton("View Profile");
        logoutButton = new JButton("Logout");
        
        // Set preferred sizes for buttons for consistency
        Dimension buttonSize = new Dimension(200, 40);
        viewAvailableCoursesButton.setPreferredSize(buttonSize);
        viewEnrolledCoursesButton.setPreferredSize(buttonSize);
        profileButton.setPreferredSize(buttonSize);
        logoutButton.setPreferredSize(new Dimension(100, 30)); // Smaller logout
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // Title
        JLabel titleLabel = new JLabel("Student Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // Main content panel with navigation buttons
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0); // Vertical padding

        centerPanel.add(viewAvailableCoursesButton, gbc);
        centerPanel.add(viewEnrolledCoursesButton, gbc);
        centerPanel.add(profileButton, gbc);
        
        add(centerPanel, BorderLayout.CENTER);

        // Logout button at the bottom left
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        southPanel.add(logoutButton);
        add(southPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50)); // Add padding
    }

    // --- Action Listeners ---

    public void addAvailableCoursesListener(ActionListener listener) {
        viewAvailableCoursesButton.addActionListener(listener);
        viewAvailableCoursesButton.setActionCommand("View Available Courses");
    }

    public void addEnrolledCoursesListener(ActionListener listener) {
        viewEnrolledCoursesButton.addActionListener(listener);
        viewEnrolledCoursesButton.setActionCommand("View Enrolled Courses");
    }

    public void addProfileListener(ActionListener listener) {
        profileButton.addActionListener(listener);
        profileButton.setActionCommand("View Profile");
    }

    public void addLogoutListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
        logoutButton.setActionCommand("Logout"); // Consistent action command
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
            JFrame frame = new JFrame("Student Dashboard View Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            StudentDashboardView dashboard = new StudentDashboardView();

            // Add simple listeners
            dashboard.addAvailableCoursesListener(e -> System.out.println("View Available Courses clicked"));
            dashboard.addEnrolledCoursesListener(e -> System.out.println("View Enrolled Courses clicked"));
            dashboard.addProfileListener(e -> System.out.println("View Profile clicked"));
            dashboard.addLogoutListener(e -> System.out.println("Logout clicked"));

            frame.getContentPane().add(dashboard.getPanel());
            frame.pack(); // Pack to respect preferred sizes
            frame.setMinimumSize(frame.getSize());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
} 