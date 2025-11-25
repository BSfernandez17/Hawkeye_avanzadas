package org.example.proyecto_ta.exceptions;

public class AccessDeniedException extends Exception {
    
    public AccessDeniedException(String mensaje) {
        super(mensaje);
    }

    public AccessDeniedException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
