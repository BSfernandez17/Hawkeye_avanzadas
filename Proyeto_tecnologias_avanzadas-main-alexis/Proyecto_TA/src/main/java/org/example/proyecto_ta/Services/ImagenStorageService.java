package org.example.proyecto_ta.Services;

import org.example.proyecto_ta.model.Camara;
import org.example.proyecto_ta.model.Video;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ImagenStorageService {

    private final Path root;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    public ImagenStorageService(@Value("${imagenes.root:IMAGENES}") String rootDir) throws Exception {
        this.root = Path.of(rootDir).toAbsolutePath();
        Files.createDirectories(this.root);
    }

    public Path saveCapture(Camara camara, Video video, String cameraId, byte[] jpgBytes) throws Exception {
        Long usuarioId = null;
        if (camara != null && camara.getUsuario() != null) {
            usuarioId = camara.getUsuario().getId();
        }

        String userFolder = usuarioId != null ? ("usuario_" + usuarioId) : "usuario_unknown";
        String camFolder = cameraId != null ? ("camara_" + cameraId) : "camara_unknown";
        String videoFolder = video != null && video.getId() != null ? ("video_" + video.getId()) : "video_unknown";
        String fileName = "captura_" + fmt.format(LocalDateTime.now()) + ".jpg";

        Path dir = root.resolve(userFolder).resolve(camFolder).resolve(videoFolder);
        Files.createDirectories(dir);

        Path outFile = dir.resolve(fileName);
        Files.write(outFile, jpgBytes);
        return outFile;
    }
}
