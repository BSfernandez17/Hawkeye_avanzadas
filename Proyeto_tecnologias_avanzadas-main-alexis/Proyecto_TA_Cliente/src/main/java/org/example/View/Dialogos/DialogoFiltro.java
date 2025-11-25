package org.example.View.Dialogos;

import javax.swing.JDialog;
import java.awt.image.BufferedImage;
import org.example.Controllers.ImagenControlador;
import org.example.Model.Camara;
import org.example.Controllers.ControladorAcciones;

public class DialogoFiltro extends JDialog {
    public DialogoFiltro(java.awt.Frame parent, boolean modal, BufferedImage imagen, ImagenControlador imagenControlador, Camara camara, ControladorAcciones acciones){
        super(parent, modal);
        setTitle("Filtro");
    }
}
