package org.example.proyecto_ta.Services;

import org.example.proyecto_ta.model.Camara;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class VideoStorageService {

    private final Path root;
    private final ConcurrentHashMap<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public VideoStorageService(@Value("${videos.root:VIDEOS}") String rootDir) throws Exception {
        this.root = Path.of(rootDir).toAbsolutePath();
        Files.createDirectories(this.root);
    }

    public Path saveMp4Segment(Camara camara, String cameraId, byte[] mp4Bytes) throws Exception {
        Long usuarioId = null;
        try {
            if (camara != null && camara.getUsuario() != null) {
                usuarioId = camara.getUsuario().getId();
            }
        } catch (Exception ignored) {}

        String userFolder = usuarioId != null ? ("usuario_" + usuarioId) : "usuario_unknown";
        String camFolder = cameraId != null ? ("camara_" + cameraId) : "camara_unknown";

        SessionInfo info = sessions.compute(cameraId, (k, v) -> {
            if (v == null) return new SessionInfo(startNewSessionDir(userFolder, camFolder), LocalDateTime.now());
            long gap = Duration.between(v.lastSegmentAt, LocalDateTime.now()).toSeconds();
            if (gap > 90) return new SessionInfo(startNewSessionDir(userFolder, camFolder), LocalDateTime.now());
            v.lastSegmentAt = LocalDateTime.now();
            return v;
        });

        int idx = info.counter.incrementAndGet();
        Path clip = info.sessionDir.resolve("clip_" + idx + ".mp4");
        Files.write(clip, mp4Bytes);
        return clip;
    }

    private Path startNewSessionDir(String userFolder, String camFolder) {
        try {
            String sessionName = "sesion_" + fmt.format(LocalDateTime.now());
            Path dir = root.resolve(userFolder).resolve(camFolder).resolve(sessionName);
            Files.createDirectories(dir);
            return dir;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class SessionInfo {
        private final Path sessionDir;
        private final AtomicInteger counter = new AtomicInteger(0);
        private volatile LocalDateTime lastSegmentAt;

        private SessionInfo(Path sessionDir, LocalDateTime lastSegmentAt) {
            this.sessionDir = sessionDir;
            this.lastSegmentAt = lastSegmentAt;
        }
    }
}
