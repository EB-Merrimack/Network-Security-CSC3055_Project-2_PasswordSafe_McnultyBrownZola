package Gui;

import javax.swing.*;
import Vault.Vault;
import java.awt.*;

public class LoginPanel extends JPanel {
    private Vault vault;
    private GUIBuilder parent;

    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton actionButton;
    private JLabel messageLabel;

    public LoginPanel(GUIBuilder parent, Vault vault) {
        this.vault = vault;
        this.parent = parent;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel passwordLabel = new JLabel("Enter Vault Password:");
        passwordField = new JPasswordField(15);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordField = new JPasswordField(15);
        confirmPasswordField.setVisible(false); // Hidden unless needed

        actionButton = new JButton("Unlock Vault");
        messageLabel = new JLabel("", SwingConstants.CENTER);

        // Position elements in grid
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(passwordLabel, gbc);

        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        add(confirmPasswordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        add(actionButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(messageLabel, gbc);

        // Check vault state and set behavior
        if (vault.getRootPasswordHash() == null || vault.getRootPasswordHash().isEmpty()) {
            setupInitializationMode();
        } else {
            setupLoginMode();
        }
    }

    private void setupInitializationMode() {
        actionButton.setText("Initialize Vault");
        confirmPasswordField.setVisible(true);

        actionButton.addActionListener(e -> initializeVault());
    }

    private void setupLoginMode() {
        actionButton.setText("Unlock Vault");
        confirmPasswordField.setVisible(false);

        actionButton.addActionListener(e -> loginToVault());
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

        vault.setRootPassword(password);
        parent.saveVault();
        JOptionPane.showMessageDialog(this, "Vault initialized successfully!");
        parent.showPanel("Main");
    }

    private void loginToVault() {
        String password = new String(passwordField.getPassword());

        if (vault.verifyRootPassword(password)) {
            JOptionPane.showMessageDialog(this, "Access granted.");
            parent.showPanel("Main");
        } else {
            messageLabel.setText("Incorrect password! Try again.");
        }
    }

    private String checkPasswordStrength(String password) {
        if (password.length() < 8) {
            return "Too short!";
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
