import gui.startGUI;

import java.awt.*;
import java.io.IOException;

public class Main2 {
    public static void main(String args[]){
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    new startGUI("Wzajemne wykluczenie2",2);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
