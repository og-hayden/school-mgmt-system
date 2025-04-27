package com.school.management.util;

import com.school.management.model.dao.CourseDAO;
import com.school.management.model.dao.EnrollmentDAO;
import com.school.management.model.dao.MessageDAO;
import com.school.management.model.dao.UserDAO;
import com.school.management.model.entities.*;
import com.school.management.util.database.DatabaseConnection;
import com.school.management.util.security.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to set up initial test data in the database.
 * WARNING: Running the main method will clear existing data in relevant tables.
 */
public class TestDataSetup {

    private static final Logger LOGGER = Logger.getLogger(TestDataSetup.class.getName());

    // Define test user credentials here for clarity
    private static final Map<String, String> testUsersCredentials = new HashMap<>();
    static {
        testUsersCredentials.put("admin@example.com", "adminpass");
        testUsersCredentials.put("teacher@example.com", "teacherpass");
        testUsersCredentials.put("student1@example.com", "student1pass");
        testUsersCredentials.put("student2@example.com", "student2pass");
    }

    public static void main(String[] args) {
        LOGGER.info("Starting test data setup...");

        // DAOs needed for setup
        UserDAO userDAO = new UserDAO();
        CourseDAO courseDAO = new CourseDAO();
        EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
        MessageDAO messageDAO = new MessageDAO();

        try {
            // 1. Clear existing data (in reverse order of dependency)
            clearData();

            // 2. Create Users
            LOGGER.info("Creating test users...");
            User admin = createUser(userDAO, "Admin", "User", "admin@example.com", testUsersCredentials.get("admin@example.com"), UserRole.ADMINISTRATOR, null);
            User teacher = createUser(userDAO, "Teacher", "User", "teacher@example.com", testUsersCredentials.get("teacher@example.com"), UserRole.TEACHER, "Computer Science");
            User student1 = createUser(userDAO, "Student", "One", "student1@example.com", testUsersCredentials.get("student1@example.com"), UserRole.STUDENT, null);
            User student2 = createUser(userDAO, "Student", "Two", "student2@example.com", testUsersCredentials.get("student2@example.com"), UserRole.STUDENT, null);
            
            if (admin == null || teacher == null || student1 == null || student2 == null) {
                 throw new RuntimeException("Failed to create one or more essential test users.");
            }
            LOGGER.info("Test users created successfully.");

            // 3. Create Courses
            LOGGER.info("Creating test courses...");
            Course courseCS101 = createCourse(courseDAO, "CS101", "Introduction to Programming", 30, teacher.getUserID());
            Course courseMA201 = createCourse(courseDAO, "MA201", "Calculus I", 2, teacher.getUserID()); // Low capacity for testing
            Course coursePH100 = createCourse(courseDAO, "PH100", "Basic Physics", 25, null); // No teacher assigned initially
            
             if (courseCS101 == null || courseMA201 == null || coursePH100 == null) {
                 LOGGER.warning("Failed to create one or more test courses.");
             } else {
                  LOGGER.info("Test courses created successfully.");
             }

            // 4. Create Enrollments
            LOGGER.info("Creating test enrollments...");
            if (courseCS101 != null) {
                enrollStudent(enrollmentDAO, student1.getUserID(), courseCS101.getCourseID());
                enrollStudent(enrollmentDAO, student2.getUserID(), courseCS101.getCourseID());
            }
             if (courseMA201 != null) {
                 enrollStudent(enrollmentDAO, student1.getUserID(), courseMA201.getCourseID());
                 // Attempt to enroll student2 in the full course (MA201 capacity 2)
                 enrollStudent(enrollmentDAO, student2.getUserID(), courseMA201.getCourseID()); // This should fail due to capacity
             }
            LOGGER.info("Test enrollments created (or attempted).");
            
            // 5. Create Messages
            LOGGER.info("Creating test messages...");
            if (courseCS101 != null) {
                 createMessage(messageDAO, student1.getUserID(), teacher.getUserID(), courseCS101.getCourseID(), "Question about CS101", "What is the deadline for assignment 1?", false);
                 createMessage(messageDAO, teacher.getUserID(), student1.getUserID(), courseCS101.getCourseID(), "Re: Question about CS101", "The deadline is next Friday.", true); // Marked as read
            }
            createMessage(messageDAO, student2.getUserID(), admin.getUserID(), null, "Account issue", "I cannot access the library portal.", false);
             LOGGER.info("Test messages created.");
            
            // --- Setup Finished --- 
            LOGGER.info("Test data setup finished successfully!");
            System.out.println("\n--- Test User Credentials ---");
            testUsersCredentials.forEach((email, password) -> 
                System.out.println("Email: " + email + ", Password: " + password)
            );
            System.out.println("-----------------------------");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Test data setup failed.", e);
        }
    }

