package com.school.management.controller;

import com.school.management.controller.auth.AuthController;
import com.school.management.model.dao.CourseDAO;
import com.school.management.model.dao.EnrollmentDAO;
import com.school.management.model.dao.MessageDAO;
import com.school.management.model.dao.UserDAO;
import com.school.management.model.entities.User;
import com.school.management.model.entities.UserRole;
import com.school.management.util.UserSession;
import com.school.management.view.auth.LoginView;
import com.school.management.view.common.MainApplicationFrame;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main application controller.
 * Initializes DAOs, views, and other controllers.
 * Manages view transitions and overall application flow.
 */
public class AppController {

    private static final Logger LOGGER = Logger.getLogger(AppController.class.getName());

    // Core Components
    private MainApplicationFrame mainFrame;
    private UserSession userSession;

    // DAOs
    private UserDAO userDAO;
    private CourseDAO courseDAO;
    private EnrollmentDAO enrollmentDAO;
    private MessageDAO messageDAO;

    // Views
    private LoginView loginView;
    // Add dashboard/panel views here as they are created
    // private AdminDashboardView adminDashboardView;
    // private TeacherDashboardView teacherDashboardView;
    // private StudentDashboardView studentDashboardView;

    // Controllers
    private AuthController authController;
    // Add other controllers here as they are created
    // private AdminController adminController;
    // private TeacherController teacherController;
    // private StudentController studentController;

    public AppController() {
        // Initialize core components first
        this.userSession = new UserSession(); // Assuming static access is intended
        
        // Initialize DAOs
        this.userDAO = new UserDAO();
        this.courseDAO = new CourseDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.messageDAO = new MessageDAO();
        
        // Initialize Views
        this.loginView = new LoginView();
        // Initialize other views...
        
        // Initialize Controllers (pass necessary dependencies)
        // Note: The frame needs to be created before controllers that might need it (e.g., for dialogs)
        // Initialize the main frame AFTER other components if they need to be passed to it,
        // OR pass the AppController itself to the frame if the frame needs to call back.
        // For now, initialize frame first, then controllers.
        this.mainFrame = new MainApplicationFrame(); 
        this.authController = new AuthController(loginView, userDAO, /*passwordUtil*/ null, /*userSession*/ null, /*inputValidator*/ null, this);
        // Initialize other controllers...
    }

    /**
     * Starts the application by setting up the initial view.
     */
    public void startApplication() {
        // Add the initial view (Login) to the frame
        mainFrame.addView(loginView, MainApplicationFrame.LOGIN_PANEL);
        
        // Show the login view initially
        mainFrame.showView(MainApplicationFrame.LOGIN_PANEL);
        
        // Make the frame visible
        mainFrame.display();
        LOGGER.info("Application started, showing Login view.");
    }

    /**
     * Called by AuthController upon successful login.
     *
     * @param user The successfully logged-in user.
     */
    public void onLoginSuccess(User user) {
        if (user != null) {
            LOGGER.log(Level.INFO, "Login successful notification received for {0} ({1})", new Object[]{user.getEmail(), user.getRole()});
            navigateToRoleDashboard(user.getRole());
        } else {
             LOGGER.log(Level.WARNING, "onLoginSuccess called with null user.");
             // Optionally show login screen again or an error
             showLoginScreen();
        }
    }

    /**
     * Navigates to the appropriate dashboard based on the user's role.
     *
     * @param role The role of the logged-in user.
     */
    public void navigateToRoleDashboard(UserRole role) {
        if (role == null) {
            LOGGER.warning("Cannot navigate, user role is null.");
            showLoginScreen(); // Go back to login if role is invalid
            return;
        }

        switch (role) {
            case ADMINISTRATOR:
                LOGGER.info("Navigating to Admin Dashboard...");
                // TODO: Implement Admin Dashboard View creation and display
                // if (adminDashboardView == null) adminDashboardView = new AdminDashboardView(...);
                // mainFrame.addView(adminDashboardView, MainApplicationFrame.ADMIN_DASHBOARD_PANEL);
                // mainFrame.showView(MainApplicationFrame.ADMIN_DASHBOARD_PANEL);
                showPlaceholderView("Admin Dashboard Area", MainApplicationFrame.ADMIN_DASHBOARD_PANEL);
                break;
            case TEACHER:
                LOGGER.info("Navigating to Teacher Dashboard...");
                // TODO: Implement Teacher Dashboard View creation and display
                // if (teacherDashboardView == null) teacherDashboardView = new TeacherDashboardView(...);
                // mainFrame.addView(teacherDashboardView, MainApplicationFrame.TEACHER_DASHBOARD_PANEL);
                // mainFrame.showView(MainApplicationFrame.TEACHER_DASHBOARD_PANEL);
                 showPlaceholderView("Teacher Dashboard Area", MainApplicationFrame.TEACHER_DASHBOARD_PANEL);
                break;
            case STUDENT:
                 LOGGER.info("Navigating to Student Dashboard...");
                 // TODO: Implement Student Dashboard View creation and display
                 // if (studentDashboardView == null) studentDashboardView = new StudentDashboardView(...);
                 // mainFrame.addView(studentDashboardView, MainApplicationFrame.STUDENT_DASHBOARD_PANEL);
                 // mainFrame.showView(MainApplicationFrame.STUDENT_DASHBOARD_PANEL);
                 showPlaceholderView("Student Dashboard Area", MainApplicationFrame.STUDENT_DASHBOARD_PANEL);
                break;
            default:
                 LOGGER.warning("Unknown user role: " + role + ". Returning to login.");
                 showLoginScreen();
                break;
        }
    }
    
    /**
     * Helper method to show a simple placeholder panel for unimplemented views.
     */
     private void showPlaceholderView(String text, String panelName) {
         JPanel placeholder = new JPanel(new BorderLayout());
         JLabel label = new JLabel(text, SwingConstants.CENTER);
         label.setFont(new Font("Arial", Font.BOLD, 24));
         placeholder.add(label, BorderLayout.CENTER);
         
         // Add a logout button to the placeholder for now
         JButton logoutButton = new JButton("Logout");
         logoutButton.addActionListener(e -> logout());
         JPanel buttonPanel = new JPanel();
         buttonPanel.add(logoutButton);
         placeholder.add(buttonPanel, BorderLayout.SOUTH);
         
         mainFrame.addView(placeholder, panelName); 
         mainFrame.showView(panelName);
     }

    /**
     * Logs out the current user and returns to the login screen.
     */
    public void logout() {
        userSession.logout();
        showLoginScreen();
        LOGGER.info("User logged out. Showing Login view.");
    }

    /**
     * Shows the login screen.
     */
    public void showLoginScreen() {
        loginView.clearForm(); // Clear any previous input/errors
        mainFrame.showView(MainApplicationFrame.LOGIN_PANEL);
    }
    
    /**
     * Main entry point for the application.
     */
     public static void main(String[] args) {
         // Set Look and Feel (Optional but recommended)
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
        
        // Ensure UI updates are on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            AppController controller = new AppController();
            controller.startApplication();
        });
    }
} 