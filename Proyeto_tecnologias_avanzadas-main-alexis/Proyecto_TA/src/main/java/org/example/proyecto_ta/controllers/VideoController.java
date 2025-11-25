package org.example.proyecto_ta.controllers;

import org.example.proyecto_ta.Services.VideoService;
import org.example.proyecto_ta.model.Video;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoServicio;

    public VideoController(VideoService videoServicio){
        this.videoServicio = videoServicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> obtenerVideo(@PathVariable Long id) {
        Optional<Video> vOpt = videoServicio.obtenerVideoPorId(id);
        if (vOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Video v = vOpt.get();
        byte[] bytes = v.getVideo();
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                v.getTipoMime() != null ? v.getTipoMime() : "video/mp4"
        ));
        headers.setContentLength(bytes.length);
        headers.setContentDisposition(
                ContentDisposition.inline().filename(
                        v.getNombreArchivo() != null ? v.getNombreArchivo() : ("video_" + v.getId() + ".mp4")
                ).build()
        );

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/camara/{idCamara}")
    public ResponseEntity<List<VideoMeta>> listarPorCamara(@PathVariable String idCamara) {
        List<Video> videos = videoServicio.obtenerVideosPorCamaraId(idCamara);
        List<VideoMeta> metas = videos.stream().map(VideoMeta::from).toList();
        return ResponseEntity.ok(metas);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        videoServicio.eliminarPorId(id);
        return ResponseEntity.ok("OK");
    }
@GetMapping("/listar-todo")

public ResponseEntity<List<VideoDetalle>> listarTodos() {
    List<Video> videos = videoServicio.listarTodos();
    List<VideoDetalle> detalles = videos.stream()
            .map(VideoDetalle::from)
            .toList();
    return ResponseEntity.ok(detalles);
}

    public static class VideoMeta {
        private Long id;
        private String titulo;
        private String tipoMime;
        private String nombreArchivo;
        private LocalDateTime fechaGrabacion;
        private long tamanoBytes;

        public static VideoMeta from(Video v) {
            VideoMeta m = new VideoMeta();
            m.id = v.getId();
            m.titulo = v.getTitulo();
            m.tipoMime = v.getTipoMime();
            m.nombreArchivo = v.getNombreArchivo();
            m.fechaGrabacion = v.getFechaGrabacion();
            m.tamanoBytes = v.getVideo() != null ? v.getVideo().length : 0;
            return m;
        }

        public Long getId() {
            return id;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getTipoMime() {
            return tipoMime;
        }

        public String getNombreArchivo() {
            return nombreArchivo;
        }

        public LocalDateTime getFechaGrabacion() {
            return fechaGrabacion;
        }

        public long getTamanoBytes() {
            return tamanoBytes;
        }
    }
    public static class VideoDetalle {
    private Long id;
    private String titulo;
    private String tipoMime;
    private String nombreArchivo;
    private LocalDateTime fechaGrabacion;
    private long tamanoBytes;

    private String idCamara;
    private String nombreCamara;

    private Long idUsuario;
    private String nombreUsuario;
    private String emailUsuario;

    public static VideoDetalle from(Video v) {
        VideoDetalle d = new VideoDetalle();
        d.id = v.getId();
        d.titulo = v.getTitulo();
        d.tipoMime = v.getTipoMime();
        d.nombreArchivo = v.getNombreArchivo();
        d.fechaGrabacion = v.getFechaGrabacion();
        d.tamanoBytes = v.getVideo() != null ? v.getVideo().length : 0;

        // Cámara
        if (v.getCamara() != null) {
            d.idCamara = v.getCamara().getId();
            d.nombreCamara = v.getCamara().getNombre();

            // Usuario propietario
            if (v.getCamara().getUsuario() != null) {
                d.idUsuario = v.getCamara().getUsuario().getId();
                d.nombreUsuario = v.getCamara().getUsuario().getNombre();
                d.emailUsuario = v.getCamara().getUsuario().getEmail();
            }
        }

        return d;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getTipoMime() { return tipoMime; }
    public String getNombreArchivo() { return nombreArchivo; }
    public LocalDateTime getFechaGrabacion() { return fechaGrabacion; }
    public long getTamanoBytes() { return tamanoBytes; }
    public String getIdCamara() { return idCamara; }
    public String getNombreCamara() { return nombreCamara; }
    public Long getIdUsuario() { return idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getEmailUsuario() { return emailUsuario; }
}

}
