package org.example.Factories;

import javax.swing.JDialog;
import java.awt.image.BufferedImage;
import org.example.Model.Camara;
import org.example.Controllers.ControladorAcciones;
import org.example.Controllers.ImagenControlador;
import org.example.View.Dialogos.DialogoCorrecto;
import org.example.View.Dialogos.DialogoFallido;
import org.example.View.Dialogos.DialogoFiltro;

public class DialogosFabrica {


    private final ImagenControlador imagenControlador;
    private ControladorAcciones controladorAcciones;

    public DialogosFabrica(ImagenControlador imagenControlador){
        this.imagenControlador = imagenControlador;

    }

    public void setDependencias(ControladorAcciones controladorAcciones){
        this.controladorAcciones = controladorAcciones;
    }

    public JDialog crearDialogoTexto(String tipo, java.awt.Frame padre, boolean modal, String mensaje){

        JDialog dialogo = switch(tipo.toLowerCase()){

            case "correcto" -> new DialogoCorrecto(padre,modal, mensaje);
            case "fallido" -> new DialogoFallido(padre,modal,mensaje);
            default -> null;
        };

        return dialogo;
    }

    public JDialog crearDialogoFiltro(java.awt.Frame padre, BufferedImage imagen, Camara camara){

        return new DialogoFiltro(padre, true, imagen, imagenControlador, camara, controladorAcciones );
    }

}
