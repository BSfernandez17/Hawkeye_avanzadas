package org.example.proyecto_ta.DTO;

import org.example.proyecto_ta.model.Rol;

public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private Boolean status;
    private String ip;

    public UsuarioDTO() {}

    public UsuarioDTO(Long id, String nombre, String email, Rol rol, Boolean status, String ip) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.ip=ip;
    }


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Rol getRol() {
        return rol;
    }
    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Boolean getStatus() {
        return status;
    }
    public void setStatus(Boolean status) {}

    public String getIp() {return ip;}
    public void setIp(String ip) {this.ip = ip;}
}
