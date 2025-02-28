package Gui;

import javax.crypto.SecretKey;
import javax.swing.*;

import Vault.Vault;
import Vault.VaultEncryption;

import java.awt.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class AddCredentialPanel extends JPanel {
    private JTextField serviceNameField, usernameField;
    private JPasswordField passwordField;
    private JButton addToVaultButton, generatePasswordButton, backButton;
    private JToggleButton showPasswordButton;

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+<>?";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String ICON_EYE_OPEN = "photos/eye-solid.png";
    private static final String ICON_EYE_CLOSED = "photos/eye-slash-solid.png";

    public AddCredentialPanel(GUIBuilder parent) {
        setLayout(new BorderLayout());

        // Create Fields
        serviceNameField = new JTextField(15);
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        passwordField.setEchoChar('●'); // Hide password by default

        // Buttons
        addToVaultButton = new JButton("Add to Vault");
        generatePasswordButton = new JButton("Generate");
        backButton = new JButton("← Back");

        // Show/Hide Password Button (50x40 pixels)
        showPasswordButton = new JToggleButton(new ImageIcon(ICON_EYE_OPEN)); // Default open eye
        showPasswordButton.setPreferredSize(new Dimension(50, 40)); // Match icon size
        showPasswordButton.setMargin(new Insets(0, 0, 0, 0));
        showPasswordButton.setBorderPainted(false); // No border
        showPasswordButton.setContentAreaFilled(false); // No background
        showPasswordButton.setFocusPainted(false); // No focus border

        // Panel for input fields
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Service Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(serviceNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Password:"), gbc);

        // Password panel (password field + generate + show toggle)
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(generatePasswordButton, BorderLayout.EAST);
        passwordPanel.add(showPasswordButton, BorderLayout.WEST);

        gbc.gridx = 1;
        inputPanel.add(passwordPanel, gbc);

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);
        buttonPanel.add(addToVaultButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        generatePasswordButton.addActionListener(e -> generateRandomPassword());
        showPasswordButton.addActionListener(e -> togglePasswordVisibility());
        addToVaultButton.addActionListener(e -> addToVault());
        backButton.addActionListener(e -> parent.showPanel("Main"));
    }

    private void generateRandomPassword() {
        passwordField.setText(generateSecurePassword(16));
    }

    private void togglePasswordVisibility() {
        if (showPasswordButton.isSelected()) {
            passwordField.setEchoChar((char) 0); // Show password
            showPasswordButton.setIcon(new ImageIcon(ICON_EYE_CLOSED)); // closed eye icon
        } else {
            passwordField.setEchoChar('●'); // Hide password
            showPasswordButton.setIcon(new ImageIcon(ICON_EYE_OPEN)); // open eye icon
        }
    }

    public void addToVault() {
        try {
            // Get the GUIBuilder instance
            GUIBuilder guiBuilder = (GUIBuilder) SwingUtilities.getWindowAncestor(this);
    
            // Retrieve the actual vault instance
            Vault vault = guiBuilder.getVault();
    
            // Retrieve the actual user password from GUIBuilder (make sure it was stored at login!)
            String userPassword = guiBuilder.getUserPassword(); 
    
            // Debugging: Print the actual password
            System.out.println("🔍 Debug: Retrieving User Password for Root Key Derivation: " + userPassword);
    
            // Verify the password to ensure it's correct
            boolean isPasswordCorrect = vault.verifyRootPassword(userPassword);
            if (!isPasswordCorrect) {
                JOptionPane.showMessageDialog(this, "Invalid password! Unable to encrypt credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            // Now derive the correct root key
            SecretKey rootKey = VaultEncryption.deriveRootKey(userPassword, Base64.getDecoder().decode(vault.getSalt()));
            SecretKey vaultKey = VaultEncryption.getVaultKey(vault, rootKey);
    
            System.out.println("✅ Debug: Successfully Retrieved Correct Vault Key!");
    
            // Retrieve user input for credentials
            String service = serviceNameField.getText();
            String username = usernameField.getText();
            char[] passwordChars = passwordField.getPassword();
            String password = new String(passwordChars);
    
            // Generate IV for this password entry
            byte[] iv = VaultEncryption.generateRandomIV();
            String encodedIV = Base64.getEncoder().encodeToString(iv);
    
            // Encrypt the password using the correct vault key
            byte[] encryptedPass = VaultEncryption.encryptAESGCM(password.getBytes(), vaultKey, iv);
            String encodedPass = Base64.getEncoder().encodeToString(encryptedPass);
    
            System.out.println("✅ Debug: Password Encrypted Successfully!");
    
            // Add the new password entry to the vault
            vault.addPassword(service, username, encodedPass, encodedIV);
    
            // Save the updated vault
            guiBuilder.saveVault();
    
            JOptionPane.showMessageDialog(this, "Credential added successfully!");
    
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateSecurePassword(int length) {
        List<Character> passwordChars = new ArrayList<>();
        passwordChars.add(UPPERCASE.charAt(SECURE_RANDOM.nextInt(UPPERCASE.length())));
        passwordChars.add(LOWERCASE.charAt(SECURE_RANDOM.nextInt(LOWERCASE.length())));
        passwordChars.add(DIGITS.charAt(SECURE_RANDOM.nextInt(DIGITS.length())));
        passwordChars.add(SPECIAL_CHARACTERS.charAt(SECURE_RANDOM.nextInt(SPECIAL_CHARACTERS.length())));

        String allCharacters = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARACTERS;
        for (int i = 4; i < length; i++) {
            passwordChars.add(allCharacters.charAt(SECURE_RANDOM.nextInt(allCharacters.length())));
        }

        // Shuffle to randomize order
        Collections.shuffle(passwordChars, SECURE_RANDOM);

        // Convert list to string
        StringBuilder password = new StringBuilder();
        for (char ch : passwordChars) {
            password.append(ch);
        }
        return password.toString();
    }
}
