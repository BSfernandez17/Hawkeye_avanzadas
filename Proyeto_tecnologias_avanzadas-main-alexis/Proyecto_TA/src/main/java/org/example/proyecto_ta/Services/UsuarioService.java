package org.example.proyecto_ta.Services;

import org.example.proyecto_ta.DTO.UsuarioDTO;
import org.example.proyecto_ta.Repositories.UsuarioRepository;
import org.example.proyecto_ta.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario guardar(Usuario usuario) {
        return usuarioRepositorio.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepositorio.findAll();
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmail(email);
    }

    public void eliminarUsuarioPorId(Long id){
        usuarioRepositorio.deleteById(id);
    }


    public Usuario editarUsuario(Long id, Usuario usuarioActualizado) {
        return usuarioRepositorio.findById(id)
                .map(usuarioExistente -> {
                    usuarioExistente.setNombre(usuarioActualizado.getNombre());
                    usuarioExistente.setEmail(usuarioActualizado.getEmail());
                    usuarioExistente.setRol(usuarioActualizado.getRol());
                    usuarioExistente.setStatus(usuarioActualizado.getStatus());
                    usuarioExistente.setIp(usuarioExistente.getIp());
                    if (usuarioActualizado.getContrasena() != null && !usuarioActualizado.getContrasena().isEmpty()) {
                        usuarioExistente.setContrasena(passwordEncoder.encode(usuarioActualizado.getContrasena()));
                    }
                    return usuarioRepositorio.save(usuarioExistente);
                })
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    public List<UsuarioDTO> listarUsuarios(){
        List<Usuario> usuarios = listarTodos();

        return usuarios.stream()
                .map(u -> new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail(), u.getRol(),u.getStatus(),u.getIp()))
                .collect(Collectors.toList());
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepositorio.findById(id);
    }
}
