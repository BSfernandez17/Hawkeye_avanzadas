package org.example.proyecto_ta;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.example.proyecto_ta.Services.CamaraService;
import org.example.proyecto_ta.Services.ImagenService;
import org.example.proyecto_ta.Services.VideoService;
import org.example.proyecto_ta.model.Camara;
import org.example.proyecto_ta.model.Imagen;
import org.example.proyecto_ta.model.Usuario;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class TCPServer {

    private static final int INGEST_PORT = 9000;
    private static final int VIEW_PORT = 9001;
    private static final int LIVE_PORT = 9002;
    private static final int MAX_HEADER_BYTES = 2048;
    private static final int MAX_FILES_PER_USER = 200;

    private final VideoService videoService;
    private final CamaraService camaraService;
    private final ImagenService imagenService;

    private final Map<String, CopyOnWriteArrayList<Socket>> viewersPorCamara = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> conexionesPorUsuario = new ConcurrentHashMap<>();
    private final Map<Socket, String> socketToUser = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    public TCPServer(VideoService videoService, CamaraService camaraService, ImagenService imagenService) {
        this.videoService = videoService;
        this.camaraService = camaraService;
        this.imagenService = imagenService;
    }

    @PostConstruct
    public void init() {
        new Thread(this::startViewerLoop, "TCP-View-Server").start();
        new Thread(this::startIngestLoop, "TCP-Ingest-Server").start();
        new Thread(this::startLiveLoop, "TCP-Live-Server").start();
    }

    private void startViewerLoop() {
        try (ServerSocket serverSocket = new ServerSocket(VIEW_PORT)) {
            System.out.println("Servidor VIEW esperando conexiones en puerto " + VIEW_PORT);
            while (running) {
                Socket viewerSocket = serverSocket.accept();
                viewerSocket.setKeepAlive(true);
                viewerSocket.setTcpNoDelay(true);
                new Thread(() -> handleViewer(viewerSocket), "Viewer-Handler-" + viewerSocket.getInetAddress()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startIngestLoop() {
        try (ServerSocket serverSocket = new ServerSocket(INGEST_PORT)) {
            System.out.println("Servidor INGEST esperando streams en puerto " + INGEST_PORT);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setKeepAlive(true);
                clientSocket.setTcpNoDelay(true);
                System.out.println("Cliente ingest conectado: " + clientSocket.getInetAddress());
                new Thread(() -> handleIngestClient(clientSocket), "Ingest-Handler-" + clientSocket.getInetAddress()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startLiveLoop() {
        try (ServerSocket serverSocket = new ServerSocket(LIVE_PORT)) {
            System.out.println("Servidor LIVE esperando frames en puerto " + LIVE_PORT);
            while (running) {
                Socket liveSocket = serverSocket.accept();
                liveSocket.setKeepAlive(true);
                liveSocket.setTcpNoDelay(true);
                new Thread(() -> handleLiveClient(liveSocket), "Live-Handler-" + liveSocket.getInetAddress()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleViewer(Socket socket) {
        String cameraId = null;
        String userKey = null;
        try (InputStream in = socket.getInputStream();
             BufferedInputStream bin = new BufferedInputStream(in)) {

            String header = readHeaderLine(bin, 256);
            if (header == null || !header.startsWith("VIEW:")) {
                try { socket.close(); } catch (Exception ignored) {}
                return;
            }

            cameraId = header.substring("VIEW:".length()).trim();
            if (cameraId.isBlank()) {
                try { socket.close(); } catch (Exception ignored) {}
                return;
            }

            // Obtener la cámara y usuario propietario
            Optional<Camara> camOpt = camaraService.obtenerCamaraPorId(cameraId);
            if (camOpt.isEmpty()) {
                try { socket.close(); } catch (Exception ignored) {}
                System.out.println("VIEW rechazado, cámara no registrada: " + cameraId);
                return;
            }

            Usuario usuario = camOpt.get().getUsuario();
            if (usuario == null) {
                try { socket.close(); } catch (Exception ignored) {}
                System.out.println("VIEW rechazado, cámara sin usuario: " + cameraId);
                return;
            }

            userKey = usuario.getId() != null ? usuario.getId().toString() : usuario.getEmail();
            if (userKey != null) userKey = userKey.trim();
            System.out.println("handleViewer: computed userKey='" + userKey + "' for cameraId=" + cameraId);
            System.out.println("handleViewer: conexionesPorUsuario snapshot before acquire: " + conexionesPorUsuario);
            if (!tryAcquireUserSlot(userKey)) {
                System.out.println("handleViewer: tryAcquireUserSlot returned FALSE for userKey='" + userKey + "'");
                try {
                    socket.getOutputStream().write(("REJECTED: max connections per user reached\n").getBytes(StandardCharsets.UTF_8));
                    socket.getOutputStream().flush();
                } catch (Exception ignored) {}
                try { socket.close(); } catch (Exception ignored) {}
                System.out.println("VIEW rechazado por límite de conexiones para usuario=" + userKey);
                return;
            }
            // registrar asociación socket->usuario para liberación segura
            socketToUser.put(socket, userKey);
            System.out.println("handleViewer: conexionesPorUsuario snapshot after acquire: " + conexionesPorUsuario);

            viewersPorCamara.computeIfAbsent(cameraId, k -> new CopyOnWriteArrayList<>()).add(socket);
            CopyOnWriteArrayList<Socket> list = viewersPorCamara.get(cameraId);
            System.out.println("Viewer suscrito a cameraId=" + cameraId + " desde " + socket.getInetAddress() + "; total viewers=" + (list != null ? list.size() : 0));

            while (running && !socket.isClosed()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }

        } catch (Exception e) {
            System.out.println("handleViewer error for socket " + socket.getInetAddress() + ": " + e.getMessage());
            e.printStackTrace();
            try { socket.close(); } catch (Exception ignored) {}
        } finally {
            removeViewer(socket, cameraId);
            if (userKey != null) releaseUserSlot(userKey);
        }
    }

    private void handleLiveClient(Socket socket) {
        String cameraId = null;
        try (InputStream rawIn = socket.getInputStream();
             BufferedInputStream bin = new BufferedInputStream(rawIn);
             DataInputStream din = new DataInputStream(bin)) {

            String headerLine = readHeaderLine(bin, MAX_HEADER_BYTES);
            if (headerLine == null || !headerLine.startsWith("LIVE:")) {
                return;
            }

            cameraId = headerLine.substring("LIVE:".length()).trim();
            if (cameraId.isBlank()) return;

            Optional<Camara> camOpt = camaraService.obtenerCamaraPorId(cameraId);
            if (camOpt.isEmpty()) {
                System.out.println("LIVE rechazado, cámara no registrada: " + cameraId);
                return;
            }

            System.out.println("LIVE recibido desde cameraId=" + cameraId);

            while (running && !socket.isClosed()) {
                int len;
                try {
                    len = din.readInt();
                } catch (Exception eof) {
                    break;
                }
                if (len <= 0 || len > 5_000_000) continue;

                byte[] frame = new byte[len];
                din.readFully(frame);
                broadcastFrame(cameraId, frame);
            }

        } catch (Exception e) {
            System.out.println("handleLiveClient error for cameraId=" + cameraId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
            System.out.println("LIVE finalizado cameraId=" + cameraId);
        }
    }

    private void handleIngestClient(Socket socket) {
        String userKey = null;
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        String random = UUID.randomUUID().toString().substring(0, 8);

        try (InputStream rawIn = socket.getInputStream();
             BufferedInputStream bin = new BufferedInputStream(rawIn)) {

            String headerLine = readHeaderLine(bin, MAX_HEADER_BYTES);
            if (headerLine == null || !headerLine.startsWith("CAMERA:")) {
                System.out.println("Header inválido o ausente. Conexión descartada.");
                return;
            }

            String cameraId = null;
            String fileName = null;

            String rest = headerLine.substring("CAMERA:".length()).trim();
            String[] parts = rest.split("\\|");
            if (parts.length > 0) cameraId = parts[0].trim();

            for (int i = 1; i < parts.length; i++) {
                String p = parts[i].trim();
                if (p.startsWith("FILE:")) fileName = p.substring("FILE:".length()).trim();
            }

            if (cameraId == null || cameraId.isBlank()) {
                System.out.println("Header sin cameraId. Conexión descartada.");
                return;
            }

            if (camaraService.obtenerCamaraPorId(cameraId).isEmpty()) {
                System.out.println("INGEST rechazado, cámara no registrada: " + cameraId);
                return;
            }

            // Obtener usuario y controlar límite por usuario
            Optional<Camara> camOpt = camaraService.obtenerCamaraPorId(cameraId);
            if (camOpt.isEmpty()) {
                System.out.println("INGEST rechazado, cámara no registrada: " + cameraId);
                return;
            }
            Usuario usuario = camOpt.get().getUsuario();
            if (usuario == null) {
                System.out.println("INGEST rechazado, cámara sin usuario: " + cameraId);
                return;
            }

            userKey = usuario.getId() != null ? usuario.getId().toString() : usuario.getEmail();
            if (!tryAcquireUserSlot(userKey)) {
                try {
                    socket.getOutputStream().write(("REJECTED: max connections per user reached\n").getBytes(StandardCharsets.UTF_8));
                    socket.getOutputStream().flush();
                } catch (Exception ignored) {}
                try { socket.close(); } catch (Exception ignored) {}
                System.out.println("INGEST rechazado por límite de conexiones para usuario=" + userKey);
                return;
            }

            // Validar límite de archivos por usuario (máximo 7: videos + imágenes)
            Long usuarioId = usuario.getId();
            if (usuarioId != null) {
                int videoCount = videoService.obtenerVideosPorUsuario(usuarioId).size();
                int imageCount = imagenService.obtenerImagenesPorUsuario(usuarioId).size();
                int totalFiles = videoCount + imageCount;
                if (totalFiles >= MAX_FILES_PER_USER) {
                    try {
                        socket.getOutputStream().write(("REJECTED: max files per user reached (" + MAX_FILES_PER_USER + " - videos: " + videoCount + ", imágenes: " + imageCount + ")\n").getBytes(StandardCharsets.UTF_8));
                        socket.getOutputStream().flush();
                    } catch (Exception ignored) {}
                    try { socket.close(); } catch (Exception ignored) {}
                    releaseUserSlot(userKey);
                    System.out.println("INGEST rechazado por límite de archivos para usuario=" + userKey + " (total=" + totalFiles + ": videos=" + videoCount + ", imágenes=" + imageCount + ")");
                    return;
                }
            }

            Path ingestDir = Paths.get("ingest", cameraId);
            Files.createDirectories(ingestDir);

            String outName;
            if (fileName != null && !fileName.isBlank()) {
                String safeFile = Paths.get(fileName).getFileName().toString();
                outName = "video_recibido_" + cameraId + "_" + timestamp + "_" + random + "_" + safeFile;
            } else {
                outName = "video_recibido_" + cameraId + "_" + timestamp + "_" + random + ".h264";
            }

            Path outPath = ingestDir.resolve(outName);

            if (fileName != null && !fileName.isBlank()) {
                DataInputStream din = new DataInputStream(bin);
                long fileSize = din.readLong();

                try (FileOutputStream fos = new FileOutputStream(outPath.toFile())) {
                    System.out.println("Recibiendo archivo from camera=" + cameraId + " size=" + fileSize + " -> " + outPath);
                    byte[] buffer = new byte[8192];
                    long remaining = fileSize;

                    while (remaining > 0) {
                        int read = din.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                        if (read == -1) break;
                        fos.write(buffer, 0, read);
                        remaining -= read;
                    }

                    fos.flush();
                    if (remaining > 0) {
                        System.out.println("Archivo incompleto. remaining=" + remaining);
                        Files.deleteIfExists(outPath);
                        return;
                    }
                }
            } else {
                try (FileOutputStream fos = new FileOutputStream(outPath.toFile())) {
                    System.out.println("Recibiendo stream from camera=" + cameraId + " -> " + outPath);
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = bin.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }

                    fos.flush();
                }
            }

            System.out.println("Stream/archivo finalizado, guardado en: " + outPath.toAbsolutePath());

            try {
                videoService.procesarYPersistirSegmento(outPath, cameraId, "Segment " + timestamp);
                System.out.println("Segmento enviado a VideoService para procesar: " + outPath);
            } catch (Exception ex) {
                System.out.println("VideoService error al procesar/persistir: " + ex.getMessage());
                try { Files.deleteIfExists(outPath); } catch (Exception ignored) {}
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
            if (userKey != null) releaseUserSlot(userKey);
        }
    }

    private boolean tryAcquireUserSlot(String userKey) {
        AtomicInteger ai = conexionesPorUsuario.computeIfAbsent(userKey, k -> new AtomicInteger(0));
        while (true) {
            int cur = ai.get();
            if (cur >= 3) return false;
            if (ai.compareAndSet(cur, cur + 1)) {
                System.out.println("Slot adquirido para usuario=" + userKey + " count=" + (cur + 1));
                return true;
            }
        }
    }

    private void releaseUserSlot(String userKey) {
        AtomicInteger ai = conexionesPorUsuario.get(userKey);
        if (ai == null) return;
        while (true) {
            int cur = ai.get();
            if (cur <= 0) {
                conexionesPorUsuario.remove(userKey, ai);
                return;
            }
            if (ai.compareAndSet(cur, cur - 1)) {
                System.out.println("Slot liberado para usuario=" + userKey + " count=" + (cur - 1));
                if (cur - 1 == 0) conexionesPorUsuario.remove(userKey, ai);
                return;
            }
        }
    }

    private void handleImageClient(Socket socket) {
        try (InputStream rawIn = socket.getInputStream();
             BufferedInputStream bin = new BufferedInputStream(rawIn);
             DataInputStream din = new DataInputStream(bin);
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream())) {

            String headerLine = readHeaderLine(bin, MAX_HEADER_BYTES);
            if (headerLine == null || !headerLine.startsWith("CAPTURE:")) {
                return;
            }

            String rest = headerLine.substring("CAPTURE:".length()).trim();
            String[] parts = rest.split("\\|");

            Long videoId = null;
            Double segundo = null;
            String filtro = null;
            Integer escala = null;
            Float brillo = null;
            Integer rotacion = null;

            for (String p : parts) {
                String kv = p.trim();
                int eq = kv.indexOf('=');
                if (eq <= 0) continue;
                String k = kv.substring(0, eq).trim().toUpperCase();
                String v = kv.substring(eq + 1).trim();

                if ("VIDEO".equals(k)) videoId = Long.parseLong(v);
                else if ("SECOND".equals(k)) segundo = Double.parseDouble(v);
                else if ("FILTER".equals(k)) filtro = v;
                else if ("SCALE".equals(k)) escala = Integer.parseInt(v);
                else if ("BRIGHT".equals(k)) brillo = Float.parseFloat(v);
                else if ("ROT".equals(k)) rotacion = Integer.parseInt(v);
            }

            Imagen img = imagenService.capturarDesdeVideo(
                    videoId,
                    segundo,
                    filtro,
                    escala,
                    brillo,
                    rotacion
            );

            byte[] jpg = img.getImagen();
            Long imgId = img.getId() != null ? img.getId() : -1;

            dout.writeLong(imgId);
            dout.writeInt(jpg.length);
            dout.write(jpg);
            dout.flush();

        } catch (Exception e) {
            try { socket.close(); } catch (Exception ignored) {}
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private void broadcastFrame(String cameraId, byte[] frameBytes) {
        CopyOnWriteArrayList<Socket> viewers = viewersPorCamara.get(cameraId);
        if (viewers == null || viewers.isEmpty()) {
            System.out.println("broadcastFrame: no hay viewers para cameraId=" + cameraId);
            return;
        }
        System.out.println("broadcastFrame: enviando frame a " + viewers.size() + " viewers para cameraId=" + cameraId);

        byte[] lenBytes = intToBytes(frameBytes.length);
        // juntar longitud + frame para una única escritura atómica en el stream
        byte[] out = new byte[4 + frameBytes.length];
        System.arraycopy(lenBytes, 0, out, 0, 4);
        System.arraycopy(frameBytes, 0, out, 4, frameBytes.length);

        for (Socket v : viewers) {
            try {
                v.getOutputStream().write(out);
                v.getOutputStream().flush();
            } catch (Exception e) {
                System.out.println("Error enviando frame a viewer " + v.getInetAddress() + ": " + e.getMessage());
                e.printStackTrace();
                try { v.close(); } catch (Exception ignored) {}
                viewers.remove(v);
            }
        }
    }

    private byte[] intToBytes(int val) {
        return new byte[] {
                (byte) ((val >> 24) & 0xFF),
                (byte) ((val >> 16) & 0xFF),
                (byte) ((val >> 8) & 0xFF),
                (byte) (val & 0xFF)
        };
    }

    private void removeViewer(Socket socket, String cameraId) {
        if (cameraId != null) {
            CopyOnWriteArrayList<Socket> list = viewersPorCamara.get(cameraId);
            if (list != null) {
                list.remove(socket);
                if (list.isEmpty()) viewersPorCamara.remove(cameraId);
            }
            // limpiar mapping socket->user si existe
            String u = socketToUser.remove(socket);
            if (u != null) {
                releaseUserSlot(u);
            }
            return;
        }

        for (Map.Entry<String, CopyOnWriteArrayList<Socket>> entry : viewersPorCamara.entrySet()) {
            entry.getValue().remove(socket);
            if (entry.getValue().isEmpty()) {
                viewersPorCamara.remove(entry.getKey());
            }
        }
        String u2 = socketToUser.remove(socket);
        if (u2 != null) releaseUserSlot(u2);
    }

    private String readHeaderLine(BufferedInputStream bin, int maxBytes) throws Exception {
        ByteArrayOutputStream headerBuf = new ByteArrayOutputStream();
        int b;
        while ((b = bin.read()) != -1) {
            headerBuf.write(b);
            if (b == '\n') break;
            if (headerBuf.size() >= maxBytes) break;
        }
        if (headerBuf.size() == 0) return null;
        String line = new String(headerBuf.toByteArray(), StandardCharsets.UTF_8).trim();
        if (line.isBlank()) return null;
        return line;
    }

    public void stop() {
        running = false;
        for (CopyOnWriteArrayList<Socket> list : viewersPorCamara.values()) {
            for (Socket s : list) {
                try { s.close(); } catch (Exception ignored) {}
            }
            list.clear();
        }
        viewersPorCamara.clear();
        System.out.println("Servidor TCP detenido");
    }
}
