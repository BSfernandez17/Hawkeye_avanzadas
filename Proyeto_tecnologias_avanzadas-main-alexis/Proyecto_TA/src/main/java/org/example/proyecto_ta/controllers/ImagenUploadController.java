package org.example.proyecto_ta.controllers;

import org.example.proyecto_ta.Services.ImagenService;
import org.example.proyecto_ta.model.Imagen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenUploadController {

    @Autowired
    private ImagenService imagenService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImagen(
            @RequestParam("file") MultipartFile file,
            @RequestParam("cameraId") String cameraId,
            @RequestParam(value = "filtro", required = false) String filtro,
            @RequestParam(value = "escala", required = false) Integer escala,
            @RequestParam(value = "brillo", required = false) Float brillo,
            @RequestParam(value = "rotacion", required = false) Integer rotacion
    ) {
        try {
            byte[] bytes = file.getBytes();
            Imagen img = imagenService.capturarDesdeLiveBytes(cameraId, bytes, filtro, escala, brillo, rotacion);
            return ResponseEntity.ok().body("Imagen guardada con ID: " + img.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al guardar imagen: " + e.getMessage());
        }
    }
}
