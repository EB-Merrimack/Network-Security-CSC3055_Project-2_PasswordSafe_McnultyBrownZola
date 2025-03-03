package Gui;

import javax.swing.*;
import java.awt.*;
import java.util.Base64;
import Vault.Vault;
import Vault.VaultEncryption;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import javax.crypto.SecretKey;

public class LookupPrivateKeyPanel extends JPanel {
    private JTextField serviceNameField;
    private JButton searchButton;
    private Vault vault;
    private GUIBuilder guiBuilder; // Needed to access stored user password

    public LookupPrivateKeyPanel(Vault vault, GUIBuilder guiBuilder) {
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

        searchButton.addActionListener(e -> searchVaultForPrivateKey());
    }

    private void searchVaultForPrivateKey() {
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

        // Retrieve and decrypt the private key
        String privateKey = getPrivateKey(serviceName, rootPassword);

        if (privateKey != null) {
            JOptionPane.showMessageDialog(this, "Private Key:\n" + privateKey, "Key Found", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No private key found for: " + serviceName, "Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String getPrivateKey(String serviceName, String rootPassword) {
        JSONArray privateKeys = vault.getPrivateKeys();

        System.out.println("🔍 Searching for service: " + serviceName); // Debug log

        for (int i = 0; i < privateKeys.size(); i++) {
            JSONObject entry = privateKeys.getObject(i);
            String storedServiceName = entry.getString("service");
            System.out.println("🔑 Found service in vault: " + storedServiceName); // Debug log

            if (storedServiceName.equalsIgnoreCase(serviceName)) {
                System.out.println("✅ Match found, decrypting private key..."); // Debug log
                return decryptPrivateKey(entry, rootPassword);
            }
        }

        return null;
    }

    private String decryptPrivateKey(JSONObject entry, String rootPassword) {
        try {
            String encryptedPrivKey = entry.getString("privkey");
            String iv = entry.getString("iv");
    
            // 🔍 Debug: Print the stored encrypted private key
            System.out.println("🔒 Debug: Retrieved Encrypted Private Key (Base64 from Vault): " + encryptedPrivKey);
    
            // ✅ Retrieve the correct vault key
            SecretKey rootKey = VaultEncryption.deriveRootKey(rootPassword, Base64.getDecoder().decode(vault.getSalt()));
            SecretKey vaultKey = VaultEncryption.getVaultKey(vault, rootKey);
    
            // 🔍 Debug: Print the vault key used for decryption
            System.out.println("🔑 Debug: Vault Key Used for Decryption: " + Base64.getEncoder().encodeToString(vaultKey.getEncoded()));
    
            // ✅ Decrypt the private key
            byte[] decryptedBytes = VaultEncryption.decryptAESGCM(
                Base64.getDecoder().decode(encryptedPrivKey),
                vaultKey,
                Base64.getDecoder().decode(iv)
            );
    
            // 🔍 Debug: Print decrypted value
            String decryptedKey = new String(decryptedBytes);
            System.out.println("🔓 Debug: Decrypted Private Key (Raw String): " + decryptedKey);
    
            return decryptedKey;
    
        } catch (Exception e) {
            e.printStackTrace();
            return "[Error decrypting private key]";
        }
    }
}