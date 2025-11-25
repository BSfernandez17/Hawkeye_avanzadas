package org.example.proyecto_ta.Repositories;

import java.util.List;
import java.util.Optional;

import org.example.proyecto_ta.model.Camara;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CamaraRepositorio extends JpaRepository<Camara, String> {

    List<Camara> findByUsuario_Id(Long idUsuario);
    Optional<Camara> findByUsuario_IdAndId(Long idUsuario, String idCamara);

    @Query("select c from Camara c join fetch c.usuario where c.id = :id")
    Optional<Camara> findWithUsuarioById(@Param("id") String id);

    @Query("""
    SELECT c.id, c.nombre,
           (COUNT(v.id) + COUNT(i.id)) AS totalArchivos
    FROM Camara c
    LEFT JOIN Video v ON v.camara.id = c.id
    LEFT JOIN Imagen i ON i.camara.id = c.id
    GROUP BY c.id, c.nombre
    ORDER BY totalArchivos DESC
""")
List<Object[]> obtenerCamarasConCantidadDeArchivos();

}
