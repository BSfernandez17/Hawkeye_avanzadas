package org.example.proyecto_ta.controllers;


import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.proyecto_ta.Services.AuthService;
import org.example.proyecto_ta.Services.UsuarioService;
import org.example.proyecto_ta.login.AuthResponse;
import org.example.proyecto_ta.login.LoginRequest;
import org.example.proyecto_ta.login.RegisterRequest;
import org.example.proyecto_ta.model.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request)
    {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(value = "register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request, HttpServletRequest servletRequest)
    {
        String clienteIp=getClientIp(servletRequest);
        return ResponseEntity.ok(authService.register(request,clienteIp));
    }
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null) {
            return request.getRemoteAddr();
        }
        // En caso de que haya varios IPs, tomar el primero
        return forwarded.split(",")[0];
    }
}
