package Gui;

import Vault.Vault;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Base64;

public class LookupPrivateKeyPanel extends JPanel {
    private JTextField serviceNameField;
    private JButton searchButton;
    private JTextArea resultArea;
    private Vault vault;

    public LookupPrivateKeyPanel(File vaultFile) {
        try {
            // Debugging: Check if vault file exists
            if (vaultFile.exists()) {
                System.out.println("Vault file found: " + vaultFile.getAbsolutePath());
            } else {
                System.out.println("Vault file does not exist.");
            }

            // Try loading the vault
            this.vault = Vault.loadVault(vaultFile);
            if (vault != null) {
                System.out.println("Vault loaded successfully!");  // Debug log
            } else {
                System.out.println("Failed to load vault.");  // Debug log
            }
        } catch (Exception e) {
            System.out.println("Failed to load vault: " + e.getMessage());  // Debug log
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

        // Button panel to fix layout issue
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(searchButton);

        // Add components
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        // Button action listener
        searchButton.addActionListener(e -> {
            System.out.println("Button clicked!");  // Debug log
            searchPrivateKey();
        });
    }

    private void searchPrivateKey() {
        String serviceName = serviceNameField.getText().trim();

        if (serviceName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a service name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ensure vault is not null
        if (vault == null) {
            JOptionPane.showMessageDialog(this, "Vault not loaded properly!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ensure private keys are not null or empty
        JSONArray privateKeys = vault.getPrivateKeys();
        if (privateKeys == null || privateKeys.size() == 0) {
            System.out.println("No private keys available in the vault.");  // Debug log
            resultArea.setText("No private keys stored.");
            return;
        }

        System.out.println("Found " + privateKeys.size() + " private key(s) in the vault.");  // Debug log

        boolean found = false;

        for (int i = 0; i < privateKeys.size(); i++) {
            JSONObject entry = privateKeys.getObject(i);
            String vaultServiceName = entry.getString("service");
            System.out.println("Searching for service: " + serviceName + " in vault entry: " + vaultServiceName);  // Debug log

            if (vaultServiceName.equalsIgnoreCase(serviceName)) {
                String privateKey = entry.getString("privkey");

                // Encode the private key in Base64
                String base64PrivateKey = Base64.getEncoder().encodeToString(privateKey.getBytes());

                resultArea.setText(" Service: " + serviceName + "\nBase64 Encoded Private Key: " + base64PrivateKey);
                found = true;
                break;
            }
        }

        if (!found) {
            resultArea.setText("No private key found for service: " + serviceName);
            System.out.println("Private key not found for service: " + serviceName);  // Debug log
        }
    }
}
