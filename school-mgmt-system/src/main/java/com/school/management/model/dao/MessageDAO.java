package com.school.management.model.dao;

import com.school.management.model.entities.Course;
import com.school.management.model.entities.Message;
import com.school.management.model.entities.User;
import com.school.management.model.entities.UserRole;
import com.school.management.util.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for the Message entity.
 * Handles operations related to the Messages table.
 */
public class MessageDAO {

    private static final Logger LOGGER = Logger.getLogger(MessageDAO.class.getName());

    /**
     * Adds a new message to the database.
     *
     * @param message The Message object to add.
     * @return true if the message was added successfully, false otherwise.
     */
    public boolean addMessage(Message message) {
        String sql = "INSERT INTO Messages (SenderUserID, RecipientUserID, CourseContextID, Subject, Body, IsRead) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1, message.getSenderUserID());
            pstmt.setInt(2, message.getRecipientUserID());
            setNullableInt(pstmt, 3, message.getCourseContextID());
            setNullableString(pstmt, 4, message.getSubject());
            pstmt.setString(5, message.getBody());
            pstmt.setBoolean(6, message.isRead()); // Set initial read status

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    message.setMessageID(generatedKeys.getInt(1)); // Set the generated ID
                    // Assuming SentTimestamp is handled by DB default
                    success = true;
                    LOGGER.log(Level.INFO, "Message added successfully with ID: {0}", message.getMessageID());
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding message from sender " + message.getSenderUserID() + " to recipient " + message.getRecipientUserID(), e);
        } finally {
            try { if (generatedKeys != null) generatedKeys.close(); } catch (SQLException e) { LOGGER.log(Level.WARNING, "Error closing generatedKeys", e); }
            DatabaseConnection.closeResources(conn, pstmt);
        }
        return success;
    }

    /**
     * Retrieves messages received by a specific user.
     *
     * @param recipientUserId The UserID of the recipient.
     * @param unreadOnly If true, retrieves only unread messages; otherwise, retrieves all.
     * @return A list of Message objects.
     */
    public List<Message> getMessagesByRecipient(int recipientUserId, boolean unreadOnly) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM Messages WHERE RecipientUserID = ?");
        if (unreadOnly) {
            sqlBuilder.append(" AND IsRead = FALSE");
        }
        sqlBuilder.append(" ORDER BY SentTimestamp DESC");

        String sql = sqlBuilder.toString();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Message> messages = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, recipientUserId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving messages for recipient ID: " + recipientUserId + (unreadOnly ? " (unread only)" : ""), e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return messages;
    }

    /**
     * Retrieves messages sent by a specific user.
     *
     * @param senderUserId The UserID of the sender.
     * @return A list of Message objects.
     */
    public List<Message> getMessagesBySender(int senderUserId) {
        String sql = "SELECT * FROM Messages WHERE SenderUserID = ? ORDER BY SentTimestamp DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Message> messages = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, senderUserId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving messages sent by sender ID: " + senderUserId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return messages;
    }

    /**
     * Retrieves a specific message by its ID.
     *
     * @param messageId The ID of the message.
     * @return The Message object, or null if not found.
     */
    public Message getMessageById(int messageId) {
        String sql = "SELECT * FROM Messages WHERE MessageID = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Message message = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, messageId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                message = mapResultSetToMessage(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving message by ID: " + messageId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt, rs);
        }
        return message;
    }

    /**
     * Marks a specific message as read.
     *
     * @param messageId The ID of the message to mark as read.
     * @return true if the message was marked as read successfully, false otherwise.
     */
    public boolean markAsRead(int messageId) {
        String sql = "UPDATE Messages SET IsRead = TRUE WHERE MessageID = ? AND IsRead = FALSE"; // Only update if currently unread
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, messageId);

            int affectedRows = pstmt.executeUpdate();
            success = affectedRows > 0;
            if (success) {
                LOGGER.log(Level.INFO, "Message marked as read: ID {0}", messageId);
            } else {
                 LOGGER.log(Level.INFO, "Message not marked as read (may not exist or already read): ID {0}", messageId);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marking message as read: ID " + messageId, e);
        } finally {
            DatabaseConnection.closeResources(conn, pstmt);
        }
        return success;
    }
    
    /**
     * Deletes a message by its ID.
     *
     * @param messageId The ID of the message to delete.
     * @return true if deleted successfully, false otherwise.
     */
    public boolean deleteMessage(int messageId) {
        String sql = "DELETE FROM Messages WHERE MessageID = ?";
         Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;
        try {
             conn = DatabaseConnection.getConnection();
             pstmt = conn.prepareStatement(sql);
             pstmt.setInt(1, messageId);
             int affectedRows = pstmt.executeUpdate();
             success = affectedRows > 0;
             if (success) {
                 LOGGER.log(Level.INFO, "Message deleted: ID {0}", messageId);
             } else {
                  LOGGER.log(Level.WARNING, "Message deletion failed or not found: ID {0}", messageId);
             }
         } catch (SQLException e) {
             LOGGER.log(Level.SEVERE, "Error deleting message: ID " + messageId, e);
         } finally {
             DatabaseConnection.closeResources(conn, pstmt);
         } 
         return success;
    }

    // --- Helper Methods ---

    /**
     * Maps a row from the ResultSet to a Message object.
     *
     * @param rs The ResultSet cursor, positioned at a valid row.
     * @return A Message object populated with data from the current row.
     * @throws SQLException if a database access error occurs.
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setMessageID(rs.getInt("MessageID"));
        message.setSenderUserID(rs.getInt("SenderUserID"));
        message.setRecipientUserID(rs.getInt("RecipientUserID"));
        message.setCourseContextID(rs.getObject("CourseContextID", Integer.class)); // Handle nullable INT
        message.setSubject(rs.getString("Subject"));
        message.setBody(rs.getString("Body"));
        message.setSentTimestamp(rs.getTimestamp("SentTimestamp"));
        message.setRead(rs.getBoolean("IsRead"));
        return message;
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
    
     // --- Simple DAO Tester --- 

    /**
     * Simple main method to test MessageDAO operations.
     * Assumes database setup and potentially existing users/courses.
     * IMPORTANT: This WILL modify the database.
     */
    public static void main(String[] args) {
        System.out.println("Testing MessageDAO...");
        MessageDAO messageDAO = new MessageDAO();
        UserDAO userDAO = new UserDAO();
        CourseDAO courseDAO = new CourseDAO(); // Optional context

        // --- Test Data --- 
        User sender = null;
        User recipient = null;
        Course courseContext = null;
        int testMessageId = -1;

        String senderEmail = "msg-sender@example.com";
        String recipientEmail = "msg-recipient@example.com";
        String courseCode = "MSGTEST101";
        
        try {
            // 1. Ensure Sender Exists
            System.out.println("\n1. Ensuring sender user exists...");
            sender = userDAO.getUserByEmail(senderEmail);
            if (sender == null) {
                sender = new User(0, "Msg", "Sender", senderEmail, "passSend", null, UserRole.STUDENT, null, null, null, null, null, null);
                if (!userDAO.addUser(sender)) throw new RuntimeException("Failed to create sender user");
                System.out.println("   Created sender ID: " + sender.getUserID());
            } else {
                 System.out.println("   Found sender ID: " + sender.getUserID());
            }

            // 2. Ensure Recipient Exists
             System.out.println("\n2. Ensuring recipient user exists...");
             recipient = userDAO.getUserByEmail(recipientEmail);
            if (recipient == null) {
                recipient = new User(0, "Msg", "Recipient", recipientEmail, "passRecv", null, UserRole.TEACHER, "Testing", null, null, null, null, null);
                if (!userDAO.addUser(recipient)) throw new RuntimeException("Failed to create recipient user");
                 System.out.println("   Created recipient ID: " + recipient.getUserID());
            } else {
                 System.out.println("   Found recipient ID: " + recipient.getUserID());
            }
            
             // 3. Ensure Course Context Exists
             System.out.println("\n3. Ensuring course context exists (optional)...");
             courseContext = courseDAO.getCourseByCode(courseCode);
              if (courseContext == null) {
                 courseContext = new Course(0, courseCode, "Message Test Course", 5, recipient.getUserID(), null, null);
                 if (!courseDAO.addCourse(courseContext)) System.out.println("   Warning: Failed to create test course context.");
                 else System.out.println("   Created course context ID: " + courseContext.getCourseID());
             } else {
                  System.out.println("   Found course context ID: " + courseContext.getCourseID());
             }

            // 4. Add a new message
            System.out.println("\n4. Adding test message...");
            Message newMessage = new Message(0, sender.getUserID(), recipient.getUserID(), 
                                           (courseContext != null ? courseContext.getCourseID() : null), 
                                           "Test Subject", "This is the test message body.", null, false);
            boolean added = messageDAO.addMessage(newMessage);
            System.out.println("   Message add success? " + added);
            if (!added) throw new RuntimeException("Failed to add message");
            testMessageId = newMessage.getMessageID();
             System.out.println("   Added Message ID: " + testMessageId);

             final int finalTestMessageId = testMessageId; // Create final variable for lambdas

             // 5. Get messages for recipient (unread)
            System.out.println("\n5. Getting unread messages for recipient " + recipient.getUserID() + "...");
            List<Message> unreadMessages = messageDAO.getMessagesByRecipient(recipient.getUserID(), true);
            System.out.println("   Found " + unreadMessages.size() + " unread messages.");
            if (unreadMessages.stream().noneMatch(m -> m.getMessageID() == finalTestMessageId)) {
                System.err.println("   Error: Added message not found in unread list!");
            }
            for(Message m : unreadMessages) System.out.println("     " + m.getSubject() + " (Read: " + m.isRead() + ")");
            
             // 6. Get all messages for recipient
            System.out.println("\n6. Getting all messages for recipient " + recipient.getUserID() + "...");
            List<Message> allMessages = messageDAO.getMessagesByRecipient(recipient.getUserID(), false);
            System.out.println("   Found " + allMessages.size() + " total messages.");
             if (allMessages.stream().noneMatch(m -> m.getMessageID() == finalTestMessageId)) {
                System.err.println("   Error: Added message not found in all messages list!");
            }

            // 7. Get messages by sender
            System.out.println("\n7. Getting messages sent by sender " + sender.getUserID() + "...");
            List<Message> sentMessages = messageDAO.getMessagesBySender(sender.getUserID());
            System.out.println("   Found " + sentMessages.size() + " sent messages.");
            if (sentMessages.stream().noneMatch(m -> m.getMessageID() == finalTestMessageId)) {
                System.err.println("   Error: Added message not found in sent list!");
            }

            // 8. Mark message as read
            System.out.println("\n8. Marking message " + finalTestMessageId + " as read...");
            boolean markedRead = messageDAO.markAsRead(finalTestMessageId);
            System.out.println("   Mark as read success? " + markedRead);
            if (!markedRead) {
                 System.err.println("   Error: Failed to mark message as read!");
            }

             // 9. Get unread messages again (should be fewer or none)
            System.out.println("\n9. Getting unread messages again for recipient " + recipient.getUserID() + "...");
            unreadMessages = messageDAO.getMessagesByRecipient(recipient.getUserID(), true);
            System.out.println("   Found " + unreadMessages.size() + " unread messages.");
            if (unreadMessages.stream().anyMatch(m -> m.getMessageID() == finalTestMessageId)) {
                System.err.println("   Error: Message should NOT be in unread list after marking read!");
            }
            
            // 10. Delete the message
             System.out.println("\n10. Deleting test message " + finalTestMessageId + "...");
            boolean deleted = messageDAO.deleteMessage(finalTestMessageId);
            System.out.println("   Delete success? " + deleted); 
             if (!deleted) {
                 System.err.println("   Error: Failed to delete message!");
             }

        } catch (Exception e) {
            System.err.println("\nAn unexpected error occurred during MessageDAO testing:");
            e.printStackTrace();
        } finally {
             System.out.println("\nMessageDAO testing finished.");
        }
    }
} 