package org.example.View.Dialogos;

import javax.swing.JDialog;

public class DialogoFallido extends JDialog {
    public DialogoFallido(java.awt.Frame parent, boolean modal, String mensaje){
        super(parent, modal);
        setTitle("Fallido");
    }
}
