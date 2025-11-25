package org.example.proyecto_ta.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "video")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_camara", nullable = false)
    private Camara camara;

    @Column(length = 100)
    private String titulo;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] video;

    @Column(name = "tipo_mime", length = 50)
    private String tipoMime;

    @Column(name = "nombre_archivo", length = 255)
    private String nombreArchivo;

    @Column(name = "fecha_grabacion", insertable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaGrabacion;

    public Video() {
    }

    public Video(Long id, Camara camara, String titulo, byte[] video, String tipoMime, String nombreArchivo, LocalDateTime fechaGrabacion) {
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
