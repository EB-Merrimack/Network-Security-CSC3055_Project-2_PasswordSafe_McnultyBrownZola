package Gui;

import javax.swing.*;
import java.awt.*;


// Class to add a service name, a username, and a password to the vault
public class AddCredentialPanel extends JPanel{
    private JTextField serviceNameField, usernameField, passwordField;
    private JButton addToVaultButton;
   

    public AddCredentialPanel() {
        serviceNameField = new JTextField(10);
        usernameField = new JTextField(10);
        passwordField = new JTextField(10);
        addToVaultButton = new JButton("Add to Vault");

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10)); 
        inputPanel.add(new JLabel("Service Name:"));
        inputPanel.add(serviceNameField);
        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(usernameField);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(passwordField);
        
        add(inputPanel, BorderLayout.NORTH);
        add(addToVaultButton, BorderLayout.SOUTH);

        addToVaultButton.addActionListener(e -> addToVault());
    }

    private void addToVault() {
        JOptionPane.showMessageDialog(null, "Needs to be added still");
    }
    
}
