package org.example.Transmision;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class LiveFrameClient implements Runnable {

    private final String host;
    private final int livePort;
    private final String cameraId;
    private final LiveImageSource source;

    private volatile boolean running = true;
    private Socket socket;

    public interface LiveImageSource {
        BufferedImage nextImage();
        boolean isOpen();
    }

    public LiveFrameClient(String host, int livePort, String cameraId, LiveImageSource source) {
        this.host = host;
        this.livePort = livePort;
        this.cameraId = cameraId;
        this.source = source;
    }

    @Override
    public void run() {
        try (Socket s = new Socket(host, livePort)) {
            this.socket = s;
            OutputStream outRaw = s.getOutputStream();
            DataOutputStream out = new DataOutputStream(outRaw);

            String header = "LIVE:" + cameraId + "\n";
            outRaw.write(header.getBytes(StandardCharsets.UTF_8));
            outRaw.flush();

            while (running && source.isOpen()) {
                BufferedImage img = source.nextImage();
                if (img == null) continue;

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "jpg", baos);
                byte[] bytes = baos.toByteArray();

                out.writeInt(bytes.length);
                out.write(bytes);
                out.flush();

                Thread.sleep(80);
            }
        } catch (Exception ignored) {
        } finally {
            running = false;
            try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        }
    }

    public void stop() {
        running = false;
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }
}
