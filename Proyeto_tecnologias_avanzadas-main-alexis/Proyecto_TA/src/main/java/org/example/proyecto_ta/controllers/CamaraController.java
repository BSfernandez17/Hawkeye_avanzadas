package org.example.proyecto_ta.controllers;

import org.example.proyecto_ta.DTO.CamaraArchivoDTO;
import org.example.proyecto_ta.DTO.CamaraDTO;
import org.example.proyecto_ta.DTO.RegistrarCamaraRequest;
import org.example.proyecto_ta.DTO.UsuarioDTO;
import org.example.proyecto_ta.Repositories.UsuarioRepository;
import org.example.proyecto_ta.Services.CamaraService;
import org.example.proyecto_ta.model.Camara;
import org.example.proyecto_ta.model.TipoCamara;
import org.example.proyecto_ta.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/camaras")
public class CamaraController {

    private final CamaraService camaraServicio;
    private final UsuarioRepository usuarioRepository;

    public CamaraController(CamaraService camaraServicio, UsuarioRepository usuarioRepository) {
        this.camaraServicio = camaraServicio;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarCamara(@RequestBody RegistrarCamaraRequest req) {
        try {
            Usuario usuario = usuarioRepository.findById(req.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            TipoCamara tipo = req.getTipo() == null ? TipoCamara.LOCAL : req.getTipo();

            if (tipo == TipoCamara.LOCAL) {
                if (req.getIndiceLocal() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("indiceLocal es requerido para cámaras LOCAL");
                }
            } else {
                if (req.getStreamUrl() == null || req.getStreamUrl().isBlank()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("streamUrl es requerido para cámaras REMOTA");
                }
            }

            Camara camara = new Camara(
                    req.getId(),
                    usuario,
                    req.getNombre(),
                    tipo,
                    req.getIndiceLocal(),
                    req.getStreamUrl(),
                    req.getServerPort()
            );

            Camara nuevaCamara = camaraServicio.guardarCamara(camara);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(nuevaCamara));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar la cámara: " + e.getMessage());
        }
    }

    @PostMapping("/registrar-lote")
    public ResponseEntity<?> registrarLote(@RequestBody List<RegistrarCamaraRequest> reqs) {
        try {
            for (RegistrarCamaraRequest req : reqs) {
                registrarCamara(req);
            }
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar cámaras: " + e.getMessage());
        }
    }

    @PostMapping("/guardarCamara")
    public ResponseEntity<?> guardarCamara(@RequestBody RegistrarCamaraRequest req) {
        return registrarCamara(req);
    }

    @GetMapping("/obtenerCamarasPorUsuario/{idUsuario}")
    public ResponseEntity<List<CamaraDTO>> obtenerCamarasPorUsuario(@PathVariable Long idUsuario) {
        List<Camara> camaras = camaraServicio.obtenerCamarasPorUsuario(idUsuario);
        List<CamaraDTO> camarasDTO = camaras.stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(camarasDTO);
    }

    @GetMapping("/obtenerCamaraPorUsuarioYid")
    public ResponseEntity<?> obtenerCamaraPorUsuarioYid(@RequestParam Long idUsuario, @RequestParam String idCamara) {
        Optional<Camara> camaraOpt = camaraServicio.obtenerCamaraPorUsuarioYid(idUsuario, idCamara);
        if (camaraOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cámara no encontrada");
        }
        return ResponseEntity.ok(toDTO(camaraOpt.get()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCamaraPorId(@PathVariable String id) {
        Optional<Camara> camaraOpt = camaraServicio.obtenerCamaraPorId(id);
        if (camaraOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cámara no encontrada");
        }
        return ResponseEntity.ok(toDTO(camaraOpt.get()));
    }

    @DeleteMapping("/eliminarCamara/{id}")
    public ResponseEntity<?> eliminarCamara(@PathVariable String id) {
        try {
            camaraServicio.eliminarCamara(id);
            return ResponseEntity.ok("Cámara eliminada correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la cámara: " + e.getMessage());
        }
    }

  @GetMapping("/reporte/camaras-por-archivos")
public ResponseEntity<?> obtenerCamarasOrdenadasPorArchivos() {
    List<Object[]> datos = camaraServicio.obtenerCamarasConCantidadDeArchivos();

    List<CamaraArchivoDTO> respuesta = datos.stream()
            .map(row -> new CamaraArchivoDTO(
                    (String) row[0],   // id cámara
                    (String) row[1],   // nombre cámara
                    (Long) row[2]      // cantidad archivos
            ))
            .collect(Collectors.toList());

    return ResponseEntity.ok(respuesta);
}

    private CamaraDTO toDTO(Camara c) {
        return new CamaraDTO(
                c.getId(),
                new UsuarioDTO(
                        c.getUsuario().getId(),
                        c.getUsuario().getNombre(),
                        c.getUsuario().getEmail(),
                        c.getUsuario().getRol(),
                        c.getUsuario().getStatus(),
                        c.getUsuario().getIp()
                ),
                c.getNombre(),
                c.getTipo(),
                c.getIndiceLocal(),
                c.getStreamUrl(),
                c.getServerPort()
        );
    }
}
