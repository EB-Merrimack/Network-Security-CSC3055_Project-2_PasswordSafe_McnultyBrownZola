package Gui;

import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;
import java.util.Base64;
import Vault.Vault;
import Vault.VaultEncryption;
import javax.crypto.SecretKey;

public class AddServiceAndPrivateKeyPanel extends JPanel {
    private JTextField serviceNameField;
    private JButton saveButton;
    private Vault vault;
    private GUIBuilder guiBuilder; // Needed to access stored user password

    public AddServiceAndPrivateKeyPanel(Vault vault, GUIBuilder guiBuilder) {
        this.vault = vault;
        this.guiBuilder = guiBuilder;

        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        serviceNameField = new JTextField(15);

        inputPanel.add(new JLabel("Service Name:"));
        inputPanel.add(serviceNameField);

        // Save Button
        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveServiceAndPrivateKey());
        //add back button
        JButton backButton = new JButton("← Back");
        backButton.addActionListener(e -> guiBuilder.showPanel("Main"));
        // Add components to panel
    // Panel to hold buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));  // Use FlowLayout for both buttons to appear side by side
    buttonPanel.add(backButton);  // Add back button to panel
    buttonPanel.add(saveButton);  // Add save button to panel

    // Add components to main panel
    add(inputPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);  // Add button panel to the south section
    }

    private void saveServiceAndPrivateKey() {
        String serviceName = serviceNameField.getText().trim();
    
        if (serviceName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a service name!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        // Retrieve the stored user password from GUIBuilder
        String rootPassword = guiBuilder.getUserPassword();
        if (rootPassword == null || rootPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error: No stored password. Please restart and log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        // Verify the root password
        if (!vault.verifyRootPassword(rootPassword)) {
            JOptionPane.showMessageDialog(this, "Incorrect root password!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        try {
            // ✅ Generate a new random private key (32 bytes long, Base64 encoded)
            byte[] privateKeyBytes = new byte[32];
            new SecureRandom().nextBytes(privateKeyBytes);
            String privateKey = Base64.getEncoder().encodeToString(privateKeyBytes);
    
            // 🔍 Debug: Print the generated private key BEFORE encryption
            System.out.println("✅ Debug: Generated Private Key (Before Encryption): " + privateKey);
    
            // ✅ Derive the correct vault key
            SecretKey rootKey = VaultEncryption.deriveRootKey(rootPassword, Base64.getDecoder().decode(vault.getSalt()));
            SecretKey vaultKey = VaultEncryption.getVaultKey(vault, rootKey);
    
            // 🔍 Debug: Print the vault key used for encryption
            System.out.println("🔐 Debug: Vault Key Used for Encryption: " + Base64.getEncoder().encodeToString(vaultKey.getEncoded()));
    
            // ✅ Generate IV for encryption
            byte[] iv = VaultEncryption.generateRandomIV();
            String encodedIV = Base64.getEncoder().encodeToString(iv);
    
            // ✅ Encrypt the private key using AES-GCM
            byte[] encryptedPrivKeyBytes = VaultEncryption.encryptAESGCM(privateKey.getBytes(), vaultKey, iv);
            String encryptedPrivKey = Base64.getEncoder().encodeToString(encryptedPrivKeyBytes);
    
            // 🔍 Debug: Print the encrypted private key BEFORE storing it
            System.out.println("🔒 Debug: Encrypted Private Key (Base64 Stored in Vault): " + encryptedPrivKey);
    
            // ✅ Store the encrypted private key in the vault
            vault.addPrivateKey(serviceName, encryptedPrivKey, encodedIV);
            guiBuilder.saveVault();
    
            JOptionPane.showMessageDialog(this, "Service and private key added successfully!");
    
        } catch (Exception e) {
            System.err.println("❌ Error: Failed to encrypt and store private key - " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: Failed to save private key!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}