package org.example.Transmision;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.example.TCPClient;
import org.example.ConexionApi.CamaraApi;
import org.example.Model.Usuario;
import org.example.Services.CamaraServicio;
import org.example.Utils.AppContext;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class CameraManager {

    private static final CameraManager INSTANCE = new CameraManager();

    private static final int INGEST_PORT_DEFAULT = 9000;
    private static final int VIEW_PORT = 9001;
    private static final int LIVE_PORT = 9002;

    private final List<Camera> cameras = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Thread> pcRunning = new ConcurrentHashMap<>();
    private final Map<String, LiveFrameClient> liveClients = new ConcurrentHashMap<>();

    private String token;

    private CameraManager() {}

    public static CameraManager get() {
        return INSTANCE;
    }

    public void register(Camera cam) {
        if (cam == null) return;
        cameras.add(cam);
    }

    public List<Camera> list() {
        return new ArrayList<>(cameras);
    }

    public synchronized void refreshFromApi() {
        String tokenCtx = AppContext.getInstance().getToken();
        Usuario usuario = AppContext.getInstance().getUsuario();
        if (tokenCtx == null || usuario == null || usuario.getId() == null) {
            System.err.println("No hay token o usuario en contexto; no se puede obtener cámaras desde API.");
            return;
        }
        try {
            CamaraServicio servicio = new CamaraServicio(new CamaraApi(tokenCtx));
            List<org.example.Model.Camara> remote = servicio.obtenerCamarasPorUsuario(usuario.getId());
            cameras.clear();
            if (remote != null) {
                for (org.example.Model.Camara rc : remote) {
                    String host = rc.getServerHost();
                    if (host == null || host.isBlank()) host = "localhost";

                    int ingestPort = rc.getServerPort() != null ? rc.getServerPort() : INGEST_PORT_DEFAULT;

                    Camera runtime = new Camera(
                            rc.getId(),
                            rc.getNombre(),
                            host,
                            ingestPort
                    );
                    cameras.add(runtime);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener cámaras desde API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized boolean startCamera(String cameraId) {
        if (cameraId == null || cameraId.isBlank()) return false;
        if (pcRunning.containsKey(cameraId)) return false;

        Camera cam = cameras.stream().filter(c -> cameraId.equals(c.getId())).findFirst().orElse(null);
        if (cam == null) return false;

        return startPcCam(cam);
    }

    private boolean startPcCam(Camera cam) {
        String cameraId = cam.getId();
        String host = cam.getServerHost();
        int ingestPort = cam.getServerPort();

        int index;
        try {
            if (!cameraId.startsWith("PC_CAM_")) {
                System.err.println("Solo se soporta PC_CAM_ por ahora: " + cameraId);
                return false;
            }
            index = Integer.parseInt(cameraId.substring("PC_CAM_".length()));
        } catch (Exception e) {
            System.err.println("cameraId inválido para PC cam: " + cameraId);
            return false;
        }

        List<Webcam> all = Webcam.getWebcams();
        if (index < 0 || index >= all.size()) {
            System.err.println("No existe webcam en índice " + index);
            return false;
        }

        Webcam webcam = all.get(index);
        if (webcam.isOpen()) {
            webcam.close();
        }
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        if (!webcam.open()) {
            System.err.println("No se pudo abrir webcam: " + cam.getName());
            return false;
        }

        LiveFrameClient liveClient = new LiveFrameClient(host, LIVE_PORT, cameraId, new LiveFrameClient.LiveImageSource() {
            @Override
            public BufferedImage nextImage() {
                synchronized (webcam) {
                    return webcam.getImage();
                }
            }

            @Override
            public boolean isOpen() {
                return webcam.isOpen();
            }
        });

        Thread liveThread = new Thread(liveClient, "LiveJPEG-" + cameraId);
        liveThread.setDaemon(true);
        liveThread.start();
        liveClients.put(cameraId, liveClient);

        String ffmpegBin = resolveFfmpegPath();
        if (ffmpegBin == null) {
            System.err.println("FFmpeg no encontrado. Usa -Dffmpeg.path=C:\\ruta\\ffmpeg.exe");
            try { webcam.close(); } catch (Exception ignored) {}
            liveClient.stop();
            liveClients.remove(cameraId);
            return false;
        }

        Thread t = new Thread(() -> {
            try {
                int fps = 10;
                int segmentSeconds = 60;

                TCPClient tcpClient = new TCPClient(host, ingestPort);
                
                // Crear carpeta para segmentos temporales dentro del proyecto cliente
                Path segmentDir = java.nio.file.Paths.get("").toAbsolutePath().resolve("target").getParent().resolve("segmentos");
                java.nio.file.Files.createDirectories(segmentDir);

                while (!Thread.currentThread().isInterrupted() && webcam.isOpen()) {
                    String ts = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new java.util.Date());
                    Path segmentFile = segmentDir.resolve("pc_segment_" + cameraId + "_" + ts + ".h264");

                    ProcessBuilder pb = new ProcessBuilder(
                            ffmpegBin,
                            "-y",
                            "-f", "image2pipe",
                            "-vcodec", "mjpeg",
                            "-r", String.valueOf(fps),
                            "-i", "-",
                            "-c:v", "libx264",
                            "-preset", "ultrafast",
                            "-tune", "zerolatency",
                            "-pix_fmt", "yuv420p",
                            "-g", String.valueOf(fps * 2),
                            "-keyint_min", String.valueOf(fps * 2),
                            "-sc_threshold", "0",
                            "-x264-params", "repeat-headers=1",
                            "-t", String.valueOf(segmentSeconds),
                            "-f", "h264",
                            segmentFile.toAbsolutePath().toString()
                    );
                    pb.redirectErrorStream(true);
                    Process ff = pb.start();

                    try (OutputStream ffIn = new BufferedOutputStream(ff.getOutputStream())) {
                        long end = System.currentTimeMillis() + segmentSeconds * 1000L;
                        while (System.currentTimeMillis() < end && webcam.isOpen() && !Thread.currentThread().isInterrupted()) {
                            BufferedImage img;
                            synchronized (webcam) {
                                img = webcam.getImage();
                            }
                            if (img == null) continue;
                            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                            ImageIO.write(img, "jpg", baos);
                            byte[] jpeg = baos.toByteArray();
                            ffIn.write(jpeg);
                            ffIn.flush();
                            try {
                                Thread.sleep(Math.max(1, 1000L / fps));
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }

                    try {
                        ff.waitFor(10, TimeUnit.SECONDS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }

                    if (Thread.currentThread().isInterrupted()) {
                        try { Files.deleteIfExists(segmentFile); } catch (Exception ignored) {}
                        break;
                    }

                    if (Files.exists(segmentFile) && Files.size(segmentFile) > 0) {
                        try {
                            tcpClient.sendVideo(cameraId, segmentFile);
                        } catch (Exception ex) {
                            System.err.println("Fallo enviando segmento de " + cameraId + ": " + ex.getMessage());
                        }
                    }

                    try { Files.deleteIfExists(segmentFile); } catch (Exception ignored) {}
                }

            } catch (Exception ex) {
                if (!Thread.currentThread().isInterrupted()) {
                    System.err.println("Error en stream PC cam " + cam.getName() + ": " + ex.getMessage());
                    ex.printStackTrace();
                }
            } finally {
                try { webcam.close(); } catch (Exception ignored) {}
                LiveFrameClient lc = liveClients.remove(cameraId);
                if (lc != null) lc.stop();
                pcRunning.remove(cameraId);
            }
        }, "PCStream-" + cameraId);

        t.setDaemon(true);
        pcRunning.put(cameraId, t);
        t.start();
        System.out.println("Stream PC iniciado para cámara: " + cam.getName());
        return true;
    }

    public synchronized boolean stopCamera(String cameraId) {
        boolean stopped = false;

        Thread pcT = pcRunning.remove(cameraId);
        if (pcT != null) {
            pcT.interrupt();
            try { pcT.join(3000); } catch (InterruptedException ignored) {}
            stopped = true;
        }

        LiveFrameClient lc = liveClients.remove(cameraId);
        if (lc != null) {
            lc.stop();
            stopped = true;
        }

        return stopped;
    }

    public boolean isRunning(String cameraId) {
        return pcRunning.containsKey(cameraId);
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public int getViewPort() {
        return VIEW_PORT;
    }

    public int getLivePort() {
        return LIVE_PORT;
    }

    private String resolveFfmpegPath() {
        String prop = System.getProperty("ffmpeg.path");
        if (prop != null && Files.exists(Path.of(prop))) return prop;

        String env = System.getenv("FFMPEG_PATH");
        if (env != null && Files.exists(Path.of(env))) return env;

        List<String> candidates = List.of(
                "ffmpeg",
                "ffmpeg.exe",
                "C:\\ffmpeg\\bin\\ffmpeg.exe",
                "C:\\Program Files\\ffmpeg\\bin\\ffmpeg.exe",
                "C:\\ProgramData\\chocolatey\\bin\\ffmpeg.exe"
        );

        for (String c : candidates) {
            try {
                Process p = new ProcessBuilder(c, "-version").start();
                if (p.waitFor(2, TimeUnit.SECONDS) && p.exitValue() == 0) return c;
            } catch (Exception ignored) {}
        }
        return null;
    }
}
