package com.school.management.model.entities;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents a Message entity, mapping to the 'Messages' table.
 * Represents communication between users within the system.
 */
public class Message {

    private int messageID;
    private int senderUserID;
    private int recipientUserID;
    private Integer courseContextID; // Nullable foreign key
    private String subject; // Nullable
    private String body;
    private Timestamp sentTimestamp;
    private boolean read; // Maps to IsRead in DB, standard boolean naming

    // Default constructor
    public Message() {
    }

    // Full constructor
    public Message(int messageID, int senderUserID, int recipientUserID, 
                   Integer courseContextID, String subject, String body, 
                   Timestamp sentTimestamp, boolean read) {
        this.messageID = messageID;
        this.senderUserID = senderUserID;
        this.recipientUserID = recipientUserID;
        this.courseContextID = courseContextID;
        this.subject = subject;
        this.body = body;
        this.sentTimestamp = sentTimestamp;
        this.read = read;
    }

    // Getters
    public int getMessageID() {
        return messageID;
    }

    public int getSenderUserID() {
        return senderUserID;
    }

    public int getRecipientUserID() {
        return recipientUserID;
    }

    public Integer getCourseContextID() {
        return courseContextID;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public Timestamp getSentTimestamp() {
        return sentTimestamp;
    }

    public boolean isRead() { // Standard getter name for boolean
        return read;
    }

    // Setters
    public void setMessageID(int messageID) { 
        this.messageID = messageID;
    }

    public void setSenderUserID(int senderUserID) {
        this.senderUserID = senderUserID;
    }

    public void setRecipientUserID(int recipientUserID) {
        this.recipientUserID = recipientUserID;
    }

    public void setCourseContextID(Integer courseContextID) {
        this.courseContextID = courseContextID;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setSentTimestamp(Timestamp sentTimestamp) { 
        this.sentTimestamp = sentTimestamp;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    // toString, equals, hashCode

    @Override
    public String toString() {
        return "Message{" + "messageID=" + messageID + ", senderUserID=" + senderUserID + ", recipientUserID=" + recipientUserID + ", courseContextID=" + courseContextID + ", subject='" + subject + '\'' + ", sentTimestamp=" + sentTimestamp + ", isRead=" + read + '}'; // Body excluded for brevity
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        // Check unique identifier (messageID)
        return messageID == message.messageID;
    }

    @Override
    public int hashCode() {
        // Use unique identifier
        return Objects.hash(messageID);
    }
    
    // --- Simple Interactive Tester --- 

    public static void main(String[] args) {
        System.out.println("Testing Message Entity...");

        Timestamp now = new Timestamp(System.currentTimeMillis());

        // Test Message creation using setters
        Message message1 = new Message();
        message1.setMessageID(501);
        message1.setSenderUserID(101); // Student
        message1.setRecipientUserID(102); // Teacher
        message1.setCourseContextID(1); // Course CS101
        message1.setSubject("Question about Assignment 1");
        message1.setBody("Dear Professor Johnson, I have a question regarding...");
        message1.setSentTimestamp(now);
        message1.setRead(false);

        System.out.println("\nCreated Message 1 via setters:");
        System.out.println("  Message ID: " + message1.getMessageID());
        System.out.println("  Sender ID: " + message1.getSenderUserID());
        System.out.println("  Recipient ID: " + message1.getRecipientUserID());
        System.out.println("  Course Context ID: " + message1.getCourseContextID());
        System.out.println("  Subject: " + message1.getSubject());
        // System.out.println("  Body: " + message1.getBody()); // Often long
        System.out.println("  Sent: " + message1.getSentTimestamp());
        System.out.println("  Is Read: " + message1.isRead());
        System.out.println("  toString(): " + message1.toString());

        // Test full constructor (e.g., system message, no course context, no subject)
        Message message2 = new Message(502, 1, 101, null, null, "Your enrollment in CS101 is confirmed.", now, true);
        Message message3 = new Message(501, 101, 102, 1, "Slightly different subject", "Body...", now, false); // Same ID as message1

        System.out.println("\nCreated Message 2 (System Msg) via full constructor:");
        System.out.println("  toString(): " + message2.toString());
        
        System.out.println("\nTesting equals and hashCode:");
        System.out.println("  Message1 equals Message2? " + message1.equals(message2)); // Should be false
        System.out.println("  Message1 equals Message3? " + message1.equals(message3)); // Should be true (by ID)
        System.out.println("  Message1 hashCode: " + message1.hashCode());
        System.out.println("  Message2 hashCode: " + message2.hashCode());
        System.out.println("  Message3 hashCode: " + message3.hashCode());
        System.out.println("  (Message1 and Message3 hashCodes should match)");

        System.out.println("\nTesting complete.");
    }
} 