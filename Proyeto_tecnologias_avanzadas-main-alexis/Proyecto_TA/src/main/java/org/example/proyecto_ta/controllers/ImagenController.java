package org.example.proyecto_ta.controllers;

import org.example.proyecto_ta.Services.ImagenService;
import org.example.proyecto_ta.model.Imagen;
import org.example.proyecto_ta.model.Camara;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    private final ImagenService imagenServicio;

    public ImagenController(ImagenService imagenServicio){
        this.imagenServicio = imagenServicio;
    }

    // ----------------------------------------------------
    // 1. Obtener imagen por ID (para mostrarla en el front)
    // ----------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable Long id) {
        Imagen img = imagenServicio.obtenerImagenPorId(id)
                .orElse(null);

        if (img == null || img.getImagen() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] bytes = img.getImagen();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                img.getTipoMime() != null ? img.getTipoMime() : "image/jpeg"
        ));
        headers.setContentLength(bytes.length);

        headers.setContentDisposition(
                ContentDisposition.inline().filename(
                        img.getNombreArchivo() != null ? img.getNombreArchivo() : ("imagen_" + id + ".jpg")
                ).build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }

    // ----------------------------------------------------
    // 2. Listar todas las imágenes con cámara y propietario
    // ----------------------------------------------------
    @GetMapping("/camara/listarcamaras")
    public List<ImagenMeta> listarTodo() {
        List<Imagen> imagenes = imagenServicio.listarTodas();
        return imagenes.stream()
                .map(ImagenMeta::from)
                .toList();
    }

    // ----------------------------------------------------
    // 3. Listar por cámara
    // ----------------------------------------------------
    @GetMapping("/camara/{idCamara}")
    public List<ImagenMeta> listarPorCamara(@PathVariable String idCamara) {
        List<Imagen> imagenes = imagenServicio.obtenerPorCamaraId(idCamara);
        return imagenes.stream()
                .map(ImagenMeta::from)
                .toList();
    }

    // ----------------------------------------------------
    // 4. DTO para devolver al frontend
    // ----------------------------------------------------
    public static class ImagenMeta {
        public Long id;
        public String titulo;
        public String filtro;
        public String tipoMime;
        public String nombreArchivo;
        public String camaraId;
        public String camaraNombre;
        public Long propietarioId;
        public String propietarioNombre;
        public String propietarioEmail;

        public static ImagenMeta from(Imagen img) {
            ImagenMeta m = new ImagenMeta();

            m.id = img.getId();
            m.titulo = img.getTitulo();
            m.filtro = img.getFiltro();
            m.tipoMime = img.getTipoMime();
            m.nombreArchivo = img.getNombreArchivo();

            Camara cam = img.getCamara();
            if (cam != null) {
                m.camaraId = cam.getId();
                m.camaraNombre = cam.getNombre();

                if (cam.getUsuario() != null) {
                    m.propietarioId = cam.getUsuario().getId();
                    m.propietarioNombre = cam.getUsuario().getNombre();
                    m.propietarioEmail = cam.getUsuario().getEmail();
                }
            }

            return m;
        }
    }
}
