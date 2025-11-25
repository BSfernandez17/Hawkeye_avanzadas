package org.example.proyecto_ta.Services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.proyecto_ta.DTO.CamaraDTO;
import org.example.proyecto_ta.DTO.UsuarioDTO;
import org.example.proyecto_ta.Repositories.CamaraRepositorio;
import org.example.proyecto_ta.model.Camara;
import org.springframework.stereotype.Service;

@Service
public class CamaraService {

    private final CamaraRepositorio camaraRepositorio;

    public CamaraService(CamaraRepositorio camaraRepositorio){
        this.camaraRepositorio = camaraRepositorio;
    }

    public Camara guardarCamara(Camara camara) {
        return camaraRepositorio.save(camara);
    }

    public List<Camara> obtenerCamarasPorUsuario(Long idUsuario){
        return camaraRepositorio.findByUsuario_Id(idUsuario);
    }
    public List<Object[]> obtenerCamarasConCantidadDeArchivos() {
        return camaraRepositorio.obtenerCamarasConCantidadDeArchivos();
    }

    public Optional<Camara> obtenerCamaraPorId(String id) {
        // Usar método que trae también el Usuario para evitar problemas de carga perezosa
        try {
            return camaraRepositorio.findWithUsuarioById(id);
        } catch (Exception e) {
            // Fallback al método por id si la consulta con join falla por algún motivo
            return camaraRepositorio.findById(id);
        }
    }

    public List<Camara> listarTodas(){
        return camaraRepositorio.findAll();
    }

    public void eliminarCamara(String id) {
        camaraRepositorio.findById(id).ifPresent(c -> camaraRepositorio.deleteById(id));
    }

    public Optional<Camara> obtenerCamaraPorUsuarioYid(Long idUsuario, String idCamara) {
        return camaraRepositorio.findByUsuario_IdAndId(idUsuario, idCamara);
    }

    public List<CamaraDTO> listarCamaras() {
        return listarTodas().stream()
                .map(c -> new CamaraDTO(
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
                ))
                .collect(Collectors.toList());
    }
}
