package com.school.management.view.common;

import com.school.management.model.entities.Course;
import com.school.management.model.entities.User;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * A dialog for composing and sending messages.
 * Primarily intended for students messaging teachers within a course context.
 */
public class SendMessageDialog extends JDialog {

    private JLabel recipientLabel;
    private JTextField subjectField;
    private JTextArea messageBodyArea;
    private JButton sendButton;
    private JButton cancelButton;

    // Store IDs for the controller
    private int recipientUserId = -1;
    private Integer courseContextId = null; // Course context is optional

    public SendMessageDialog(Frame owner, User teacher, Course course) {
        super(owner, "Send Message", true); // Modal dialog
        initComponents(teacher, course);
        layoutComponents();
        pack(); // Adjust dialog size to fit components
        setLocationRelativeTo(owner); // Center relative to the owner frame
    }

    private void initComponents(User teacher, Course course) {
        String recipientName = (teacher != null) ? (teacher.getFirstName() + " " + teacher.getLastName()) : "Unknown Teacher";
        String courseInfo = (course != null) ? ("Re: " + course.getCourseCode() + " - " + course.getName()) : "General Inquiry";
        
        recipientLabel = new JLabel("To: " + recipientName);
        subjectField = new JTextField(courseInfo, 40); // Pre-fill subject based on context
        messageBodyArea = new JTextArea(10, 40);
        messageBodyArea.setLineWrap(true);
        messageBodyArea.setWrapStyleWord(true);

        sendButton = new JButton("Send");
        cancelButton = new JButton("Cancel");

        // Store IDs
        if (teacher != null) {
            this.recipientUserId = teacher.getUserID();
        }
        if (course != null) {
            this.courseContextId = course.getCourseID();
        }
        
        // Add listeners for buttons (typically added by the controller)
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

        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(new JLabel("Message:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
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

    public int getRecipientUserId() {
        return recipientUserId;
    }

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

    public void addSendListener(ActionListener listener) {
        sendButton.addActionListener(listener);
        // Set a default command if needed, or let controller handle
        sendButton.setActionCommand("Send Message Dialog Send");
    }

    // --- Utility --- 

    public void displayError(String field, String message) {
        // Simple error display for now
        JOptionPane.showMessageDialog(this, field + ": " + message, "Input Error", JOptionPane.ERROR_MESSAGE);
        // Could highlight the field if needed
    }

    public void closeDialog() {
        dispose();
    }
} 