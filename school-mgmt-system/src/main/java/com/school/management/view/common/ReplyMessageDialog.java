package com.school.management.view.common;

import com.school.management.model.entities.Message;
import com.school.management.model.entities.User;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * A dialog for composing and sending replies to messages.
 * Primarily intended for teachers replying to students based on an original message.
 */
public class ReplyMessageDialog extends JDialog {

    private JLabel recipientLabel;
    private JTextField subjectField;
    private JTextArea messageBodyArea;
    private JButton sendButton;
    private JButton cancelButton;

    // Store IDs for the controller
    private int recipientUserId = -1; // The student who sent the original message
    private Integer courseContextId = null; // Course context from original message

    /**
     * Constructor for the Reply Dialog.
     * @param owner The parent frame.
     * @param originalSender The student who sent the original message (the recipient of the reply).
     * @param originalMessage The message being replied to.
     */
    public ReplyMessageDialog(Frame owner, User originalSender, Message originalMessage) {
        super(owner, "Reply to Message", true); // Modal dialog
        if (originalSender == null || originalMessage == null) {
            throw new IllegalArgumentException("Original sender and message cannot be null for replying.");
        }
        this.recipientUserId = originalSender.getUserID();
        this.courseContextId = originalMessage.getCourseContextID(); // Preserve course context if it exists
        
        initComponents(originalSender, originalMessage);
        layoutComponents();
        pack(); // Adjust dialog size to fit components
        setLocationRelativeTo(owner); // Center relative to the owner frame
    }

    private void initComponents(User originalSender, Message originalMessage) {
        String recipientName = originalSender.getFirstName() + " " + originalSender.getLastName();
        
        // Pre-fill subject with "Re: [Original Subject]"
        String originalSubject = originalMessage.getSubject() != null ? originalMessage.getSubject() : "";
        String replySubject = originalSubject.toLowerCase().startsWith("re:") ? originalSubject : "Re: " + originalSubject;
        
        recipientLabel = new JLabel("To: " + recipientName);
        subjectField = new JTextField(replySubject, 40); 
        messageBodyArea = new JTextArea(10, 40);
        messageBodyArea.setLineWrap(true);
        messageBodyArea.setWrapStyleWord(true);

        sendButton = new JButton("Send Reply");
        cancelButton = new JButton("Cancel");
        
        cancelButton.addActionListener(e -> dispose()); // Default cancel action
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.add(recipientLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel (Subject and Body)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        centerPanel.add(subjectField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.NORTHEAST; // Align label top-right relative to text area
        centerPanel.add(new JLabel("Message:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0; gbc.weightx = 1.0; // Ensure text area expands
        JScrollPane scrollPane = new JScrollPane(messageBodyArea);
        centerPanel.add(scrollPane, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Button Panel (South)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(sendButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // --- Getters for Controller --- 

    /**
     * Gets the UserID of the intended recipient (the original sender).
     * @return The UserID of the recipient.
     */
    public int getRecipientUserId() {
        return recipientUserId;
    }

    /**
     * Gets the optional CourseContextID from the original message.
     * @return The CourseID, or null if the original message had no course context.
     */
    public Integer getCourseContextId() {
        return courseContextId;
    }

    public String getSubject() {
        return subjectField.getText().trim();
    }

    public String getBody() {
        return messageBodyArea.getText().trim();
    }

    // --- Listeners --- 

    /**
     * Adds an ActionListener to the Send button.
     * @param listener The listener to add.
     */
    public void addSendListener(ActionListener listener) {
        sendButton.addActionListener(listener);
        // Set a specific command for the controller to identify
        sendButton.setActionCommand("Send Reply Dialog Send"); 
    }

    // --- Utility --- 

    /**
     * Displays an error message related to a specific field or general input.
     * @param field The field name (or a general description).
     * @param message The error message text.
     */
    public void displayError(String field, String message) {
        JOptionPane.showMessageDialog(this, field + ": " + message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Closes the dialog window.
     */
    public void closeDialog() {
        dispose();
    }
} 