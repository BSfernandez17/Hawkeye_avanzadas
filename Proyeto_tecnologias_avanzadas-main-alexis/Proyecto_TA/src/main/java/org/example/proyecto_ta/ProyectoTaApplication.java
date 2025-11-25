package org.example.proyecto_ta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProyectoTaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProyectoTaApplication.class, args);
    }

}
