package org.example.Factories;

import javax.swing.JPanel;
import org.example.Controllers.ControladorAcciones;
import org.example.Controllers.CamaraControlador;
import org.example.Controllers.ControladorCamaraIP;
import org.example.Controllers.UsuarioControlador;
import org.example.Model.GestorSesion;
import org.example.View.InterfazInicio;
import org.example.View.InterfazPrincipal;

public class VentanaFabrica{

    private static VentanaFabrica instancia;
    private ControladorAcciones controladorAcciones;
    private GestorSesion gestorSesion;
    private CamaraControlador camaraControlador;
    private ControladorCamaraIP controladorCamaraIP;
    private UsuarioControlador usuarioControlador;

    private VentanaFabrica() {}

    public static VentanaFabrica getInstancia() {
        if (instancia == null) {
            instancia = new VentanaFabrica();
        }
        return instancia;
    }

    public void setDependencias( ControladorAcciones controladorAcciones, GestorSesion gestorSesion,CamaraControlador camaraControlador,ControladorCamaraIP controladorCamaraIP,UsuarioControlador usuarioControlador ){
        this.controladorAcciones = controladorAcciones;
        this.gestorSesion = gestorSesion;
        this.camaraControlador = camaraControlador;
        this.controladorCamaraIP = controladorCamaraIP;
        this.usuarioControlador = usuarioControlador;
    }

    public CamaraControlador getCamaraControlador(){
        return camaraControlador;
    }

    public void cerrarVentanas(){
        camaraControlador.eliminarObservadores();
    }

    public JPanel crearVentana(String tipo){

        JPanel ventana = switch(tipo.toLowerCase()){

            case "inicio" -> new InterfazInicio(usuarioControlador, controladorAcciones);
            case "principal" -> new InterfazPrincipal(controladorAcciones, gestorSesion);
            default -> {
                System.err.println("Tipo de ventana desconocido: " + tipo);
                yield null;
            }

        };

        return ventana;
    }
}
