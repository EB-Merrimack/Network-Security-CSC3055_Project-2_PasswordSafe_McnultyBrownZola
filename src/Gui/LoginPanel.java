package Gui;

import javax.swing.*;
import Vault.Vault;
//import Vault.VaultOpener;

import java.awt.*;

public class LoginPanel extends JPanel {
    private Vault vault;
    private GUIBuilder parent;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton actionButton;
    private JLabel messageLabel;
    private JLabel confirmPasswordLabel;  // Declare confirmPasswordLabel here
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
        confirmPasswordField.setVisible(false); // Hidden unless needed

        actionButton = new JButton("Unlock Vault");
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setVisible(false); // Initially hidden

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

/**
 * Sets up the panel for vault initialization mode. Updates UI components
 * to prompt the user for a password and confirmation. Configures the
 * action button to trigger vault initialization.
 */

    private void setupInitializationMode() {
        actionButton.setText("Initialize Vault");
        confirmPasswordField.setVisible(true);
        confirmPasswordLabel.setVisible(true);

        actionButton.addActionListener(e -> initializeVault());
    }

    /**
     * Sets up the panel for vault login mode. Updates UI components to prompt the
     * user for a password. Configures the action button to trigger vault login.
     */
    private void setupLoginMode() {
        actionButton.setText("Unlock Vault");
        confirmPasswordField.setVisible(false);

        actionButton.addActionListener(e -> loginToVault());
    }

    // Method to initialize the vault
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

        // Store the password in GUIBuilder for later 
        parent.setUserPassword(password);
        System.out.println("✅ Debug: Stored Password in GUIBuilder: " + password);

        vault.setRootPassword(password);
        parent.saveVault();
        isUserLoggedIn = true;
        JOptionPane.showMessageDialog(this, "Vault initialized successfully!");
        parent.showPanel("Main");
    }

    /**
     * Handles the login process for the vault. Retrieves the user's password input
     * from the password field, stores it in the GUIBuilder, and attempts to unseal
     * the vault using the entered password. If unsealing is successful, verifies
     * the root password and grants access to the vault if correct.
     */
private void loginToVault() {
    String password = new String(passwordField.getPassword());

    System.out.println("🔍 Debug: User entered password: " + password);

    // Store the password in GUIBuilder so it can be used later
    parent.setUserPassword(password);
    System.out.println("✅ Debug: Stored User Password in GUIBuilder: " + password);

        try {
            // Try to unseal the vault using the entered password
            System.out.println("🔑 Attempting to unseal the vault...");
            //VaultOpener vaultOpener = new VaultOpener(password);
            // If unsealing is successful, continue with login
            JOptionPane.showMessageDialog(this, "Vault successfully unsealed.");
        } catch (Exception e) {
            // Handle error if unsealing fails
            showMessage("Failed to unseal the vault. Incorrect password.");
            return;
        }
     

    // Proceed with verifying the root password after unsealing the vault
    if (vault.verifyRootPassword(password)) {
        JOptionPane.showMessageDialog(this, "Access granted.");
        parent.showPanel("Main");
        isUserLoggedIn = true;
    } else {
        showMessage("Incorrect password! Try again.");}
    }


    /**
     * Displays a message in the message label, typically used for
     * displaying errors, warnings, or information to the user.
     * @param message the message to display
     */
    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
    }

    // Method to check the stregnth of a password
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