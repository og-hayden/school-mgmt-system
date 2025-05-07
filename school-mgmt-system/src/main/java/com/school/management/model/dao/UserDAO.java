package com.school.management.model.dao;

import com.school.management.model.entities.User;
import com.school.management.model.entities.UserRole;
import com.school.management.util.database.DatabaseConnection;
import com.school.management.util.security.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for the User entity.
 * Handles all CRUD operations and other queries related to the Users table.
 * IMPORTANT: Uses SHA-256 with salting due to BCrypt library... issues during development. Super fun.
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    /**
     * Adds a new user to the database.
     * Assumes the User object already contains the correctly hashed password and the corresponding Base64 encoded salt.
     *
     * @param user The User object to add (must have PasswordHash and Salt set).
     * @return true if the user was added successfully, false otherwise.
     */
    public boolean addUser(User user) {
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty() || 
            user.getSalt() == null || user.getSalt().isEmpty()) {
            LOGGER.log(Level.SEVERE, "Attempted to add user {0} with missing PasswordHash or Salt.", user.getEmail());
            return false;
        }

        String sql = "INSERT INTO Users (FirstName, LastName, Email, PasswordHash, Salt, Role, Department, ProfilePicturePath) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPasswordHash());
            pstmt.setString(5, user.getSalt());
            pstmt.setString(6, user.getRole().name()); // Store enum name as string
            // Handle nullable fields
            setNullableString(pstmt, 7, user.getDepartment());
            setNullableString(pstmt, 8, user.getProfilePicturePath());
            // PasswordResetToken and Expiry are typically set separately

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Retrieve the auto-generated UserID
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setUserID(generatedKeys.getInt(1));
                    success = true;
                    LOGGER.log(Level.INFO, "User added successfully with ID: {0}", user.getUserID());
                } else {
                     LOGGER.log(Level.SEVERE, "Failed to retrieve generated UserID after insert.");
                }
            }
        } catch (SQLException e) {
             // Check for unique constraint violation (email likely)
            if ("23000".equals(e.getSQLState())) { // SQL state for integrity constraint violation
                LOGGER.log(Level.WARNING, "Attempted to add user with duplicate email: " + user.getEmail(), e);
            } else {
                LOGGER.log(Level.SEVERE, "Error adding user: " + user.getEmail(), e);
            }
        } finally {
            DatabaseConnection.closeResources(null, pstmt, generatedKeys); // Close statement and keys
             DatabaseConnection.closeConnection(conn); // Close connection separately
        }
        return success;
    }

    /**
     * Retrieves a user by their UserID.
     *
     * @param userId The ID of the user to retrieve.
     * @return The User object if found, null otherwise.
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE UserID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user by ID: " + userId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return user;
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user to retrieve.
     * @return The User object if found, null otherwise.
     */
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE Email = ?";
         Connection conn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
        User user = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user by email: " + email, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return user;
    }
    
    /**
     * Retrieves all users from the database.
     *
     * @return A list of all User objects.
     */
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM Users ORDER BY LastName, FirstName";
        Connection conn = null;
        Statement stmt = null; // Simple query, no parameters
        ResultSet rs = null;
        List<User> users = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all users.", e);
        } finally {
            DatabaseConnection.closeResources(conn, stmt, rs);
        }
        return users;
    }

    /**
     * Retrieves all users with a specific role.
     *
     * @param role The UserRole to filter by.
     * @return A list of User objects with the specified role.
     */
    public List<User> getUsersByRole(UserRole role) {
         String sql = "SELECT * FROM Users WHERE Role = ? ORDER BY LastName, FirstName";
         Connection conn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
        List<User> users = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, role.name());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                 users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving users by role: " + role, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return users;
    }


    /**
     * Updates an existing user's information in the database.
     * Does NOT update the password hash or salt here. See updatePassword.
     *
     * @param user The User object with updated information (must have valid UserID).
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateUser(User user) {
        // Exclude PasswordHash and Salt from this general update
        String sql = "UPDATE Users SET FirstName = ?, LastName = ?, Email = ?, Role = ?, Department = ?, ProfilePicturePath = ? WHERE UserID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole().name());
            setNullableString(pstmt, 5, user.getDepartment());
            setNullableString(pstmt, 6, user.getProfilePicturePath());
            pstmt.setInt(7, user.getUserID());

            int affectedRows = pstmt.executeUpdate();
            success = affectedRows > 0;
            if(success) {
                 LOGGER.log(Level.INFO, "User updated successfully: ID {0}", user.getUserID());
            } else {
                 LOGGER.log(Level.WARNING, "User update failed or user not found: ID {0}", user.getUserID());
            }

        } catch (SQLException e) {
             if ("23000".equals(e.getSQLState())) { // Check for unique constraint violation (email likely)
                 LOGGER.log(Level.WARNING, "Attempted to update user ID {0} with duplicate email: {1}", new Object[]{user.getUserID(), user.getEmail()});
             } else {
                 LOGGER.log(Level.SEVERE, "Error updating user: ID " + user.getUserID(), e);
             }
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
        return success;
    }

    /**
     * Updates only the password hash and salt for a given user.
     * Generates a new salt and hashes the new plain password.
     *
     * @param userId The ID of the user whose password needs updating.
     * @param newPlainPassword The new plain text password.
     * @return true if the password was updated successfully, false otherwise.
     */
    public boolean updatePassword(int userId, String newPlainPassword) {
        if (newPlainPassword == null || newPlainPassword.isEmpty()) {
             LOGGER.log(Level.WARNING, "Attempted to update password with null or empty value for UserID: {0}", userId);
             return false;
        }
        
        // Generate new salt and hash
        String newSalt = PasswordUtil.generateSaltBase64();
        byte[] saltBytes = Base64.getDecoder().decode(newSalt);
        String newHashedPassword = PasswordUtil.hashPassword(newPlainPassword, saltBytes);

        if (newHashedPassword == null) {
            LOGGER.log(Level.SEVERE, "Failed to hash new password for UserID: {0}", userId);
            return false;
        }

        String sql = "UPDATE Users SET PasswordHash = ?, Salt = ? WHERE UserID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newHashedPassword);
            pstmt.setString(2, newSalt);
            pstmt.setInt(3, userId);

            int affectedRows = pstmt.executeUpdate();
            success = affectedRows > 0;
             if(success) {
                 LOGGER.log(Level.INFO, "Password updated successfully for UserID: {0}", userId);
            } else {
                 LOGGER.log(Level.WARNING, "Password update failed or user not found: UserID {0}", userId);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating password for UserID: " + userId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
        return success;
    }

    /**
     * Deletes a user from the database by their UserID.
     *
     * @param userId The ID of the user to delete.
     * @return true if the user was deleted successfully, false otherwise.
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE UserID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);

            int affectedRows = pstmt.executeUpdate();
            success = affectedRows > 0;
             if(success) {
                 LOGGER.log(Level.INFO, "User deleted successfully: ID {0}", userId);
            } else {
                 LOGGER.log(Level.WARNING, "User deletion failed or user not found: ID {0}", userId);
            }
        } catch (SQLException e) {
            // Handle potential foreign key constraint issues if cascading isn't fully set up
            LOGGER.log(Level.SEVERE, "Error deleting user: ID " + userId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
        return success;
    }
    
    // --- Password Reset Methods ---
    
    /**
     * Saves a password reset token and its expiry timestamp for a user.
     *
     * @param userId The user's ID.
     * @param token The generated reset token.
     * @param expiry The timestamp when the token expires.
     * @return true if the token was saved successfully, false otherwise.
     */
    public boolean savePasswordResetToken(int userId, String token, Timestamp expiry) {
        String sql = "UPDATE Users SET PasswordResetToken = ?, PasswordResetExpiry = ? WHERE UserID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            pstmt.setTimestamp(2, expiry);
            pstmt.setInt(3, userId);
            
            int affectedRows = pstmt.executeUpdate();
            success = affectedRows > 0;
            if (success) {
                LOGGER.log(Level.INFO, "Password reset token saved for UserID: {0}", userId);
            } else {
                LOGGER.log(Level.WARNING, "Failed to save password reset token for UserID: {0}", userId);
            }
        } catch (SQLException e) {
             LOGGER.log(Level.SEVERE, "Error saving password reset token for UserID: " + userId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
        return success;
    }
    
    /**
     * Finds a user by a valid (non-expired) password reset token.
     *
     * @param token The password reset token.
     * @return The User object if a valid token is found, null otherwise.
     */
    public User findUserByValidResetToken(String token) {
        String sql = "SELECT * FROM Users WHERE PasswordResetToken = ? AND PasswordResetExpiry > NOW()";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                user = mapResultSetToUser(rs);
                LOGGER.log(Level.INFO, "Valid password reset token found for UserID: {0}", user.getUserID());
            } else {
                 LOGGER.log(Level.INFO, "No user found with valid reset token: {0}", token);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by reset token: " + token, e);
        } finally {
             DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return user;
    }
    
    /**
     * Clears the password reset token and expiry for a user (e.g., after successful reset).
     *
     * @param userId The user's ID.
     * @return true if the token was cleared successfully, false otherwise.
     */
    public boolean clearPasswordResetToken(int userId) {
        String sql = "UPDATE Users SET PasswordResetToken = NULL, PasswordResetExpiry = NULL WHERE UserID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            int affectedRows = pstmt.executeUpdate();
            // Consider success even if token was already null
            success = true; 
            LOGGER.log(Level.INFO, "Password reset token cleared for UserID: {0} (Affected rows: {1})", new Object[]{userId, affectedRows});
            
        } catch (SQLException e) {
             LOGGER.log(Level.SEVERE, "Error clearing password reset token for UserID: " + userId, e);
             success = false; // Ensure success is false on error
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
        return success;
    }

    // --- Helper Methods ---

    /**
     * Maps a row from the ResultSet to a User object.
     *
     * @param rs The ResultSet cursor, positioned at a valid row.
     * @return A User object populated with data from the current row.
     * @throws SQLException if a database access error occurs.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserID(rs.getInt("UserID"));
        user.setFirstName(rs.getString("FirstName"));
        user.setLastName(rs.getString("LastName"));
        user.setEmail(rs.getString("Email"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        user.setSalt(rs.getString("Salt")); // Retrieve the salt
        user.setRole(UserRole.fromString(rs.getString("Role"))); // Convert string to enum
        user.setDepartment(rs.getString("Department"));
        user.setProfilePicturePath(rs.getString("ProfilePicturePath"));
        user.setPasswordResetToken(rs.getString("PasswordResetToken"));
        user.setPasswordResetExpiry(rs.getTimestamp("PasswordResetExpiry"));
        user.setCreatedAt(rs.getTimestamp("CreatedAt"));
        user.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
        return user;
    }
    
    /**
     * Helper method to safely set nullable String values in a PreparedStatement.
     * If the value is null or empty, it sets SQL NULL, otherwise sets the string.
     *
     * @param pstmt The PreparedStatement.
     * @param parameterIndex The index of the parameter to set.
     * @param value The String value to set.
     * @throws SQLException if a database access error occurs.
     */
    private void setNullableString(PreparedStatement pstmt, int parameterIndex, String value) throws SQLException {
        if (value != null && !value.trim().isEmpty()) {
            pstmt.setString(parameterIndex, value.trim());
        } else {
            pstmt.setNull(parameterIndex, Types.VARCHAR);
        }
    }

    /**
     * Retrieves a map of UserID to "FirstName LastName" for a given list of UserIDs.
     * Useful for quickly getting display names without fetching full User objects.
     *
     * @param userIds A list of UserIDs to fetch names for.
     * @return A Map where the key is the UserID and the value is the user's full name.
     *         Returns an empty map if the input list is null/empty or if an error occurs.
     */
    public Map<Integer, String> getUserNamesByIds(List<Integer> userIds) {
        Map<Integer, String> userNames = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return userNames; // Return empty map if no IDs provided
        }

        // Build the IN clause string: (?, ?, ?, ...)
        StringBuilder sqlBuilder = new StringBuilder("SELECT UserID, FirstName, LastName FROM Users WHERE UserID IN (");
        for (int i = 0; i < userIds.size(); i++) {
            sqlBuilder.append("?");
            if (i < userIds.size() - 1) {
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.append(")");

        String sql = sqlBuilder.toString();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            // Set the UserID parameters for the IN clause
            for (int i = 0; i < userIds.size(); i++) {
                pstmt.setInt(i + 1, userIds.get(i));
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("UserID");
                String firstName = rs.getString("FirstName");
                String lastName = rs.getString("LastName");
                userNames.put(userId, firstName + " " + lastName);
            }
            LOGGER.log(Level.FINE, "Fetched {0} user names for the provided IDs.", userNames.size());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user names by IDs.", e);
            // Return potentially partially filled map or empty map on error
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return userNames;
    }

    /**
     * Updates only the profile picture path for a given user.
     *
     * @param userId The ID of the user to update.
     * @param profilePicturePath The new path to the profile picture (can be null).
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateProfilePicturePath(int userId, String profilePicturePath) {
         if (userId <= 0) {
             LOGGER.log(Level.WARNING, "Cannot update profile picture path for invalid User ID: {0}", userId);
             return false;
         }
         
         String sql = "UPDATE Users SET ProfilePicturePath = ? WHERE UserID = ?";
         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
     
             pstmt.setString(1, profilePicturePath); // Path can be null
             pstmt.setInt(2, userId);
     
             int affectedRows = pstmt.executeUpdate();
             if (affectedRows > 0) {
                 LOGGER.log(Level.INFO, "Profile picture path updated successfully for User ID: {0}", userId);
                 return true;
             } else {
                 LOGGER.log(Level.WARNING, "Profile picture path update failed, no user found for ID: {0}", userId);
                 return false;
             }
         } catch (SQLException e) {
             LOGGER.log(Level.SEVERE, "Database error updating profile picture path for User ID: " + userId, e);
             return false;
         }
     }
} 