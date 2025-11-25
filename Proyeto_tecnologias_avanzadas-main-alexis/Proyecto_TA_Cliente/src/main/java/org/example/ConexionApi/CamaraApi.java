package org.example.ConexionApi;

import static org.example.Configuracion.Configuracion.ipServidor;

import org.example.Model.Camara;
import org.example.Repositories.CamaraRepositorio;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.lang.reflect.Type;

public class CamaraApi implements CamaraRepositorio {

    private static final String API_URL = "http://" + ipServidor + ":8080/api/camaras/";
    private static final Gson gson = new Gson();
    private final String token;

    public CamaraApi(String token) {
        this.token = token;
    }

    @Override
    public Camara guardarCamara(Camara camara) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        Integer idUsuario = camara.getUsuario() != null ? camara.getUsuario().getId() : null;

        JsonObject req = new JsonObject();
        req.addProperty("id", camara.getId());
        req.addProperty("nombre", camara.getNombre());
        req.addProperty("serverHost", camara.getServerHost());
        req.addProperty("serverPort", camara.getServerPort() != null ? camara.getServerPort() : 9000);
        if (camara.getTipo() != null) req.addProperty("tipo", camara.getTipo());
        if (camara.getIndiceLocal() != null) req.addProperty("indiceLocal", camara.getIndiceLocal());
        if (camara.getStreamUrl() != null) req.addProperty("streamUrl", camara.getStreamUrl());
        if (idUsuario != null) req.addProperty("idUsuario", idUsuario);

        String jsonRequest = gson.toJson(req);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "registrar"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Request registrarCamara: " + jsonRequest);
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());

        if (response.statusCode() != 201) return null;
        return gson.fromJson(response.body(), Camara.class);
    }

    @Override
    public List<Camara> obtenerCamarasPorUsuario(int idUsuario) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String url = API_URL + "obtenerCamarasPorUsuario/" + idUsuario;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Type tipoLista = new TypeToken<List<Camara>>(){}.getType();
            return gson.fromJson(response.body(), tipoLista);
        }
        return null;
    }

    @Override
    public boolean eliminarCamaraPorId(String idCamara) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String url = API_URL + "eliminarCamara/" + idCamara;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }

    @Override
    public Camara obtenerCamaraPorUsuarioYid(int idUsuario, String idCamara) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String url = API_URL + "obtenerCamaraPorUsuarioYid?idUsuario=" + idUsuario + "&idCamara=" + idCamara;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), Camara.class);
        }
        return null;
    }
}
