package Gui;

import Vault.Vault;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class LookupPrivateKeyPanel extends JPanel {
    private JTextField serviceNameField;
    private JButton searchButton;
    private JTextArea resultArea;
    private Vault vault;

    public LookupPrivateKeyPanel(File vaultFile) {
        // Load the vault from file
        this.vault = Vault.loadVault(vaultFile);

        // UI Components
        serviceNameField = new JTextField(15);
        searchButton = new JButton("Lookup");
        resultArea = new JTextArea(5, 30);
        resultArea.setEditable(false);

        // Layout Setup
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        inputPanel.add(new JLabel("Service Name:"));
        inputPanel.add(serviceNameField);

        add(inputPanel, BorderLayout.NORTH);
        add(searchButton, BorderLayout.CENTER);
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        // Add Action Listener
        searchButton.addActionListener(e -> searchPrivateKey());
    }

    private void searchPrivateKey() {
        String serviceName = serviceNameField.getText().trim();

        if (serviceName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a service name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JSONArray privateKeys = vault.getPrivateKeys(); // Get stored private keys
        boolean found = false;

        for (int i = 0; i < privateKeys.size(); i++) {
            JSONObject entry = privateKeys.getObject(i);
            if (entry.getString("service").equalsIgnoreCase(serviceName)) {
                String privateKey = entry.getString("privkey");
                resultArea.setText("Service: " + serviceName + "\nPrivate Key: " + privateKey);
                found = true;
                break;
            }
        }

        if (!found) {
            resultArea.setText("No private key found for service: " + serviceName);
        }
    }
}
