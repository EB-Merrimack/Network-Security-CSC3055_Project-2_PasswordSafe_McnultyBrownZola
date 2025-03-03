package Gui;

import javax.swing.*;
import Vault.Vault;
//import Vault.VaultSealer;
//import Vault.VaultSealer;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;

import java.awt.*;
import java.io.File;

public class GUIBuilder extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Vault vault;
    private static final String VAULT_FILE = "src/json/vault.json";
    private String userPassword;
    
    
        public GUIBuilder() {
            setTitle("Secrets Vault");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent window from closing immediately
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
    
            // Add login & main menu panels
            mainPanel.add(new LoginPanel(this, vault), "Login");
            mainPanel.add(new MainPanel(this, vault), "Main");
    
            add(mainPanel);
    
            // Initially show the Login panel
            cardLayout.show(mainPanel, "Login");
    
     // Set custom close operation with a confirmation prompt if logged in
     addWindowListener(new java.awt.event.WindowAdapter() {
        /**
         * Handle the window closing event. If the user is logged in, a confirmation
         * prompt is shown to confirm logout. If the user selects "Yes", the vault is
         * sealed and the application is closed. If the user selects "No", the
         * application stays open and the user stays on the current screen.
         * If the user is not logged in, the application is closed immediately.
         */
        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            if (isUserLoggedIn()) {
                int result = JOptionPane.showConfirmDialog(GUIBuilder.this,
                        "You are currently logged in. Closing the application will log you out. Are you sure you want to close and logout?",
                        "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    // Log out and seal the vault
                    logoutAndSealVault();
                    System.exit(0);  // Close the application
                }
    
                // If user selects "No", do nothing and stay on the current screen
                if (result == JOptionPane.NO_OPTION) {
                    // No need to do anything, just stay on the current page
                    ((JFrame) windowEvent.getWindow()).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                }
            } else {
                System.exit(0);  // Close the application if not logged in
            }
        }
    });
    }
    
        private boolean isUserLoggedIn() {
            return LoginPanel.isUserLoggedIn;}
    
        // Method to log out and seal the vault
        public void logoutAndSealVault() {
            if (userPassword == null || userPassword.isEmpty()) {
                System.err.println("❌ Error: Cannot seal vault, user password is missing!");
                return;
            }
        
            System.out.println("🔒 Debug: Sealing vault before exit...");
            vault.sealVault(userPassword);
        }
    
        // Method to save the vault
        public void saveVault() {
            try {
                JsonIO.writeFormattedObject(vault, new File(VAULT_FILE));
                System.out.println("Vault saved successfully.");
            } catch (Exception e) {
                System.err.println("Error saving vault: " + e.getMessage());
            }
        }
    
        // Show the panel names
        public void showPanel(String panelName) {
            cardLayout.show(mainPanel, panelName); // Switches to an already added panel
        }
    
        public void showPanel(String panelName, JPanel panel) {
            mainPanel.add(panel, panelName);  // Dynamically adds a new panel if needed
            cardLayout.show(mainPanel, panelName);
        }
    
        // Show pop-up messages
        public void showPopupMessage(String message) {
            JOptionPane.showMessageDialog(this, message, "Message", JOptionPane.INFORMATION_MESSAGE);
        }
    
        // Getters and setters
        public Vault getVault() {
            return this.vault;
        }
    
        public void setUserPassword(String password) {
            this.userPassword = password;
        }
        
        public String getUserPassword() {
            return this.userPassword;
    }
}
