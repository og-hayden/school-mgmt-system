package com.school.management.service;

import com.school.management.model.entities.Message;

import java.util.List;

/**
 * Service layer interface for Message related operations.
 */
public interface MessageService {

    /**
     * Retrieves all messages for a specific recipient user ID.
     *
     * @param recipientId The User ID of the recipient.
     * @return A list of Message objects. Returns an empty list if no messages are found.
     * @throws RuntimeException if there is an error retrieving messages.
     */
    List<Message> getMessagesForRecipient(int recipientId);

    /**
     * Retrieves a specific message by its ID.
     *
     * @param messageId The ID of the message.
     * @return The Message object, or null if not found.
     * @throws RuntimeException if there is an error retrieving the message.
     */
    Message getMessageById(int messageId);

    /**
     * Marks a specific message as read.
     *
     * @param messageId The ID of the message to mark as read.
     * @throws RuntimeException if there is an error updating the message status or if the message doesn't exist.
     */
    void markMessageAsRead(int messageId);
    
    /**
     * Sends a new message (adds it to the database).
     *
     * @param message The Message object to send.
     * @throws RuntimeException if there is an error sending the message.
     */
    void sendMessage(Message message);
} 