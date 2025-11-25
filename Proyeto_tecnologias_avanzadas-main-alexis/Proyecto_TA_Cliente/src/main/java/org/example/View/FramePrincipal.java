package org.example.View;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class FramePrincipal extends JFrame {
    public void cambiarPanel(JPanel panel){
        setContentPane(panel);
        revalidate();
        repaint();
    }
}