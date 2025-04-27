package com.school.management.util.validation;

import java.util.regex.Pattern;
import javax.swing.JOptionPane; // For interactive testing

/**
 * Utility class providing static methods for common input validation tasks.
 */
public class InputValidator {

    // Simple regex for email validation (adjust for stricter rules if needed)
    // Allows user+domain@domain.tld format
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" + 
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Regex for a simple positive integer check
    private static final Pattern POSITIVE_INTEGER_PATTERN = Pattern.compile("^[1-9]\\d*$");

    /**
     * Private constructor to prevent instantiation.
     */
    private InputValidator() {
        throw new IllegalStateException("Utility class should not be instantiated.");
    }

    /**
     * Checks if a string is not null and not empty after trimming whitespace.
     *
     * @param input The string to check.
     * @return true if the string is not null and has content, false otherwise.
     */
    public static boolean isNotEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }

    /**
     * Validates an email address against a standard pattern.
     *
     * @param email The email string to validate.
     * @return true if the email format is valid, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Checks if a string represents a positive integer (greater than 0).
     *
     * @param input The string to check.
     * @return true if the string is a valid positive integer, false otherwise.
     */
    public static boolean isPositiveInteger(String input) {
        if (!isNotEmpty(input)) {
            return false;
        }
        return POSITIVE_INTEGER_PATTERN.matcher(input.trim()).matches();
    }

    /**
     * Checks if a string's length is within the specified minimum and maximum (inclusive).
     *
     * @param input The string to check.
     * @param minLength The minimum allowed length (inclusive).
     * @param maxLength The maximum allowed length (inclusive).
     * @return true if the string is not null and its length is within the bounds, false otherwise.
     * @throws IllegalArgumentException if minLength or maxLength are negative, or min > max.
     */
    public static boolean isWithinLength(String input, int minLength, int maxLength) {
        if (minLength < 0 || maxLength < 0) {
             throw new IllegalArgumentException("Minimum and maximum length must not be negative.");
        }
        if (minLength > maxLength) {
            throw new IllegalArgumentException("Minimum length cannot be greater than maximum length.");
        }
        // Allow null/empty string if minLength is 0, otherwise require non-null
        if (input == null && minLength > 0) {
            return false;
        }
         if (input == null && minLength == 0) {
             return true; // Null is considered length 0
         }

        int length = input.length(); // No trim here, check raw length based on DB constraints
        return length >= minLength && length <= maxLength;
    }

    // --- Simple Interactive Tester --- 

    /**
     * Simple main method to allow interactive testing of validation methods using JOptionPane.
     * Compile and run this file directly to test.
     * Note: This requires Swing libraries available.
     */
    public static void main(String[] args) {
        String[] options = {"Check Not Empty", "Check Email", "Check Positive Integer", "Check Length", "Exit"};
        int choice;

        do {
            choice = JOptionPane.showOptionDialog(null, "Choose a validation to test:", 
                                                "Input Validator Test", JOptionPane.DEFAULT_OPTION, 
                                                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (choice == JOptionPane.CLOSED_OPTION || choice == 4) { // Exit or closed window
                break;
            }

            String input = JOptionPane.showInputDialog(null, "Enter input string:");
            if (input == null) continue; // User cancelled input

            boolean result = false;
            String message = "";

            try {
                switch (choice) {
                    case 0: // Check Not Empty
                        result = isNotEmpty(input);
                        message = "Input '" + input + "' isNotEmpty: " + result;
                        break;
                    case 1: // Check Email
                        result = isValidEmail(input);
                        message = "Input '" + input + "' isValidEmail: " + result;
                        break;
                    case 2: // Check Positive Integer
                         result = isPositiveInteger(input);
                        message = "Input '" + input + "' isPositiveInteger: " + result;
                        break;
                    case 3: // Check Length
                         String minStr = JOptionPane.showInputDialog(null, "Enter minimum length:");
                         String maxStr = JOptionPane.showInputDialog(null, "Enter maximum length:");
                         if (isPositiveInteger(minStr) && isPositiveInteger(maxStr)) { // Basic check
                            int min = Integer.parseInt(minStr);
                            int max = Integer.parseInt(maxStr);
                            result = isWithinLength(input, min, max);
                             message = String.format("Input '%s' isWithinLength(%d, %d): %s", input, min, max, result);
                         } else {
                             message = "Invalid min/max length entered.";
                         }
                        break;
                }
                JOptionPane.showMessageDialog(null, message);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        } while (true);

        System.out.println("Exiting validator test.");
    }
} 