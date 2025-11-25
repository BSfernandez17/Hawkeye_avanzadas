package org.example.Services;

import org.example.Model.Camara;
import org.example.Repositories.CamaraRepositorio;
import org.example.Pool.CamaraPool;
import java.util.List;

public class CamaraServicio {

    private final CamaraRepositorio camaraRepositorio;
    private final CamaraPool camaraPool;

    public CamaraServicio(CamaraRepositorio camaraRepositorio){
        this.camaraRepositorio = camaraRepositorio;
        this.camaraPool = new CamaraPool(2,10,2000);
    }

    public Camara obtenerCamaraPooled() throws Exception {
        return (Camara) camaraPool.getObject();
    }

    public void liberarCamara(Camara camara){
        camaraPool.releaseObject(camara);
    }

    public Camara guardarCamara(Camara camara) throws Exception{
        Camara toSave = camara;
        if(toSave == null){
            toSave = obtenerCamaraPooled();
        }
        Camara persisted = camaraRepositorio.guardarCamara(toSave);
        liberarCamara(toSave);
        return persisted;
    }

    public List<Camara> obtenerCamarasPorUsuario(int idUsuario) throws Exception{
        return camaraRepositorio.obtenerCamarasPorUsuario(idUsuario);
    }

    public Camara obtenerCamaraPorUsuarioYid(int idUsuario, String idCamara) throws Exception{
        return camaraRepositorio.obtenerCamaraPorUsuarioYid(idUsuario, idCamara);
    }

    public boolean eliminarCamaraPorId(String idCamara) throws Exception{
        return camaraRepositorio.eliminarCamaraPorId(idCamara);
    }
}
