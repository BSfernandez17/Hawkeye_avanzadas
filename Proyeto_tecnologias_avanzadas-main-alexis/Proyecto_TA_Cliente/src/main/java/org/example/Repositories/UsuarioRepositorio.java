package org.example.Repositories;

import org.example.Model.Usuario;

public interface UsuarioRepositorio {
    Usuario obtenerUsuarioPorEmail(String email) throws Exception;
}
