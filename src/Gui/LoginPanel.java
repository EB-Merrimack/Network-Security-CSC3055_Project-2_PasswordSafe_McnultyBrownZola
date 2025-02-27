package Gui;

import javax.swing.*;

import Vault.Vault;

import java.awt.*;

public class LoginPanel extends JPanel{
    private Vault vault;
    
       public LoginPanel(GUIBuilder parent, Vault vault) {
        this.vault = vault;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel label = new JLabel("Enter Vault Password:");
        JPasswordField passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Unlock Vault");
        
        loginButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());

            if (vault.getRootPasswordHash() == null || vault.getRootPasswordHash().isEmpty()) {
                // ✅ First-time setup: Set and save the password
                vault.setRootPassword(password);
                parent.saveVault();
                JOptionPane.showMessageDialog(this, "Root password set! Vault unlocked.");
                parent.showPanel("Main");
            } else {
                // ✅ Verify the entered password
                if (vault.verifyRootPassword(password)) {
                    JOptionPane.showMessageDialog(this, "Access granted.");
                    parent.showPanel("Main");
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect password!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

            
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(label, gbc);
        
        gbc.gridx = 1;
        add(passwordField, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(loginButton, gbc);
    }
    
}
