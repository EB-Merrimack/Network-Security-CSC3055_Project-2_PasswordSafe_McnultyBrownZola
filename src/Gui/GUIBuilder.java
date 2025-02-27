package Gui;

import javax.swing.*;

import Vault.Vault;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;

import java.awt.*;
import java.io.File;

public class GUIBuilder extends JFrame{
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Vault vault;
    private static final String VAULT_FILE = "src/Vault/vault.json";

    


    public GUIBuilder() {

        setTitle("Secrets Vault");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        File file = new File(VAULT_FILE);
        if (file.exists()) {
            try {
                JSONObject jsonVault = JsonIO.readObject(file);
                vault = new Vault();
                vault.deserialize(jsonVault);
                System.out.println("Vault loaded successfully.");
            } catch (Exception e) {
                System.err.println("Error loading vault: " + e.getMessage());
                vault = new Vault(); // Create a new vault if loading fails
            }
        } else {
            System.out.println("Creating a new vault...");
            vault = new Vault();
            saveVault(); // Save new vault immediately
        }

        // ✅ Add login & main menu panels
        mainPanel.add(new LoginPanel(this, vault), "Login");
        mainPanel.add(new MainPanel(this, vault), "Main");
        
        add(mainPanel);
    
        // Initially show the Login panel
        cardLayout.show(mainPanel, "Login");
    }

    public void saveVault() {
        try {
            JsonIO.writeFormattedObject(vault, new File(VAULT_FILE));
            System.out.println("Vault saved successfully.");
        } catch (Exception e) {
            System.err.println("Error saving vault: " + e.getMessage());
        }
    }
    
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName); // ✅ Switches to an already added panel
    }
    
    public void showPanel(String panelName, JPanel panel) {
        mainPanel.add(panel, panelName);  // ✅ Dynamically adds a new panel if needed
        cardLayout.show(mainPanel, panelName);
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUIBuilder::new);
    }
    public void showPopupMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }
}
