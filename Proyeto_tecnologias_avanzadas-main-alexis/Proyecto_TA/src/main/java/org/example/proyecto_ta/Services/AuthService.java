package org.example.proyecto_ta.Services;

import lombok.RequiredArgsConstructor;
import org.example.proyecto_ta.Repositories.UsuarioRepository;
import org.example.proyecto_ta.login.AuthResponse;
import org.example.proyecto_ta.login.LoginRequest;
import org.example.proyecto_ta.login.RegisterRequest;
import org.example.proyecto_ta.model.Rol;
import org.example.proyecto_ta.model.Usuario;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepositorio;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        UserDetails user = usuarioRepositorio.findByEmail(request.getEmail()).orElseThrow();

        System.out.println("Email ingresado: " + request.getEmail());
        System.out.println("Contraseña ingresada: " + request.getContrasena());
        System.out.println("Contraseña encriptada en la base de datos: " + user.getPassword());

        boolean passwordMatches = passwordEncoder.matches(request.getContrasena(), user.getPassword());
        System.out.println("¿Contraseña válida?: " + passwordMatches);

        if (!passwordMatches) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        String token = jwtService.getToken(user);
        return AuthResponse.builder()
                .token(token)
                .build();
    }

    public AuthResponse register(RegisterRequest request, String clienteIp) {
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .contrasena(passwordEncoder.encode(request.getContrasena()))
                .email(request.getEmail())
                .rol(Rol.USER)
                .status(false)
                .ip(clienteIp)
                .build();
        System.out.println(usuario);
        usuarioRepositorio.save(usuario);
        return AuthResponse.builder()
                .token(jwtService.getToken(usuario))
                .build();
    }
}
