package Gui;

import Vault.Vault;
import Vault.VaultEncryption;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.util.Base64;

public class AddServiceAndKeyGenPanel extends JPanel {
    private Vault vault;
    private JTextField serviceField;
    private JTextField userField;
    private JTextField passField;
    private JButton generateKeyButton;
    private JButton addServiceButton;
    private byte[] generatedKey;

    public AddServiceAndKeyGenPanel(GUIBuilder parent, Vault vault) {
        this.vault = vault;

        // Set layout for the panel
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Initialize components
        serviceField = new JTextField(20);
        userField = new JTextField(20);
        passField = new JTextField(20);
        generateKeyButton = new JButton("Generate Key");
        addServiceButton = new JButton("Add Service Credentials");

        // Add action listeners
        generateKeyButton.addActionListener(e -> generateKey());
        addServiceButton.addActionListener(e -> addServiceCredentials());

        // Add components to the panel
        add(new JLabel("Service:"));
        add(serviceField);
        add(new JLabel("User:"));
        add(userField);
        add(new JLabel("Password:"));
        add(passField);
        add(generateKeyButton);
        add(addServiceButton);
    }

    // Generate a random key
    private void generateKey() {
        try {
            generatedKey = VaultEncryption.generateRandomKey();
            String encodedKey = Base64.getEncoder().encodeToString(generatedKey);
            passField.setText(encodedKey); // Set generated key as password
            System.out.println("Generated key: " + encodedKey);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating key", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Add service credentials to the vault
    private void addServiceCredentials() {
        if (serviceField.getText().isEmpty() || userField.getText().isEmpty() || passField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Add credentials to the vault (This assumes encryption is handled in Vault
        // class)
        try {
            String service = serviceField.getText();
            String user = userField.getText();
            String encryptedPass = passField.getText(); // Assuming it's Base64 encoded
            byte[] iv = VaultEncryption.generateRandomIV(); // Generate IV for the password

            vault.addPassword(service, user, encryptedPass, iv);
            JOptionPane.showMessageDialog(this, "Service credentials added successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding service credentials", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}