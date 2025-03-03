package Gui;

import javax.swing.*;
import java.awt.*; 


public class GenerateRandomPasswordPanel extends JPanel{
    private JTextField serviceNameField, usernameField, passwordField;
    private JButton addToVaultButton;
   

    public GenerateRandomPasswordPanel() {
        serviceNameField = new JTextField(10);
        usernameField = new JTextField(10);
        passwordField = new JTextField(10);
        addToVaultButton = new JButton("Add to Vault");

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10)); 
        inputPanel.add(new JLabel("Service Name:"));
        inputPanel.add(serviceNameField);
        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(usernameField);
        inputPanel.add(new JLabel("Password Length:"));
        inputPanel.add(passwordField);
        
        add(inputPanel, BorderLayout.NORTH);
        add(addToVaultButton, BorderLayout.SOUTH);

        addToVaultButton.addActionListener(e -> addToVault());
    }

    //COULD MAKE PUBLIC METHOD AND CALLED FOR MULTIPLE ACTIONS
    private void addToVault() {
        JOptionPane.showMessageDialog(null, "Needs to be added still");
    }

    //METHOD FOR GENERATING A PASSWORD OF A GIVEN LENGTH (HAVE WARNING FOR PASSWORDS LESS THAN 7)
    
}
