package Gui;

import javax.swing.*;
import java.awt.*;

public class AddServiceAndPrivateKeyPanel extends JPanel {
    private JTextField serviceNameField;
    private JPasswordField privateKeyField;
    private JButton saveButton;

    public AddServiceAndPrivateKeyPanel() {
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        serviceNameField = new JTextField(15);
        privateKeyField = new JPasswordField(15);
        
        inputPanel.add(new JLabel("Service Name:"));
        inputPanel.add(serviceNameField);
        inputPanel.add(new JLabel("Private Key:"));
        inputPanel.add(privateKeyField);

        // Save Button
        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveService());

        // Add components to panel
        add(inputPanel, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);
    }

    private void saveService() {
        String serviceName = serviceNameField.getText();
        String privateKey = new String(privateKeyField.getPassword());

        if (serviceName.isEmpty() || privateKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Both fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Placeholder action
        JOptionPane.showMessageDialog(this, "Service and Private Key saved!", "Success", JOptionPane.INFORMATION_MESSAGE);

        // Clear fields after saving
        serviceNameField.setText("");
        privateKeyField.setText("");
    }
}
