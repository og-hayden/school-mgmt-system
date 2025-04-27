package com.school.management.model.entities;

/**
 * Represents the roles a user can have within the system.
 * Matches the ENUM definition in the Users table.
 */
public enum UserRole {
    ADMINISTRATOR, 
    TEACHER, 
    STUDENT;

    /**
     * Provides a case-insensitive way to get a UserRole from a string.
     * Useful for mapping database values or user input.
     *
     * @param roleString The string representation of the role.
     * @return The corresponding UserRole enum constant.
     * @throws IllegalArgumentException if the string does not match any role.
     */
    public static UserRole fromString(String roleString) {
        if (roleString == null) {
            throw new IllegalArgumentException("Role string cannot be null.");
        }
        for (UserRole role : UserRole.values()) {
            if (role.name().equalsIgnoreCase(roleString)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No enum constant UserRole." + roleString);
    }
} 