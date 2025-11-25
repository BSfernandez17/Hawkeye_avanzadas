package org.example.ConexionApi;

import static org.example.Configuracion.Configuracion.ipServidor;
import org.example.Model.Usuario;
import org.example.Repositories.UsuarioRepositorio;
import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;

public class UsuarioApi implements UsuarioRepositorio {
    private static final String API_URL = "http://" + ipServidor + ":8080/auth/";
    private static final Gson gson = new Gson();
    private static String jwtToken;

    public static void setJwtToken(String token) {
        jwtToken = token;
    }

    @Override
    public Usuario obtenerUsuarioPorEmail(String email) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String url = API_URL + "obtenerUsuarioPorEmail?email=" +
                java.net.URLEncoder.encode(email, "UTF-8");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + jwtToken) // Incluir el token JWT
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String responseBody = response.body();

            Usuario usuario = gson.fromJson(responseBody, Usuario.class);
            return usuario;
        } else {
            return null;
        }

    }

    public Usuario registrarUsuario(Usuario usuario) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // Convertir el objeto Usuario a JSON
        String json = gson.toJson(usuario);

        // Log para verificar el cuerpo de la solicitud
        System.out.println("Cuerpo de la solicitud: " + json);

        // Crear la solicitud POST al backend
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "registro"))  // Asegúrate de que el endpoint coincida con el del backend
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        // Enviar la solicitud y registrar la respuesta
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Estado de la respuesta: " + response.statusCode());
        System.out.println("Cuerpo de la respuesta: " + response.body());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            // Si el backend respondió correctamente, convertir la respuesta a un objeto Usuario
            return gson.fromJson(response.body(), Usuario.class);
        } else {
            System.err.println("Error al registrar usuario: " + response.statusCode());
            return null;
        }
    }

    public String autenticarUsuario(String email, String contrasena) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        System.out.println("Ejecutando el método autenticarUsuario");
        String json = gson.toJson(new Usuario(email, contrasena));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Estado de la respuesta: " + response.statusCode());
        System.out.println("Cuerpo de la respuesta: " + response.body());

        if (response.statusCode() != 200) {
            throw new Exception("Error de red: " + response.statusCode() + " - " + response.body());
        }

        String responseBody = response.body();
        try {
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            jwtToken = jsonResponse.get("token").getAsString();
            if (jwtToken == null || jwtToken.isBlank() || jwtToken.split("\\.").length != 3) {
                System.err.println("El token recibido no tiene el formato JWT válido.");
                return null;
            }
            System.out.println("Token extraído y válido: " + jwtToken);
        } catch (Exception e) {
            System.err.println("Error al procesar la respuesta del servidor: " + e.getMessage());
            return null;
        }
        return jwtToken;
    }

    public boolean registrarUsuario(String nombre, String email, String contrasena) {
        try {
            // Crear el JSON para el registro
            JsonObject registroJson = new JsonObject();
            registroJson.addProperty("nombre", nombre);
            registroJson.addProperty("email", email);
            registroJson.addProperty("contrasena", contrasena);

            // Configurar la solicitud HTTP
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(registroJson.toString()))
                .build();

            // Enviar la solicitud y obtener la respuesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Depuración: Imprimir el cuerpo de la respuesta
            System.out.println("Estado de la respuesta: " + response.statusCode());
            System.out.println("Cuerpo de la respuesta: " + response.body());

            // Verificar el estado de la respuesta y manejar errores
            if (response.statusCode() == 200) {
                System.out.println("Usuario registrado exitosamente.");
                return true;
            } else {
                System.out.println("Error al registrar usuario. Código de estado: " + response.statusCode());
                System.out.println("Cuerpo de la respuesta: " + response.body());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
