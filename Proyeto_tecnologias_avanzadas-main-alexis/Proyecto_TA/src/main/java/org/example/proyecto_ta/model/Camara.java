package org.example.proyecto_ta.model;

import jakarta.persistence.*;

@Entity
@Table(name = "camara")
public class Camara {

    @Id
    @Column(length = 100)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCamara tipo;

    @Column
    private Integer indiceLocal;

    @Column(length = 512)
    private String streamUrl;


    @Column
    private Integer serverPort;

    public Camara() {
    }

    public Camara(String id, Usuario usuario, String nombre, TipoCamara tipo, Integer indiceLocal, String streamUrl, Integer serverPort) {
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

    public Usuario getUsuario() {
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

    public void setUsuario(Usuario usuario) {
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
