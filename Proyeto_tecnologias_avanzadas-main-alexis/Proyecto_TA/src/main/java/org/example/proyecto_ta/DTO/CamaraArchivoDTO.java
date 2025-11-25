package org.example.proyecto_ta.DTO;

public class CamaraArchivoDTO {
    private String idCamara;
    private String nombreCamara;
    private Long cantidadArchivos;

    public CamaraArchivoDTO(String idCamara, String nombreCamara, Long cantidadArchivos) {
        this.idCamara = idCamara;
        this.nombreCamara = nombreCamara;
        this.cantidadArchivos = cantidadArchivos;
    }

    public String getIdCamara() { return idCamara; }
    public String getNombreCamara() { return nombreCamara; }
    public Long getCantidadArchivos() { return cantidadArchivos; }
}
