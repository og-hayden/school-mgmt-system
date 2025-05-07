package com.school.management.view.common;

import com.school.management.model.dao.UserDAO;
import com.school.management.model.entities.User;
import com.school.management.model.entities.UserRole;
import com.school.management.util.FileUtil;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A panel to display user profile information and allow actions like changing password or uploading a picture.
 */
public class UserProfilePanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(UserProfilePanel.class.getName());
    private static final int PROFILE_PIC_SIZE = 128; // Size for the display label
    private static final String PROFILE_PICTURES_DIR = "data/profile_pictures"; // Directory to store profile pictures

    // User object
    private User currentUser; // Added: Store the current user

    // Profile Picture components
    private JLabel profilePictureLabel;
    private JButton uploadPictureButton;

    // User Info Labels (using JLabels for display only initially)
    private JLabel nameLabel;
    private JLabel emailLabel;
    private JLabel roleLabel;
    private JLabel departmentLabel; // Only visible for Teachers

    // Action Buttons
    private JButton changePasswordButton;
    private JButton backButton; // To go back to the specific role dashboard

    // DAO for database operations
    private UserDAO userDAO; // Added

    public UserProfilePanel() {
        this.userDAO = new UserDAO(); // Initialize DAO
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        // Profile Picture Area
        profilePictureLabel = new JLabel();
        profilePictureLabel.setPreferredSize(new Dimension(PROFILE_PIC_SIZE, PROFILE_PIC_SIZE));
        profilePictureLabel.setMinimumSize(new Dimension(PROFILE_PIC_SIZE, PROFILE_PIC_SIZE));
        profilePictureLabel.setMaximumSize(new Dimension(PROFILE_PIC_SIZE, PROFILE_PIC_SIZE));
        profilePictureLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        profilePictureLabel.setHorizontalAlignment(JLabel.CENTER);
        profilePictureLabel.setVerticalAlignment(JLabel.CENTER);
        profilePictureLabel.setText("No Picture"); // Placeholder text
        profilePictureLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        
        uploadPictureButton = new JButton("Upload Picture");

        // Info Labels
        nameLabel = new JLabel("Name: ");
        emailLabel = new JLabel("Email: ");
        roleLabel = new JLabel("Role: ");
        departmentLabel = new JLabel("Department: ");
        departmentLabel.setVisible(false); // Hide by default

        // Action Buttons
        changePasswordButton = new JButton("Change Password");
        backButton = new JButton("Back"); // Back button action command set by controller
    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Profile Picture (Top Left)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 4; // Span multiple rows
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(profilePictureLabel, gbc);
        
        // Reset grid height
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor

        // Name
        gbc.gridx = 1; gbc.gridy = 0;
        add(nameLabel, gbc);

        // Email
        gbc.gridx = 1; gbc.gridy = 1;
        add(emailLabel, gbc);

        // Role
        gbc.gridx = 1; gbc.gridy = 2;
        add(roleLabel, gbc);

        // Department (conditionally visible)
        gbc.gridx = 1; gbc.gridy = 3;
        add(departmentLabel, gbc);

        // Buttons Panel (Bottom, spans across)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.add(changePasswordButton);
        buttonsPanel.add(uploadPictureButton);
        buttonsPanel.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2; // Span across picture and text columns
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(buttonsPanel, gbc);
        
        // Add glue to push components to the top and left if the panel is larger
        gbc.gridx = 2; gbc.gridy = 5; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        add(Box.createGlue(), gbc);
    }

    // --- Data Population --- 
    public void setUserProfile(User user) {
        this.currentUser = user; // Store the user

        if (user == null) {
            LOGGER.warning("Attempted to set user profile with null user.");
            // Clear fields or show placeholder text
            nameLabel.setText("Name: N/A");
            emailLabel.setText("Email: N/A");
            roleLabel.setText("Role: N/A");
            departmentLabel.setVisible(false);
            profilePictureLabel.setIcon(null);
            profilePictureLabel.setText("No Picture");
            return;
        }

        nameLabel.setText("Name: " + user.getFirstName() + " " + user.getLastName());
        emailLabel.setText("Email: " + user.getEmail());
        roleLabel.setText("Role: " + user.getRole());

        if (user.getRole() == UserRole.TEACHER && user.getDepartment() != null && !user.getDepartment().isEmpty()) {
            departmentLabel.setText("Department: " + user.getDepartment());
            departmentLabel.setVisible(true);
        } else {
            departmentLabel.setVisible(false);
        }
        
        // Load Profile Picture
        loadProfilePicture(user.getProfilePicturePath());
    }
    
    private void loadProfilePicture(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists() && imageFile.isFile()) {
                    BufferedImage originalImage = ImageIO.read(imageFile);
                    if (originalImage != null) {
                         // Resize image to fit the label
                         Image scaledImage = originalImage.getScaledInstance(PROFILE_PIC_SIZE, PROFILE_PIC_SIZE, Image.SCALE_SMOOTH);
                         profilePictureLabel.setIcon(new ImageIcon(scaledImage));
                         profilePictureLabel.setText(null); // Remove placeholder text
                         LOGGER.fine("Successfully loaded profile picture from: " + imagePath);
                     } else {
                         LOGGER.warning("Could not decode image file: " + imagePath);
                         setPlaceholderPicture("Load Error");
                     }
                } else {
                     LOGGER.warning("Profile picture file not found or is not a file: " + imagePath);
                     LOGGER.warning("Looked for file at absolute path: " + imageFile.getAbsolutePath()); 
                     setPlaceholderPicture("Not Found");
                }
            } catch (IOException e) {
                 LOGGER.log(Level.SEVERE, "Error loading profile picture from path: " + imagePath, e);
                 setPlaceholderPicture("IO Error");
            } catch (Exception e) {
                 LOGGER.log(Level.SEVERE, "Unexpected error loading profile picture: " + imagePath, e);
                 setPlaceholderPicture("Error");
            }
        } else {
            LOGGER.fine("No profile picture path provided for user.");
            setPlaceholderPicture("No Picture");
        }
    }

    private void setPlaceholderPicture(String text) {
         profilePictureLabel.setIcon(null);
         profilePictureLabel.setText(text);
         profilePictureLabel.revalidate();
         profilePictureLabel.repaint();
    }

    // --- Action Listeners --- 
    public void addChangePasswordListener(ActionListener listener) {
        changePasswordButton.addActionListener(listener);
        changePasswordButton.setActionCommand("Change Password"); // Specific command
    }

    public void addUploadPictureListener(ActionListener listener) {
        // Keep the externally provided listener if needed (e.g., for navigation by controller)
        if (listener != null) {
             uploadPictureButton.addActionListener(listener);
             uploadPictureButton.setActionCommand("Upload Picture"); // Ensure external listener still works if needed
        }
        
        // Add the internal listener for file handling
        uploadPictureButton.addActionListener(e -> handleUploadPictureAction());
    }
    
     public void addBackListener(ActionListener listener) {
         backButton.addActionListener(listener);
         // Action command typically set by the controller that adds the listener
     }

    // --- Getters --- 
    public JPanel getPanel() {
        return this;
    }

    public JButton getBackButton() {
        return backButton;
    }

    private void handleUploadPictureAction() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "No user loaded to upload picture for.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        // Set filter for common image types
        FileFilter imageFilter = new FileNameExtensionFilter(
                "Image files (JPG, PNG)", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(imageFilter);
        fileChooser.setAcceptAllFileFilterUsed(false); // Don't allow "All Files"


        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // 1. Validate the selected file
            if (!FileUtil.isValidImageFile(selectedFile)) {
                JOptionPane.showMessageDialog(this,
                        "Invalid file selected.\n" +
                        "Please choose a JPG or PNG image smaller than 5MB.",
                        "Invalid File", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Ensure target directory exists
             if (!FileUtil.ensureDirectoryExists(PROFILE_PICTURES_DIR)) {
                 JOptionPane.showMessageDialog(this,
                         "Could not create directory to store profile pictures.",
                         "File System Error", JOptionPane.ERROR_MESSAGE);
                 return;
             }

            // 3. Generate unique filename (e.g., userID_timestamp.ext)
            String baseName = String.valueOf(currentUser.getUserID());
            String uniqueFileName = FileUtil.generateUniqueFileName(selectedFile.getName(), baseName);
            if (uniqueFileName == null) {
                 JOptionPane.showMessageDialog(this,
                         "Could not generate a unique filename (check file extension?).",
                         "Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            
            // Construct target path string (relative path for storage, using forward slashes)
             String targetRelativePath = (PROFILE_PICTURES_DIR + "/" + uniqueFileName).replace("\\", "/");


            // 4. Copy the file
            // FileUtil handles the actual OS-specific path creation for copying
            Path newFilePath = FileUtil.copyFileToDirectory(selectedFile, PROFILE_PICTURES_DIR, uniqueFileName); 

            if (newFilePath != null) {
                // 5. Update database (store the path with forward slashes)
                boolean dbUpdated = userDAO.updateProfilePicturePath(currentUser.getUserID(), targetRelativePath); 

                if (dbUpdated) {
                    // 6. Update current user object and UI
                    currentUser.setProfilePicturePath(targetRelativePath);
                    loadProfilePicture(targetRelativePath); // Reload the picture in the UI
                    JOptionPane.showMessageDialog(this,
                            "Profile picture uploaded successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to update profile picture information in the database.",
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                    try {
                        Files.deleteIfExists(newFilePath);
                    } catch (IOException ioex) {
                        LOGGER.log(Level.WARNING, "Failed to delete partially uploaded file: " + newFilePath, ioex);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to copy the selected file.",
                        "File Copy Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 