package com.school.management.service;

import com.school.management.model.dao.MessageDAO;
import com.school.management.model.entities.Message;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageServiceImpl implements MessageService {

    private static final Logger LOGGER = Logger.getLogger(MessageServiceImpl.class.getName());
    private final MessageDAO messageDAO;

    public MessageServiceImpl(MessageDAO messageDAO) {
        this.messageDAO = messageDAO;
    }

    @Override
    public List<Message> getMessagesForRecipient(int recipientId) {
        try {
            return messageDAO.getMessagesByRecipient(recipientId, false);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching messages for recipient ID: " + recipientId, e);
            throw new RuntimeException("Error retrieving messages: " + e.getMessage(), e);
        }
    }

    @Override
    public Message getMessageById(int messageId) {
        try {
            return messageDAO.getMessageById(messageId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching message by ID: " + messageId, e);
            throw new RuntimeException("Error retrieving message by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public void markMessageAsRead(int messageId) {
        try {
            boolean success = messageDAO.markAsRead(messageId);
            if (!success) {
                LOGGER.log(Level.WARNING, "Failed to mark message as read, ID: {0}. Message might not exist.", messageId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error marking message as read, ID: " + messageId, e);
            throw new RuntimeException("Error updating message status: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendMessage(Message message) {
        try {
            boolean success = messageDAO.addMessage(message);
            if (!success) {
                LOGGER.log(Level.WARNING, "Failed to add message via DAO (Sender: {0}, Recipient: {1})", 
                            new Object[]{message.getSenderUserID(), message.getRecipientUserID()});
                throw new RuntimeException("Failed to send message (DAO operation failed).");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending message (Service Level)", e);
            throw new RuntimeException("Error sending message: " + e.getMessage(), e);
        }
    }
} 