package org.example.Utils;

public record VideoMeta(Long id, String titulo, String tipoMime, String nombreArchivo) {
    @Override
    public String toString() {
        String t = titulo != null ? titulo : "Video " + id;
        String n = nombreArchivo != null ? nombreArchivo : "";
        return t + " (" + n + ")";
    }
}
