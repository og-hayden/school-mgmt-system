package com.school.management.model.entities;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents a User entity, mapping to the 'Users' table in the database.
 * Includes all user-related information including credentials and metadata.
 */
public class User {

    private int userID;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash; 
    private String salt; 
    private UserRole role;
    private String department; 
    private String profilePicturePath; 
    private String passwordResetToken; 
    private Timestamp passwordResetExpiry; 
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {
    }

    public User(int userID, String firstName, String lastName, String email, 
                String passwordHash, String salt, UserRole role, String department, 
                String profilePicturePath, String passwordResetToken, 
                Timestamp passwordResetExpiry, Timestamp createdAt, Timestamp updatedAt) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.department = department;
        this.profilePicturePath = profilePicturePath;
        this.passwordResetToken = passwordResetToken;
        this.passwordResetExpiry = passwordResetExpiry;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getUserID() {
        return userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public UserRole getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public Timestamp getPasswordResetExpiry() {
        return passwordResetExpiry;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    // Setters (for fields that can be modified)
     public void setUserID(int userID) { 
         this.userID = userID;
     }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public void setPasswordResetExpiry(Timestamp passwordResetExpiry) {
        this.passwordResetExpiry = passwordResetExpiry;
    }

     public void setCreatedAt(Timestamp createdAt) { 
         this.createdAt = createdAt;
     }

     public void setUpdatedAt(Timestamp updatedAt) { 
         this.updatedAt = updatedAt;
     }

    // toString, equals, hashCode (optional but recommended)

    @Override
    public String toString() {
        return "User{" + "userID=" + userID + ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + ", email='" + email + '\'' + ", role=" + role + ", department='" + department + '\'' + '}'; // Simplified toString
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        // Primarily check unique identifier (email or userID if available)
        if (userID != 0 && user.userID != 0) {
            return userID == user.userID;
        }
        return email.equals(user.email); // Use email as unique key if ID is 0
    }

    @Override
    public int hashCode() {
        // Use unique identifier for hashCode
        if (userID != 0) {
            return Objects.hash(userID);
        }
        return Objects.hash(email); 
    }

    // --- Simple Interactive Tester --- 


    public static void main(String[] args) {
        System.out.println("Testing User Entity and UserRole Enum...");

        // Test UserRole
        UserRole roleFromEnum = UserRole.TEACHER;
        UserRole roleFromString = null;
        try {
            roleFromString = UserRole.fromString("student"); // Case-insensitive test
            System.out.println("UserRole from enum: " + roleFromEnum);
            System.out.println("UserRole from string 'student': " + roleFromString);
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing UserRole from string: " + e.getMessage());
        }

        // Test User creation
        User testUser1 = new User();
        testUser1.setUserID(101);
        testUser1.setFirstName("Alice");
        testUser1.setLastName("Smith");
        testUser1.setEmail("alice.smith@example.com");
        testUser1.setRole(UserRole.STUDENT);
        testUser1.setSalt("someGeneratedSaltBase64"); // Example salt
        testUser1.setPasswordHash("someGeneratedHashBase64"); // Example hash
        testUser1.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        System.out.println("\nCreated User 1 via setters:");
        System.out.println("  ID: " + testUser1.getUserID());
        System.out.println("  Name: " + testUser1.getFirstName() + " " + testUser1.getLastName());
        System.out.println("  Email: " + testUser1.getEmail());
        System.out.println("  Role: " + testUser1.getRole());
        System.out.println("  Salt: " + testUser1.getSalt());
        System.out.println("  Hash: " + testUser1.getPasswordHash());
        System.out.println("  toString(): " + testUser1.toString());

         // Test full constructor and equals/hashCode
         User testUser2 = new User(102, "Bob", "Johnson", "bob.j@example.com", "anotherHash", "anotherSalt", 
                                  UserRole.TEACHER, "Computer Science", null, null, null, 
                                  new Timestamp(System.currentTimeMillis()), null);

         User testUser3 = new User(101, "Alice", "Smith", "alice.smith@example.com", "differentHash", "differentSalt", 
                                  UserRole.STUDENT, null, null, null, null, 
                                  new Timestamp(System.currentTimeMillis()), null);

        System.out.println("\nCreated User 2 via full constructor:");
        System.out.println("  toString(): " + testUser2.toString());

        System.out.println("\nTesting equals and hashCode:");
        System.out.println("  User1 equals User2? " + testUser1.equals(testUser2)); // Should be false
        System.out.println("  User1 equals User3? " + testUser1.equals(testUser3)); // Should be true (based on ID)
        System.out.println("  User1 hashCode: " + testUser1.hashCode());
        System.out.println("  User2 hashCode: " + testUser2.hashCode());
        System.out.println("  User3 hashCode: " + testUser3.hashCode());
        System.out.println("  (User1 and User3 hashCodes should match)");

        System.out.println("\nTesting complete.");
    }
} 