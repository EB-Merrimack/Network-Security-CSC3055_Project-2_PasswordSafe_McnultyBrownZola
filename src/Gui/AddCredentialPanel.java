package Gui;

import Vault.Vault;
import Vault.VaultEncryption;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.util.Base64;
import java.io.File;

public class AddCredentialPanel extends JPanel {
    private JTextField serviceNameField, usernameField;
    private JPasswordField passwordField;
    private JButton addToVaultButton;
    private Vault vault;
    private String rootPassword;

    public AddCredentialPanel(File vaultFile, String rootPassword) {
        this.vault = Vault.loadVault(vaultFile);
        this.rootPassword = rootPassword;

        serviceNameField = new JTextField(10);
        usernameField = new JTextField(10);
        passwordField = new JPasswordField(10);
        addToVaultButton = new JButton("Add to Vault");

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Service Name:"));
        inputPanel.add(serviceNameField);
        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(usernameField);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(passwordField);

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(addToVaultButton, BorderLayout.SOUTH);

        addToVaultButton.addActionListener(e -> addToVault());
    }

    private void addToVault() {
        String service = serviceNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (service.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Derive vault key from root password
            SecretKey vaultKey = VaultEncryption.deriveRootKey(rootPassword, Base64.getDecoder().decode(vault.generateSalt()));

            // Generate IV and encrypt the password
            byte[] iv = VaultEncryption.generateRandomIV();
            String ivBase64 = Base64.getEncoder().encodeToString(iv);
            byte[] encryptedPassword = VaultEncryption.encryptAESGCM(password.getBytes(), vaultKey, iv);
            String encryptedPasswordBase64 = Base64.getEncoder().encodeToString(encryptedPassword);

            // Add credentials to vault
            vault.addPassword(service, username, encryptedPasswordBase64, ivBase64);
            JOptionPane.showMessageDialog(this, "Credential added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error encrypting password.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
