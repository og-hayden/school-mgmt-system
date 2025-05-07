package com.school.management.controller.teacher;

import com.school.management.controller.AppController;
import com.school.management.model.dao.CourseDAO;
import com.school.management.model.dao.EnrollmentDAO;
import com.school.management.model.dao.UserDAO;
import com.school.management.model.entities.Course;
import com.school.management.model.entities.Message;
import com.school.management.model.entities.User;
import com.school.management.service.CourseService;
import com.school.management.service.MessageService;
import com.school.management.service.UserService;
import com.school.management.util.UserSession;
import com.school.management.util.security.PasswordUtil;
import com.school.management.view.common.ChangePasswordDialog;
import com.school.management.view.common.MainApplicationFrame;
import com.school.management.view.common.ReplyMessageDialog;
import com.school.management.view.common.UserProfilePanel;
import com.school.management.view.teacher.CourseStudentsPanel;
import com.school.management.view.teacher.TeacherDashboardView;
import com.school.management.view.teacher.TeacherMessagesPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Controller for the Teacher module.
 * Manages TeacherDashboardView and CourseStudentsPanel interactions.
 */
public class TeacherController implements ActionListener, ListSelectionListener {

    private static final Logger LOGGER = Logger.getLogger(TeacherController.class.getName());

    private final AppController appController;
    private final MainApplicationFrame mainFrame;
    private final UserService userService;
    private final CourseService courseService;
    private final MessageService messageService;
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final UserSession userSession;

    // Teacher views managed by this controller
    private final TeacherDashboardView dashboardView;
    private final CourseStudentsPanel courseStudentsPanel;
    private final TeacherMessagesPanel messagesPanel;
    private final UserProfilePanel userProfilePanel;

    // Define card names for the teacher section within the main frame
    public static final String TEACHER_DASHBOARD = "TeacherDashboardCard";
    public static final String COURSE_STUDENTS = "CourseStudentsCard";
    public static final String TEACHER_MESSAGES = "TeacherMessagesCard";
    public static final String TEACHER_PROFILE = "TeacherProfileCard";

    public TeacherController(AppController appController, MainApplicationFrame mainFrame,
                             UserService userService, CourseService courseService, MessageService messageService,
                             UserDAO userDAO, CourseDAO courseDAO, EnrollmentDAO enrollmentDAO,
                             UserSession userSession) {
        this.appController = appController;
        this.mainFrame = mainFrame;
        this.userService = userService;
        this.courseService = courseService;
        this.messageService = messageService;
        this.userDAO = userDAO;
        this.courseDAO = courseDAO;
        this.enrollmentDAO = enrollmentDAO;
        this.userSession = userSession;

        // Initialize the views this controller manages
        this.dashboardView = new TeacherDashboardView();
        this.courseStudentsPanel = new CourseStudentsPanel();
        this.messagesPanel = new TeacherMessagesPanel();
        this.userProfilePanel = new UserProfilePanel();

        initializeViews();
    }

    /**
     * Adds teacher views to the main frame and sets up listeners.
     */
    public void initializeViews() {
        LOGGER.info("Initializing Teacher views and adding to main frame.");
        mainFrame.addView(dashboardView.getPanel(), TEACHER_DASHBOARD);
        mainFrame.addView(courseStudentsPanel.getPanel(), COURSE_STUDENTS);
        mainFrame.addView(messagesPanel.getPanel(), TEACHER_MESSAGES);
        mainFrame.addView(userProfilePanel.getPanel(), TEACHER_PROFILE);

        // Attach listeners to view components
        attachListeners();
    }

    private void attachListeners() {
        // Dashboard Listeners
        dashboardView.addViewStudentsListener(this);
        dashboardView.addMessagesListener(this);
        dashboardView.addProfileListener(this);
        dashboardView.addLogoutListener(this);

        // Course Students Panel Listeners
        courseStudentsPanel.addBackListener(this);

        // Messages Panel Listeners
        messagesPanel.addMarkReadListener(this);
        messagesPanel.addReplyListener(this);
        messagesPanel.addBackListener(this);
        messagesPanel.getMessageTable().getSelectionModel().addListSelectionListener(this);

        // Profile Panel Listeners
        userProfilePanel.addChangePasswordListener(this);
        userProfilePanel.addUploadPictureListener(this);
        userProfilePanel.addBackListener(this);
    }

