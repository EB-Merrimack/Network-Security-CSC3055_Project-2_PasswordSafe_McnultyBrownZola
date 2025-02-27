package Gui;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import Vault.Vault;
import Vault.VaultEncryption;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import java.util.Base64;
import javax.crypto.SecretKey;

public class LookupCredentialPanel extends JPanel {
    private JTextField serviceNameField;
    private JButton searchButton;
    private Vault vault;

    public LookupCredentialPanel(Vault vault) {
        this.vault = vault;

        setLayout(new BorderLayout());

        serviceNameField = new JTextField(15);
        searchButton = new JButton("Lookup");

        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        inputPanel.add(new JLabel("Service Name:"));
        inputPanel.add(serviceNameField);

        add(inputPanel, BorderLayout.CENTER);
        add(searchButton, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> searchVault());
    }

    private void searchVault() {
        String serviceName = serviceNameField.getText().trim();

        if (serviceName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a service name!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Prompt user for root password to verify access
        String rootPassword = JOptionPane.showInputDialog(this, "Enter Root Password:", "Authentication", JOptionPane.PLAIN_MESSAGE);

        if (rootPassword == null || rootPassword.isEmpty() || !vault.verifyRootPassword(rootPassword)) {
            JOptionPane.showMessageDialog(this, "Incorrect root password!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Optional<String> credential = getCredential(serviceName, rootPassword);

        if (credential.isPresent()) {
            JOptionPane.showMessageDialog(this, credential.get(), "Credential Found", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No credentials found for: " + serviceName, "Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Optional<String> getCredential(String serviceName, String rootPassword) {
        JSONArray passwords = vault.getPasswords();

        System.out.println("🔍 Searching for service: " + serviceName); // Debug log

        for (int i = 0; i < passwords.size(); i++) {
            JSONObject entry = passwords.getObject(i);
            String storedServiceName = entry.getString("service");
            System.out.println("🔑 Found service in vault: " + storedServiceName); // Debug log

            if (storedServiceName.equalsIgnoreCase(serviceName)) {
                System.out.println("Match found, decrypting password..."); // Debug log
                return Optional.of("User: " + entry.getString("user") + "\nPassword: " + decryptPassword(entry, rootPassword));
            }
        }

        return Optional.empty();
    }

    private String decryptPassword(JSONObject entry, String rootPassword) {
        try {
            String encryptedPass = entry.getString("pass");
            String iv = entry.getString("iv");

            // Derive vault key using the root password
            SecretKey vaultKey = VaultEncryption.deriveRootKey(rootPassword, Base64.getDecoder().decode(vault.generateSalt()));
            System.out.println("🔐 Decrypting with key: " + Base64.getEncoder().encodeToString(vaultKey.getEncoded())); // Debug log

            byte[] decryptedBytes = VaultEncryption.decryptAESGCM(
                Base64.getDecoder().decode(encryptedPass),
                vaultKey,
                Base64.getDecoder().decode(iv)
            );

            System.out.println("Decryption successful!"); // Debug log
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace(); // Log exception details
            return "[Error decrypting password]";
        }
    }
}
