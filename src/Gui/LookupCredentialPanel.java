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
    private GUIBuilder guiBuilder; // Needed to access stored user password

    public LookupCredentialPanel(Vault vault, GUIBuilder guiBuilder) {
        this.vault = vault;
        this.guiBuilder = guiBuilder;

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

        // Retrieve the stored user password from GUIBuilder
        String rootPassword = guiBuilder.getUserPassword();

        if (rootPassword == null || rootPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error: No stored password. Please restart and log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verify the stored password is correct
        if (!vault.verifyRootPassword(rootPassword)) {
            JOptionPane.showMessageDialog(this, "Incorrect root password!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Retrieve and decrypt the credential
        String credential = getCredential(serviceName, rootPassword);

        if (credential != null) {
            JOptionPane.showMessageDialog(this, credential, "Credential Found", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No credentials found for: " + serviceName, "Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String getCredential(String serviceName, String rootPassword) {
        JSONArray passwords = vault.getPasswords();

        System.out.println("🔍 Searching for service: " + serviceName); // Debug log

        for (int i = 0; i < passwords.size(); i++) {
            JSONObject entry = passwords.getObject(i);
            String storedServiceName = entry.getString("service");
            System.out.println("🔑 Found service in vault: " + storedServiceName); // Debug log

            if (storedServiceName.equalsIgnoreCase(serviceName)) {
                System.out.println("✅ Match found, decrypting password..."); // Debug log
                return "User: " + entry.getString("user") + "\nPassword: " + decryptPassword(entry, rootPassword);
            }
        }

        return null;
    }

    private String decryptPassword(JSONObject entry, String rootPassword) {
        try {
            String encryptedPass = entry.getString("pass");
            String iv = entry.getString("iv");

            // 🔹 Retrieve the correct vault key
            SecretKey rootKey = VaultEncryption.deriveRootKey(rootPassword, Base64.getDecoder().decode(vault.getSalt()));
            SecretKey vaultKey = VaultEncryption.getVaultKey(vault, rootKey);  // ✅ Use the stored vault key

            System.out.println("🔐 Decrypting with key: " + Base64.getEncoder().encodeToString(vaultKey.getEncoded())); // Debug log

            byte[] decryptedBytes = VaultEncryption.decryptAESGCM(
                Base64.getDecoder().decode(encryptedPass),
                vaultKey,
                Base64.getDecoder().decode(iv)
            );

            System.out.println("✅ Decryption successful!"); // Debug log
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace(); // Log exception details
            return "[Error decrypting password]";
        }
    }
}