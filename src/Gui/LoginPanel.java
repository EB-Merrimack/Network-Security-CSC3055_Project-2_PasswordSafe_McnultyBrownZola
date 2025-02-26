package Gui;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel{
       public LoginPanel(GUIBuilder parent) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel label = new JLabel("Enter Vault Password:");
        JPasswordField passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Unlock Vault");
        
        loginButton.addActionListener(e -> parent.showPanel("Main", new MainPanel(parent)));

        
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
