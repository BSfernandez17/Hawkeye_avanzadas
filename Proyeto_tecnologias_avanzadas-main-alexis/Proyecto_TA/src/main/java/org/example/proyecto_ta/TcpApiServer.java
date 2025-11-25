package org.example.proyecto_ta;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.example.proyecto_ta.Repositories.VideoRepositorio;
import org.example.proyecto_ta.Services.ImagenService;
import org.example.proyecto_ta.model.Imagen;
import org.example.proyecto_ta.model.Video;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class TcpApiServer {

    private static final int API_PORT = 9003;
    private static final int MAX_HEADER_BYTES = 2048;
    private static final int MAX_IMAGE_BYTES = 10_000_000;

    private final VideoRepositorio videoRepositorio;
    private final ImagenService imagenService;
    private volatile boolean running = true;

    public TcpApiServer(VideoRepositorio videoRepositorio, ImagenService imagenService) {
        this.videoRepositorio = videoRepositorio;
        this.imagenService = imagenService;
    }

    @PostConstruct
    public void init() {
        new Thread(this::loop, "TCP-API-Server").start();
    }

    private void loop() {
        try (ServerSocket ss = new ServerSocket(API_PORT)) {
            System.out.println("Servidor API TCP en puerto " + API_PORT);
            while (running) {
                Socket s = ss.accept();
                s.setTcpNoDelay(true);
                new Thread(() -> handle(s), "TCP-API-" + s.getInetAddress()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handle(Socket socket) {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            int hlen = in.readInt();
            if (hlen <= 0 || hlen > MAX_HEADER_BYTES) return;

            byte[] hb = new byte[hlen];
            in.readFully(hb);
            String header = new String(hb, StandardCharsets.UTF_8).trim();

            if (header.startsWith("LIST_VIDEOS:")) {
                String cameraId = header.substring("LIST_VIDEOS:".length()).trim();
                List<Video> videos = videoRepositorio.findByCamara_Id(cameraId);

                out.writeInt(videos.size());
                for (Video v : videos) {
                    out.writeLong(v.getId());
                    out.writeUTF(v.getTitulo() != null ? v.getTitulo() : ("Video " + v.getId()));
                    out.writeUTF(v.getTipoMime() != null ? v.getTipoMime() : "video/mp4");
                    out.writeUTF(v.getNombreArchivo() != null ? v.getNombreArchivo() : ("video_" + v.getId() + ".mp4"));
                }
                out.flush();
                return;
            }

            if (header.startsWith("CAPTURE:")) {
                String[] parts = header.split("\\|");
                Long videoId = Long.parseLong(parts[0].substring("CAPTURE:".length()).trim());

                Double sec = 0.0;
                String filtro = "NONE";
                Integer scale = 100;
                Float bright = 0f;
                Integer rot = 0;

                for (int i = 1; i < parts.length; i++) {
                    String p = parts[i].trim();
                    if (p.startsWith("SEC:")) sec = Double.parseDouble(p.substring(4));
                    if (p.startsWith("FILTER:")) filtro = p.substring(7);
                    if (p.startsWith("SCALE:")) scale = Integer.parseInt(p.substring(6));
                    if (p.startsWith("BRIGHT:")) bright = Float.parseFloat(p.substring(7));
                    if (p.startsWith("ROT:")) rot = Integer.parseInt(p.substring(4));
                }

                try {
                    Imagen img = imagenService.capturarDesdeVideo(videoId, sec, filtro, scale, bright, rot);
                    byte[] jpg = img.getImagen();
                    out.writeUTF("OK:" + img.getId());
                    out.writeInt(jpg.length);
                    out.write(jpg);
                    out.flush();
                    return;
                } catch (IllegalStateException ise) {
                    out.writeUTF("REJECTED:" + ise.getMessage());
                    out.flush();
                    return;
                } catch (Exception ex) {
                    out.writeUTF("ERR:CAPTURE_FAILED");
                    out.flush();
                    return;
                }
            }

            if (header.startsWith("CAPTURE_ALL:")) {
                String[] parts = header.split("\\|");
                Long videoId = Long.parseLong(parts[0].substring("CAPTURE_ALL:".length()).trim());

                Double sec = 0.0;
                Integer scale = 50;
                Float bright = 0.3f;

                for (int i = 1; i < parts.length; i++) {
                    String p = parts[i].trim();
                    if (p.startsWith("SEC:")) sec = Double.parseDouble(p.substring(4));
                    if (p.startsWith("SCALE:")) scale = Integer.parseInt(p.substring(6));
                    if (p.startsWith("BRIGHT:")) bright = Float.parseFloat(p.substring(7));
                }

                try {
                    List<Imagen> imgs = imagenService.aplicarTodosLosFiltrosYGuardarDesdeVideo(videoId, sec, scale, bright);
                    out.writeUTF("OK_ALL:" + imgs.size());
                    for (Imagen im : imgs) {
                        out.writeLong(im.getId());
                    }
                    out.flush();
                    return;
                } catch (IllegalStateException ise) {
                    out.writeUTF("REJECTED:" + ise.getMessage());
                    out.flush();
                    return;
                } catch (Exception ex) {
                    out.writeUTF("ERR:CAPTURE_ALL_FAILED");
                    out.flush();
                    return;
                }
            }

            if (header.startsWith("SAVE_IMAGE:")) {
                String rest = header.substring("SAVE_IMAGE:".length()).trim();
                String[] parts = rest.split("\\|");

                String cameraId = null;
                String filtro = "NONE";
                Integer scale = 100;
                Float bright = 0f;
                Integer rot = 0;

                for (String p : parts) {
                    String t = p.trim();
                    if (t.startsWith("CAMERA:")) cameraId = t.substring(7).trim();
                    if (t.startsWith("FILTER:")) filtro = t.substring(7).trim();
                    if (t.startsWith("SCALE:")) scale = Integer.parseInt(t.substring(6).trim());
                    if (t.startsWith("BRIGHT:")) bright = Float.parseFloat(t.substring(7).trim());
                    if (t.startsWith("ROT:")) rot = Integer.parseInt(t.substring(4).trim());
                }

                int len = in.readInt();
                if (len <= 0 || len > MAX_IMAGE_BYTES) {
                    out.writeUTF("ERR:IMAGEN_INVALIDA");
                    out.flush();
                    return;
                }

                byte[] jpg = new byte[len];
                in.readFully(jpg);

                try {
                    Imagen img = imagenService.capturarDesdeLiveBytes(cameraId, jpg, filtro, scale, bright, rot);
                    out.writeUTF("OK:" + img.getId());
                    out.flush();
                    return;
                } catch (IllegalStateException ise) {
                    out.writeUTF("REJECTED:" + ise.getMessage());
                    out.flush();
                    return;
                } catch (Exception ex) {
                    out.writeUTF("ERR:SAVE_IMAGE_FAILED");
                    out.flush();
                    return;
                }
            }

            if (header.startsWith("SAVE_ALL_FILTERS:")) {
                String rest = header.substring("SAVE_ALL_FILTERS:".length()).trim();
                String[] parts = rest.split("\\|");

                String cameraId = null;
                Integer scale = 50;
                Float bright = 0.3f;

                for (String p : parts) {
                    String t = p.trim();
                    if (t.startsWith("CAMERA:")) cameraId = t.substring(7).trim();
                    if (t.startsWith("SCALE:")) scale = Integer.parseInt(t.substring(6).trim());
                    if (t.startsWith("BRIGHT:")) bright = Float.parseFloat(t.substring(7).trim());
                }

                int len = in.readInt();
                if (len <= 0 || len > MAX_IMAGE_BYTES) {
                    out.writeUTF("ERR:IMAGEN_INVALIDA");
                    out.flush();
                    return;
                }

                byte[] jpg = new byte[len];
                in.readFully(jpg);

                try {
                    List<Imagen> imgs = imagenService.aplicarTodosLosFiltrosYGuardarDesdeLive(cameraId, jpg, scale, bright);
                    out.writeUTF("OK_ALL:" + imgs.size());
                    for (Imagen im : imgs) {
                        out.writeLong(im.getId());
                    }
                    out.flush();
                    return;
                } catch (IllegalStateException ise) {
                    out.writeUTF("REJECTED:" + ise.getMessage());
                    out.flush();
                    return;
                } catch (Exception ex) {
                    out.writeUTF("ERR:SAVE_ALL_FILTERS_FAILED");
                    out.flush();
                    return;
                }
            }

            out.writeUTF("ERR:HEADER_DESCONOCIDO");
            out.flush();

        } catch (Exception e) {
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    public void stop() {
        running = false;
    }
}
