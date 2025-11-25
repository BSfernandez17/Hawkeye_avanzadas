package org.example.View;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class LivePreviewPanel extends JPanel {

    private volatile boolean running;
    private Thread worker;
    private BufferedImage currentFrame;

    public LivePreviewPanel() {
        setPreferredSize(new Dimension(480, 270));
        setBackground(new Color(15, 20, 35));
        setBorder(BorderFactory.createLineBorder(new Color(60, 75, 110), 2));
    }

    public void start(String host, int port, String cameraId) {
        stop();
        running = true;

        worker = new Thread(() -> {
            try (Socket socket = new Socket(host, port)) {
                socket.setSoTimeout(8000);
                String header = "VIEW:" + cameraId + "\n";
                socket.getOutputStream().write(header.getBytes(StandardCharsets.UTF_8));

                DataInputStream din = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                while (running) {
                    int len = din.readInt();
                    if (len <= 0 || len > 8_000_000) continue;

                    byte[] data = new byte[len];
                    din.readFully(data);

                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                    if (img != null) {
                        currentFrame = img;
                        repaint();
                    }
                }
            } catch (Exception e) {
                if (running) {
                    running = false;
                    repaint();
                }
            }
        }, "Live-" + cameraId);
        worker.setDaemon(true);
        worker.start();
    }

    public void stop() {
        running = false;
        if (worker != null) worker.interrupt();
        worker = null;
        currentFrame = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (currentFrame != null) {
            g.drawImage(currentFrame, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(100, 110, 130));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(new Color(180, 190, 210));
            g.setFont(new Font("Segoe UI", Font.BOLD, 18));
            String msg = running ? "Conectando..." : "Sin señal";
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(msg)) / 2;
            int y = getHeight() / 2;
            g.drawString(msg, x, y);
        }
    }
}