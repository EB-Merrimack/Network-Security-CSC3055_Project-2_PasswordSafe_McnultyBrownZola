package Gui;

import javax.swing.*;
import java.awt.*;

public class GUIBuilder extends JFrame{
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public GUIBuilder() {

        setTitle("Secrets Vault");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new LoginPanel(), "Login");
        mainPanel.add(new MainPanel(), "Main");
        
        add(mainPanel);
    
        // Initially show the Login panel
        cardLayout.show(mainPanel, "Login");
    }
    
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUIBuilder::new);
    }
}
