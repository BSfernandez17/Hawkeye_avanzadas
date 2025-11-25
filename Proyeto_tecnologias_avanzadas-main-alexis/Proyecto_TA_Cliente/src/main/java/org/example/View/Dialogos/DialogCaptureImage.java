package org.example.View.Dialogos;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import org.example.Utils.AppContext;
import org.example.Controllers.ImagenControlador;
import org.example.Controllers.ImagenControlador.CapturaMultipleRespuesta;
import org.example.Controllers.ImagenControlador.CapturaRespuesta;
import org.example.Transmision.Camera;

public class DialogCaptureImage extends JDialog {

    private static final Color BG_MAIN         = new Color(10, 15, 28);
    private static final Color BG_CARD         = new Color(20, 28, 48);
    private static final Color CARD_BG         = new Color(30, 40, 65);
    private static final Color PRIMARY         = new Color(79, 140, 255);
    private static final Color PRIMARY_HOVER  = new Color(50, 110, 240);
    private static final Color SUCCESS         = new Color(34, 197, 94);
    private static final Color SUCCESS_HOVER   = new Color(22, 160, 75);
    private static final Color WARNING         = new Color(251, 146, 60);
    private static final Color DANGER          = new Color(248, 113, 113);
    private static final Color TEXT_PRIMARY    = new Color(240, 245, 255);
    private static final Color TEXT_SECONDARY = new Color(150, 170, 210);
    private static final Color INPUT_BG        = new Color(30, 40, 65);
    private static final Color INPUT_BORDER    = new Color(60, 75, 110);
    private static final Color INPUT_FOCUS     = new Color(100, 180, 255);

    private final Camera cam;
    private final ImagenControlador imagenControlador;
    private final LiveStreamPanel livePanel;
    private final JLabel lblPreview;

    private final JComboBox<String> comboFiltro;
    private final JSpinner spScale;
    private final JSpinner spBright;
    private final JSpinner spRot;

    private final JButton btnFreeze;
    private final JButton btnApply;
    private final JButton btnSave;
    private final JButton btnResume;
    private final JButton btnClose;

    private volatile BufferedImage frozenOriginal;
    private volatile byte[] frozenOriginalBytes;
    private volatile BufferedImage frozenFiltered;
    private final AtomicBoolean frozen = new AtomicBoolean(false);

    // Agrega este campo para almacenar el token JWT
    private String jwtToken;

