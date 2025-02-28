package Gui;

import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;
import java.util.ArrayList;
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

    private void addToVault() {
        JOptionPane.showMessageDialog(this, "Needs to be added still");
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
