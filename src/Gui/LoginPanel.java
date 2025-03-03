package Gui;

import javax.swing.*;
import Vault.Vault;
import Vault.VaultOpener;
import java.awt.*;
import java.io.File;

public class LoginPanel extends JPanel {
    private Vault vault;
    private GUIBuilder parent;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton actionButton;
    private JLabel messageLabel;
    private JLabel confirmPasswordLabel;
    public static boolean isUserLoggedIn = false;

    public LoginPanel(GUIBuilder parent, Vault vault) {
        this.vault = vault;
        this.parent = parent;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel passwordLabel = new JLabel("Enter Vault Password:");
        passwordField = new JPasswordField(15);

        confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setVisible(false);
        confirmPasswordField = new JPasswordField(15);
        confirmPasswordField.setVisible(false);

        actionButton = new JButton("Unlock Vault");
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setVisible(false);

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

        // Check if the encrypted vault file exists or if the JSON file exists
        File vaultFile = new File("src/json/vault.enc");
        File jsonFile = new File("src/json/vault.json");

        if (vaultFile.exists()) {
            setupLoginMode();
        } else if (jsonFile.exists()) {
            setupInitializationMode();
        } else {
            setupInitializationMode();  // If neither file exists, prompt for initialization
        }
    }

    private void setupInitializationMode() {
        actionButton.setText("Initialize Vault");
        confirmPasswordField.setVisible(true);
        confirmPasswordLabel.setVisible(true);

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
            showMessage("Passwords do not match! Try again.");
            return;
        }

        String passwordStrength = checkPasswordStrength(password);
        if (!passwordStrength.equals("Strong")) {
            showMessage("Weak password! " + passwordStrength);
            return;
        }

        parent.setUserPassword(password);
        System.out.println("✅ Debug: Stored Password in GUIBuilder: " + password);

        vault.setRootPassword(password);
        parent.saveVault();
        isUserLoggedIn = true;
        JOptionPane.showMessageDialog(this, "Vault initialized successfully!");
        parent.showPanel("Main");
    }

    private void loginToVault() {
        String password = new String(passwordField.getPassword());

        System.out.println("🔍 Debug: User entered password: " + password);
        parent.setUserPassword(password);
        System.out.println("✅ Debug: Stored User Password in GUIBuilder: " + password);

        try {
            System.out.println("🔑 Attempting to unseal the vault...");
            VaultOpener vaultOpener = new VaultOpener(password);
            
            if (vaultOpener.unseal(vault)) {  // The `unseal()` method should return `true` if successful
                JOptionPane.showMessageDialog(this, "Vault successfully unsealed.");
                isUserLoggedIn = true;
                parent.showPanel("Main");
            } else {
                throw new Exception("Failed to unseal vault.");
            }
        } catch (Exception e) {
            showMessage("Failed to unseal the vault. Incorrect password.");
        }
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
    }

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
