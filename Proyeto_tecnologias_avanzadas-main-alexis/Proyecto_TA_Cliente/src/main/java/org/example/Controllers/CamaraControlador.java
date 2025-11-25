package org.example.Controllers;

import org.example.ConexionApi.CamaraApi;
import org.example.Model.Camara;
import org.example.Services.CamaraServicio;

import java.util.List;

public class CamaraControlador {

    private final CamaraServicio camaraServicio;

    public CamaraControlador(String token) {
        this.camaraServicio = new CamaraServicio(new CamaraApi(token));
    }

    public Camara registrarCamara(Camara camara) throws Exception {
        return camaraServicio.guardarCamara(camara);
    }

    public boolean eliminarCamara(String idCamara) throws Exception {
        return camaraServicio.eliminarCamaraPorId(idCamara);
    }

    public List<Camara> obtenerCamarasPorUsuario(int idUsuario) throws Exception {
        return camaraServicio.obtenerCamarasPorUsuario(idUsuario);
    }

    public Camara obtenerCamaraPorUsuarioYid(int idUsuario, String idCamara) throws Exception {
        return camaraServicio.obtenerCamaraPorUsuarioYid(idUsuario, idCamara);
    }

    public void eliminarObservadores() {
    }
}
