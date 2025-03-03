package Gui;

import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;
import java.util.Base64;
import Vault.Vault;
import Vault.VaultEncryption;
import javax.crypto.SecretKey;

// Add service and private key to vault
public class AddServiceAndPrivateKeyPanel extends JPanel {
    private JTextField serviceNameField;
    private JButton saveButton, backButton;
    private Vault vault;
    private GUIBuilder guiBuilder; // Needed to access stored user password

    public AddServiceAndPrivateKeyPanel(Vault vault, GUIBuilder guiBuilder) {
        this.vault = vault;
        this.guiBuilder = guiBuilder;

        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        serviceNameField = new JTextField(15);

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Service Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(serviceNameField, gbc);

        // Save Button
        saveButton = new JButton("Save");
        backButton = new JButton("← Back");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));  // Use FlowLayout for both buttons to appear side by side
        buttonPanel.add(backButton);  // Add back button to panel
        buttonPanel.add(saveButton);  // Add save button to panel

        saveButton.addActionListener(e -> saveServiceAndPrivateKey());
        backButton.addActionListener(e -> guiBuilder.showPanel("Main"));

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Saves the service name and a new randomly generated private key to the vault.
     * The private key is encrypted with AES-GCM using the vault key, and stored in the vault.
     * The user is prompted to enter a service name, and the stored user password is verified before saving.
     * If the user password is incorrect, an error message is shown.
     * If the service name is empty, an error message is shown.
     * If the private key fails to be saved, an error message is shown.
     */
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
            // Generate a new random private key
            byte[] privateKeyBytes = new byte[32];
            new SecureRandom().nextBytes(privateKeyBytes);
            String privateKey = Base64.getEncoder().encodeToString(privateKeyBytes);
    
            // Retrieve Vault Key
            SecretKey rootKey = VaultEncryption.deriveRootKey(rootPassword, Base64.getDecoder().decode(vault.getSalt()));
            SecretKey vaultKey = VaultEncryption.getVaultKey(vault, rootKey);
    
            // Generate IV for encryption
            byte[] iv = VaultEncryption.generateRandomIV();
            String encodedIV = Base64.getEncoder().encodeToString(iv);
    
            // Encrypt the private key using AES-GCM
            byte[] encryptedPrivKeyBytes = VaultEncryption.encryptAESGCM(privateKey.getBytes(), vaultKey, iv);
            String encryptedPrivKey = Base64.getEncoder().encodeToString(encryptedPrivKeyBytes);
        
            // Store the encrypted private key in the vault
            vault.addPrivateKey(serviceName, encryptedPrivKey, encodedIV);
            guiBuilder.saveVault();
    
            JOptionPane.showMessageDialog(this, "Service and private key added successfully!");
    
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: Failed to save private key!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
