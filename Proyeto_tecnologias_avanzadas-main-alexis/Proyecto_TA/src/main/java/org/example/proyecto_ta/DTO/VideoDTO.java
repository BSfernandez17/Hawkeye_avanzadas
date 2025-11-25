package org.example.proyecto_ta.DTO;


import java.time.LocalDateTime;


public class VideoDTO {
    private Long id;
    private CamaraDTO camara;
    private String titulo;
    private byte[] video;
    private String tipoMime;
    private String nombreArchivo;
    private LocalDateTime fechaGrabacion;

    public VideoDTO(){}

    public VideoDTO(Long id, CamaraDTO camara, String titulo, byte[] video, String tipoMime, String nombreArchivo, LocalDateTime fechaGrabacion) {
        this.id = id;
        this.camara = camara;
        this.titulo = titulo;
        this.video = video;
        this.tipoMime = tipoMime;
        this.nombreArchivo = nombreArchivo;
        this.fechaGrabacion = fechaGrabacion;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CamaraDTO getCamara() {
        return camara;
    }

    public void setCamara(CamaraDTO camara) {
        this.camara = camara;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public byte[] getVideo() {
        return video;
    }

    public void setVideo(byte[] video) {
        this.video = video;
    }

    public String getTipoMime() {
        return tipoMime;
    }

    public void setTipoMime(String tipoMime) {
        this.tipoMime = tipoMime;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public LocalDateTime getFechaGrabacion() {
        return fechaGrabacion;
    }

    public void setFechaGrabacion(LocalDateTime fechaGrabacion) {
        this.fechaGrabacion = fechaGrabacion;
    }

}
