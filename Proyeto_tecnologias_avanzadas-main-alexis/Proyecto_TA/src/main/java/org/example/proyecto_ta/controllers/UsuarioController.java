package org.example.proyecto_ta.controllers;

import java.util.List;
import java.util.Optional;

import org.example.proyecto_ta.Services.UsuarioService;
import org.example.proyecto_ta.model.Usuario;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioServicio;

    public UsuarioController(UsuarioService usuarioServicio){
        this.usuarioServicio = usuarioServicio;
    }


    @PostMapping("/guardarUsuario")
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return usuarioServicio.guardar(usuario);
    }

    @GetMapping("/obtenerUsuarioPorEmail")
    public Optional<Usuario> obtenerUsuarioPorEmail(@RequestParam String email){
        return usuarioServicio.buscarPorEmail(email);
    }
    @GetMapping("/obtenerUsuarioPorId/{id}")
    public Optional<Usuario> obtenerUsuarioPorId(@PathVariable Long id){
        return  usuarioServicio.buscarPorId(id);
    }

    @GetMapping("/obtenerUsuarios")
    public List<Usuario> obtenerUsuarios() {
        return usuarioServicio.listarTodos();
    }

    @DeleteMapping("/eliminarUsuarioPorId/{id}")
    public void eliminarUsuarioPorID(@PathVariable Long id){
        usuarioServicio.eliminarUsuarioPorId(id);
    }

    @PutMapping("/editarUsuario/{id}")
    public Usuario editarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioActualizado) {
        return usuarioServicio.editarUsuario(id, usuarioActualizado);
    }
}
