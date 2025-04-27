package com.school.management.util;

import com.school.management.model.entities.User;
import com.school.management.model.entities.UserRole;

/**
 * Manages the current user session using static fields.
 * Provides a simple way to access the logged-in user's information globally.
 */
public class UserSession {

    private static User currentUser = null;

    /**
     * Logs in a user by setting the current session user.
     *
     * @param user The user who has logged in.
     */
    public static void login(User user) {
        if (user != null) {
            currentUser = user;
            System.out.println("Session: User " + user.getEmail() + " logged in.");
        } else {
             System.err.println("Session Warning: Attempted to login with a null user.");
        }
    }

    /**
     * Logs out the current user by clearing the session.
     */
    public static void logout() {
        if (currentUser != null) {
            System.out.println("Session: User " + currentUser.getEmail() + " logged out.");
            currentUser = null;
        } else {
             System.out.println("Session: No user was logged in.");
        }
    }

    /**
     * Gets the currently logged-in user.
     *
     * @return The User object for the current session, or null if no user is logged in.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise.
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Gets the role of the currently logged-in user.
     *
     * @return The UserRole enum value, or null if no user is logged in.
     */
    public static UserRole getUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    /**
     * Gets the ID of the currently logged-in user.
     *
     * @return The user ID, or -1 if no user is logged in.
     */
    public static int getUserId() {
        return currentUser != null ? currentUser.getUserID() : -1;
    }

    // --- Simple Tester --- 

    /**
     * Simple main method to test UserSession functionality.
     */
    public static void main(String[] args) {
        System.out.println("--- Testing UserSession --- ");

        // 1. Initial State
        System.out.println("\n1. Initial State:");
        System.out.println("   Is Logged In? " + UserSession.isLoggedIn());
        System.out.println("   Current User? " + UserSession.getCurrentUser());
        System.out.println("   User ID? " + UserSession.getUserId());
        System.out.println("   User Role? " + UserSession.getUserRole());

        // 2. Login Simulation
        System.out.println("\n2. Simulating Login:");
        // Create a dummy user (no DB interaction needed for this test)
        User testUser = new User(101, "Test", "User", "test@session.com", "hashedpass", "somesalt", UserRole.STUDENT, null, null, null, null, null, null);
        UserSession.login(testUser);

        // 3. State After Login
        System.out.println("\n3. State After Login:");
        System.out.println("   Is Logged In? " + UserSession.isLoggedIn());
        User loggedInUser = UserSession.getCurrentUser();
        if (loggedInUser != null) {
            System.out.println("   Current User Email: " + loggedInUser.getEmail());
             System.out.println("   User ID: " + UserSession.getUserId());
             System.out.println("   User Role: " + UserSession.getUserRole());
        } else {
             System.err.println("   ERROR: User should be logged in!");
        }
        
        // 4. Logout Simulation
        System.out.println("\n4. Simulating Logout:");
        UserSession.logout();
        
        // 5. State After Logout
        System.out.println("\n5. State After Logout:");
         System.out.println("   Is Logged In? " + UserSession.isLoggedIn());
        System.out.println("   Current User? " + UserSession.getCurrentUser());
        System.out.println("   User ID? " + UserSession.getUserId());
        System.out.println("   User Role? " + UserSession.getUserRole());

        // Test logging out when already logged out
        System.out.println("\n6. Attempting Logout Again:");
        UserSession.logout();
        
        System.out.println("\n--- UserSession Test Finished ---");
    }
} 