    /**
     * Loads data for the current teacher and shows the dashboard.
     */
    public void showDashboard() {
        LOGGER.info("Showing Teacher Dashboard.");
        loadAssignedCourses();
        mainFrame.showView(TEACHER_DASHBOARD);
    }

    /**
     * Loads the courses assigned to the currently logged-in teacher.
     */
    private void loadAssignedCourses() {
        User currentUser = userSession.getCurrentUser();
        if (currentUser == null) {
            LOGGER.severe("Cannot load assigned courses: No user logged in.");
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Session error. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            appController.logout(); // Force logout on session error
            return;
        }
        int teacherId = currentUser.getUserID();
        LOGGER.log(Level.FINE, "Loading assigned courses for TeacherID: {0}", teacherId);
        try {
            List<Course> assignedCourses = courseDAO.getCoursesByTeacher(teacherId);
            dashboardView.displayAssignedCourses(assignedCourses);
            LOGGER.log(Level.FINE, "Displayed {0} assigned courses.", assignedCourses.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading assigned courses for TeacherID: " + teacherId, e);
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error loading assigned courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads and displays the students enrolled in the selected course.
     */
    private void showCourseStudents() {
        int selectedCourseId = dashboardView.getSelectedCourseId();
        if (selectedCourseId == -1) {
            LOGGER.warning("View Students clicked but no course selected.");
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Please select a course first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LOGGER.log(Level.INFO, "Showing students for CourseID: {0}", selectedCourseId);

        try {
            // Fetch course details (needed for the panel title)
            Course selectedCourse = courseDAO.getCourseById(selectedCourseId);
            if (selectedCourse == null) {
                 LOGGER.log(Level.SEVERE, "Could not find details for selected CourseID: {0}", selectedCourseId);
                 JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error: Could not find course details.", "Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            
            // Fetch enrolled students (User objects)
            List<User> enrolledStudents = enrollmentDAO.getStudentsByCourse(selectedCourseId);
            
            // Display students in the panel
            courseStudentsPanel.displayStudents(selectedCourse, enrolledStudents);
            LOGGER.log(Level.FINE, "Displayed {0} students for CourseID: {1}", new Object[]{enrolledStudents.size(), selectedCourseId});
            
            // Switch view
            mainFrame.showView(COURSE_STUDENTS);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading students for CourseID: " + selectedCourseId, e);
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error loading enrolled students: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Message Handling Methods ---

    /**
     * Fetches and displays messages for the current teacher.
     */
    private void showMessagesView() {
        User currentUser = userSession.getCurrentUser();
        if (currentUser == null) {
            handleSessionError("Cannot show messages: No user logged in.");
            return;
        }
        int teacherId = currentUser.getUserID();
        LOGGER.log(Level.INFO, "Loading messages for TeacherID: {0}", teacherId);

        try {
            // Fetch messages for the teacher
            List<Message> messages = messageService.getMessagesForRecipient(teacherId);

            // Fetch sender names (assuming UserService has a method like getUsersByIds)
            List<Integer> senderIds = messages.stream()
                                              .map(Message::getSenderUserID)
                                              .distinct()
                                              .collect(Collectors.toList());
            Map<Integer, String> senderNames = userService.getUserDisplayNames(senderIds);

            // Fetch course names (assuming CourseService has a method like getCourseNamesByIds)
            List<Integer> courseIds = messages.stream()
                                              .filter(m -> m.getCourseContextID() != null)
                                              .map(Message::getCourseContextID)
                                              .distinct()
                                              .collect(Collectors.toList());
             Map<Integer, String> courseNames = courseService.getCourseNames(courseIds);

            // Display messages in the panel
            messagesPanel.displayMessages(messages, senderNames, courseNames);
            LOGGER.log(Level.FINE, "Displayed {0} messages for TeacherID: {1}", new Object[]{messages.size(), teacherId});

            // Switch view
            mainFrame.showView(TEACHER_MESSAGES);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading messages for TeacherID: " + teacherId, e);
            showErrorDialog("Error loading messages: " + e.getMessage());
        }
    }

     /**
      * Handles the selection of a message in the table. Fetches and displays the message body.
      * This method is called by the ListSelectionListener.
      */
     private void handleMessageSelection() {
         int selectedMessageId = messagesPanel.getSelectedMessageId();
         if (selectedMessageId == -1) {
             messagesPanel.displayMessageBody(""); 
             return;
         }
         LOGGER.log(Level.FINE, "Message selected, ID: {0}", selectedMessageId);

         // Fetch the message body
         try {
             Message message = messageService.getMessageById(selectedMessageId);
             if (message != null) {
                 messagesPanel.displayMessageBody(message.getBody());
             } else {
                  LOGGER.log(Level.WARNING, "Selected message with ID {0} not found.", selectedMessageId);
                  messagesPanel.displayMessageBody("Error: Could not load message content.");
             }
         } catch (Exception e) {
              LOGGER.log(Level.SEVERE, "Error fetching body for MessageID: " + selectedMessageId, e);
              messagesPanel.displayMessageBody("Error loading message content: " + e.getMessage());
         }
     }

     /**
      * Handles the "Mark as Read" button click.
      */
     private void handleMarkAsRead() {
          int selectedMessageId = messagesPanel.getSelectedMessageId();
          if (selectedMessageId == -1) {
              LOGGER.warning("Mark Read clicked but no message selected.");
              return; // Should not happen if button is disabled correctly, but check anyway.
          }
          LOGGER.log(Level.INFO, "Marking MessageID: {0} as read.", selectedMessageId);

          try {
              messageService.markMessageAsRead(selectedMessageId);
              messagesPanel.markMessageAsReadInTable(selectedMessageId); // Update UI immediately
              LOGGER.log(Level.FINE, "MessageID: {0} marked as read successfully.", selectedMessageId);
          } catch (Exception e) {
              LOGGER.log(Level.SEVERE, "Error marking message as read, ID: " + selectedMessageId, e);
              showErrorDialog("Error updating message status: " + e.getMessage());
          }
     }

    /**
     * Handles the "Reply" button click.
     * Fetches the original message and sender, then shows the ReplyMessageDialog.
     */
    private void handleReplyAction() {
        int selectedMessageId = messagesPanel.getSelectedMessageId();
        if (selectedMessageId == -1) {
            showErrorDialog("Please select a message to reply to.");
            return;
        }
        LOGGER.log(Level.INFO, "Reply action triggered for MessageID: {0}", selectedMessageId);

        User currentTeacher = userSession.getCurrentUser();
        if (currentTeacher == null) {
            handleSessionError("reply to message");
            return;
        }

        try {
            // 1. Fetch the original message
            Message originalMessage = messageService.getMessageById(selectedMessageId);
            if (originalMessage == null) {
                LOGGER.log(Level.WARNING, "Reply failed: Original MessageID {0} not found.", selectedMessageId);
                showErrorDialog("Could not find the selected message to reply to.");
                return;
            }

            // 2. Fetch the original sender (the student)
            User student = userDAO.getUserById(originalMessage.getSenderUserID());
            if (student == null) {
                LOGGER.log(Level.SEVERE, "Reply failed: Original sender UserID {0} not found for MessageID {1}", 
                           new Object[]{originalMessage.getSenderUserID(), selectedMessageId});
                showErrorDialog("Could not find the details of the sender.");
                return;
            }
            
            // 3. Create and show ReplyMessageDialog
            ReplyMessageDialog replyDialog = new ReplyMessageDialog(mainFrame.getFrame(), student, originalMessage);

            // 4. Add listener to dialog's send button
            replyDialog.addSendListener(sendEvent -> handleReplyDialogSendAction(replyDialog, originalMessage));

            replyDialog.setVisible(true); // Show the dialog

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error preparing reply dialog for MessageID: " + selectedMessageId, e);
            showErrorDialog("An error occurred while preparing the reply: " + e.getMessage());
        }
    }

    /**
     * Handles sending the message from the ReplyMessageDialog.
     * @param dialog The reply dialog instance.
     * @param originalMessage The message being replied to.
     */
    private void handleReplyDialogSendAction(ReplyMessageDialog dialog, Message originalMessage) {
        User currentTeacher = userSession.getCurrentUser();
        if (currentTeacher == null) {
            handleSessionError("send reply");
            dialog.closeDialog();
            return;
        }

        // Basic Validation
        String subject = dialog.getSubject();
        String body = dialog.getBody();
        if (subject.isEmpty()) {
            dialog.displayError("Subject", "Subject cannot be empty.");
            return;
        }
        if (body.isEmpty()) {
            dialog.displayError("Message Body", "Message body cannot be empty.");
            return;
        }

        int senderId = currentTeacher.getUserID();
        int recipientId = dialog.getRecipientUserId(); // Student's ID from dialog
        Integer courseId = dialog.getCourseContextId(); // Course context from original message

        // Create the new message object (reply)
        Message replyMessage = new Message(0, senderId, recipientId, courseId, subject, body, null, false);

        LOGGER.log(Level.INFO, "Attempting to send reply from TeacherID {0} to UserID {1} regarding CourseID {2}", 
                     new Object[]{senderId, recipientId, courseId});

        try {
            // Call the service to send/add the message
            messageService.sendMessage(replyMessage);
            LOGGER.log(Level.INFO, "Reply sent successfully.");
            JOptionPane.showMessageDialog(dialog, "Reply sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.closeDialog(); // Close dialog on success
        
             
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending reply", e);
            JOptionPane.showMessageDialog(dialog, "Failed to send reply: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            // Keep dialog open on error
        }
    }

    // --- Profile Handling ---
    private void showProfileView() {
        User currentUser = appController.getCurrentUser(); 
        if (currentUser == null) {
           handleSessionError("show profile");
           return;
        }
        LOGGER.info("Showing Profile for UserID: " + currentUser.getUserID());
        userProfilePanel.setUserProfile(currentUser);
        // Set the correct back action command before showing
        userProfilePanel.getBackButton().setActionCommand("Back To Teacher Dashboard From Profile");
        mainFrame.showView(TEACHER_PROFILE);
    }

    private void handleChangePasswordAction() {
        User currentUser = appController.getCurrentUser();
        if (currentUser == null) {
            LOGGER.warning("Change Password action failed: No user logged in.");
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Session error. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            appController.logout(); // Use app controller logout
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

            // Validation (same as AdminController)
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
                // Fetch user again for verification
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

                // Update Password via DAO
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
        LOGGER.info("Teacher Upload Picture action triggered (Implementation Pending).");
        JOptionPane.showMessageDialog(mainFrame.getFrame(), "Upload Picture functionality is not yet implemented.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- Utility Methods ---

    private void handleSessionError(String logMessage) {
        LOGGER.severe(logMessage);
        JOptionPane.showMessageDialog(mainFrame.getFrame(), "Session error. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
        appController.logout(); // Force logout on session error
    }

    private void showErrorDialog(String message) {
         JOptionPane.showMessageDialog(mainFrame.getFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Handles action events from the teacher views.
     *
     * @param e The ActionEvent.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        LOGGER.log(Level.INFO, "TeacherController received action: {0}", command);

        // Dashboard actions
        if (command.equals("View Students")) { // From TeacherDashboardView
            showCourseStudents();
        } else if (command.equals("View Messages")) { // From TeacherDashboardView
            showMessagesView();
        } else if (command.equals("View Profile")) { // From TeacherDashboardView
            showProfileView();
        } else if (command.equals("Logout")) { // From TeacherDashboardView
            appController.logout();
        } 
        // Messages panel actions
        else if (command.equals("Mark Read")) { // From TeacherMessagesPanel
            handleMarkAsRead();
        } 
        else if (command.equals("Reply To Message")) { // From TeacherMessagesPanel
            handleReplyAction();
        } 
        // Back buttons
        else if (command.equals("Back To Teacher Dashboard") || command.equals("Back to Dashboard")) { // Generic back from CourseStudentsPanel
            showDashboard();
        } else if (command.equals("Back To Teacher Dashboard From Profile")) { // Specific from profile panel
             showDashboard();
        } else if (command.equals("Back To Teacher Dashboard From Messages")) { // Specific back from messages panel (Re-added)
             showDashboard();
        } 
        // Profile actions
        else if (command.equals("Change Password")) {
            handleChangePasswordAction();
        } 
        else {
            LOGGER.log(Level.WARNING, "Unhandled action command in TeacherController: {0}", command);
        }
    }

     /**
      * Handles selection changes in the message list (from ListSelectionListener).
      *
      * @param e the event that characterizes the change.
      */
     @Override
     public void valueChanged(ListSelectionEvent e) {
         if (!e.getValueIsAdjusting() && e.getSource() == messagesPanel.getMessageTable().getSelectionModel()) {
             handleMessageSelection();
         }
     }
} 