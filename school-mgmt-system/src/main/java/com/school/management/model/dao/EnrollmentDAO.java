package com.school.management.model.dao;

import com.school.management.model.entities.Course;
import com.school.management.model.entities.Enrollment;
import com.school.management.model.entities.User;
import com.school.management.model.entities.UserRole;
import com.school.management.util.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for the Enrollment entity.
 * Handles operations related to the Enrollments table, managing student-course relationships.
 */
public class EnrollmentDAO {

    private static final Logger LOGGER = Logger.getLogger(EnrollmentDAO.class.getName());

    /**
     * Adds a new enrollment record to the database after checking course capacity.
     *
     * @param enrollment The Enrollment object containing student and course IDs.
     * @return The generated EnrollmentID if successful and capacity allows, -1 otherwise (or if already enrolled).
     */
    public int addEnrollment(Enrollment enrollment) {
        Connection conn = null;
        PreparedStatement checkPstmt = null;
        PreparedStatement insertPstmt = null;
        ResultSet rs = null;
        ResultSet generatedKeys = null;
        int generatedId = -1;

        // SQL to check current enrollment count and max capacity
        String checkSql = "SELECT c.MaximumCapacity, COUNT(e.EnrollmentID) as CurrentEnrollment " +
                        "FROM Courses c LEFT JOIN Enrollments e ON c.CourseID = e.CourseID " +
                        "WHERE c.CourseID = ? GROUP BY c.CourseID";
        
        // SQL to check if the student is already enrolled
        String alreadyEnrolledSql = "SELECT 1 FROM Enrollments WHERE StudentUserID = ? AND CourseID = ? LIMIT 1";

        String insertSql = "INSERT INTO Enrollments (StudentUserID, CourseID) VALUES (?, ?)";

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Check if already enrolled
            checkPstmt = conn.prepareStatement(alreadyEnrolledSql);
            checkPstmt.setInt(1, enrollment.getStudentUserID());
            checkPstmt.setInt(2, enrollment.getCourseID());
            rs = checkPstmt.executeQuery();
            if (rs.next()) {
                LOGGER.log(Level.WARNING, "Student {0} is already enrolled in course {1}. Cannot add enrollment.", 
                           new Object[]{enrollment.getStudentUserID(), enrollment.getCourseID()});
                 conn.rollback(); // Rollback transaction
                 return -1; // Indicate already enrolled
            }
            DatabaseConnection.closeResultSet(rs); // Close previous result set
            DatabaseConnection.closeStatement(checkPstmt); // Close previous statement

            // 2. Check capacity
            checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setInt(1, enrollment.getCourseID());
            rs = checkPstmt.executeQuery();

            if (rs.next()) {
                int maxCapacity = rs.getInt("MaximumCapacity");
                int currentEnrollment = rs.getInt("CurrentEnrollment");

                if (currentEnrollment >= maxCapacity) {
                    LOGGER.log(Level.WARNING, "Course {0} is full (Capacity: {1}, Current: {2}). Cannot add enrollment for student {3}.", 
                               new Object[]{enrollment.getCourseID(), maxCapacity, currentEnrollment, enrollment.getStudentUserID()});
                    conn.rollback(); // Rollback transaction
                    return -1; // Indicate course is full
                }
            } else {
                LOGGER.log(Level.WARNING, "Course {0} not found. Cannot add enrollment.", enrollment.getCourseID());
                conn.rollback(); // Rollback transaction
                return -1; // Indicate course not found
            }
            
            DatabaseConnection.closeResultSet(rs); // Close capacity check result set
            DatabaseConnection.closeStatement(checkPstmt); // Close capacity check statement

            // 3. If checks pass, insert the enrollment
            insertPstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertPstmt.setInt(1, enrollment.getStudentUserID());
            insertPstmt.setInt(2, enrollment.getCourseID());

            int affectedRows = insertPstmt.executeUpdate();

            if (affectedRows > 0) {
                generatedKeys = insertPstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                    enrollment.setEnrollmentID(generatedId); // Set the ID back
                    enrollment.setEnrollmentDate(new Timestamp(System.currentTimeMillis())); // Approximate time
                    conn.commit(); // Commit transaction
                    LOGGER.log(Level.INFO, "Enrollment added successfully with ID: {0} for Student {1} in Course {2}", 
                               new Object[]{generatedId, enrollment.getStudentUserID(), enrollment.getCourseID()});
                } else {
                     LOGGER.log(Level.SEVERE, "Failed to retrieve generated EnrollmentID after insert.");
                     conn.rollback(); // Rollback on failure to get ID
                }
            } else {
                conn.rollback(); // Rollback if insert failed
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding enrollment for student " + enrollment.getStudentUserID() + " in course " + enrollment.getCourseID(), e);
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex); }
            generatedId = -1; // Ensure -1 on error
        } finally {
            // Close all resources used in this complex operation
            DatabaseConnection.closeResultSet(generatedKeys);
            DatabaseConnection.closeResultSet(rs);
             // Check and close both prepared statements
             try { if (checkPstmt != null && !checkPstmt.isClosed()) checkPstmt.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Error closing checkPstmt", e); }
             try { if (insertPstmt != null && !insertPstmt.isClosed()) insertPstmt.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Error closing insertPstmt", e); }
             // Restore auto-commit and close connection
             try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Error restoring auto-commit", e); }
            DatabaseConnection.closeConnection(conn);
        }
        return generatedId;
    }

    /**
     * Deletes an enrollment record based on the student and course IDs.
     *
     * @param studentId The ID of the student.
     * @param courseId The ID of the course.
     * @return true if the enrollment was deleted successfully, false otherwise.
     */
    public boolean deleteEnrollment(int studentId, int courseId) {
        String sql = "DELETE FROM Enrollments WHERE StudentUserID = ? AND CourseID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);

            int affectedRows = pstmt.executeUpdate();
            success = affectedRows > 0;
            if (success) {
                LOGGER.log(Level.INFO, "Enrollment deleted successfully for Student {0}, Course {1}", new Object[]{studentId, courseId});
            } else {
                LOGGER.log(Level.WARNING, "Enrollment deletion failed or enrollment not found for Student {0}, Course {1}", new Object[]{studentId, courseId});
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting enrollment for Student " + studentId + ", Course " + courseId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
        return success;
    }

    /**
     * Retrieves all enrollments for a specific student.
     *
     * @param studentId The UserID of the student.
     * @return A list of Enrollment objects.
     */
    public List<Enrollment> getEnrollmentsByStudent(int studentId) {
        String sql = "SELECT * FROM Enrollments WHERE StudentUserID = ? ORDER BY EnrollmentDate DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Enrollment> enrollments = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving enrollments for student ID: " + studentId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return enrollments;
    }

    /**
     * Retrieves all students (as User objects) enrolled in a specific course.
     *
     * @param courseId The ID of the course.
     * @return A list of User objects representing the enrolled students.
     */
    public List<User> getStudentsByCourse(int courseId) {
        // Join Enrollments and Users tables
        String sql = "SELECT u.* FROM Users u JOIN Enrollments e ON u.UserID = e.StudentUserID WHERE e.CourseID = ? ORDER BY u.LastName, u.FirstName";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> students = new ArrayList<>();
        // We need a UserDAO instance or map the user directly here
        // For simplicity here, we'll map directly. A UserDAO dependency is cleaner design.

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, courseId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                 // Manually map ResultSet to User object - duplicating UserDAO logic slightly
                 // Ideally, call a map method from UserDAO if possible
                 students.add(mapResultSetToUser_Local(rs)); 
            }
        } catch (SQLException e) {
             LOGGER.log(Level.SEVERE, "Error retrieving students for course ID: " + courseId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return students;
    }

    /**
     * Checks if a specific student is enrolled in a specific course.
     *
     * @param studentId The UserID of the student.
     * @param courseId The ID of the course.
     * @return true if the student is enrolled, false otherwise.
     */
    public boolean isEnrolled(int studentId, int courseId) {
        String sql = "SELECT 1 FROM Enrollments WHERE StudentUserID = ? AND CourseID = ? LIMIT 1";
         Connection conn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
        boolean enrolled = false;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            rs = pstmt.executeQuery();
            enrolled = rs.next(); // True if a row is found
        } catch (SQLException e) {
             LOGGER.log(Level.SEVERE, "Error checking enrollment for Student " + studentId + ", Course " + courseId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return enrolled;
    }


    // --- Helper Methods ---

    /**
     * Maps a row from the ResultSet to an Enrollment object.
     *
     * @param rs The ResultSet cursor, positioned at a valid row.
     * @return An Enrollment object populated with data from the current row.
     * @throws SQLException if a database access error occurs.
     */
    private Enrollment mapResultSetToEnrollment(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentID(rs.getInt("EnrollmentID"));
        enrollment.setStudentUserID(rs.getInt("StudentUserID"));
        enrollment.setCourseID(rs.getInt("CourseID"));
        enrollment.setEnrollmentDate(rs.getTimestamp("EnrollmentDate"));
        return enrollment;
    }
    
    /**
     * Maps a row from the ResultSet to a User object (local version).
     * Ideally, this mapping logic should reside solely within UserDAO.
     * Duplicated here for getStudentsByCourse simplicity without DAO dependency.
     *
     * @param rs The ResultSet cursor, positioned at a valid row from a Users table query/join.
     * @return A User object.
     * @throws SQLException
     */
     private User mapResultSetToUser_Local(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserID(rs.getInt("UserID"));
        user.setFirstName(rs.getString("FirstName"));
        user.setLastName(rs.getString("LastName"));
        user.setEmail(rs.getString("Email"));
        user.setPasswordHash(rs.getString("PasswordHash")); // Included for completeness
        user.setSalt(rs.getString("Salt"));               // Included for completeness
        user.setRole(UserRole.fromString(rs.getString("Role"))); 
        user.setDepartment(rs.getString("Department"));
        user.setProfilePicturePath(rs.getString("ProfilePicturePath"));
        user.setPasswordResetToken(rs.getString("PasswordResetToken"));
        user.setPasswordResetExpiry(rs.getTimestamp("PasswordResetExpiry"));
        user.setCreatedAt(rs.getTimestamp("CreatedAt"));
        user.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
        return user;
    }
    
    // --- Simple DAO Tester --- 

    /**
     * Simple main method to test EnrollmentDAO operations.
     * Assumes database setup and potentially existing users/courses.
     * IMPORTANT: This WILL modify the database.
     */
    public static void main(String[] args) {
        System.out.println("Testing EnrollmentDAO...");
        EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
        CourseDAO courseDAO = new CourseDAO(); // Needed for capacity info
        UserDAO userDAO = new UserDAO(); // Needed for student info

        // --- Test Data --- 
        // Ensure a student and course exist for testing. Create if necessary.
        int testStudentId = -1; // Will be set after ensuring student exists
        int testCourseId = -1; // Will be set after ensuring course exists
        String testStudentEmail = "enroll-test@example.com";
        String testCourseCode = "ENRLTEST1";
        
        User testStudent = null;
        Course testCourse = null;
        
        try {
             // 1. Ensure Test Student Exists (or create one)
             System.out.println("\n1. Ensuring test student exists...");
             testStudent = userDAO.getUserByEmail(testStudentEmail);
             if (testStudent == null) {
                 System.out.println("   Test student not found, creating...");
                 testStudent = new User(0, "Enrollment", "Test", testStudentEmail, "password123", null, UserRole.STUDENT, null, null, null, null, null, null);
                 if (userDAO.addUser(testStudent)) {
                     System.out.println("   Test student created with ID: " + testStudent.getUserID());
                 } else {
                     System.err.println("   Failed to create test student. Aborting test.");
                     return;
                 }
             } else {
                  System.out.println("   Found test student with ID: " + testStudent.getUserID());
             }
             testStudentId = testStudent.getUserID();

             // 2. Ensure Test Course Exists (or create one with capacity)
              System.out.println("\n2. Ensuring test course exists...");
             testCourse = courseDAO.getCourseByCode(testCourseCode);
             if (testCourse == null) {
                 System.out.println("   Test course not found, creating...");
                 testCourse = new Course(0, testCourseCode, "DAO Enrollment Test Course", 1, null, null, null); // Capacity 1
                 if (courseDAO.addCourse(testCourse)) {
                     System.out.println("   Test course created with ID: " + testCourse.getCourseID());
                 } else {
                      System.err.println("   Failed to create test course. Aborting test.");
                      return;
                 }
             } else {
                  System.out.println("   Found test course with ID: " + testCourse.getCourseID());
             }
             testCourseId = testCourse.getCourseID();
             
             // Create effectively final variables for use in lambdas
             final int finalTestStudentId = testStudentId;
             final int finalTestCourseId = testCourseId;
             
             // 3. Clean up any previous enrollment for this student/course
             System.out.println("\n3. Cleaning up previous enrollment...");
             enrollmentDAO.deleteEnrollment(testStudentId, testCourseId);
             
             // 4. Check if enrolled (should be false)
             System.out.println("\n4. Checking initial enrollment status...");
             boolean enrolled = enrollmentDAO.isEnrolled(testStudentId, testCourseId);
             System.out.println("   Is enrolled? " + enrolled);
             if (enrolled) {
                 System.err.println("   Error: Student should NOT be enrolled at this point!");
             }
             
             // 5. Add Enrollment
             System.out.println("\n5. Attempting to enroll student " + testStudentId + " in course " + testCourseId + "...");
             Enrollment newEnrollment = new Enrollment(0, testStudentId, testCourseId, null);
             int enrollmentId = enrollmentDAO.addEnrollment(newEnrollment);
             System.out.println("   addEnrollment returned ID: " + enrollmentId);
             if (enrollmentId <= 0) {
                  System.err.println("   Failed to add enrollment!");
             }

             // 6. Check if enrolled (should be true)
             System.out.println("\n6. Checking enrollment status after add...");
             enrolled = enrollmentDAO.isEnrolled(testStudentId, testCourseId);
             System.out.println("   Is enrolled? " + enrolled);
              if (!enrolled) {
                 System.err.println("   Error: Student SHOULD be enrolled now!");
             }
             
             // 7. Attempt to add again (should fail - already enrolled or full)
             System.out.println("\n7. Attempting to enroll same student again (should fail)...");
             enrollmentId = enrollmentDAO.addEnrollment(newEnrollment);
             System.out.println("   Second addEnrollment returned ID: " + enrollmentId);
             if (enrollmentId > 0) {
                  System.err.println("   Error: Should not have been able to enroll again!");
             }

             // 8. Get Enrollments by Student
             System.out.println("\n8. Getting enrollments for student " + testStudentId + "...");
             List<Enrollment> studentEnrollments = enrollmentDAO.getEnrollmentsByStudent(testStudentId);
             System.out.println("   Found " + studentEnrollments.size() + " enrollments:");
             for(Enrollment e : studentEnrollments) {
                 System.out.println("     " + e);
             }
             if(studentEnrollments.stream().noneMatch(e -> e.getCourseID() == finalTestCourseId)) {
                  System.err.println("   Error: Test course enrollment not found for student!");
             }

             // 9. Get Students by Course
             System.out.println("\n9. Getting students for course " + testCourseId + "...");
             List<User> courseStudents = enrollmentDAO.getStudentsByCourse(testCourseId);
             System.out.println("   Found " + courseStudents.size() + " students:");
             for(User u : courseStudents) {
                 System.out.println("     " + u.getUserID() + ": " + u.getFirstName() + " " + u.getLastName());
             }
              if(courseStudents.stream().noneMatch(u -> u.getUserID() == finalTestStudentId)) {
                  System.err.println("   Error: Test student not found for course!");
             }
             
             // 10. Delete Enrollment
             System.out.println("\n10. Deleting enrollment...");
             boolean deleted = enrollmentDAO.deleteEnrollment(testStudentId, testCourseId);
             System.out.println("   Delete success? " + deleted);
              if (!deleted) {
                 System.err.println("   Error: Failed to delete enrollment!");
             }
             
              // 11. Check if enrolled (should be false again)
             System.out.println("\n11. Checking enrollment status after delete...");
             enrolled = enrollmentDAO.isEnrolled(testStudentId, testCourseId);
             System.out.println("   Is enrolled? " + enrolled);
             if (enrolled) {
                 System.err.println("   Error: Student should NOT be enrolled after delete!");
             }

        } catch (Exception e) {
             System.err.println("\nAn unexpected error occurred during EnrollmentDAO testing:");
             e.printStackTrace();
        } finally {
             // Optional: Clean up the test student and course if desired
             // Be cautious if they might be used by other tests or manually created data
             System.out.println("\nEnrollmentDAO testing finished.");
            // if (testStudent != null && testStudent.getUserID() > 0) userDAO.deleteUser(testStudent.getUserID());
            // if (testCourse != null && testCourse.getCourseID() > 0) courseDAO.deleteCourse(testCourse.getCourseID());
        }
    }
} 