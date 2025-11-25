package org.example.proyecto_ta.Repositories;

import org.example.proyecto_ta.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepositorio extends JpaRepository<Video, Long> {
    List<Video> findByCamara_Id(String idCamara);
}
