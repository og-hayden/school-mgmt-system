package com.school.management.controller.admin;

import com.school.management.controller.AppController;
import com.school.management.model.dao.CourseDAO;
import com.school.management.model.dao.UserDAO;
import com.school.management.model.entities.Course;
import com.school.management.model.entities.User;
import com.school.management.model.entities.UserRole;
import com.school.management.util.security.PasswordUtil;
import com.school.management.view.admin.AdminDashboardView;
import com.school.management.view.admin.AssignTeacherDialog;
import com.school.management.view.admin.CourseFormDialog;
import com.school.management.view.admin.CourseManagementPanel;
import com.school.management.view.admin.UserFormDialog;
import com.school.management.view.admin.UserManagementPanel;
import com.school.management.view.common.ChangePasswordDialog;
import com.school.management.view.common.MainApplicationFrame;
import com.school.management.view.common.UserProfilePanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;

/**
 * Controller responsible for handling actions within the Admin module.
 * Manages navigation between admin panels and data loading.
 */
public class AdminController implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());

    private final AppController appController;
    private final MainApplicationFrame mainFrame;
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;

    // Admin views managed by this controller
    private final AdminDashboardView dashboardView;
    private final UserManagementPanel userManagementPanel;
    private final CourseManagementPanel courseManagementPanel;
    private final UserProfilePanel userProfilePanel;

    // Define card names for the admin section within the main frame
    public static final String ADMIN_DASHBOARD = "AdminDashboardCard";
    public static final String USER_MANAGEMENT = "UserManagementCard";
    public static final String COURSE_MANAGEMENT = "CourseManagementCard";
    public static final String ADMIN_PROFILE = "AdminProfileCard";

    public AdminController(AppController appController, MainApplicationFrame mainFrame, UserDAO userDAO, CourseDAO courseDAO) {
        this.appController = appController;
        this.mainFrame = mainFrame;
        this.userDAO = userDAO;
        this.courseDAO = courseDAO;

        // Initialize the views this controller manages
        this.dashboardView = new AdminDashboardView();
        this.userManagementPanel = new UserManagementPanel();
        this.courseManagementPanel = new CourseManagementPanel();
        this.userProfilePanel = new UserProfilePanel();

        initializeViews(); 
    }

    /**
     * Adds admin views to the main frame and sets up listeners.
     */
    public void initializeViews() {
         LOGGER.info("Initializing Admin views and adding to main frame.");
         // Add views to the main frame using unique names
         mainFrame.addView(dashboardView, ADMIN_DASHBOARD);
         mainFrame.addView(userManagementPanel, USER_MANAGEMENT);
         mainFrame.addView(courseManagementPanel, COURSE_MANAGEMENT);
         mainFrame.addView(userProfilePanel, ADMIN_PROFILE);
         
         // Attach listeners
         attachListeners();
    }
    
    /**
     * Attaches action listeners to the buttons in the admin views.
     */
    private void attachListeners() {
        // Dashboard Listeners
        dashboardView.addUserManagementListener(this);
        dashboardView.addCourseManagementListener(this);
        dashboardView.addProfileListener(this);
        dashboardView.addLogoutListener(this);

        // User Management Panel Listeners
        userManagementPanel.addAddUserListener(this);
        userManagementPanel.addEditUserListener(this);
        userManagementPanel.addDeleteUserListener(this); 
        userManagementPanel.addBackButtonListener(this);

        // Course Management Panel Listeners
        courseManagementPanel.addAddCourseListener(this);       
        courseManagementPanel.addEditCourseListener(this);      
        courseManagementPanel.addDeleteCourseListener(this);    
        courseManagementPanel.addAssignTeacherListener(this);   
        courseManagementPanel.addUnassignTeacherListener(this); 
        courseManagementPanel.addBackButtonListener(this);

        // User Profile Panel Listeners
        userProfilePanel.addChangePasswordListener(this);
        userProfilePanel.addUploadPictureListener(this);
        userProfilePanel.addBackListener(this);
    }

    /**
     * Shows the main Admin Dashboard view.
     */
    public void showDashboard() {
        LOGGER.info("Showing Admin Dashboard.");
        mainFrame.showView(ADMIN_DASHBOARD);
    }

    /**
     * Loads data and shows the User Management panel.
     */
    public void showUserManagement() {
        LOGGER.info("Showing User Management Panel.");
        loadUsers();
        mainFrame.showView(USER_MANAGEMENT);
    }

    /**
     * Loads data and shows the Course Management panel.
     */
    public void showCourseManagement() {
        LOGGER.info("Showing Course Management Panel.");
        loadCourses();
        mainFrame.showView(COURSE_MANAGEMENT);
    }

    /**
     * Loads the current admin's profile data and shows the User Profile panel.
     */
    private void showProfileView() {
        User currentUser = appController.getCurrentUser();
        if (currentUser == null) {
            LOGGER.warning("Cannot show profile: No user logged in.");
             JOptionPane.showMessageDialog(mainFrame.getFrame(), "Session error. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            handleLogout();
            return;
        }
        LOGGER.info("Showing Profile for UserID: " + currentUser.getUserID());
        userProfilePanel.setUserProfile(currentUser);
        // Set the correct back action command before showing
        userProfilePanel.getBackButton().setActionCommand("Back To Admin Dashboard From Profile");
        mainFrame.showView(ADMIN_PROFILE);
    }

    /**
     * Fetches users from the DAO and updates the User Management Panel.
     */
    private void loadUsers() {
        try {
            LOGGER.fine("Loading users from database...");
            List<User> users = userDAO.getAllUsers();
            userManagementPanel.displayUsers(users);
            LOGGER.fine("User list updated in panel.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading users.", e);
             JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error loading users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Fetches courses from the DAO and updates the Course Management Panel.
     */
    private void loadCourses() {
        Map<Integer, String> teacherNames = null; // Map teacher ID to display name
        try {
             LOGGER.fine("Loading courses from database...");
            List<Course> courses = courseDAO.getAllCourses();
            // Fetch teacher names for assigned courses
            if (courses != null && !courses.isEmpty()) {
                List<Integer> teacherIds = courses.stream()
                                                  .map(Course::getTeacherUserID)
                                                  .filter(id -> id != null && id > 0) // Filter out null or 0 IDs
                                                  .distinct()
                                                  .collect(Collectors.toList());
                if (!teacherIds.isEmpty()) {
                    teacherNames = userDAO.getUserNamesByIds(teacherIds);
                }
            }
            courseManagementPanel.displayCourses(courses, teacherNames);
            LOGGER.fine("Course list updated in panel.");
        } catch (Exception e) { 
            LOGGER.log(Level.SEVERE, "Error loading courses.", e);
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error loading courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles action events from the admin views.
     *
     * @param e The ActionEvent.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand(); // Get text from button
        LOGGER.log(Level.INFO, "AdminController received action: {0}", command);
        
        Object source = e.getSource();

        // Navigation from Dashboard
        if (command.equals("Manage Users")) {
            showUserManagement();
        } else if (command.equals("Manage Courses")) {
            showCourseManagement();
        } else if (command.equals("View Profile")) {
            showProfileView();
        } 
        // Back buttons from Panels
        else if (command.equals("Back to Dashboard")) {
            showDashboard();
        } else if (command.equals("Back To Admin Dashboard From Profile")) {
            showDashboard();
        } 
        // Logout
        else if (command.equals("Logout")) {
             handleLogout();
        } 
        else if (command.equals("Add User")) {
            handleAddUserAction();
        } else if (command.equals("Edit Selected User")) {
            handleEditUserAction();
        } else if (command.equals("Delete Selected User")) {
            handleDeleteUserAction();
        } 
        else if (command.equals("Add Course")) {
            handleAddCourseAction();
        } else if (command.equals("Edit Selected Course")) {
            handleEditCourseAction();
        } else if (command.equals("Delete Selected Course")) {
            handleDeleteCourseAction();
        } else if (command.equals("Assign Teacher")) {
            handleAssignTeacherAction();
        } else if (command.equals("Unassign Teacher")) {
            handleUnassignTeacherAction();
        } 
        // Profile Actions
        else if (command.equals("Change Password")) {
            handleChangePasswordAction();
        } 
        else {
             LOGGER.log(Level.WARNING, "Unhandled action command in AdminController: {0}", command);
        }
    }
    
    // --- Action Handlers --- 
    
    /**
     * Handles the "Add User" button click from the User Management Panel.
     * Shows the UserFormDialog in add mode.
     */
    private void handleAddUserAction() {
        LOGGER.info("Showing Add User dialog.");
        UserFormDialog addUserDialog = new UserFormDialog(mainFrame.getFrame()); // Add mode constructor
        // Add listener directly here to call the processing method
        addUserDialog.addSaveListener(e -> processAddUserForm(addUserDialog)); 
        addUserDialog.setVisible(true);
    }
    
    /**
     * Processes the data submitted from the UserFormDialog when adding a new user.
     * Generates a default password, hashes it, saves the user, and shows the password.
     * @param dialog The UserFormDialog instance.
     */
    private void processAddUserForm(UserFormDialog dialog) {
        User userFromForm = dialog.getUserDetails();
        if (userFromForm == null) {
            // Error message already shown by dialog's validation probably
            LOGGER.warning("Attempted to process add user form, but getUserDetails returned null.");
            return; 
        }

        // --- Generate Default Password & Hash --- 
        String defaultPassword = PasswordUtil.generateRandomPassword(10); // Generate 10-char password
        String saltBase64 = PasswordUtil.generateSaltBase64();
        String hashedPasswordBase64 = null;
        try {
            hashedPasswordBase64 = PasswordUtil.hashPassword(defaultPassword, Base64.getDecoder().decode(saltBase64));
            if (hashedPasswordBase64 == null) {
                throw new Exception("Password hashing returned null."); // Should not happen if algorithm exists
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to hash default password during user creation.", ex);
            JOptionPane.showMessageDialog(dialog, "Error creating user: Failed to secure password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // --- Populate User Object --- 
        userFromForm.setPasswordHash(hashedPasswordBase64);
        userFromForm.setSalt(saltBase64);
        // Other fields (FirstName, LastName, Email, Role, Department) are set by dialog.getUserDetails()

        // --- Database Operation --- 
        try {
            // Check for duplicate email before adding
            if (userDAO.getUserByEmail(userFromForm.getEmail()) != null) {
                 JOptionPane.showMessageDialog(dialog, "Error creating user: Email already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                 LOGGER.warning("Add user failed: Email already exists - " + userFromForm.getEmail());
                 return;
            }
            
            if (userDAO.addUser(userFromForm)) {
                LOGGER.log(Level.INFO, "User added successfully: ID {0}, Email {1}", 
                           new Object[]{userFromForm.getUserID(), userFromForm.getEmail()});
                
                // --- Display Success and Default Password --- 
                String successMessage = String.format(
                    "User '%s %s' (%s) created successfully.\n\n" +
                    "Temporary Password: %s\n\n" +
                    "Please instruct the user to change their password upon first login.",
                    userFromForm.getFirstName(), 
                    userFromForm.getLastName(), 
                    userFromForm.getEmail(), 
                    defaultPassword // Show the plain text default password
                );
                JOptionPane.showMessageDialog(mainFrame.getFrame(), // Show relative to main frame, not dialog
                                              successMessage, 
                                              "User Created", 
                                              JOptionPane.INFORMATION_MESSAGE);
                                              
                dialog.dispose(); // Close the form dialog using dispose()
                loadUsers(); // Refresh the user list in the background panel
            } else {
                 LOGGER.warning("Failed to add user to database: Email " + userFromForm.getEmail());
                 JOptionPane.showMessageDialog(dialog, "Error creating user: Could not save to database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error adding user: " + userFromForm.getEmail(), ex);
            JOptionPane.showMessageDialog(dialog, "An unexpected error occurred while creating the user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleEditUserAction() {
        int selectedUserId = userManagementPanel.getSelectedUserId();
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Please select a user to edit.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
         LOGGER.info("Showing Edit User dialog for UserID: " + selectedUserId);
        try {
            User userToEdit = userDAO.getUserById(selectedUserId);
            if (userToEdit == null) {
                 JOptionPane.showMessageDialog(mainFrame.getFrame(), "Could not find user details.", "Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            UserFormDialog editUserDialog = new UserFormDialog(mainFrame.getFrame(), userToEdit);
            editUserDialog.addSaveListener(e -> {
                User updatedUser = editUserDialog.getUserDetails();
                if (updatedUser != null) {
                    try {
                        // --- Check for duplicate email IF it changed ---
                        boolean emailChanged = !updatedUser.getEmail().equalsIgnoreCase(userToEdit.getEmail());
                        if (emailChanged) {
                             User existingUser = userDAO.getUserByEmail(updatedUser.getEmail());
                             if (existingUser != null) {
                                  JOptionPane.showMessageDialog(editUserDialog, "Email address already exists for another user.", "Error", JOptionPane.ERROR_MESSAGE);
                                  LOGGER.warning("Edit user failed: New email already exists - " + updatedUser.getEmail());
                                  return; // Stop processing
                             }
                        }
                        // --- Proceed with update ---
                        if (userDAO.updateUser(updatedUser)) {
                            LOGGER.info("User updated successfully: " + updatedUser.getEmail());
                            JOptionPane.showMessageDialog(editUserDialog, "User updated successfully!");
                            loadUsers(); // Refresh table
                            editUserDialog.dispose();
                        } else {
                             LOGGER.warning("Failed to update user: " + updatedUser.getEmail());
                            JOptionPane.showMessageDialog(editUserDialog, "Failed to update user.");
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error updating user: " + updatedUser.getEmail(), ex);
                        JOptionPane.showMessageDialog(editUserDialog, "Error updating user: " + ex.getMessage());
                    }
                }
            });
            editUserDialog.setVisible(true);

        } catch (Exception ex) {
             LOGGER.log(Level.SEVERE, "Error fetching user for edit: UserID " + selectedUserId, ex);
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error fetching user details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleDeleteUserAction() {
        int selectedUserId = userManagementPanel.getSelectedUserId();
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Please select a user to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(mainFrame.getFrame(),
                "Are you sure you want to delete the selected user (ID: " + selectedUserId + ")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
             LOGGER.info("Attempting to delete UserID: " + selectedUserId);
            try {
                if (userDAO.deleteUser(selectedUserId)) {
                     LOGGER.info("User deleted successfully: UserID " + selectedUserId);
                    JOptionPane.showMessageDialog(mainFrame.getFrame(), "User deleted successfully!");
                    loadUsers(); // Refresh table
                } else {
                     LOGGER.warning("Failed to delete user: UserID " + selectedUserId);
                    JOptionPane.showMessageDialog(mainFrame.getFrame(), "Failed to delete user.");
                }
            } catch (Exception ex) {
                 LOGGER.log(Level.SEVERE, "Error deleting user: UserID " + selectedUserId, ex);
                JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error deleting user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleAddCourseAction() {
        LOGGER.info("Showing Add Course dialog.");
        CourseFormDialog addCourseDialog = new CourseFormDialog(mainFrame.getFrame());
        addCourseDialog.addSaveListener(e -> {
            Course newCourse = addCourseDialog.getCourseDetails();
            if (newCourse != null) {
                try {
                    // --- Check for duplicate course code ---
                    if (courseDAO.getCourseByCode(newCourse.getCourseCode()) != null) {
                         JOptionPane.showMessageDialog(addCourseDialog, "Course Code already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                         LOGGER.warning("Add course failed: Course code already exists - " + newCourse.getCourseCode());
                         return; // Stop processing
                    }
                    // --- Proceed with add ---
                    if (courseDAO.addCourse(newCourse)) {
                         LOGGER.info("Course added successfully: " + newCourse.getCourseCode());
                        JOptionPane.showMessageDialog(addCourseDialog, "Course added successfully!");
                        loadCourses(); // Refresh table
                        addCourseDialog.dispose();
                    } else {
                         LOGGER.warning("Failed to add course: " + newCourse.getCourseCode());
                        JOptionPane.showMessageDialog(addCourseDialog, "Failed to add course. Code might already exist.");
                    }
                } catch (Exception ex) {
                     LOGGER.log(Level.SEVERE, "Error adding course: " + newCourse.getCourseCode(), ex);
                    JOptionPane.showMessageDialog(addCourseDialog, "Error adding course: " + ex.getMessage());
                }
            }
        });
        addCourseDialog.setVisible(true);
    }
    
    private void handleEditCourseAction() {
        int selectedCourseId = courseManagementPanel.getSelectedCourseId();
        if (selectedCourseId == -1) {
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Please select a course to edit.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
         LOGGER.info("Showing Edit Course dialog for CourseID: " + selectedCourseId);
        try {
            Course courseToEdit = courseDAO.getCourseById(selectedCourseId);
            if (courseToEdit == null) {
                 JOptionPane.showMessageDialog(mainFrame.getFrame(), "Could not find course details.", "Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            CourseFormDialog editCourseDialog = new CourseFormDialog(mainFrame.getFrame(), courseToEdit);
            editCourseDialog.addSaveListener(e -> {
                Course updatedCourse = editCourseDialog.getCourseDetails();
                if (updatedCourse != null) {
                    try {
                        // --- Check for duplicate course code IF it changed ---
                        boolean codeChanged = !updatedCourse.getCourseCode().equalsIgnoreCase(courseToEdit.getCourseCode());
                        if (codeChanged) {
                             Course existingCourse = courseDAO.getCourseByCode(updatedCourse.getCourseCode());
                             if (existingCourse != null) {
                                 JOptionPane.showMessageDialog(editCourseDialog, "Course Code already exists for another course.", "Error", JOptionPane.ERROR_MESSAGE);
                                 LOGGER.warning("Edit course failed: New course code already exists - " + updatedCourse.getCourseCode());
                                 return; // Stop processing
                             }
                        }
                        // --- Proceed with update ---
                        if (courseDAO.updateCourse(updatedCourse)) {
                            LOGGER.info("Course updated successfully: " + updatedCourse.getCourseCode());
                            JOptionPane.showMessageDialog(editCourseDialog, "Course updated successfully!");
                            loadCourses(); // Refresh table
                            editCourseDialog.dispose();
                        } else {
                             LOGGER.warning("Failed to update course: " + updatedCourse.getCourseCode());
                            JOptionPane.showMessageDialog(editCourseDialog, "Failed to update course.");
                        }
                    } catch (Exception ex) {
                         LOGGER.log(Level.SEVERE, "Error updating course: " + updatedCourse.getCourseCode(), ex);
                        JOptionPane.showMessageDialog(editCourseDialog, "Error updating course: " + ex.getMessage());
                    }
                }
            });
            editCourseDialog.setVisible(true);

        } catch (Exception ex) {
             LOGGER.log(Level.SEVERE, "Error fetching course for edit: CourseID " + selectedCourseId, ex);
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error fetching course details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleDeleteCourseAction() {
        int selectedCourseId = courseManagementPanel.getSelectedCourseId();
        if (selectedCourseId == -1) {
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Please select a course to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(mainFrame.getFrame(),
                "Are you sure you want to delete the selected course (ID: " + selectedCourseId + ")? This will also unenroll students.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
             LOGGER.info("Attempting to delete CourseID: " + selectedCourseId);
            try {
                if (courseDAO.deleteCourse(selectedCourseId)) {
                     LOGGER.info("Course deleted successfully: CourseID " + selectedCourseId);
                    JOptionPane.showMessageDialog(mainFrame.getFrame(), "Course deleted successfully!");
                    loadCourses(); // Refresh table
                } else {
                     LOGGER.warning("Failed to delete course: CourseID " + selectedCourseId);
                    JOptionPane.showMessageDialog(mainFrame.getFrame(), "Failed to delete course.");
                }
            } catch (Exception ex) {
                 LOGGER.log(Level.SEVERE, "Error deleting course: CourseID " + selectedCourseId, ex);
                JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error deleting course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleAssignTeacherAction() {
        int selectedCourseId = courseManagementPanel.getSelectedCourseId();
        if (selectedCourseId == -1) {
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Please select a course to assign a teacher.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LOGGER.info("Showing Assign Teacher dialog for CourseID: " + selectedCourseId);
        try {
            List<User> availableTeachers = userDAO.getUsersByRole(UserRole.TEACHER);
            if (availableTeachers == null || availableTeachers.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame.getFrame(), "No teachers available to assign.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            AssignTeacherDialog assignDialog = new AssignTeacherDialog(mainFrame.getFrame(), selectedCourseId, availableTeachers);
            assignDialog.addAssignListener(e -> {
                int selectedTeacherId = assignDialog.getSelectedTeacherId();
                if (selectedTeacherId != -1) {
                    try {
                        if (courseDAO.assignTeacher(selectedCourseId, selectedTeacherId)) {
                             LOGGER.info("Teacher (ID:" + selectedTeacherId + ") assigned successfully to CourseID: " + selectedCourseId);
                            JOptionPane.showMessageDialog(assignDialog, "Teacher assigned successfully!");
                            loadCourses(); // Refresh table
                            assignDialog.dispose();
                        } else {
                             LOGGER.warning("Failed to assign teacher (ID:" + selectedTeacherId + ") to CourseID: " + selectedCourseId);
                            JOptionPane.showMessageDialog(assignDialog, "Failed to assign teacher.");
                        }
                    } catch (Exception ex) {
                         LOGGER.log(Level.SEVERE, "Error assigning teacher to CourseID " + selectedCourseId, ex);
                        JOptionPane.showMessageDialog(assignDialog, "Error assigning teacher: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(assignDialog, "No teacher selected.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            assignDialog.setVisible(true);

        } catch (Exception ex) {
             LOGGER.log(Level.SEVERE, "Error fetching teachers for assignment to CourseID " + selectedCourseId, ex);
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error fetching available teachers: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleUnassignTeacherAction() {
        int selectedCourseId = courseManagementPanel.getSelectedCourseId();
        if (selectedCourseId == -1) {
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Please select a course to unassign its teacher.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(mainFrame.getFrame(),
                "Are you sure you want to unassign the teacher from the selected course (ID: " + selectedCourseId + ")?",
                "Confirm Unassignment", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
             LOGGER.info("Attempting to unassign teacher from CourseID: " + selectedCourseId);
            try {
                if (courseDAO.unassignTeacher(selectedCourseId)) {
                     LOGGER.info("Teacher unassigned successfully from CourseID " + selectedCourseId);
                    JOptionPane.showMessageDialog(mainFrame.getFrame(), "Teacher unassigned successfully!");
                    loadCourses(); // Refresh table
                } else {
                     LOGGER.warning("Failed to unassign teacher from CourseID " + selectedCourseId);
                    JOptionPane.showMessageDialog(mainFrame.getFrame(), "Failed to unassign teacher.");
                }
            } catch (Exception ex) {
                 LOGGER.log(Level.SEVERE, "Error unassigning teacher from CourseID " + selectedCourseId, ex);
                JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error unassigning teacher: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleChangePasswordAction() {
        User currentUser = appController.getCurrentUser();
        if (currentUser == null) {
            LOGGER.warning("Change Password action failed: No user logged in.");
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Session error. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            handleLogout(); 
            return;
        }

        LOGGER.info("Showing Change Password dialog for UserID: " + currentUser.getUserID());
        ChangePasswordDialog dialog = new ChangePasswordDialog(mainFrame.getFrame());

        dialog.addSaveListener(e -> {
            char[] currentPasswordChars = dialog.getCurrentPassword();
            char[] newPasswordChars = dialog.getNewPassword();
            char[] confirmPasswordChars = dialog.getConfirmPassword();

            String currentPassword = new String(currentPasswordChars);
            String newPassword = new String(newPasswordChars);
            String confirmPassword = new String(confirmPasswordChars);

            // --- Validation --- 
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                dialog.displayError("All password fields must be filled.");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                 dialog.displayError("New password and confirmation do not match.");
                 return;
             }
             if (newPassword.length() < 8) { 
                 dialog.displayError("New password must be at least 8 characters long.");
                 return;
             }
            
            try {
                // Fetch user again to get current hash and salt for verification
                User userToVerify = userDAO.getUserById(currentUser.getUserID());
                if (userToVerify == null) {
                    throw new Exception("Could not retrieve current user data.");
                }
                
                // Verify current password
                boolean currentPasswordMatches = PasswordUtil.checkPassword(currentPassword, 
                                                                            userToVerify.getPasswordHash(), 
                                                                            userToVerify.getSalt());
                if (!currentPasswordMatches) {
                    dialog.displayError("Incorrect current password.");
                    return;
                }

                // --- Update Password --- 
                if (userDAO.updatePassword(currentUser.getUserID(), newPassword)) {
                    LOGGER.info("Password updated successfully for UserID: " + currentUser.getUserID());
                    JOptionPane.showMessageDialog(dialog, "Password changed successfully!");
                    dialog.closeDialog();
                } else {
                     LOGGER.warning("Password update failed via DAO for UserID: " + currentUser.getUserID());
                    dialog.displayError("Failed to update password. Please try again.");
                }

            } catch (Exception ex) {
                 LOGGER.log(Level.SEVERE, "Error changing password for UserID: " + currentUser.getUserID(), ex);
                 dialog.displayError("An error occurred: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private void handleUploadPictureAction() {
        LOGGER.info("Upload Picture action triggered (Implementation Pending).");
        JOptionPane.showMessageDialog(mainFrame.getFrame(), "Upload Picture functionality is not yet implemented.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Handles the logout action.
     */
    private void handleLogout() {
        LOGGER.info("Logout action triggered from Admin module.");
        appController.logout(); // Call the main logout method in AppController
    }
} 