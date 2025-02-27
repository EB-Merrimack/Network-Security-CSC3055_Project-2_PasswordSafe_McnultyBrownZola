
import javax.swing.JFrame;
import Gui.GUIBuilder;


public class Main {
   
    public static void main(String[] args) throws Exception {
        GUIBuilder gui = new GUIBuilder();
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setSize(800, 600);
        gui.setVisible(true);

        
    }
}

       