    public DialogCaptureImage(Frame owner, Camera cam) {
        super(owner, "Capturar Imagen - " + cam.getName(), true);
        this.cam = cam;
        this.imagenControlador = new ImagenControlador();

        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 600));
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(BG_CARD);
        left.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 0, 10, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        JLabel title = new JLabel("Captura y Edición LIVE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        left.add(title, c);

        addControl(left, c, "Filtro", comboFiltro = createCombo());
        addControl(left, c, "Escala (%)", spScale = createSpinner(100, 10, 300, 5));
        addControl(left, c, "Brillo", spBright = createSpinner(0.0, -2.0, 2.0, 0.1));
        addControl(left, c, "Rotación (°)", spRot = createSpinner(0, -360, 360, 5));

        c.gridwidth = 1;
        c.insets = new Insets(16, 0, 8, 0);

        btnFreeze = createActionButton("Capturar ahora", PRIMARY);
        btnApply = createActionButton("Aplicar filtro", SUCCESS);
        btnSave = createActionButton("Guardar en servidor", WARNING);
        btnResume = createActionButton("Reanudar LIVE", new Color(100, 110, 130));
        btnClose = createActionButton("Cerrar", new Color(156, 163, 175));

        c.gridy++; left.add(btnFreeze, c);
        c.gridy++; left.add(btnApply, c);
        c.gridy++; left.add(btnSave, c);
        c.gridy++; left.add(btnResume, c);
        c.gridy++; c.insets = new Insets(24, 0, 0, 0);
        left.add(btnClose, c);

        add(left, BorderLayout.WEST);

        JPanel center = new JPanel(new CardLayout(0, 0));
        center.setBackground(Color.BLACK);
        center.setBorder(BorderFactory.createLineBorder(new Color(60, 75, 110), 2));

        livePanel = new LiveStreamPanel();
        lblPreview = new JLabel();
        lblPreview.setHorizontalAlignment(JLabel.CENTER);
        lblPreview.setBackground(Color.BLACK);
        lblPreview.setOpaque(true);

        center.add(livePanel, "LIVE");
        center.add(new JScrollPane(lblPreview), "PREVIEW");

        add(center, BorderLayout.CENTER);

        btnFreeze.addActionListener(e -> onFreeze());
        btnApply.addActionListener(e -> onApply());
        btnSave.addActionListener(e -> onSave());
        btnResume.addActionListener(e -> onResume());
        btnClose.addActionListener(e -> dispose());

        setLocationRelativeTo(owner);
        updateButtons();
        livePanel.start();
    }

    private JComboBox<String> createCombo() {
        JComboBox<String> combo = new JComboBox<>(new String[]{"NONE", "GRISES", "REDUCIR", "BRILLO", "ROTAR", "TODOS"});
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        combo.setBackground(INPUT_BG);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1));
        return combo;
    }

    private JSpinner createSpinner(double value, double min, double max, double step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(INPUT_BG);
            tf.setForeground(TEXT_PRIMARY);
            tf.setCaretColor(TEXT_PRIMARY);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(INPUT_BORDER, 1),
                    new EmptyBorder(8, 10, 8, 10)
            ));
        }
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        return spinner;
    }

    private void addControl(JPanel panel, GridBagConstraints c, String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setForeground(TEXT_SECONDARY);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        c.gridx = 0; c.gridy++;
        c.weightx = 0.0; c.insets = new Insets(10, 0, 4, 0);
        panel.add(l, c);

        c.gridy++;
        c.weightx = 1.0; c.insets = new Insets(0, 0, 10, 0);
        panel.add(field, c);
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(240, 48));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(darken(bg, 0.85f));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private Color darken(Color c, float factor) {
        return new Color(
                Math.max((int)(c.getRed()   * factor), 0),
                Math.max((int)(c.getGreen() * factor), 0),
                Math.max((int)(c.getBlue()  * factor), 0),
                c.getAlpha()
        );
    }

    private void onFreeze() {
        BufferedImage frame = livePanel.getLastFrame();
        if (frame == null) {
            JOptionPane.showMessageDialog(this, "No hay imagen disponible aún.", "Sin frame", JOptionPane.WARNING_MESSAGE);
            return;
        }
        frozenOriginal = deepCopy(frame);
        frozenFiltered = deepCopy(frame);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(frozenOriginal, "jpg", baos);
            frozenOriginalBytes = baos.toByteArray();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al procesar imagen.");
            return;
        }
        frozen.set(true);
        showPreview(frozenFiltered);
        updateButtons();
    }

    private void onApply() {
        if (!frozen.get()) return;
        String filtro = (String) comboFiltro.getSelectedItem();
        int scale = ((Number) spScale.getValue()).intValue();
        float bright = ((Number) spBright.getValue()).floatValue();
        int rot = ((Number) spRot.getValue()).intValue();

        BufferedImage img = deepCopy(frozenOriginal);
        if (!"TODOS".equals(filtro)) {
            img = aplicarFiltrosLocal(img, filtro, scale, bright, rot);
        }
        frozenFiltered = img;
        showPreview(img);
    }

    // Método para establecer el token desde fuera
    public void setJwtToken(String token) {
        this.jwtToken = token;
    }

    private void uploadImageToServer(byte[] imageBytes, String cameraId, String filtro, int escala, float brillo, int rotacion) {
        try {
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            URL url = new URL("http://localhost:8080/api/imagenes/upload");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            String token = AppContext.getJwtToken();
            if (token != null && !token.isBlank()) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                // file part
                out.writeBytes("--" + boundary + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"captura.jpg\"\r\n");
                out.writeBytes("Content-Type: image/jpeg\r\n\r\n");
                out.write(imageBytes);
                out.writeBytes("\r\n");
                // cameraId
                out.writeBytes("--" + boundary + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"cameraId\"\r\n\r\n");
                out.writeBytes(cameraId + "\r\n");
                // filtro
                out.writeBytes("--" + boundary + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"filtro\"\r\n\r\n");
                out.writeBytes(filtro + "\r\n");
                // escala
                out.writeBytes("--" + boundary + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"escala\"\r\n\r\n");
                out.writeBytes(String.valueOf(escala) + "\r\n");
                // brillo
                out.writeBytes("--" + boundary + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"brillo\"\r\n\r\n");
                out.writeBytes(String.valueOf(brillo) + "\r\n");
                // rotacion
                out.writeBytes("--" + boundary + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"rotacion\"\r\n\r\n");
                out.writeBytes(String.valueOf(rotacion) + "\r\n");
                // end
                out.writeBytes("--" + boundary + "--\r\n");
                out.flush();
            }
            int responseCode = conn.getResponseCode();
            InputStream is = responseCode == 200 ? conn.getInputStream() : conn.getErrorStream();
            StringBuilder response = new StringBuilder();
            int ch;
            while ((ch = is.read()) != -1) response.append((char) ch);
            is.close();
            if (responseCode == 200) {
                JOptionPane.showMessageDialog(this, response.toString(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, response.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al subir imagen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        if (!frozen.get()) return;
        String filtro = (String) comboFiltro.getSelectedItem();
        int scale = ((Number) spScale.getValue()).intValue();
        float bright = ((Number) spBright.getValue()).floatValue();
        int rot = ((Number) spRot.getValue()).intValue();
        uploadImageToServer(frozenOriginalBytes, cam.getId(), filtro, scale, bright, rot);
    }

    private void onResume() {
        frozen.set(false);
        frozenOriginal = frozenFiltered = null;
        frozenOriginalBytes = null;
        showLive();
        updateButtons();
    }

    private void updateButtons() {
        boolean f = frozen.get();
        btnFreeze.setEnabled(!f);
        btnApply.setEnabled(f);
        btnSave.setEnabled(f);
        btnResume.setEnabled(f);
    }

    private void showPreview(BufferedImage img) {
        if (img != null) {
            ImageIcon icon = new ImageIcon(img);
            lblPreview.setIcon(icon);
        }
        CardLayout cl = (CardLayout) ((JPanel) getContentPane().getComponent(1)).getLayout();
        cl.show((JPanel) getContentPane().getComponent(1), "PREVIEW");
    }

    private void showLive() {
        CardLayout cl = (CardLayout) ((JPanel) getContentPane().getComponent(1)).getLayout();
        cl.show((JPanel) getContentPane().getComponent(1), "LIVE");
    }

    private BufferedImage aplicarFiltrosLocal(BufferedImage src, String filtro, Integer escala, Float brillo, Integer rotacion) {
        BufferedImage out = src;
        if (filtro != null && !"NONE".equals(filtro)) {
            switch (filtro) {
                case "GRISES" -> out = aGrises(out);
                case "REDUCIR" -> out = reducir(out, escala != null ? escala : 50);
                case "BRILLO" -> out = brillo(out, brillo != null ? brillo : 1.5f);
                case "ROTAR" -> out = rotar(out, rotacion != null ? rotacion : 90);
            }
        }
        if (escala != null && escala != 100) out = reducir(out, escala);
        if (brillo != null && brillo != 0.0f) out = brillo(out, 1.0f + brillo);
        if (rotacion != null && rotacion != 0) out = rotar(out, rotacion);
        return out;
    }

    private BufferedImage aGrises(BufferedImage src) {
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return gray;
    }

    private BufferedImage reducir(BufferedImage src, int porcentaje) {
        if (porcentaje <= 0 || porcentaje > 300) porcentaje = 100;
        int w = src.getWidth() * porcentaje / 100;
        int h = src.getHeight() * porcentaje / 100;
        Image scaled = src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        BufferedImage out = new BufferedImage(w, h, src.getType());
        Graphics2D g = out.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        return out;
    }

    private BufferedImage brillo(BufferedImage src, float factor) {
        RescaleOp op = new RescaleOp(factor, 0, null);
        return op.filter(src, null);
    }

    private BufferedImage rotar(BufferedImage src, int grados) {
        double rad = Math.toRadians(grados);
        double sin = Math.abs(Math.sin(rad));
        double cos = Math.abs(Math.cos(rad));
        int w = src.getWidth(), h = src.getHeight();
        int nw = (int)(w * cos + h * sin);
        int nh = (int)(h * cos + w * sin);

        BufferedImage rot = new BufferedImage(nw, nh, src.getType());
        Graphics2D g = rot.createGraphics();
        g.translate(nw / 2.0, nh / 2.0);
        g.rotate(rad);
        g.translate(-w / 2.0, -h / 2.0);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return rot;
    }

    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean alphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, alphaPremultiplied, null);
    }

    private class LiveStreamPanel extends JPanel {
        private volatile BufferedImage lastFrame;
        private volatile boolean running;
        private Thread readerThread;

        LiveStreamPanel() {
            setBackground(Color.BLACK);
            setBorder(BorderFactory.createLineBorder(new Color(60, 75, 110), 2));
        }

        void start() {
            running = true;
            readerThread = new Thread(() -> {
                String host = cam.getServerHost() != null && !cam.getServerHost().isBlank() ? cam.getServerHost() : "localhost";
                try (Socket s = new Socket(host, 9001)) {
                    s.setTcpNoDelay(true);
                    s.getOutputStream().write(("VIEW:" + cam.getId() + "\n").getBytes(StandardCharsets.UTF_8));
                    DataInputStream din = new DataInputStream(new BufferedInputStream(s.getInputStream()));

                    while (running) {
                        int len = din.readInt();
                        if (len <= 0 || len > 8_000_000) continue;
                        byte[] data = new byte[len];
                        din.readFully(data);
                        BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                        if (img != null) {
                            lastFrame = img;
                            if (!frozen.get()) repaint();
                        }
                    }
                } catch (Exception ignored) {}
                running = false;
            }, "LiveStream-Capture-" + cam.getId());
            readerThread.setDaemon(true);
            readerThread.start();
        }

        BufferedImage getLastFrame() { return lastFrame; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (lastFrame == null) {
                g.setColor(new Color(80, 90, 110));
                g.setFont(new Font("Segoe UI", Font.BOLD, 22));
                String msg = "Esperando señal LIVE...";
                FontMetrics fm = g.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(msg)) / 2;
                int y = getHeight() / 2;
                g.drawString(msg, x, y);
                return;
            }

            int pw = getWidth(), ph = getHeight();
            int iw = lastFrame.getWidth(), ih = lastFrame.getHeight();
            double scale = Math.min((double) pw / iw, (double) ph / ih);
            int dw = (int)(iw * scale), dh = (int)(ih * scale);
            int x = (pw - dw) / 2, y = (ph - dh) / 2;

            g.drawImage(lastFrame, x, y, dw, dh, this);
        }
    }
}
