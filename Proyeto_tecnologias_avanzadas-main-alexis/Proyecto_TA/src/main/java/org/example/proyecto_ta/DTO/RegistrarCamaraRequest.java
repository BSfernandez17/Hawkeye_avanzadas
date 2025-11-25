package org.example.proyecto_ta.DTO;

import org.example.proyecto_ta.model.TipoCamara;

public class RegistrarCamaraRequest {

    private String id;
    private String nombre;
    private TipoCamara tipo;
    private Integer indiceLocal;
    private String streamUrl;
    private String serverHost;
    private Integer serverPort;
    private Long idUsuario;

    public RegistrarCamaraRequest() {
    }

    public RegistrarCamaraRequest(String id, String nombre, TipoCamara tipo, Integer indiceLocal, String streamUrl, String serverHost, Integer serverPort, Long idUsuario) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.indiceLocal = indiceLocal;
        this.streamUrl = streamUrl;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.idUsuario = idUsuario;
    }

    public String getId() {
        return id;
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

    public String getServerHost() {
        return serverHost;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
}
