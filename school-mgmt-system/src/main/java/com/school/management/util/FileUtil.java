package com.school.management.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for file operations.
 */
public class FileUtil {

    private static final Logger LOGGER = Logger.getLogger(FileUtil.class.getName());
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    /**
     * Copies a source file to a target directory, optionally renaming it.
     * Creates the target directory if it doesn't exist.
     *
     * @param sourceFile The file to copy.
     * @param targetDir The directory to copy the file into.
     * @param targetFileName The desired name for the copied file (without extension). Extension is taken from source.
     * @return The Path of the newly created file, or null if an error occurred.
     */
    public static Path copyFileToDirectory(File sourceFile, String targetDir, String targetFileName) {
        if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
            LOGGER.warning("Source file is invalid or does not exist.");
            return null;
        }
        if (targetDir == null || targetDir.isEmpty()) {
            LOGGER.warning("Target directory path is null or empty.");
            return null;
        }
         if (targetFileName == null || targetFileName.isEmpty()) {
             LOGGER.warning("Target file name is null or empty.");
             return null;
         }

        try {
            // Ensure the target directory exists
            Path targetDirPath = Paths.get(targetDir);
            if (!Files.exists(targetDirPath)) {
                Files.createDirectories(targetDirPath);
                 LOGGER.info("Created target directory: " + targetDirPath);
            } else if (!Files.isDirectory(targetDirPath)) {
                LOGGER.severe("Target path exists but is not a directory: " + targetDir);
                return null;
            }

            Path targetFilePath = targetDirPath.resolve(targetFileName); 

            Path resultPath = Files.copy(sourceFile.toPath(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Successfully copied '" + sourceFile.getName() + "' to '" + resultPath.toString() + "'");
            return resultPath;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to copy file '" + sourceFile.getName() + "' to '" + targetDir + "'", e);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error copying file", e);
            return null;
        }
    }

    /**
     * Checks if a file is a valid image based on extension and size.
     *
     * @param file The file to check.
     * @return true if the file is a valid image, false otherwise.
     */
    public static boolean isValidImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        // Check extension
        String extension = getFileExtension(file.getName());
        if (extension == null || !ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            LOGGER.warning("Invalid image extension: " + extension);
            return false;
        }

        // Check size
        if (file.length() > MAX_IMAGE_SIZE_BYTES) {
             LOGGER.warning("Image file size exceeds limit (" + MAX_IMAGE_SIZE_BYTES + " bytes): " + file.length() + " bytes");
             return false;
        }

        return true;
    }

    /**
     * Extracts the file extension from a filename.
     *
     * @param filename The name of the file.
     * @return The extension (lowercase) without the dot, or null if no extension found.
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return null; // No extension found
    }

    /**
     * Generates a unique filename based on a base name and the original extension.
     * Uses UUID to ensure uniqueness.
     *
     * @param originalFileName The original name of the file (used for extension).
     * @param baseName A base name (e.g., user ID) to include in the new name.
     * @return A unique filename string (e.g., "123_uuid.jpg"), or null if error.
     */
     public static String generateUniqueFileName(String originalFileName, String baseName) {
         String extension = getFileExtension(originalFileName);
         if (extension == null) {
             LOGGER.warning("Cannot generate unique name: Original file has no extension.");
             return null;
         }
         String uniqueID = UUID.randomUUID().toString().substring(0, 8); // Shortened UUID
         return baseName + "_" + uniqueID + "." + extension;
     }
     
     /**
      * Creates a directory if it does not already exist.
      * @param dirPath The path of the directory to create.
      * @return true if the directory exists or was successfully created, false otherwise.
      */
     public static boolean ensureDirectoryExists(String dirPath) {
         Path path = Paths.get(dirPath);
         if (Files.exists(path)) {
             if (Files.isDirectory(path)) {
                 return true; 
             } else {
                 LOGGER.severe("Path exists but is not a directory: " + dirPath);
                 return false; 
             }
         } else {
             try {
                 Files.createDirectories(path);
                 LOGGER.info("Created directory: " + dirPath);
                 return true;
             } catch (IOException e) {
                 LOGGER.log(Level.SEVERE, "Failed to create directory: " + dirPath, e);
                 return false;
             }
         }
     }
} 