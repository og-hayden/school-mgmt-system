package com.school.management.model.entities;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents an Enrollment entity, mapping to the 'Enrollments' table.
 * Links a student to a specific course they are enrolled in.
 */
public class Enrollment {

    private int enrollmentID;
    private int studentUserID;
    private int courseID;
    private Timestamp enrollmentDate;

    // Default constructor
    public Enrollment() {
    }

    // Full constructor
    public Enrollment(int enrollmentID, int studentUserID, int courseID, Timestamp enrollmentDate) {
        this.enrollmentID = enrollmentID;
        this.studentUserID = studentUserID;
        this.courseID = courseID;
        this.enrollmentDate = enrollmentDate;
    }

    // Getters
    public int getEnrollmentID() {
        return enrollmentID;
    }

    public int getStudentUserID() {
        return studentUserID;
    }

    public int getCourseID() {
        return courseID;
    }

    public Timestamp getEnrollmentDate() {
        return enrollmentDate;
    }

    // Setters
    public void setEnrollmentID(int enrollmentID) { 
        this.enrollmentID = enrollmentID;
    }

    public void setStudentUserID(int studentUserID) {
        this.studentUserID = studentUserID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public void setEnrollmentDate(Timestamp enrollmentDate) { 
        this.enrollmentDate = enrollmentDate;
    }

    // toString, equals, hashCode

    @Override
    public String toString() {
        return "Enrollment{" + "enrollmentID=" + enrollmentID + ", studentUserID=" + studentUserID + ", courseID=" + courseID + ", enrollmentDate=" + enrollmentDate + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enrollment that = (Enrollment) o;
        // Primary check on ID if available
        if (enrollmentID != 0 && that.enrollmentID != 0) {
            return enrollmentID == that.enrollmentID;
        }
        // Otherwise, check the unique combination of student and course
        return studentUserID == that.studentUserID && courseID == that.courseID;
    }

    @Override
    public int hashCode() {
         // Use unique combination of student and course if ID is not set
         if (enrollmentID != 0) {
             return Objects.hash(enrollmentID);
         }
        return Objects.hash(studentUserID, courseID);
    }
    
    // --- Simple Interactive Tester --- 

    public static void main(String[] args) {
        System.out.println("Testing Enrollment Entity...");

        // Test Enrollment creation
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        Enrollment enrollment1 = new Enrollment();
        enrollment1.setEnrollmentID(1001);
        enrollment1.setStudentUserID(101); // Example student ID
        enrollment1.setCourseID(1); // Example course ID
        enrollment1.setEnrollmentDate(now);

        System.out.println("\nCreated Enrollment 1 via setters:");
        System.out.println("  Enrollment ID: " + enrollment1.getEnrollmentID());
        System.out.println("  Student ID: " + enrollment1.getStudentUserID());
        System.out.println("  Course ID: " + enrollment1.getCourseID());
        System.out.println("  Date: " + enrollment1.getEnrollmentDate());
        System.out.println("  toString(): " + enrollment1.toString());

        // Test full constructor
        Enrollment enrollment2 = new Enrollment(1002, 103, 2, now);
        Enrollment enrollment3 = new Enrollment(1001, 101, 1, now); // Same as enrollment1 by ID
        Enrollment enrollment4 = new Enrollment(0, 101, 1, now); // Same student/course as 1, but ID 0
        Enrollment enrollment5 = new Enrollment(0, 105, 3, now); // Different student/course, ID 0

        System.out.println("\nCreated Enrollment 2 via full constructor:");
        System.out.println("  toString(): " + enrollment2.toString());
        
        System.out.println("\nTesting equals and hashCode:");
        System.out.println("  Enrollment1 equals Enrollment2? " + enrollment1.equals(enrollment2)); // Should be false
        System.out.println("  Enrollment1 equals Enrollment3? " + enrollment1.equals(enrollment3)); // Should be true (by ID)
        System.out.println("  Enrollment1 equals Enrollment4? " + enrollment1.equals(enrollment4)); // Should be true (by student/course since ID=0)
        System.out.println("  Enrollment4 equals Enrollment5? " + enrollment4.equals(enrollment5)); // Should be false
        System.out.println("  Enrollment1 hashCode: " + enrollment1.hashCode());
        System.out.println("  Enrollment2 hashCode: " + enrollment2.hashCode());
        System.out.println("  Enrollment3 hashCode: " + enrollment3.hashCode());
        System.out.println("  Enrollment4 hashCode: " + enrollment4.hashCode());
        System.out.println("  Enrollment5 hashCode: " + enrollment5.hashCode());
        System.out.println("  (Enrollment1 and Enrollment3 hashCodes should match)");
        System.out.println("  (Enrollment1 and Enrollment4 hashCodes might NOT match - ID vs composite key)");

        System.out.println("\nTesting complete.");
    }
} 