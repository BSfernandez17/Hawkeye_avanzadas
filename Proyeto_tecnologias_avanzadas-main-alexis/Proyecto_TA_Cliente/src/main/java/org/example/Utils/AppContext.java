
package org.example.Utils;

import org.example.Model.Usuario;

public class AppContext implements IContext {
    // Método estático para actualizar el token JWT globalmente
    public static void setJwtToken(String token) {
        getInstance().setToken(token);
    }
    private static AppContext instance;
    private String token;
    private Usuario usuario;

    private AppContext() {}

    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    @Override
    public void setToken(String token) {
        if (token == null) {
            this.token = null;
            return;
        }
        if (!token.contains(".")) {
            throw new IllegalArgumentException("Token JWT inválido: " + token);
        }
        this.token = token;
    }

    @Override
    public void setUsuario(Usuario usuario){
        this.usuario = usuario;
    }


    @Override
    public String getToken() {
        return token;
    }

    // Método estático para obtener el token JWT de forma global
    public static String getJwtToken() {
        return getInstance().getToken();
    }

    @Override
    public Usuario getUsuario(){
        return usuario;
    }
}
