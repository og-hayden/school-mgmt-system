package com.school.management.view.teacher;

import com.school.management.model.entities.Message;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Panel for teachers to view received messages.
 */
public class TeacherMessagesPanel extends JPanel {

    private JTable messageTable;
    private DefaultTableModel tableModel;
    private JTextArea messageBodyArea;
    private JButton markReadButton;
    private JButton replyButton;
    private JButton backButton;
    private JSplitPane splitPane;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    // Define column names
    private final String[] columnNames = {"MsgID", "From", "Subject", "Course", "Date", "Read"};
    private final int MESSAGE_ID_COL = 0;
    private final int READ_STATUS_COL = 5;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public TeacherMessagesPanel() {
        initComponents();
        layoutComponents();
        setupListeners();
    }

    private void initComponents() {
        // Non-editable table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                 if (columnIndex == MESSAGE_ID_COL) {
                    return Integer.class;
                 } else if (columnIndex == READ_STATUS_COL) {
                     return Boolean.class;
                 }
                 return String.class;
            }
        };
        messageTable = new JTable(tableModel);

        // Hide the ID column visually
        messageTable.getColumnModel().getColumn(MESSAGE_ID_COL).setMinWidth(0);
        messageTable.getColumnModel().getColumn(MESSAGE_ID_COL).setMaxWidth(0);
        messageTable.getColumnModel().getColumn(MESSAGE_ID_COL).setWidth(0);
        
        // Custom renderer for the "Read" column to show bold for unread
        messageTable.setDefaultRenderer(Object.class, new UnreadMessageRenderer());

        // Allow single row selection
        messageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Initialize the sorter
        sorter = new TableRowSorter<>(tableModel);
        messageTable.setRowSorter(sorter);

        messageBodyArea = new JTextArea();
        messageBodyArea.setEditable(false);
        messageBodyArea.setLineWrap(true);
        messageBodyArea.setWrapStyleWord(true);
        
        markReadButton = new JButton("Mark as Read");
        markReadButton.setEnabled(false);
        replyButton = new JButton("Reply");
        replyButton.setEnabled(false);
        
        backButton = new JButton("Back to Dashboard"); // Or specific dashboard name
        searchField = new JTextField(20); // Initialize search field

        // Add listener to search field
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // Title
        JLabel titleLabel = new JLabel("Received Messages", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // --- Left Panel (Table + Search) ---
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        JScrollPane tableScrollPane = new JScrollPane(messageTable);
        tableScrollPane.setPreferredSize(new Dimension(400, 400)); // Keep preferred size
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);

        // --- Right Panel (Body) ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel(" Message Content:", SwingConstants.LEFT), BorderLayout.NORTH);
        rightPanel.add(messageBodyArea, BorderLayout.CENTER);

        // Split Pane - Use leftPanel now
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(410); // Initial divider location
        splitPane.setResizeWeight(0.4); // How space is distributed on resize
        add(splitPane, BorderLayout.CENTER);

        // Button Panel (South)
        JPanel southPanel = new JPanel(new BorderLayout(10, 5));
        
        // Action buttons
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionButtonPanel.add(markReadButton);
        actionButtonPanel.add(replyButton);
        southPanel.add(actionButtonPanel, BorderLayout.CENTER);

        // Back button
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.add(backButton);
        southPanel.add(navigationPanel, BorderLayout.WEST);

        add(southPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void setupListeners() {
        // Update message body and button state when selection changes
        messageTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = messageTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // Get MessageID from hidden column
                    int modelRow = messageTable.convertRowIndexToModel(selectedRow);
                    int messageId = (Integer) tableModel.getValueAt(modelRow, MESSAGE_ID_COL);
                    boolean isRead = (Boolean) tableModel.getValueAt(modelRow, READ_STATUS_COL);
                    
                    markReadButton.setEnabled(!isRead);
                    replyButton.setEnabled(true);
                } else {
                    markReadButton.setEnabled(false);
                    replyButton.setEnabled(false);
                    messageBodyArea.setText(""); // Clear body if no selection
                }
            }
        });
    }

    // --- Data Handling ---

    /**
     * Populates the messages table.
     *
     * @param messages The list of Message objects to display.
     * @param senderNames Map of Sender UserID -> "FirstName LastName".
     * @param courseNames Map of Course Context ID -> Course Name (or Code).
     */
    public void displayMessages(List<Message> messages, Map<Integer, String> senderNames, Map<Integer, String> courseNames) {
        tableModel.setRowCount(0); // Clear existing data
        messageBodyArea.setText(""); // Clear body view
        markReadButton.setEnabled(false); // Disable button
        
        if (messages != null) {
            for (Message message : messages) {
                String senderDisplay = senderNames != null && senderNames.containsKey(message.getSenderUserID()) 
                                     ? senderNames.get(message.getSenderUserID()) 
                                     : "User ID: " + message.getSenderUserID();
                
                String courseDisplay = message.getCourseContextID() != null && courseNames != null && courseNames.containsKey(message.getCourseContextID()) 
                                     ? courseNames.get(message.getCourseContextID())
                                     : "(General)";
                                     
                String dateDisplay = message.getSentTimestamp() != null 
                                     ? DATE_FORMAT.format(message.getSentTimestamp()) 
                                     : "";

                Object[] rowData = {
                        message.getMessageID(),
                        senderDisplay,
                        message.getSubject(),
                        courseDisplay,
                        dateDisplay,
                        message.isRead()
                };
                tableModel.addRow(rowData);
            }
        }
    }
    
    /**
     * Updates the body text area with the content of the selected message.
     * @param body The message content.
     */
    public void displayMessageBody(String body) {
        messageBodyArea.setText(body != null ? body : "");
        messageBodyArea.setCaretPosition(0); // Scroll to top
    }
    
     /**
     * Updates the read status of a specific row in the table model.
     * @param messageId The ID of the message to mark as read.
     */
     public void markMessageAsReadInTable(int messageId) {
         for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (messageId == (Integer) tableModel.getValueAt(i, MESSAGE_ID_COL)) {
                tableModel.setValueAt(true, i, READ_STATUS_COL);
                markReadButton.setEnabled(false); // Disable after marking read
                messageTable.repaint(); // Repaint to show style change
                break;
            }
         }
     }

    /**
     * Gets the MessageID of the currently selected row in the table.
     *
     * @return The selected MessageID, or -1 if no row is selected.
     */
    public int getSelectedMessageId() {
        int selectedRow = messageTable.getSelectedRow();
        if (selectedRow >= 0) {
             int modelRow = messageTable.convertRowIndexToModel(selectedRow);
             Object idObj = tableModel.getValueAt(modelRow, MESSAGE_ID_COL);
            if (idObj instanceof Integer) {
                return (Integer) idObj;
            }
        }
        return -1; // No selection or ID not found/invalid
    }

    // --- Action Listeners ---

    public void addMarkReadListener(ActionListener listener) {
        markReadButton.addActionListener(listener);
        markReadButton.setActionCommand("Mark Read");
    }

    public void addReplyListener(ActionListener listener) {
        replyButton.addActionListener(listener);
        replyButton.setActionCommand("Reply To Message");
    }

    public void addBackListener(ActionListener listener) {
        backButton.addActionListener(listener);
         // Use a specific command if needed for TeacherController
        backButton.setActionCommand("Back To Teacher Dashboard From Messages"); 
    }
    
     // --- Getters for Controller --- 
    public JPanel getPanel() {
         return this;
     }
     
    public JTable getMessageTable() {
        return messageTable;
    }

    // --- Custom Cell Renderer --- 
    private class UnreadMessageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                                                   boolean isSelected, boolean hasFocus, 
                                                   int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Check the read status from the hidden column
             int modelRow = table.convertRowIndexToModel(row);
             boolean isRead = (Boolean) table.getModel().getValueAt(modelRow, READ_STATUS_COL);
            
            if (!isRead) {
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            } else {
                c.setFont(c.getFont().deriveFont(Font.PLAIN));
            }
            
            return c;
        }
    }

    /**
     * Filters the messages table based on the text in the search field.
     * Searches across From, Subject, and Course columns.
     */
    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null); // No filter
        } else {
            // Case-insensitive search across From (1), Subject (2), Course (3)
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1, 2, 3));
        }
    }

    // --- Main method for visual testing ---
    public static void main(String[] args) {
        // Set Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Nimbus L&F not found, using default.");
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Teacher Messages Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            TeacherMessagesPanel messagesPanel = new TeacherMessagesPanel();

            // Add Dummy Data for Testing
            // Use ArrayList for dummy data creation
            List<Message> dummyMessages = new ArrayList<>();
            dummyMessages.add(new Message(1, 10, 5, 1, "Question about CS101 Homework", "Hi Professor...", new Timestamp(System.currentTimeMillis() - 86400000), false)); // Unread, 1 day ago
            dummyMessages.add(new Message(2, 15, 5, null, "General Inquiry", "Hello...", new Timestamp(System.currentTimeMillis() - 172800000), true)); // Read, 2 days ago
            dummyMessages.add(new Message(3, 10, 5, 1, "Follow-up on CS101", "Thanks for the help...", new Timestamp(System.currentTimeMillis() - 3600000), false)); // Unread, 1 hour ago
            
            Map<Integer, String> dummySenders = Map.of(
                10, "Alice Wonder", 
                15, "Bob Builder"
            );
            Map<Integer, String> dummyCourses = Map.of(
                1, "CS101"
            );
            
            messagesPanel.displayMessages(dummyMessages, dummySenders, dummyCourses);
            
             // Add listener to display message body (simplified for test)
             messagesPanel.messageTable.getSelectionModel().addListSelectionListener(e -> {
                 if (!e.getValueIsAdjusting() && messagesPanel.messageTable.getSelectedRow() >= 0) {
                     int msgId = messagesPanel.getSelectedMessageId();
                     // Find the message body in dummy data
                     dummyMessages.stream()
                         .filter(m -> m.getMessageID() == msgId)
                         .findFirst()
                         .ifPresent(m -> messagesPanel.displayMessageBody(m.getBody()));
                 }
             });

            // Add simple listeners for buttons
            messagesPanel.addMarkReadListener(e -> {
                 int msgId = messagesPanel.getSelectedMessageId();
                 System.out.println("Mark Read clicked for Message ID: " + msgId);
                 if (msgId != -1) {
                     messagesPanel.markMessageAsReadInTable(msgId);
                 }
            });
            messagesPanel.addReplyListener(e -> System.out.println("Reply button clicked"));
            messagesPanel.addBackListener(e -> System.out.println("Back button clicked"));

            frame.getContentPane().add(messagesPanel.getPanel());
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
} 