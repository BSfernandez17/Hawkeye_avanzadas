package org.example.Model;

public class Usuario {

    private Integer id = null;
    private String nombre;
    private String contrasena;
    private String email;
    private String rol;
    private boolean estado;
    private String ip;

    public Usuario(Integer id, String nombre, String contrasena, String email, String rol, boolean Estado, String ip){
        this.id = id;
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.email = email;
        this.rol = rol;
        this.estado = estado;
        this.ip = ip;
    }

    public Usuario(String email, String contrasena) {
        this.email = email;
        this.contrasena = contrasena;
    }

    public Usuario(String email) {
        this.email = email;
    }

    public static class Builder{
        private Integer id;
        private String nombre;
        private String contrasena;
        private String email;
        private String rol;
        private boolean estado;
        private String ip;

        public Builder id(Integer id){
            this.id = id;
            return this;
        }

        public Builder nombre(String nombre){
            this.nombre = nombre;
            return this;
        }

        public Builder contrasena(String contrasena){
            this.contrasena = contrasena;
            return this;
        }

        public Builder email(String email){
            this.email = email;
            return this;
        }

        public Builder rol(String rol){
            this.rol = rol;
            return this;
        }

        public Builder estado(boolean estado)
        {
            this.estado = estado;
            return this;
        }

        public Builder ip (String ip){
            this.ip = ip;
            return this;
        }

        public Usuario build(){
            return new Usuario(id,nombre, contrasena, email, rol,estado,ip);
        }

    }

    public Integer getId(){
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRol() {
        return rol;
    }
    public String getNombre() {
        return nombre;
    }
    public String getContrasena() {
        return contrasena;
    }
    public String getEmail() {
        return email;
    }
    public boolean getEstado()
    {
        return estado;
    }
    public String getIp(){
        return ip;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
