package com.school.management.model.entities;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents a Course entity, mapping to the 'Courses' table in the database.
 */
public class Course {

    private int courseID;
    private String courseCode;
    private String name;
    private int maximumCapacity;
    private Integer teacherUserID; // Use Integer to allow null for unassigned teacher
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default constructor
    public Course() {
    }

    // Full constructor
    public Course(int courseID, String courseCode, String name, int maximumCapacity, 
                  Integer teacherUserID, Timestamp createdAt, Timestamp updatedAt) {
        this.courseID = courseID;
        this.courseCode = courseCode;
        this.name = name;
        this.maximumCapacity = maximumCapacity;
        this.teacherUserID = teacherUserID;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getCourseID() {
        return courseID;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getName() {
        return name;
    }

    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    public Integer getTeacherUserID() { // Return Integer to represent null possibility
        return teacherUserID;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setCourseID(int courseID) { // Typically set only by DAO from DB
        this.courseID = courseID;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaximumCapacity(int maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    public void setTeacherUserID(Integer teacherUserID) { // Accept Integer
        this.teacherUserID = teacherUserID;
    }

    public void setCreatedAt(Timestamp createdAt) { // Typically set by DAO
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) { // Typically set by DAO
        this.updatedAt = updatedAt;
    }

    // toString, equals, hashCode

    @Override
    public String toString() {
        return "Course{" + "courseID=" + courseID + ", courseCode='" + courseCode + '\'' + ", name='" + name + '\'' + ", maximumCapacity=" + maximumCapacity + ", teacherUserID=" + teacherUserID + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        // Check unique identifier (courseCode or courseID if available)
         if (courseID != 0 && course.courseID != 0) {
             return courseID == course.courseID;
         }
        return courseCode.equals(course.courseCode);
    }

    @Override
    public int hashCode() {
         // Use unique identifier
         if (courseID != 0) {
             return Objects.hash(courseID);
         }
        return Objects.hash(courseCode);
    }
    
    // --- Simple Interactive Tester --- 

    /**
     * Simple main method to test Course object creation.
     * Compile and run this file directly to test.
     */
    public static void main(String[] args) {
        System.out.println("Testing Course Entity...");

        // Test Course creation
        Course course1 = new Course();
        course1.setCourseID(1);
        course1.setCourseCode("CS101");
        course1.setName("Introduction to Programming");
        course1.setMaximumCapacity(50);
        course1.setTeacherUserID(102); // Example teacher ID
        course1.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        System.out.println("\nCreated Course 1 via setters:");
        System.out.println("  ID: " + course1.getCourseID());
        System.out.println("  Code: " + course1.getCourseCode());
        System.out.println("  Name: " + course1.getName());
        System.out.println("  Capacity: " + course1.getMaximumCapacity());
        System.out.println("  Teacher ID: " + course1.getTeacherUserID());
        System.out.println("  toString(): " + course1.toString());

        // Test full constructor and null teacher ID
        Course course2 = new Course(2, "MATH203", "Calculus II", 30, null, 
                                new Timestamp(System.currentTimeMillis()), null);
        
        Course course3 = new Course(1, "CS101", "Intro to Prog", 45, 102, 
                                new Timestamp(System.currentTimeMillis()), null);

        System.out.println("\nCreated Course 2 (No Teacher) via full constructor:");
        System.out.println("  toString(): " + course2.toString());
        
        System.out.println("\nTesting equals and hashCode:");
        System.out.println("  Course1 equals Course2? " + course1.equals(course2)); // Should be false
        System.out.println("  Course1 equals Course3? " + course1.equals(course3)); // Should be true (based on ID)
        System.out.println("  Course1 hashCode: " + course1.hashCode());
        System.out.println("  Course2 hashCode: " + course2.hashCode());
        System.out.println("  Course3 hashCode: " + course3.hashCode());
        System.out.println("  (Course1 and Course3 hashCodes should match)");

        System.out.println("\nTesting complete.");
    }
} 