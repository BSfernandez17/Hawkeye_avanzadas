package org.example.proyecto_ta.DTO;

import org.example.proyecto_ta.model.TipoCamara;

public class CamaraDTO {

    private String id;
    private UsuarioDTO usuario;
    private String nombre;
    private TipoCamara tipo;
    private Integer indiceLocal;
    private String streamUrl;
    private Integer serverPort;

    public CamaraDTO() {
    }

    public CamaraDTO(String id, UsuarioDTO usuario, String nombre, TipoCamara tipo, Integer indiceLocal, String streamUrl, Integer serverPort) {
        this.id = id;
        this.usuario = usuario;
        this.nombre = nombre;
        this.tipo = tipo;
        this.indiceLocal = indiceLocal;
        this.streamUrl = streamUrl;
        this.serverPort = serverPort;
    }

    public String getId() {
        return id;
    }

    public UsuarioDTO getUsuario() {
        return usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoCamara getTipo() {
        return tipo;
    }

    public Integer getIndiceLocal() {
        return indiceLocal;
    }

    public String getStreamUrl() {
        return streamUrl;
    }


    public Integer getServerPort() {
        return serverPort;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsuario(UsuarioDTO usuario) {
        this.usuario = usuario;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTipo(TipoCamara tipo) {
        this.tipo = tipo;
    }

    public void setIndiceLocal(Integer indiceLocal) {
        this.indiceLocal = indiceLocal;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }


    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }
}
