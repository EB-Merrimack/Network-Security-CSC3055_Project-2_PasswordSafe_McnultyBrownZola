import javax.swing.JFrame;

import Gui.GUIBuilder;

public class Main {
    public static void main(String[] args) {
        GUIBuilder gui = new GUIBuilder();
        // What to do when the window closes:
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Size of the window, in pixels
        gui.setSize(800, 600);
        // Make the window "visible"
        gui.setVisible(true);
    }
    
}
