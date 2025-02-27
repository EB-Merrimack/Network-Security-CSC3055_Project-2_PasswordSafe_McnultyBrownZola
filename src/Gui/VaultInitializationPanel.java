package Gui;

import javax.swing.*;

import encryption.VaultInitializationEncryption;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VaultInitializationPanel extends JPanel {
    private JTextField passwordField;
    private JTextField confirmPasswordField;
    private JButton initializeButton;
    private JLabel messageLabel;

    public VaultInitializationPanel() {
        setLayout(new BorderLayout());

        // Create components
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2));

        JLabel passwordLabel = new JLabel("Enter Password:");
        passwordField = new JPasswordField(20);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordField = new JPasswordField(20);

        initializeButton = new JButton("Initialize Vault");
        messageLabel = new JLabel("", SwingConstants.CENTER);

        // Add components to input panel
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);
        inputPanel.add(confirmPasswordLabel);
        inputPanel.add(confirmPasswordField);
        inputPanel.add(new JLabel());  // Empty space
        inputPanel.add(initializeButton);

        // Add panels to main panel
        add(inputPanel, BorderLayout.CENTER);
        add(messageLabel, BorderLayout.SOUTH);

        // Action listener for the initialize button
        initializeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initializeVault();
            }
        });
    }

    private void initializeVault() {
        String password = new String(((JPasswordField) passwordField).getPassword());
        String confirmPassword = new String(((JPasswordField) confirmPasswordField).getPassword());

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match! Try again.");
            return;
        }

        // Check password strength
        String passwordStrength = checkPasswordStrength(password);
        if (!passwordStrength.equals("Strong")) {
            messageLabel.setText("Weak password! " + passwordStrength);
            return;
        }

        // Encrypt the password and store it
        String encryptedPassword = VaultInitializationEncryption.encryptPassword(password);
        if (encryptedPassword != null) {
            boolean success = VaultInitializationEncryption.storeEncryptedPassword(encryptedPassword);
            if (success) {
                messageLabel.setText("Vault Initialized successfully!");
            } else {
                messageLabel.setText("Failed to store the encrypted password.");
            }
        } else {
            messageLabel.setText("Failed to encrypt the password.");
        }
    }

    // Method to check password strength
    private String checkPasswordStrength(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters long.";
        }

        // Regex to check for at least one uppercase letter, one lowercase letter, one number, and one special character
        Pattern uppercasePattern = Pattern.compile("[A-Z]");
        Pattern lowercasePattern = Pattern.compile("[a-z]");
        Pattern digitPattern = Pattern.compile("[0-9]");
        Pattern specialCharPattern = Pattern.compile("[^a-zA-Z0-9]");

        Matcher uppercaseMatcher = uppercasePattern.matcher(password);
        Matcher lowercaseMatcher = lowercasePattern.matcher(password);
        Matcher digitMatcher = digitPattern.matcher(password);
        Matcher specialCharMatcher = specialCharPattern.matcher(password);

        if (!uppercaseMatcher.find()) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!lowercaseMatcher.find()) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!digitMatcher.find()) {
            return "Password must contain at least one number.";
        }
        if (!specialCharMatcher.find()) {
            return "Password must contain at least one special character.";
        }

        // If all checks pass, password is strong
        return "Strong";
    }
}
