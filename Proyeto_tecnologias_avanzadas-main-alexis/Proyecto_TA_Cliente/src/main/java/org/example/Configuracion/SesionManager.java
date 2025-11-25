package org.example.Configuracion;

public class SesionManager {

    private static String jwtToken;
    public static void guardarToken(String token) {
        jwtToken = token;
    }
    public static String obtenerToken() {
        return jwtToken;
    }
    public static void limpiarToken() {
        jwtToken = null;
    }
}