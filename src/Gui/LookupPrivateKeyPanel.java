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
        this.vault = Vault.loadVault(vaultFile);

        // UI 
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
        searchButton.addActionListener(e -> searchPrivateKey());
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

        // Ensure private keys are not null
        JSONArray privateKeys = vault.getPrivateKeys();
        if (privateKeys == null || privateKeys.size() == 0) {
            resultArea.setText("No private keys stored.");
            return;
        }

        boolean found = false;

        for (int i = 0; i < privateKeys.size(); i++) {
            JSONObject entry = privateKeys.getObject(i);
            if (entry.getString("service").equalsIgnoreCase(serviceName)) {
                String privateKey = entry.getString("privkey");
                resultArea.setText("🔑 Service: " + serviceName + "\nPrivate Key: " + privateKey);
                found = true;
                break;
            }
        }

        if (!found) {
            resultArea.setText("No private key found for service: " + serviceName);
        }
    }
}
