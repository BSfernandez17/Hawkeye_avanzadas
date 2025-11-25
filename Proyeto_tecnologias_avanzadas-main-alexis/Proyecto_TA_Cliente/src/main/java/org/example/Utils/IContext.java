package org.example.Utils;

import org.example.Model.Usuario;

public interface IContext {
    void setToken(String token);
    String getToken();
    void setUsuario(Usuario usuario);
    Usuario getUsuario();
}