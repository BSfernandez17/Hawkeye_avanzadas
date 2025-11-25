package org.example.Repositories;

import org.example.Model.Camara;
import java.util.List;

public interface CamaraRepositorio {
    Camara guardarCamara(Camara camara) throws Exception;
    List<Camara> obtenerCamarasPorUsuario(int idUsuario) throws Exception;
    Camara obtenerCamaraPorUsuarioYid(int idUsuario, String idCamara) throws Exception;
    boolean eliminarCamaraPorId(String idCamara) throws Exception;
}
