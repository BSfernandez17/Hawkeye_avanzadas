package org.example.Services;

import org.example.Model.Usuario;
import org.example.Repositories.UsuarioRepositorio;

public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio){
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Usuario obtenerUsuarioPorEmail(String email)throws Exception{
        return usuarioRepositorio.obtenerUsuarioPorEmail(email);
    }

}
