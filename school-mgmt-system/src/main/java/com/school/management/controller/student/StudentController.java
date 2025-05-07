package com.school.management.controller.student;

import com.school.management.controller.AppController;
import com.school.management.model.dao.CourseDAO;
import com.school.management.model.dao.EnrollmentDAO;
import com.school.management.model.dao.UserDAO;
import com.school.management.model.entities.Course;
import com.school.management.model.entities.Enrollment;
import com.school.management.model.entities.Message;
import com.school.management.model.entities.User;
import com.school.management.service.MessageService;
import com.school.management.util.UserSession;
import com.school.management.view.common.MainApplicationFrame;
import com.school.management.view.common.SendMessageDialog;
import com.school.management.view.student.AvailableCoursesPanel;
import com.school.management.view.student.EnrolledCoursesPanel;
import com.school.management.view.student.StudentDashboardView;
import com.school.management.view.common.UserProfilePanel;
import com.school.management.view.common.ChangePasswordDialog;
import com.school.management.util.security.PasswordUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Student module.
 * Manages StudentDashboardView, AvailableCoursesPanel, and EnrolledCoursesPanel interactions.
 */
public class StudentController implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(StudentController.class.getName());

    private final AppController appController;
    private final MainApplicationFrame mainFrame;
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final MessageService messageService;
    private final UserSession userSession;

    // Student views managed by this controller
    private final StudentDashboardView dashboardView;
    private final AvailableCoursesPanel availableCoursesPanel;
    private final EnrolledCoursesPanel enrolledCoursesPanel;
    private final UserProfilePanel userProfilePanel;

    // Define card names for the student section within the main frame
    public static final String STUDENT_DASHBOARD = "StudentDashboardCard";
    public static final String AVAILABLE_COURSES = "AvailableCoursesCard";
    public static final String ENROLLED_COURSES = "EnrolledCoursesCard";
    public static final String STUDENT_PROFILE = "StudentProfileCard";

    public StudentController(AppController appController, MainApplicationFrame mainFrame,
                             UserDAO userDAO, CourseDAO courseDAO, EnrollmentDAO enrollmentDAO,
                             MessageService messageService,
                             UserSession userSession) {
        this.appController = appController;
        this.mainFrame = mainFrame;
        this.userDAO = userDAO;
        this.courseDAO = courseDAO;
        this.enrollmentDAO = enrollmentDAO;
        this.messageService = messageService;
        this.userSession = userSession;

        // Initialize the views this controller manages
        this.dashboardView = new StudentDashboardView();
        this.availableCoursesPanel = new AvailableCoursesPanel();
        this.enrolledCoursesPanel = new EnrolledCoursesPanel();
        this.userProfilePanel = new UserProfilePanel();

        initializeViews();
    }

    /**
     * Adds student views to the main frame and sets up listeners.
     */
    public void initializeViews() {
        LOGGER.info("Initializing Student views and adding to main frame.");
        mainFrame.addView(dashboardView.getPanel(), STUDENT_DASHBOARD);
        mainFrame.addView(availableCoursesPanel.getPanel(), AVAILABLE_COURSES);
        mainFrame.addView(enrolledCoursesPanel.getPanel(), ENROLLED_COURSES);
        mainFrame.addView(userProfilePanel.getPanel(), STUDENT_PROFILE);

        // Attach listeners to view components
        attachListeners();
    }

    private void attachListeners() {
        // Dashboard Listeners
        dashboardView.addAvailableCoursesListener(this);
        dashboardView.addEnrolledCoursesListener(this);
        dashboardView.addProfileListener(this);
        dashboardView.addLogoutListener(this);

        // Available Courses Panel Listeners
        availableCoursesPanel.addEnrollListener(this);
        availableCoursesPanel.addBackListener(this);

        // Enrolled Courses Panel Listeners
        enrolledCoursesPanel.addSendMessageListener(this);
        enrolledCoursesPanel.addBackListener(this);

        // Profile Panel Listeners
        userProfilePanel.addChangePasswordListener(this);
        userProfilePanel.addUploadPictureListener(this);
        userProfilePanel.addBackListener(this);
    }

    /**
     * Shows the main Student Dashboard view.
     */
    public void showDashboard() {
        LOGGER.info("Showing Student Dashboard.");
        mainFrame.showView(STUDENT_DASHBOARD);
    }

    /**
     * Loads data and shows the Available Courses panel.
     */
    public void showAvailableCourses() {
        LOGGER.info("Showing Available Courses Panel.");
        loadAvailableCourses();
        mainFrame.showView(AVAILABLE_COURSES);
    }

    /**
     * Loads data and shows the Enrolled Courses panel.
     */
    public void showEnrolledCourses() {
        LOGGER.info("Showing Enrolled Courses Panel.");
        loadEnrolledCourses();
        mainFrame.showView(ENROLLED_COURSES);
    }

    /**
     * Fetches available courses (not full, not already enrolled in) and updates the panel.
     */
    private void loadAvailableCourses() {
        User currentUser = userSession.getCurrentUser();
        if (currentUser == null) {
            handleSessionError("load available courses");
            return;
        }
        int studentId = currentUser.getUserID();
        LOGGER.log(Level.FINE, "Loading available courses for StudentID: {0}", studentId);
        try {
            List<Course> available = courseDAO.getAvailableCourses(studentId);
            Map<Integer, String> teacherNames = fetchTeacherNames(available);
            Map<Integer, Integer> enrollmentCounts = fetchEnrollmentCounts(available);

            availableCoursesPanel.displayAvailableCourses(available, teacherNames, enrollmentCounts);
            LOGGER.log(Level.FINE, "Displayed {0} available courses.", available.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading available courses for StudentID: " + studentId, e);
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error loading available courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Fetches courses the student is enrolled in and updates the panel.
     */
    private void loadEnrolledCourses() {
        User currentUser = userSession.getCurrentUser();
        if (currentUser == null) {
            handleSessionError("load enrolled courses");
            return;
        }
        int studentId = currentUser.getUserID();
         LOGGER.log(Level.FINE, "Loading enrolled courses for StudentID: {0}", studentId);
        try {
            List<Course> enrolled = enrollmentDAO.getEnrolledCoursesByStudent(studentId);
            Map<Integer, String> teacherNames = fetchTeacherNames(enrolled);

            enrolledCoursesPanel.displayEnrolledCourses(enrolled, teacherNames);
            LOGGER.log(Level.FINE, "Displayed {0} enrolled courses.", enrolled.size());
        } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Error loading enrolled courses for StudentID: " + studentId, e);
            JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error loading enrolled courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
     /**
      * Handles the "Enroll" button action.
      */
     private void handleEnrollAction() {
         User currentUser = userSession.getCurrentUser();
         if (currentUser == null) {
             handleSessionError("enroll in course");
             return;
         }
         int studentId = currentUser.getUserID();
         int selectedCourseId = availableCoursesPanel.getSelectedCourseId();
 
         if (selectedCourseId == -1) {
             JOptionPane.showMessageDialog(mainFrame.getFrame(), "Please select a course to enroll in.", "Info", JOptionPane.INFORMATION_MESSAGE);
             return;
         }
 
         LOGGER.log(Level.INFO, "Attempting enrollment for StudentID {0} into CourseID {1}", new Object[]{studentId, selectedCourseId});
 
         try {
             boolean alreadyEnrolled = enrollmentDAO.isEnrolled(studentId, selectedCourseId);
             Course course = courseDAO.getCourseById(selectedCourseId);
 
             if (alreadyEnrolled) {
                  LOGGER.log(Level.WARNING, "Enrollment failed: StudentID {0} already enrolled in CourseID {1}", new Object[]{studentId, selectedCourseId});
                 JOptionPane.showMessageDialog(mainFrame.getFrame(), "You are already enrolled in this course.", "Enrollment Failed", JOptionPane.WARNING_MESSAGE);
             } else if (course == null) { // Check for null course separately
                  LOGGER.log(Level.SEVERE, "Enrollment failed: CourseID {0} not found.", selectedCourseId);
                  JOptionPane.showMessageDialog(mainFrame.getFrame(), "Could not find the selected course.", "Error", JOptionPane.ERROR_MESSAGE);
             } else { // Course exists, now check capacity
                  int enrollmentCount = courseDAO.getEnrollmentCount(selectedCourseId);
                  boolean isFull = enrollmentCount >= course.getMaximumCapacity();
                  
                  if (isFull) {
                    LOGGER.log(Level.WARNING, "Enrollment failed: CourseID {0} is full.", selectedCourseId);
                    JOptionPane.showMessageDialog(mainFrame.getFrame(), "This course is currently full.", "Enrollment Failed", JOptionPane.WARNING_MESSAGE);
                    loadAvailableCourses(); // Refresh list as capacity might have changed
                  } else {
                      // Attempt enrollment
                      Enrollment enrollment = new Enrollment(0, studentId, selectedCourseId, null); // ID is auto-gen, date is default
                      if (enrollmentDAO.addEnrollment(enrollment) > 0) {
                          LOGGER.log(Level.INFO, "Enrollment successful for StudentID {0} in CourseID {1}", new Object[]{studentId, selectedCourseId});
                          JOptionPane.showMessageDialog(mainFrame.getFrame(), "Successfully enrolled in the course!", "Enrollment Successful", JOptionPane.INFORMATION_MESSAGE);
                          loadAvailableCourses(); // Refresh available courses list
                          
                      } else {
                           LOGGER.log(Level.WARNING, "Enrollment failed for StudentID {0} in CourseID {1} (DAO level)", new Object[]{studentId, selectedCourseId});
                           JOptionPane.showMessageDialog(mainFrame.getFrame(), "Failed to enroll in the course. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                      }
                  }
             }
         } catch (Exception e) {
              LOGGER.log(Level.SEVERE, "Error during enrollment for StudentID " + studentId + " in CourseID " + selectedCourseId, e);
             JOptionPane.showMessageDialog(mainFrame.getFrame(), "An error occurred during enrollment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     /**
      * Handles the "Send Message" button action.
      * Fetches course/teacher details and shows the SendMessageDialog.
      */
     private void handleSendMessageAction() {
          int selectedCourseId = enrolledCoursesPanel.getSelectedCourseId();
          if (selectedCourseId == -1) {
              JOptionPane.showMessageDialog(mainFrame.getFrame(), "Please select a course to send a message.", "Info", JOptionPane.INFORMATION_MESSAGE);
              return;
          }
          
          LOGGER.log(Level.INFO, "Send Message action triggered for CourseID: {0}", selectedCourseId);

          try {
              // 1. Get Course details
              Course course = courseDAO.getCourseById(selectedCourseId);
              if (course == null) {
                  LOGGER.log(Level.WARNING, "Send Message failed: CourseID {0} not found.", selectedCourseId);
                  JOptionPane.showMessageDialog(mainFrame.getFrame(), "Could not find the selected course.", "Error", JOptionPane.ERROR_MESSAGE);
                  return;
              }

              // 2. Check if teacher is assigned
              Integer teacherId = course.getTeacherUserID();
              if (teacherId == null || teacherId <= 0) {
                   LOGGER.log(Level.WARNING, "Send Message failed: No teacher assigned to CourseID {0}", selectedCourseId);
                  JOptionPane.showMessageDialog(mainFrame.getFrame(), "No teacher is currently assigned to this course.", "Info", JOptionPane.INFORMATION_MESSAGE);
                  return;
              }

              // 3. Get Teacher details
              User teacher = userDAO.getUserById(teacherId);
              if (teacher == null) {
                  LOGGER.log(Level.SEVERE, "Send Message failed: Teacher UserID {0} not found for CourseID {1}", new Object[]{teacherId, selectedCourseId});
                  JOptionPane.showMessageDialog(mainFrame.getFrame(), "Could not find the teacher assigned to this course.", "Error", JOptionPane.ERROR_MESSAGE);
                  return;
              }

              // 4. Create and show SendMessageDialog
              SendMessageDialog dialog = new SendMessageDialog(mainFrame.getFrame(), teacher, course);

              // 5. Add listener to dialog's send button
              dialog.addSendListener(sendEvent -> handleDialogSendAction(dialog));

              dialog.setVisible(true); // Show the dialog

          } catch (Exception e) {
              LOGGER.log(Level.SEVERE, "Error preparing to send message for CourseID: " + selectedCourseId, e);
              JOptionPane.showMessageDialog(mainFrame.getFrame(), "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          }
     }

     /**
      * Handles the actual sending of the message when the dialog's Send button is clicked.
      */
     private void handleDialogSendAction(SendMessageDialog dialog) {
         User currentUser = userSession.getCurrentUser();
         if (currentUser == null) {
             handleSessionError("send message");
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

         int senderId = currentUser.getUserID();
         int recipientId = dialog.getRecipientUserId();
         Integer courseId = dialog.getCourseContextId();

         // Create the message object (ID and Timestamp are auto-generated)
         Message message = new Message(0, senderId, recipientId, courseId, subject, body, null, false);

         LOGGER.log(Level.INFO, "Attempting to send message from UserID {0} to UserID {1} regarding CourseID {2}", 
                     new Object[]{senderId, recipientId, courseId});

         try {
             // Call the service to send/add the message
             messageService.sendMessage(message); // Assumes this method exists
             LOGGER.log(Level.INFO, "Message sent successfully.");
             JOptionPane.showMessageDialog(dialog, "Message sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
             dialog.closeDialog(); // Close dialog on success
         } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Error sending message", e);
             JOptionPane.showMessageDialog(dialog, "Failed to send message: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             // Keep dialog open on error for user to retry or copy text
         }
     }

     /**
      * Handles action events from the student views.
      *
      * @param e The ActionEvent.
      */
     @Override
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         LOGGER.log(Level.INFO, "StudentController received action: {0}", command);
 
         switch (command) {
             case "View Available Courses":
                 showAvailableCourses();
                 break;
             case "View Enrolled Courses":
                 showEnrolledCourses();
                 break;
             case "Enroll":
                 handleEnrollAction();
                 break;
             case "Send Message":
                 handleSendMessageAction();
                 break;
             case "View Profile":
                 showProfileView();
                 break;
             case "Back To Student Dashboard":
                 showDashboard();
                 break;
             case "Back To Student Dashboard From Profile":
                 showDashboard();
                 break;
             case "Logout":
                 appController.logout();
                 break;
             case "Change Password":
                 handleChangePasswordAction();
                 break;
             default:
                 LOGGER.log(Level.WARNING, "Unhandled action command in StudentController: {0}", command);
                 break;
         }
     }
     
     // --- Helper Methods ---
 
     private void handleSessionError(String action) {
         LOGGER.log(Level.SEVERE, "User session is invalid or user is null when attempting to {0}. Logging out.", action);
         JOptionPane.showMessageDialog(mainFrame.getFrame(), "Your session has expired or is invalid. Please log in again.", "Session Error", JOptionPane.ERROR_MESSAGE);
         appController.logoutUser();
     }
     
     private Map<Integer, String> fetchTeacherNames(List<Course> courses) throws Exception {
         if (courses == null || courses.isEmpty()) {
             return Map.of(); // Return empty map
         }
         List<Integer> teacherIds = courses.stream()
                                           .map(Course::getTeacherUserID)
                                           .filter(id -> id != null && id > 0)
                                           .distinct()
                                           .collect(Collectors.toList());

         if (teacherIds.isEmpty()) {
             return Map.of();
         }
         return userDAO.getUserNamesByIds(teacherIds);
     }
     
      private Map<Integer, Integer> fetchEnrollmentCounts(List<Course> courses) throws Exception {
          if (courses == null || courses.isEmpty()) {
              return Map.of();
          }
          List<Integer> courseIds = courses.stream().map(Course::getCourseID).collect(Collectors.toList());
          return courseDAO.getEnrollmentCounts(courseIds);
      }

    private void showProfileView() {
        User currentUser = appController.getCurrentUser();
        if (currentUser == null) {
            handleSessionError("show profile");
            return;
        }
        LOGGER.info("Showing Profile for UserID: " + currentUser.getUserID());
        userProfilePanel.setUserProfile(currentUser);
        userProfilePanel.getBackButton().setActionCommand("Back To Student Dashboard From Profile");
        mainFrame.showView(STUDENT_PROFILE);
    }

    private void handleChangePasswordAction() {
        User currentUser = appController.getCurrentUser();
        if (currentUser == null) {
            handleSessionError("change password");
            return;
        }

        ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(mainFrame.getFrame(), true);
        changePasswordDialog.setVisible(true);

        String newPassword = changePasswordDialog.getNewPassword();
        String currentPassword = changePasswordDialog.getCurrentPassword();

        if (newPassword != null && !newPassword.isEmpty() && currentPassword != null && !currentPassword.isEmpty()) {
             LOGGER.info("Attempting password change for UserID: " + currentUser.getUserID());
             User userFromDb = userDAO.getUserById(currentUser.getUserID());
             if (userFromDb == null) {
                 JOptionPane.showMessageDialog(mainFrame.getFrame(), "Error fetching user data.", "Error", JOptionPane.ERROR_MESSAGE);
                 return;
             }
             boolean currentPasswordMatches = PasswordUtil.verifyPassword(currentPassword, userFromDb.getPasswordHash(), userFromDb.getSalt());

            if (currentPasswordMatches) {
                boolean updated = userDAO.updatePassword(currentUser.getUserID(), newPassword);
                if (updated) {
                    JOptionPane.showMessageDialog(mainFrame.getFrame(), "Password updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                     LOGGER.info("Password updated successfully for UserID: " + currentUser.getUserID());
                } else {
                    JOptionPane.showMessageDialog(mainFrame.getFrame(), "Failed to update password.", "Error", JOptionPane.ERROR_MESSAGE);
                     LOGGER.warning("Password update failed for UserID: " + currentUser.getUserID());
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame.getFrame(), "Incorrect current password.", "Error", JOptionPane.ERROR_MESSAGE);
                 LOGGER.warning("Password change attempt failed due to incorrect current password for UserID: " + currentUser.getUserID());
            }
        } else if (newPassword != null) {
             LOGGER.fine("Password change dialog closed without valid input.");
        }
    }

    private void handleUploadPictureAction() {
        LOGGER.info("Upload Picture action triggered (Implementation Pending).");
        JOptionPane.showMessageDialog(mainFrame.getFrame(), "Upload Picture functionality is not yet implemented.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
} 