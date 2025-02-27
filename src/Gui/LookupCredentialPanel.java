package Gui;

import javax.swing.*;
import java.awt.*;

public class LookupCredentialPanel extends JPanel{
    private JTextField serviceNameField;
    private JButton searchButton;

    public LookupCredentialPanel() {
        serviceNameField = new JTextField(10);
        searchButton = new JButton("Lookup");

        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 10)); 
        inputPanel.add(new JLabel("Service Name:"));
        inputPanel.add(serviceNameField);

        add(inputPanel, BorderLayout.NORTH);
        add(searchButton, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> searchVault());
    }
    
    private void searchVault() {
        JOptionPane.showMessageDialog(null, "Needs to be added still");

    }
}