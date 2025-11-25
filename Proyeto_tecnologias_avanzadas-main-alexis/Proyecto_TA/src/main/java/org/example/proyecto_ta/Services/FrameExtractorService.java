package org.example.proyecto_ta.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FrameExtractorService {

    private final String ffmpegBin;

    public FrameExtractorService(@Value("${ffmpeg.bin:ffmpeg}") String ffmpegBin) {
        this.ffmpegBin = ffmpegBin;
    }

    public Path extractFrame(Path mp4File, Double second, Path outDir) throws Exception {
        Files.createDirectories(outDir);

        double ts = (second == null || second < 0) ? 0.0 : second;
        String outName = "frame_" + UUID.randomUUID().toString().replace("-", "") + ".jpg";
        Path outFile = outDir.resolve(outName);

        ProcessBuilder pb = new ProcessBuilder(
                ffmpegBin,
                "-y",
                "-ss", String.valueOf(ts),
                "-i", mp4File.toAbsolutePath().toString(),
                "-frames:v", "1",
                "-q:v", "2",
                outFile.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);

        Process p = pb.start();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            while (r.readLine() != null) {}
        }

        int code = p.waitFor();
        if (code != 0 || !Files.exists(outFile) || Files.size(outFile) == 0) {
            throw new RuntimeException("No se pudo extraer frame de " + mp4File);
        }
        return outFile;
    }
}
