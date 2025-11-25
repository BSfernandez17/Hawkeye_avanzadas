package org.example;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TCPClient {

    private final String serverHost;
    private final int serverPort;

    public TCPClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void sendVideo(String cameraId, Path videoFile) throws Exception {
        if (cameraId == null || cameraId.isBlank()) {
            throw new IllegalArgumentException("cameraId requerido");
        }
        if (videoFile == null || !Files.exists(videoFile)) {
            throw new IllegalArgumentException("videoFile no existe");
        }

        try (Socket socket = new Socket(serverHost, serverPort)) {
            OutputStream out = socket.getOutputStream();

            String header = "CAMERA:" + cameraId + "|FILE:" + videoFile.getFileName().toString() + "\n";
            out.write(header.getBytes(StandardCharsets.UTF_8));
            out.flush();

            DataOutputStream dos = new DataOutputStream(out);

            long fileSize = Files.size(videoFile);
            dos.writeLong(fileSize);
            dos.flush();

            try (InputStream inputStream = Files.newInputStream(videoFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                dos.flush();
            }

            System.out.println("Video enviado: " + videoFile.getFileName() + " size=" + fileSize + " camera=" + cameraId);
        }
    }
}
