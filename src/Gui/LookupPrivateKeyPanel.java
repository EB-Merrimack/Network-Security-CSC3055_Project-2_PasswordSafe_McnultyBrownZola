package Gui;

import Vault.Vault;
import Vault.VaultEncryption;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class LookupPrivateKeyPanel extends JPanel {
    private JTextField serviceNameField;
    private JButton searchButton;
    private JTextArea resultArea;
    private Vault vault;
    private SecretKey vaultKey; // 🔹 Decrypted Vault Key

    public LookupPrivateKeyPanel(File vaultFile) {
        try {
            // Debugging: Check if vault file exists
            if (vaultFile.exists()) {
                System.out.println("Vault file found: " + vaultFile.getAbsolutePath());
            } else {
                System.out.println("Vault file does not exist.");
            }

            // Load and decrypt the Vault Key
            vaultKey = loadVaultKey();

            // Try loading the vault
            this.vault = Vault.loadVault(vaultFile);
            if (vault != null) {
                System.out.println("Vault loaded successfully!"); // Debug log
            } else {
                System.out.println("Failed to load vault."); // Debug log
            }
        } catch (Exception e) {
            System.out.println("Failed to load vault: " + e.getMessage()); // Debug log
            e.printStackTrace();
        }

        // UI Setup
        serviceNameField = new JTextField(15);
        searchButton = new JButton("Lookup");
        resultArea = new JTextArea(5, 30);
        resultArea.setEditable(false);

        // Layout Setup
        setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        inputPanel.add(new JLabel("Service Name:"));
        inputPanel.add(serviceNameField);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(searchButton);

        // Add components
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        // Button action listener
        searchButton.addActionListener(e -> {
            System.out.println("Button clicked!"); // Debug log
            searchPrivateKey();
        });
    }

    private SecretKey loadVaultKey() {
        try {
            // Load the vault key JSON
            File vaultKeyFile = new File("vault_key.json");
            if (!vaultKeyFile.exists()) {
                throw new RuntimeException("Vault key file not found.");
            }

            String jsonContent = new String(java.nio.file.Files.readAllBytes(vaultKeyFile.toPath()));
            JSONObject vaultKeyJson = new JSONObject();
            byte[] salt = Base64.getDecoder().decode(vaultKeyJson.getString("salt"));
            byte[] encryptedVaultKey = Base64.getDecoder().decode(vaultKeyJson.getString("vaultKeyValue"));
            byte[] iv = Base64.getDecoder().decode(vaultKeyJson.getString("vaultKeyIV"));

            // Prompt user for password
            String userPassword = JOptionPane.showInputDialog("Enter your password:");
            if (userPassword == null || userPassword.isEmpty()) {
                throw new RuntimeException("Password is required to decrypt vault.");
            }

            // Derive Root Key
            SecretKey rootKey = VaultEncryption.deriveRootKey(userPassword, salt);

            // Decrypt Vault Key
            byte[] decryptedVaultKey = VaultEncryption.decryptAESGCM(encryptedVaultKey, rootKey, iv);
            return new SecretKeySpec(decryptedVaultKey, "AES");

        } catch (Exception e) {
            System.err.println("Error loading vault key: " + e.getMessage());
            throw new RuntimeException("Failed to decrypt vault key.");
        }
    }

    private void searchPrivateKey() {
        String serviceName = serviceNameField.getText().trim();

        if (serviceName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a service name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (vault == null || vaultKey == null) {
            JOptionPane.showMessageDialog(this, "Vault or Vault Key not loaded properly!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JSONArray privateKeys = vault.getPrivateKeys();
        if (privateKeys == null || privateKeys.size() == 0) {
            System.out.println("No private keys available in the vault.");
            resultArea.setText("No private keys stored.");
            return;
        }

        for (int i = 0; i < privateKeys.size(); i++) {
            JSONObject entry = privateKeys.getObject(i);
            String vaultServiceName = entry.getString("service");

            if (vaultServiceName.equalsIgnoreCase(serviceName)) {
                byte[] encryptedKey = Base64.getDecoder().decode(entry.getString("privkey"));
                byte[] iv = Base64.getDecoder().decode(entry.getString("iv"));

                try {
                    byte[] decryptedPrivateKey = VaultEncryption.decryptAESGCM(encryptedKey, vaultKey, iv);
                    resultArea.setText(
                            " Service: " + serviceName + "\nDecrypted Private Key: " + new String(decryptedPrivateKey));
                    return;
                } catch (Exception e) {
                    resultArea.setText("Failed to decrypt private key.");
                }
            }
        }

        resultArea.setText("No private key found for service: " + serviceName);
    }
}