    /**
     * Clears data from tables in the correct order.
     */
    private static void clearData() {
        LOGGER.info("Clearing existing data...");
        String[] tablesToClear = {"Messages", "Enrollments", "Courses", "Users"};
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            // Disable FK checks temporarily for easier deletion, re-enable afterwards
            stmt.execute("SET FOREIGN_KEY_CHECKS=0;");
            LOGGER.info("Disabled foreign key checks.");
            for (String tableName : tablesToClear) {
                String sql = "DELETE FROM " + tableName;
                int deletedRows = stmt.executeUpdate(sql);
                LOGGER.log(Level.INFO, "Deleted {0} rows from {1}", new Object[]{deletedRows, tableName});
            }
            stmt.execute("SET FOREIGN_KEY_CHECKS=1;");
             LOGGER.info("Re-enabled foreign key checks.");
             LOGGER.info("Data cleared successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error clearing data.", e);
            throw new RuntimeException("Failed to clear database data.", e);
        } finally {
             DatabaseConnection.closeResources(conn, stmt);
        }
    }

    /**
     * Helper to create a user.
     */
    private static User createUser(UserDAO dao, String firstName, String lastName, String email, String password, UserRole role, String department) {
        byte[] saltBytes = PasswordUtil.generateSalt();
        String hashedPassword = PasswordUtil.hashPassword(password, saltBytes);
        if (hashedPassword == null) {
             LOGGER.log(Level.SEVERE, "Failed to hash password for user {0}", email);
             return null;
        }
        String saltString = Base64.getEncoder().encodeToString(saltBytes);
        
        User user = new User(0, firstName, lastName, email, hashedPassword, saltString, role, department, null, null, null, null, null);
        if (dao.addUser(user)) {
             LOGGER.log(Level.INFO, "   Created User: {0} ({1}), ID: {2}", new Object[]{email, role, user.getUserID()});
             return user;
        } else {
             LOGGER.log(Level.WARNING, "   Failed to create user: {0}", email);
            return null;
        }
    }

    /**
     * Helper to create a course.
     */
    private static Course createCourse(CourseDAO dao, String code, String name, int capacity, Integer teacherId) {
        Course course = new Course(0, code, name, capacity, teacherId, null, null);
         if (dao.addCourse(course)) {
             LOGGER.log(Level.INFO, "   Created Course: {0} ({1}), ID: {2}", new Object[]{code, name, course.getCourseID()});
             return course;
         } else {
             LOGGER.log(Level.WARNING, "   Failed to create course: {0}", code);
             return null;
         }
    }
    
    /**
     * Helper to enroll a student.
     */
    private static boolean enrollStudent(EnrollmentDAO dao, int studentId, int courseId) {
        Enrollment enrollment = new Enrollment(0, studentId, courseId, null);
         // We use addEnrollment which includes checks (like capacity)
         int enrollmentId = dao.addEnrollment(enrollment); // Get the returned ID (or -1)
         boolean success = (enrollmentId > 0); // Success if ID is positive
         
         if (success) {
              LOGGER.log(Level.INFO, "   Enrolled Student ID {0} in Course ID {1} (EnrollmentID: {2})", new Object[]{studentId, courseId, enrollmentId});
         } else {
             // This might be expected (e.g., course full), so log as warning/info
             LOGGER.log(Level.INFO, "   Enrollment failed or student already enrolled: Student ID {0} in Course ID {1}", new Object[]{studentId, courseId});
         }
         return success;
    }
    
    /**
     * Helper to create a message.
     */
     private static boolean createMessage(MessageDAO dao, int senderId, int recipientId, Integer courseId, String subject, String body, boolean isRead) {
         Message message = new Message(0, senderId, recipientId, courseId, subject, body, null, isRead);
         boolean success = dao.addMessage(message);
          if (success) {
              LOGGER.log(Level.INFO, "   Created Message from {0} to {1} (Subject: {2}) ID: {3}", new Object[]{senderId, recipientId, subject, message.getMessageID()});
         } else {
              LOGGER.log(Level.WARNING, "   Failed to create message from {0} to {1} (Subject: {2})", new Object[]{senderId, recipientId, subject});
         }
         return success;
     }
} 