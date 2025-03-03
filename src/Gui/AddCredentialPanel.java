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

// Add service name, username, and password to vault
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

    public AddCredentialPanel(JFrame parentFrame) {
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
        addToVaultButton.addActionListener(e -> addToVault((GUIBuilder) parentFrame));
        backButton.addActionListener(e -> ((GUIBuilder) parentFrame).showPanel("Main"));
    }

    /**
     * Generates a random secure password and sets it to the password field.
     * 
     * This method utilizes the generateSecurePassword function to create a
     * secure password with a specified length and updates the passwordField
     * with this newly generated password.
     */
    private void generateRandomPassword() {
        passwordField.setText(generateSecurePassword(16));
    }

    private void togglePasswordVisibility() {
        if (showPasswordButton.isSelected()) {
            passwordField.setEchoChar((char) 0); // Show password
            showPasswordButton.setIcon(new ImageIcon(ICON_EYE_CLOSED)); // Closed eye icon
        } else {
            passwordField.setEchoChar('●'); // Hide password
            showPasswordButton.setIcon(new ImageIcon(ICON_EYE_OPEN)); // Open eye icon
        }
    }

    /**
     * Adds a service credential to the vault.
     * 
     * This method retrieves user input for service name, username, and password,
     * verifies the user's root password for encryption, and securely stores the
     * encrypted credentials in the vault. It then saves the updated vault and 
     * clears the input fields.
     * 
     * @param parent The GUIBuilder instance used to interact with the vault.
     * 
     * @throws Exception If there is an error during encryption or vault operations.
     */
    private void addToVault(GUIBuilder parent) {
        try {
            // Get the GUIBuilder instance
            GUIBuilder guiBuilder = (GUIBuilder) SwingUtilities.getWindowAncestor(this);

            // Retrieve the actual vault instance
            Vault vault = guiBuilder.getVault();

            // Retrieve the actual user password from GUIBuilder (make sure it was stored at login!)
            String userPassword = guiBuilder.getUserPassword();

            // Verify the password to ensure it's correct
            boolean isPasswordCorrect = vault.verifyRootPassword(userPassword);
            if (!isPasswordCorrect) {
                JOptionPane.showMessageDialog(this, "Invalid password! Unable to encrypt credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Derive root key and retrieve vault key
            SecretKey rootKey = VaultEncryption.deriveRootKey(userPassword, Base64.getDecoder().decode(vault.getSalt()));
            SecretKey vaultKey = VaultEncryption.getVaultKey(vault, rootKey);

            // Retrieve user input for credentials
            String service = serviceNameField.getText().trim();
            String username = usernameField.getText().trim();
            char[] passwordChars = passwordField.getPassword();
            String password = new String(passwordChars);

            if (service.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill out all fields before adding credentials.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check password strength
            String passwordStrength = checkPasswordStrength(password);
            if (!passwordStrength.equals("Strong")) {
                // Warn the user about weak password but allow them to proceed
                int option = JOptionPane.showConfirmDialog(this, 
                    "Your password is weak: " + passwordStrength + "\nDo you still want to proceed?", 
                    "Weak Password Warning", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE);

                if (option != JOptionPane.YES_OPTION) {
                    return; // Stop if user does not want to proceed
                }
            }

            // Generate IV for this password entry
            byte[] iv = VaultEncryption.generateRandomIV();
            String encodedIV = Base64.getEncoder().encodeToString(iv);

            // Encrypt the password using the correct vault key
            byte[] encryptedPass = VaultEncryption.encryptAESGCM(password.getBytes(), vaultKey, iv);
            String encodedPass = Base64.getEncoder().encodeToString(encryptedPass);

            // Add the new password entry to the vault
            vault.addPassword(service, username, encodedPass, encodedIV);

            // Save the updated vault
            parent.saveVault();

            // Show confirmation message
            JOptionPane.showMessageDialog(this, "Credential added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear input fields
            clearInputFields();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding credential. See console for details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Clears the input fields for service name, username, and password.
     */
    private void clearInputFields() {
        serviceNameField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }

    /**
     * Generates a secure password with a specified length, including at least one of each type of character.
     * 
     * This method first adds one of each type of character (uppercase, lowercase, digit, and special character)
     * to a list, then adds random characters from the entire character set until the list is of the specified
     * length. The list is then shuffled to randomize the order of the characters, and the list is converted to
     * a string to return as the generated password.
     * 
     * @param length The length of the password to generate.
     * @return A secure password of the specified length, including at least one of each type of character.
     */
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
    
    // Method to check the strength of a password
    private String checkPasswordStrength(String password) {
        if (password.length() < 8) {
            return "Too short! Password must be greater than 8 characters!";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Must contain an uppercase letter!";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Must contain a number!";
        }
        if (!password.matches(".*[!@#$%^&*()].*")) {
            return "Must contain a special character!";
        }
        return "Strong";
    }
}
