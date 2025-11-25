package org.example.View.Dialogos;

import javax.swing.JDialog;

public class DialogoCorrecto extends JDialog {
    public DialogoCorrecto(java.awt.Frame parent, boolean modal, String mensaje){
        super(parent, modal);
        setTitle("Correcto");
    }
}
