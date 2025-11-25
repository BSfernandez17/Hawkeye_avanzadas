package org.example.proyecto_ta.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.sql.Timestamp;

@Entity
@Table(name = "imagen")
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_camara", nullable = false)
    private Camara camara;  // referencia a Camara.id (String)

    @Column(length = 100)
    private String titulo;

    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] imagen;

    @Column(length = 50)
    private String filtro;

    @Column(name = "tipo_mime", length = 50)
    private String tipoMime;

    @Column(name = "nombre_archivo", length = 255)
    private String nombreArchivo;

    @Column(name = "fecha_captura", insertable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp fechaCaptura;


    @Column(name = "video_id")
    private Long videoId;

    public Imagen() {
    }

    public Imagen(Long id, Camara camara, String titulo, byte[] imagen, String filtro, String tipoMime, String nombreArchivo, Timestamp fechaCaptura) {
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

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }
}
