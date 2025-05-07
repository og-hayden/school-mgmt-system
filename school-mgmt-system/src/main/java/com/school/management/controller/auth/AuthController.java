package com.school.management.controller.auth;

import com.school.management.controller.AppController;
import com.school.management.model.dao.UserDAO;
import com.school.management.model.entities.User;
import com.school.management.util.UserSession;
import com.school.management.util.security.PasswordUtil;
import com.school.management.util.validation.InputValidator;
import com.school.management.view.auth.LoginView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Controller responsible for handling authentication logic,
 * primarily user login and logout.
 */
public class AuthController implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    private final LoginView loginView;
    private final UserDAO userDAO;
    private AppController appController; // Main application controller for navigation

    public AuthController(LoginView loginView, UserDAO userDAO, 
                          /* PasswordUtil passwordUtil */ Object ignoredPasswordUtil, 
                          /* UserSession userSession */ Object ignoredUserSession, 
                          /* InputValidator inputValidator */ Object ignoredInputValidator, 
                          AppController appController) {
        this.loginView = loginView;
        this.userDAO = userDAO;
        this.appController = appController;
        
            
        this.loginView.addLoginListener(this);
        this.loginView.addForgotPasswordListener(this); // Add listener for forgot password
    }

    /**
     * Handles the login button click event from the LoginView.
     *
     * @param e The ActionEvent triggered by the button click.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        LOGGER.log(Level.FINE, "AuthController received action: {0}", command);
        
        if ("Login".equals(command)) {
            handleLoginAttempt();
        } else if ("Forgot Password".equals(command)) {
            handleForgotPasswordAction();
        } else {
             LOGGER.log(Level.WARNING, "Unknown action command in AuthController: {0}", command);
        }
    }

    /**
     * Processes the login attempt using credentials from the LoginView.
     */
    private void handleLoginAttempt() {
        String email = loginView.getEmail();
        char[] passwordChars = loginView.getPassword();
        String password = new String(passwordChars);

        // Clear previous errors
        loginView.displayError(null);

        // --- Input Validation ---
        if (!InputValidator.isNotEmpty(email) || !InputValidator.isNotEmpty(password)) {
            loginView.displayError("Email and password cannot be empty.");
            clearPasswordArray(passwordChars);
            return;
        }
        if (!InputValidator.isValidEmail(email)) {
            loginView.displayError("Invalid email format.");
            clearPasswordArray(passwordChars);
            return;
        }

        // --- Authentication Logic ---
        try {
            User user = userDAO.getUserByEmail(email);

            if (user == null) {
                loginView.displayError("Invalid email or password."); // Generic error
                LOGGER.log(Level.WARNING, "Login attempt failed: User not found for email {0}", email);
            } else {
                // Retrieve salt and hash
                String salt = user.getSalt();
                String storedHash = user.getPasswordHash();
                
                // --- DEBUG LOGGING --- 
                LOGGER.log(Level.INFO, "Attempting password check for user: {0}", email);
                LOGGER.log(Level.INFO, "   Retrieved Salt (Base64): {0}", salt);
                LOGGER.log(Level.INFO, "   Retrieved Hash (Base64): {0}", storedHash);
                LOGGER.log(Level.INFO, "   Password entered (length): {0}", password.length()); 
                
                if (salt == null || storedHash == null) {
                     loginView.displayError("Authentication error. Please contact support.");
                     LOGGER.log(Level.SEVERE, "Login attempt failed: User {0} has missing salt or hash.", email);
                } else if (PasswordUtil.checkPassword(password, storedHash, salt)) {
                    // --- Login Successful ---
                    UserSession.login(user);
                    LOGGER.log(Level.INFO, "Login successful for user {0} ({1})", new Object[]{user.getEmail(), user.getRole()});
                    loginView.clearForm();
                    // Navigate to the appropriate dashboard via AppController
                    if (appController != null) {
                        appController.onLoginSuccess(user);
                    } else {
                        LOGGER.log(Level.WARNING, "AppController is null, cannot navigate after login.");
                        loginView.displayError("Login successful, but navigation failed.");
                    }
                } else {
                    // --- Login Failed (Incorrect Password) ---
                    loginView.displayError("Invalid email or password."); // Generic error
                    LOGGER.log(Level.WARNING, "Login attempt failed: Incorrect password for email {0}", email);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred during login attempt for " + email, ex);
            loginView.displayError("An error occurred during login. Please try again.");
        } finally {
             // Securely clear the password array
            clearPasswordArray(passwordChars);
        }
    }
    
    /**
     * Handles the "Forgot Password?" button click.
     * Prompts the user for their email and shows a confirmation message.
     */
    private void handleForgotPasswordAction() {
        LOGGER.info("Forgot Password action initiated.");
        
        String email = JOptionPane.showInputDialog(
            loginView, // Parent component
            "Please enter your account email address:", 
            "Forgot Password", 
            JOptionPane.PLAIN_MESSAGE
        );

        if (email != null) { // User didn't cancel
            email = email.trim();
            if (InputValidator.isValidEmail(email)) {
                // Notionally send the email
                LOGGER.log(Level.INFO, "Password reset requested for email: {0}. (Notional email sent)", email);
                JOptionPane.showMessageDialog(
                    loginView, 
                    "If an account exists for " + email + ", a password reset link has been sent.",
                    "Password Reset Requested",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else if (!email.isEmpty()) {
                 // Email entered but invalid format
                 JOptionPane.showMessageDialog(
                     loginView, 
                     "Invalid email format entered.",
                     "Input Error",
                     JOptionPane.ERROR_MESSAGE
                 );
            } else {
                 // Empty email entered (or just spaces)
                 JOptionPane.showMessageDialog(
                     loginView, 
                     "Email address cannot be empty.",
                     "Input Error",
                     JOptionPane.ERROR_MESSAGE
                 );
            }
        } else {
             LOGGER.info("Forgot Password dialog cancelled by user.");
        }
    }

    /**
     * Securely clears a character array, typically used for passwords.
     * @param array The character array to clear.
     */
    private void clearPasswordArray(char[] array) {
        if (array != null) {
            Arrays.fill(array, '\0'); 
        }
    }
} 