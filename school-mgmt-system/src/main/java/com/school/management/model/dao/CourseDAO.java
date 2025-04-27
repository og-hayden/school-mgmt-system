package com.school.management.model.dao;

import com.school.management.model.entities.Course;
import com.school.management.model.entities.User;
import com.school.management.util.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for the Course entity.
 * Handles all CRUD operations and other queries related to the Courses table.
 */
public class CourseDAO {

    private static final Logger LOGGER = Logger.getLogger(CourseDAO.class.getName());

    /**
     * Adds a new course to the database.
     *
     * @param course The Course object to add.
     * @return true if the course was added successfully, false otherwise.
     */
    public boolean addCourse(Course course) {
        String sql = "INSERT INTO Courses (CourseCode, Name, MaximumCapacity, TeacherUserID) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getName());
            pstmt.setInt(3, course.getMaximumCapacity());
            setNullableInt(pstmt, 4, course.getTeacherUserID()); // Handle potentially null teacher ID

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    course.setCourseID(generatedKeys.getInt(1)); // Set the generated ID back on the object
                    success = true;
                    LOGGER.log(Level.INFO, "Course added successfully with ID: {0}", course.getCourseID());
                }
            }
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) { // Unique constraint violation (likely CourseCode)
                LOGGER.log(Level.WARNING, "Attempted to add course with duplicate CourseCode: " + course.getCourseCode(), e);
            } else {
                 LOGGER.log(Level.SEVERE, "Error adding course: " + course.getCourseCode(), e);
            }
        } finally {
            // Close resources carefully
            try { if (generatedKeys != null) generatedKeys.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Error closing generatedKeys", e); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Error closing PreparedStatement", e); }
            DatabaseConnection.closeConnection(conn); // Close connection
        }
        return success;
    }

    /**
     * Retrieves a course by its CourseID.
     *
     * @param courseId The ID of the course to retrieve.
     * @return The Course object if found, null otherwise.
     */
    public Course getCourseById(int courseId) {
        String sql = "SELECT * FROM Courses WHERE CourseID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Course course = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, courseId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                course = mapResultSetToCourse(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving course by ID: " + courseId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return course;
    }

     /**
     * Retrieves a course by its unique CourseCode.
     *
     * @param courseCode The code of the course to retrieve.
     * @return The Course object if found, null otherwise.
     */
    public Course getCourseByCode(String courseCode) {
        String sql = "SELECT * FROM Courses WHERE CourseCode = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Course course = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, courseCode);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                course = mapResultSetToCourse(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving course by code: " + courseCode, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return course;
    }

    /**
     * Retrieves all courses from the database.
     *
     * @return A list of all Course objects.
     */
    public List<Course> getAllCourses() {
        String sql = "SELECT * FROM Courses ORDER BY CourseCode";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<Course> courses = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all courses.", e);
        } finally {
            DatabaseConnection.closeResources(conn, stmt, rs);
        }
        return courses;
    }

     /**
     * Retrieves all courses taught by a specific teacher.
     *
     * @param teacherId The UserID of the teacher.
     * @return A list of Course objects assigned to the teacher.
     */
    public List<Course> getCoursesByTeacher(int teacherId) {
        String sql = "SELECT * FROM Courses WHERE TeacherUserID = ? ORDER BY CourseCode";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Course> courses = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, teacherId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                 courses.add(mapResultSetToCourse(rs));
            }
        } catch (SQLException e) {
             LOGGER.log(Level.SEVERE, "Error retrieving courses for teacher ID: " + teacherId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return courses;
    }

    /**
     * Updates an existing course's information in the database.
     *
     * @param course The Course object with updated information (must have valid CourseID).
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateCourse(Course course) {
        String sql = "UPDATE Courses SET CourseCode = ?, Name = ?, MaximumCapacity = ?, TeacherUserID = ? WHERE CourseID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getName());
            pstmt.setInt(3, course.getMaximumCapacity());
            setNullableInt(pstmt, 4, course.getTeacherUserID());
            pstmt.setInt(5, course.getCourseID());

            int affectedRows = pstmt.executeUpdate();
            success = affectedRows > 0;
            if(success) {
                 LOGGER.log(Level.INFO, "Course updated successfully: ID {0}", course.getCourseID());
            } else {
                 LOGGER.log(Level.WARNING, "Course update failed or course not found: ID {0}", course.getCourseID());
            }

        } catch (SQLException e) {
             if ("23000".equals(e.getSQLState())) { // Unique constraint violation (likely CourseCode)
                 LOGGER.log(Level.WARNING, "Attempted to update course ID {0} with duplicate CourseCode: {1}", new Object[]{course.getCourseID(), course.getCourseCode()});
             } else {
                LOGGER.log(Level.SEVERE, "Error updating course: ID " + course.getCourseID(), e);
             }
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
        return success;
    }

    /**
     * Deletes a course from the database by its CourseID.
     *
     * @param courseId The ID of the course to delete.
     * @return true if the course was deleted successfully, false otherwise.
     */
    public boolean deleteCourse(int courseId) {
        String sql = "DELETE FROM Courses WHERE CourseID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, courseId);

            int affectedRows = pstmt.executeUpdate();
            success = affectedRows > 0;
             if(success) {
                 LOGGER.log(Level.INFO, "Course deleted successfully: ID {0}", courseId);
            } else {
                 LOGGER.log(Level.WARNING, "Course deletion failed or course not found: ID {0}", courseId);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting course: ID " + courseId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
        return success;
    }

    /**
     * Assigns a teacher to a course.
     *
     * @param courseId The ID of the course.
     * @param teacherId The UserID of the teacher to assign.
     * @return true if the assignment was successful, false otherwise.
     */
    public boolean assignTeacher(int courseId, int teacherId) {
        // Optional: Check if teacherId corresponds to a user with TEACHER role first (in service layer ideally)
        String sql = "UPDATE Courses SET TeacherUserID = ? WHERE CourseID = ?";
        return executeSimpleUpdate(sql, teacherId, courseId, "assign teacher to course ID " + courseId);
    }

    /**
     * Unassigns any teacher from a course (sets TeacherUserID to NULL).
     *
     * @param courseId The ID of the course.
     * @return true if the unassignment was successful, false otherwise.
     */
    public boolean unassignTeacher(int courseId) {
        String sql = "UPDATE Courses SET TeacherUserID = NULL WHERE CourseID = ?";
        return executeSimpleUpdate(sql, courseId, "unassign teacher from course ID " + courseId);
    }

    /**
     * Gets the current number of students enrolled in a specific course.
     *
     * @param courseId The ID of the course.
     * @return The number of enrolled students, or -1 if an error occurs.
     */
    public int getEnrollmentCount(int courseId) {
        String sql = "SELECT COUNT(*) FROM Enrollments WHERE CourseID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = -1;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, courseId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting enrollment count for course ID: " + courseId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return count;
    }

    // --- Helper Methods ---

    /**
     * Maps a row from the ResultSet to a Course object.
     *
     * @param rs The ResultSet cursor, positioned at a valid row.
     * @return A Course object populated with data from the current row.
     * @throws SQLException if a database access error occurs.
     */
    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseID(rs.getInt("CourseID"));
        course.setCourseCode(rs.getString("CourseCode"));
        course.setName(rs.getString("Name"));
        course.setMaximumCapacity(rs.getInt("MaximumCapacity"));
        // Handle nullable TeacherUserID carefully
        int teacherId = rs.getInt("TeacherUserID");
        if (rs.wasNull()) {
            course.setTeacherUserID(null);
        } else {
            course.setTeacherUserID(teacherId);
        }
        course.setCreatedAt(rs.getTimestamp("CreatedAt"));
        course.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
        return course;
    }
    
     /**
     * Helper method to safely set nullable Integer values in a PreparedStatement.
     * If the value is null, it sets SQL NULL, otherwise sets the int.
     *
     * @param pstmt The PreparedStatement.
     * @param parameterIndex The index of the parameter to set.
     * @param value The Integer value to set.
     * @throws SQLException if a database access error occurs.
     */
    private void setNullableInt(PreparedStatement pstmt, int parameterIndex, Integer value) throws SQLException {
        if (value != null) {
            pstmt.setInt(parameterIndex, value);
        } else {
            pstmt.setNull(parameterIndex, Types.INTEGER);
        }
    }
    
    /**
     * Executes a simple update statement (like assign/unassign teacher) with logging.
     *
     * @param sql The SQL update statement.
     * @param params The parameters for the PreparedStatement.
     * @param actionDescription Description of the action for logging.
     * @return true if the update affected at least one row, false otherwise.
     */
    private boolean executeSimpleUpdate(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;
        // Extract action description if provided as the last parameter (slightly hacky but avoids repeating method signature)
        String actionDescription = "simple update";
        Object[] actualParams = params;
        if (params.length > 0 && params[params.length-1] instanceof String) {
             actionDescription = (String) params[params.length-1];
             actualParams = new Object[params.length - 1];
             System.arraycopy(params, 0, actualParams, 0, params.length - 1);
        }
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            // Set parameters
            for (int i = 0; i < actualParams.length; i++) {
                 if (actualParams[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) actualParams[i]);
                } else if (actualParams[i] instanceof String) {
                    pstmt.setString(i + 1, (String) actualParams[i]);
                } else if (actualParams[i] == null) {
                    // Infer type - this is limited, better to have specific methods
                    pstmt.setNull(i + 1, Types.NULL); // Or be more specific if possible
                } else {
                     // Handle other types if necessary
                    pstmt.setObject(i + 1, actualParams[i]);
                }
            }

            int affectedRows = pstmt.executeUpdate();
            success = affectedRows > 0;
            if(success) {
                 LOGGER.log(Level.INFO, "Successfully executed {0}. Affected rows: {1}", new Object[]{actionDescription, affectedRows});
            } else {
                 LOGGER.log(Level.WARNING, "Execution of {0} failed or affected 0 rows.", actionDescription);
            }
        } catch (SQLException e) {
             LOGGER.log(Level.SEVERE, "Error executing " + actionDescription, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
        return success;
    }
    
     // --- Simple DAO Tester --- 

    /**
     * Simple main method to test CourseDAO operations.
     * Assumes the database and tables exist and are accessible.
     * IMPORTANT: This test WILL modify the database.
     */
    public static void main(String[] args) {
        System.out.println("Testing CourseDAO...");
        CourseDAO courseDAO = new CourseDAO();

        // --- Test Data ---
        String testCode = "TEST999";
        String testName = "DAO Test Course";
        String updatedName = "DAO Test Course (Updated)";
        int testCapacity = 10;
        // Assume teacher with UserID 1 exists for testing assign/unassign (or adjust ID)
        int testTeacherId = 1; 
        
        Course testCourse = null;
        Course existing = null; // Declare here for wider scope
        boolean success;

        try {
            // 1. Clean up any previous test course
            System.out.println("\n1. Attempting cleanup...");
            existing = courseDAO.getCourseByCode(testCode);
            if (existing != null) {
                System.out.println("   Found existing test course (ID: " + existing.getCourseID() + "). Deleting...");
                courseDAO.deleteCourse(existing.getCourseID());
            }

            // 2. Add Course
            System.out.println("\n2. Adding new course...");
            Course newCourse = new Course(0, testCode, testName, testCapacity, null, null, null);
            success = courseDAO.addCourse(newCourse);
            System.out.println("   Add success? " + success);
            if (!success) {
                System.err.println("   Failed to add course, cannot proceed with further tests.");
                return;
            }
            System.out.println("   Added Course ID: " + newCourse.getCourseID());
            int addedCourseId = newCourse.getCourseID(); // Store ID for later use

            // 3. Get Course by ID
            System.out.println("\n3. Getting course by ID (" + addedCourseId + ")...");
            testCourse = courseDAO.getCourseById(addedCourseId);
            System.out.println("   Retrieved: " + (testCourse != null ? testCourse.toString() : "null"));
            if (testCourse == null || !testName.equals(testCourse.getName())) {
                 System.err.println("   Retrieved course is null or name mismatch!");
            }

            // 4. Get Course by Code
            System.out.println("\n4. Getting course by Code ('" + testCode + "')...");
            testCourse = courseDAO.getCourseByCode(testCode);
            System.out.println("   Retrieved: " + (testCourse != null ? testCourse.toString() : "null"));
             if (testCourse == null || addedCourseId != testCourse.getCourseID()) {
                 System.err.println("   Retrieved course is null or ID mismatch!");
            }

            // 5. Assign Teacher
            System.out.println("\n5. Assigning Teacher (ID: " + testTeacherId + ")...");
            success = courseDAO.assignTeacher(addedCourseId, testTeacherId);
            System.out.println("   Assign success? " + success);
            testCourse = courseDAO.getCourseById(addedCourseId); // Re-fetch
            System.out.println("   Course after assign: " + (testCourse != null ? testCourse.toString() : "null"));
            if (testCourse == null || testCourse.getTeacherUserID() == null || testCourse.getTeacherUserID() != testTeacherId) {
                 System.err.println("   Teacher assignment verification failed!");
            }
            
            // 6. Update Course
            System.out.println("\n6. Updating course name...");
            if(testCourse != null) {
                testCourse.setName(updatedName);
                success = courseDAO.updateCourse(testCourse);
                System.out.println("   Update success? " + success);
                testCourse = courseDAO.getCourseById(addedCourseId); // Re-fetch
                System.out.println("   Course after update: " + (testCourse != null ? testCourse.toString() : "null"));
                 if (testCourse == null || !updatedName.equals(testCourse.getName())) {
                    System.err.println("   Course name update verification failed!");
                }
            } else {
                 System.out.println("   Skipping update test as course object is null.");
            }
            
            // 7. Unassign Teacher
            System.out.println("\n7. Unassigning Teacher...");
            success = courseDAO.unassignTeacher(addedCourseId);
            System.out.println("   Unassign success? " + success);
            testCourse = courseDAO.getCourseById(addedCourseId); // Re-fetch
            System.out.println("   Course after unassign: " + (testCourse != null ? testCourse.toString() : "null"));
             if (testCourse == null || testCourse.getTeacherUserID() != null) {
                 System.err.println("   Teacher unassignment verification failed!");
            }
            
            // 8. Get Enrollment Count (should be 0 initially)
            System.out.println("\n8. Getting Enrollment Count...");
            int count = courseDAO.getEnrollmentCount(addedCourseId);
            System.out.println("   Enrollment Count: " + count);
             if (count != 0) {
                 System.err.println("   Expected 0 enrollments initially!");
            }

             // 9. Get All Courses (just check if list is returned)
             System.out.println("\n9. Getting All Courses...");
             List<Course> allCourses = courseDAO.getAllCourses();
             System.out.println("   Retrieved " + allCourses.size() + " courses.");
             if (allCourses.isEmpty()) {
                  System.err.println("   Warning: getAllCourses returned an empty list (is DB empty?)");
             }

        } catch (Exception e) {
            System.err.println("\nAn unexpected error occurred during testing:");
            e.printStackTrace();
        } finally {
            // 10. Final Cleanup
             System.out.println("\n10. Final cleanup...");
             // No need to call getCourseByCode again if 'existing' was already checked 
             // However, if an error happened before 'existing' was set, we might need to re-check
             // For simplicity in this test, we assume 'existing' might still hold the value or be null.
             // A more robust test might re-query here.
             if (existing != null && existing.getCourseID() > 0) { // Check if we had a valid course ID
                 System.out.println("   Deleting test course (ID: " + existing.getCourseID() + ")...");
                 success = courseDAO.deleteCourse(existing.getCourseID());
                 System.out.println("   Delete success? " + success);
             } else {
                 // Attempt delete by code if ID wasn't retrieved but maybe it exists
                  Course courseToDelete = courseDAO.getCourseByCode(testCode);
                  if(courseToDelete != null) {
                     System.out.println("   Deleting test course found by code (ID: " + courseToDelete.getCourseID() + ")...");
                     success = courseDAO.deleteCourse(courseToDelete.getCourseID());
                     System.out.println("   Delete success? " + success);
                  } else {
                     System.out.println("   Test course not found for final cleanup.");
                  }
             }
        }

        System.out.println("\nCourseDAO testing finished.");
    }
} 