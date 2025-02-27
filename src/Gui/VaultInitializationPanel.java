package Gui;

import javax.swing.*;
import encryption.ScryptKeyDerivation;
import merrimackutil.json.types.JSONObject;
import encryption.VaultKeyEntry;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VaultInitializationPanel extends JPanel {
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton initializeButton;
    private JLabel messageLabel;

    public VaultInitializationPanel() {
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2));

        JLabel passwordLabel = new JLabel("Enter Password:");
        passwordField = new JPasswordField(20);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordField = new JPasswordField(20);

        initializeButton = new JButton("Initialize Vault");
        messageLabel = new JLabel("", SwingConstants.CENTER);

        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);
        inputPanel.add(confirmPasswordLabel);
        inputPanel.add(confirmPasswordField);
        inputPanel.add(new JLabel());
        inputPanel.add(initializeButton);

        add(inputPanel, BorderLayout.CENTER);
        add(messageLabel, BorderLayout.SOUTH);

        initializeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initializeVault();
            }
        });
    }

    private void initializeVault() {
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match! Try again.");
            return;
        }

        String passwordStrength = checkPasswordStrength(password);
        if (!passwordStrength.equals("Strong")) {
            messageLabel.setText("Weak password! " + passwordStrength);
            return;
        }

        try {
            // Generate a 16-byte salt (IV)
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            // Derive root key from password using Scrypt
            byte[] rootKey = ScryptKeyDerivation.deriveRootKey(password, salt);

            // Generate a new random vault key
            byte[] vaultKey = new byte[32]; // AES-256 key size
            new SecureRandom().nextBytes(vaultKey);

            // Encode keys for storage
            String encodedVaultKey = Base64.getEncoder().encodeToString(vaultKey);
            String encodedSalt = Base64.getEncoder().encodeToString(salt);

            // Create VaultKeyEntry object
            VaultKeyEntry vaultEntry = new VaultKeyEntry(encodedVaultKey, encodedSalt, null);

            // Save to vault.json
            saveVaultKeyEntry(vaultEntry);

            messageLabel.setText("Vault Initialized successfully!");
        } catch (Exception e) {
            messageLabel.setText("Failed to initialize vault.");
            e.printStackTrace();
        }
    }

    private void saveVaultKeyEntry(VaultKeyEntry entry) {
        try (FileWriter file = new FileWriter("vault.json")) {
            JSONObject json = (JSONObject) entry.toJSONType();
            file.write(json.getFormattedJSON());
        } catch (IOException e) {
            messageLabel.setText("Error saving vault key entry.");
            e.printStackTrace();
        }
    }

    private String checkPasswordStrength(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters long.";
        }

        Pattern uppercasePattern = Pattern.compile("[A-Z]");
        Pattern lowercasePattern = Pattern.compile("[a-z]");
        Pattern digitPattern = Pattern.compile("[0-9]");
        Pattern specialCharPattern = Pattern.compile("[^a-zA-Z0-9]");

        if (!uppercasePattern.matcher(password).find()) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!lowercasePattern.matcher(password).find()) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!digitPattern.matcher(password).find()) {
            return "Password must contain at least one number.";
        }
        if (!specialCharPattern.matcher(password).find()) {
            return "Password must contain at least one special character.";
        }

        return "Strong";
    }
}
