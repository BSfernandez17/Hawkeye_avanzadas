package org.example.Model;

public class GestorSesion {
    private static GestorSesion instancia;
    private Usuario usuario;
    private String token;

    private GestorSesion(){}

    public static GestorSesion getInstancia(){
        if(instancia==null){
            instancia = new GestorSesion();
        }
        return instancia;
    }

    public void iniciarSesion(Usuario usuario) {
        this.usuario = usuario;
    }

    public void cerrarSesion() {
        if (usuario != null) {
            usuario = null;
        }
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void guardarToken(String token) {
        // Decodificar el token para obtener información del usuario
        String[] partes = token.split("\\.");
        if (partes.length == 3) {
            String payload = new String(java.util.Base64.getDecoder().decode(partes[1]));
            org.json.JSONObject json = new org.json.JSONObject(payload);
            String email = json.getString("sub");
            this.usuario = new Usuario(email); // Configurar el usuario con el email extraído
            if (json.has("nombre")) {
                this.usuario.setNombre(json.getString("nombre"));
            }
            if (json.has("rol")) {
                this.usuario.setRol(json.getString("rol"));
            }
            System.out.println("Contenido del token decodificado: " + payload);
        }
    }

    public String obtenerToken() {
        return token;
    }
}
