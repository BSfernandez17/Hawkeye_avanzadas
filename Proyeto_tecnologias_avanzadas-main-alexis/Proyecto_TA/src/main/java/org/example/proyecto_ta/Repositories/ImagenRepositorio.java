package org.example.proyecto_ta.Repositories;


import java.util.List;

import org.example.proyecto_ta.model.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ImagenRepositorio extends JpaRepository<Imagen, Long>{
    List<Imagen> findByCamara_Id(String id);
    List<Imagen> findByCamara_Usuario_Id(Long usuarioId);
}
