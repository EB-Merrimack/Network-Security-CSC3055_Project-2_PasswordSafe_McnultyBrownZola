import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import Gui.GUIBuilder;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;

public class Main {
    private static final String vaultfile = "src/json/vault.json";
    
    public static void main(String[] args) {
        File vaultFile = new File(vaultfile);
        if (!vaultFile.exists()) {
            try {
                Vault vault = new Vault();
                JsonIO.writeFormattedObject((JSONSerializable) vault, vaultFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        GUIBuilder gui = new GUIBuilder();
        // What to do when the window closes:
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Size of the window, in pixels
        gui.setSize(800, 600);
        // Make the window "visible"
        gui.setVisible(true);
    }
}
