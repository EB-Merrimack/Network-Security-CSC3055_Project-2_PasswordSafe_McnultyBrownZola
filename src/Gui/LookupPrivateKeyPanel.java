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
    private JButton searchButton, backButton;
    private Vault vault;
    private GUIBuilder guiBuilder; // Needed to access stored user password

    public LookupPrivateKeyPanel(Vault vault, GUIBuilder guiBuilder) {
        this.vault = vault;
        this.guiBuilder = guiBuilder;

        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        serviceNameField = new JTextField(15);
        searchButton = new JButton("Lookup");
        backButton = new JButton("← Back");


        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Service Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(serviceNameField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(searchButton);
        buttonPanel.add(backButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> searchVaultForPrivateKey());
        backButton.addActionListener(e -> guiBuilder.showPanel("Main"));

    }

    /**
     * Search the vault for the private key of the service with the given name.
     * Prompts the user for the root password if it is not already stored.
     * Shows a message dialog with the private key if it is found, or a warning
     * if not found.
     */
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

    /**
     * Retrieves the private key for the given service name from the vault.
     * Given the root password, it will decrypt the private key.
     * If the private key is not found, null is returned.
     * @param serviceName The service name to look up.
     * @param rootPassword The root password to decrypt the private key with.
     * @return The decrypted private key, or null if not found.
     */
    private String getPrivateKey(String serviceName, String rootPassword) {
        JSONArray privateKeys = vault.getPrivateKeys();


        for (int i = 0; i < privateKeys.size(); i++) {
            JSONObject entry = privateKeys.getObject(i);
            String storedServiceName = entry.getString("service");

            if (storedServiceName.equalsIgnoreCase(serviceName)) {
                return decryptPrivateKey(entry, rootPassword);
            }
        }

        return null;
    }

    /**
     * Decrypts the private key stored in the provided JSON entry using the specified root password.
     * 
     * This method extracts the encrypted private key and the initialization vector (IV)
     * from the JSON entry, derives the vault key using the root password, and then
     * decrypts the private key using AES-GCM encryption.
     * 
     * @param entry The JSONObject containing the encrypted private key and associated IV.
     * @param rootPassword The root password used to derive the vault key for decryption.
     * @return The decrypted private key as a string, or an error message if decryption fails.
     */

    private String decryptPrivateKey(JSONObject entry, String rootPassword) {
        try {
            String encryptedPrivKey = entry.getString("privkey");
            String iv = entry.getString("iv");
        
            // Retrieve the vault key
            SecretKey rootKey = VaultEncryption.deriveRootKey(rootPassword, Base64.getDecoder().decode(vault.getSalt()));
            SecretKey vaultKey = VaultEncryption.getVaultKey(vault, rootKey);
        
            // Decrypt the private key
            byte[] decryptedBytes = VaultEncryption.decryptAESGCM(
                Base64.getDecoder().decode(encryptedPrivKey),
                vaultKey,
                Base64.getDecoder().decode(iv)
            );
    
            // Print decrypted value
            String decryptedKey = new String(decryptedBytes);    
            return decryptedKey;
    
        } catch (Exception e) {
            e.printStackTrace();
            return "[Error decrypting private key]";
        }
    }
}