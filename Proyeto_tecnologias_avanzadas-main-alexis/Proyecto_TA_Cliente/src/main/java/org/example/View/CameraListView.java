package org.example.View;

import org.example.Transmision.Camera;
import org.example.Transmision.CameraManager;
import org.example.View.Dialogos.DialogCaptureImage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CameraListView {

    private final JFrame frame;
    private final JPanel gridPanel;

    private static final int VIEW_PORT = 9001;

    private static final Color BG_MAIN = new Color(10, 15, 28);
    private static final Color BG_CARD = new Color(20, 28, 48);
    private static final Color PRIMARY = new Color(79, 140, 255);
    private static final Color PRIMARY_HOVER = new Color(50, 110, 240);
    private static final Color SUCCESS = new Color(0, 220, 0);
    private static final Color DANGER = new Color(248, 113, 113);
    private static final Color TEXT_PRIMARY = new Color(240, 245, 255);
    private static final Color TEXT_SECONDARY = new Color(150, 170, 210);
    private static final Color CARD_BG = new Color(30, 40, 65);
    private static final Color CARD_HOVER = new Color(40, 55, 90);

    public CameraListView() {
        frame = new JFrame("HawkEye - Monitoreo");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.getContentPane().setBackground(BG_MAIN);
        frame.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_CARD);
        top.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Centro de Monitoreo", JLabel.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        top.add(title, BorderLayout.WEST);

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        topButtons.setOpaque(false);

        JButton btnRegistrar = createPrimaryButton("Registrar Cámaras");
        btnRegistrar.addActionListener(e -> openRegistrarDialog());
        topButtons.add(btnRegistrar);

        JButton btnRefresh = createPrimaryButton("Refrescar");
        btnRefresh.addActionListener(e -> refreshGrid());
        topButtons.add(btnRefresh);

        top.add(topButtons, BorderLayout.EAST);
        frame.add(top, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(0, 3, 16, 16));
        gridPanel.setBackground(BG_MAIN);
        gridPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setBackground(BG_MAIN);
        frame.add(scroll, BorderLayout.CENTER);

        refreshGrid();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY);
            }
        });
        return btn;
    }

    private void openRegistrarDialog() {
        JDialog dialog = new JDialog(frame, "Registrar Cámaras", true);
        dialog.setSize(560, 460);
        dialog.setLocationRelativeTo(frame);
        dialog.getContentPane().setBackground(BG_MAIN);

        RegistrarCamaraPanel panel = new RegistrarCamaraPanel();
        dialog.add(panel, BorderLayout.CENTER);

        JButton cerrar = createPrimaryButton("Cerrar");
        cerrar.addActionListener(e -> dialog.dispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(BG_MAIN);
        south.setBorder(new EmptyBorder(16, 16, 16, 16));
        south.add(cerrar);
        dialog.add(south, BorderLayout.SOUTH);

        dialog.setVisible(true);
        refreshGrid();
    }

    private void refreshGrid() {
        gridPanel.removeAll();
        try {
            CameraManager.get().refreshFromApi();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        java.util.List<Camera> cams = CameraManager.get().list();

        if (cams.isEmpty()) {
            JLabel empty = new JLabel("No hay cámaras registradas", JLabel.CENTER);
            empty.setFont(new Font("Segoe UI", Font.BOLD, 22));
            empty.setForeground(TEXT_SECONDARY);
            gridPanel.setLayout(new GridBagLayout());
            gridPanel.add(empty);
        } else {
            gridPanel.setLayout(new GridLayout(0, 3, 16, 16));
            for (Camera cam : cams) {
                gridPanel.add(new CameraCard(cam));
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private class CameraCard extends JPanel {
        private final Camera cam;
        private final JLabel statusLabel;
        private final LivePreviewPanel previewPanel;

        CameraCard(Camera cam) {
            this.cam = cam;
            setLayout(new BorderLayout());
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 65, 100), 1),
                    new EmptyBorder(16, 16, 16, 16)
            ));

            JLabel name = new JLabel(cam.getName() + " (ID: " + cam.getId() + ")");
            name.setFont(new Font("Segoe UI", Font.BOLD, 17));
            name.setForeground(TEXT_PRIMARY);

            boolean inicial = CameraManager.get().isRunning(cam.getId());
            statusLabel = new JLabel(inicial ? "EN VIVO" : "APAGADA");
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            statusLabel.setForeground(inicial ? SUCCESS : DANGER);

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.add(name, BorderLayout.WEST);
            header.add(statusLabel, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            previewPanel = new LivePreviewPanel();
            add(previewPanel, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.setOpaque(false);

            JButton btnStart = createSmallButton("Iniciar Live", new Color(0, 160, 80));
            btnStart.addActionListener(e -> {
                if (CameraManager.get().startCamera(cam.getId())) {
                    String host = cam.getServerHost() != null ? cam.getServerHost() : "localhost";
                    previewPanel.start(host, VIEW_PORT, cam.getId());
                    updateStatus();
                } else {
                    JOptionPane.showMessageDialog(frame, "No se pudo iniciar la cámara", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            JButton btnStop = createSmallButton("Detener", new Color(180, 50, 50));
            btnStop.addActionListener(e -> {
                previewPanel.stop();
                CameraManager.get().stopCamera(cam.getId());
                updateStatus();
            });

            JButton btnCapture = createSmallButton("Capturar", new Color(90, 90, 200));
            btnCapture.addActionListener(e -> new DialogCaptureImage(frame, cam).setVisible(true));

            buttons.add(btnCapture);
            buttons.add(btnStart);
            buttons.add(btnStop);
            add(buttons, BorderLayout.SOUTH);

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    setBackground(CARD_HOVER);
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    setBackground(CARD_BG);
                }
            });
        }

        private JButton createSmallButton(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setForeground(Color.WHITE);
            b.setBackground(bg);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            return b;
        }

        private void updateStatus() {
            boolean running = CameraManager.get().isRunning(cam.getId());
            statusLabel.setText(running ? "EN VIVO" : "APAGADA");
            statusLabel.setForeground(running ? SUCCESS : DANGER);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CameraListView::new);
    }
}
