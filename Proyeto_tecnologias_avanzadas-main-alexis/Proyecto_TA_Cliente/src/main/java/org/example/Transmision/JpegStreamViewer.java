package org.example.Transmision;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class JpegStreamViewer {

    public static ViewerHandle start(String host, int viewPort, String cameraId, String title) throws Exception {
        Socket socket = new Socket(host, viewPort);
        OutputStream out = socket.getOutputStream();
        out.write(("VIEW:" + cameraId + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();

        JFrame frame = new JFrame(title);
        frame.setSize(720, 520);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(label, BorderLayout.CENTER);
        frame.setVisible(true);

        Thread t = new Thread(() -> {
            try (InputStream rawIn = socket.getInputStream();
                 BufferedInputStream bin = new BufferedInputStream(rawIn);
                 DataInputStream din = new DataInputStream(bin)) {

                while (!socket.isClosed()) {
                    int len;
                    try {
                        len = din.readInt();
                    } catch (Exception eof) {
                        break;
                    }
                    if (len <= 0 || len > 5_000_000) continue;

                    byte[] imgBytes = new byte[len];
                    din.readFully(imgBytes);

                    BufferedImage img = ImageIO.read(new java.io.ByteArrayInputStream(imgBytes));
                    if (img != null) {
                        ImageIcon icon = new ImageIcon(img);
                        SwingUtilities.invokeLater(() -> label.setIcon(icon));
                    }
                }
            } catch (Exception ignored) {
            } finally {
                try { socket.close(); } catch (Exception ignored2) {}
                SwingUtilities.invokeLater(frame::dispose);
            }
        }, "JpegViewer-" + cameraId);

        t.setDaemon(true);
        t.start();

        return new ViewerHandle(socket, frame, t);
    }

    public static class ViewerHandle {
        private final Socket socket;
        private final JFrame frame;
        private final Thread thread;

        private ViewerHandle(Socket socket, JFrame frame, Thread thread) {
            this.socket = socket;
            this.frame = frame;
            this.thread = thread;
        }

        public void stop() {
            try { socket.close(); } catch (Exception ignored) {}
            SwingUtilities.invokeLater(frame::dispose);
        }
    }
}
