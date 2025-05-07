package com.school.management.view.common;

import java.awt.*;
import javax.swing.*;

/**
 * The main window frame for the application.
 * Uses a CardLayout to switch between different view panels (Login, Dashboards, etc.).
 */
public class MainApplicationFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanelContainer;
    
    // Define card names as constants for easier management
    public static final String LOGIN_PANEL = "LoginPanel";
    public static final String ADMIN_DASHBOARD_PANEL = "AdminDashboardPanel";
    public static final String TEACHER_DASHBOARD_PANEL = "TeacherDashboardPanel";
    public static final String STUDENT_DASHBOARD_PANEL = "StudentDashboardPanel";
    // Add more panel names as needed

    public MainApplicationFrame() {
        super("School Management System"); // Window Title
        initComponents();
        configureFrame();
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        mainPanelContainer = new JPanel(cardLayout);
        // Add the container to the frame's content pane
        add(mainPanelContainer, BorderLayout.CENTER);
    }
    
    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600)); // Set a reasonable minimum size
        setSize(1024, 768); // Set a default size
        setLocationRelativeTo(null); // Center the window
    }
    
    /**
     * Adds a view panel to the main container with a specific name.
     * @param panel The JPanel to add.
     * @param name The name to associate with this panel in the CardLayout.
     */
    public void addView(JPanel panel, String name) {
        if (panel != null && name != null && !name.trim().isEmpty()) {
             mainPanelContainer.add(panel, name);
             System.out.println("View added to frame: " + name); // Debugging
        } else {
             System.err.println("Attempted to add null panel or panel with null/empty name.");
        }
    }

    /**
     * Switches the visible panel in the CardLayout container.
     *
     * @param name The name of the panel to show (should match one of the constants like LOGIN_PANEL).
     */
    public void showView(String name) {
         System.out.println("Attempting to show view: " + name); // Debugging
         if (name != null) {
             cardLayout.show(mainPanelContainer, name);
         } else {
              System.err.println("Attempted to show view with null name.");
         }
    }
    
     /**
     * Returns the main frame instance.
     *
     * @return This JFrame.
     */
    public JFrame getFrame() {
        return this;
    }
    
     /**
     * Makes the frame visible.
     */
    public void display() {
         setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainApplicationFrame frame = new MainApplicationFrame();
            
            // Create dummy panels for testing card layout
            JPanel loginTestPanel = new JPanel();
            loginTestPanel.setBackground(Color.LIGHT_GRAY);
            loginTestPanel.add(new JLabel("This is the Login Panel Area"));
            
            JPanel dashboardTestPanel = new JPanel();
            dashboardTestPanel.setBackground(Color.CYAN);
            dashboardTestPanel.add(new JLabel("This is the Dashboard Panel Area"));
            
            // Add views to the frame
            frame.addView(loginTestPanel, MainApplicationFrame.LOGIN_PANEL);
            frame.addView(dashboardTestPanel, "DashboardTestPanel"); // Use a different name for testing
            
            // Initially show the login panel
            frame.showView(MainApplicationFrame.LOGIN_PANEL);
            
             // Add buttons to test switching (put them outside the card layout area)
             JButton showLoginBtn = new JButton("Show Login");
             showLoginBtn.addActionListener(e -> frame.showView(MainApplicationFrame.LOGIN_PANEL));
             JButton showDashBtn = new JButton("Show Dashboard");
             showDashBtn.addActionListener(e -> frame.showView("DashboardTestPanel"));
             
             JPanel buttonPanel = new JPanel();
             buttonPanel.add(showLoginBtn);
             buttonPanel.add(showDashBtn);
             frame.add(buttonPanel, BorderLayout.SOUTH); // Add buttons at the bottom
            
            frame.display(); // Make the frame visible
        });
    }
} 