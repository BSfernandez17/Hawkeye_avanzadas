package org.example.proyecto_ta.DTO;

public class CaptureRequest {
    private Double segundo;
    private String filtro;
    private Integer porcentajeEscala;
    private Float brillo;
    private Integer rotacion;

    public Double getSegundo() {
        return segundo;
    }

    public void setSegundo(Double segundo) {
        this.segundo = segundo;
    }

    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
    }

    public Integer getPorcentajeEscala() {
        return porcentajeEscala;
    }

    public void setPorcentajeEscala(Integer porcentajeEscala) {
        this.porcentajeEscala = porcentajeEscala;
    }

    public Float getBrillo() {
        return brillo;
    }

    public void setBrillo(Float brillo) {
        this.brillo = brillo;
    }

    public Integer getRotacion() {
        return rotacion;
    }

    public void setRotacion(Integer rotacion) {
        this.rotacion = rotacion;
    }
}
