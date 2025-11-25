package org.example.proyecto_ta.DTO;

import org.example.proyecto_ta.model.Camara;

import java.sql.Timestamp;

public class ImagenDTO {
    private Long id;
    private Camara camara;
    private String titulo;
    private byte[] imagen;
    private String filtro;
    private String tipoMime;
    private String nombreArchivo;
    private Timestamp fechaCaptura;

    public ImagenDTO(){

    }

    public ImagenDTO(Long id, Camara camara, String titulo, byte[] imagen, String filtro, String tipoMime, String nombreArchivo, Timestamp fechaCaptura) {
        this.id = id;
        this.camara = camara;
        this.titulo = titulo;
        this.imagen = imagen;
        this.filtro = filtro;
        this.tipoMime = tipoMime;
        this.nombreArchivo = nombreArchivo;
        this.fechaCaptura = fechaCaptura;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Camara getCamara() {
        return camara;
    }

    public void setCamara(Camara camara) {
        this.camara = camara;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
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

    public Timestamp getFechaCaptura() {
        return fechaCaptura;
    }

    public void setFechaCaptura(Timestamp fechaCaptura) {
        this.fechaCaptura = fechaCaptura;
    }
}
