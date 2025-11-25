package org.example.View;

import org.example.ConexionApi.CamaraApi;
import org.example.Model.Camara;
import org.example.Model.Usuario;
import org.example.Services.CamaraServicio;
import org.example.Transmision.Camera;
import org.example.Transmision.CameraDetector;
import org.example.Transmision.CameraManager;
import org.example.Utils.AppContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RegistrarCamaraPanel extends JPanel {

    // === COLORES UNIFICADOS (igual que LoginView y CameraListView) ===
    private static final Color BG_MAIN         = new Color(10, 15, 28);
    private static final Color BG_CARD         = new Color(20, 28, 48);
    private static final Color CARD_BG         = new Color(30, 40, 65);
    private static final Color PRIMARY         = new Color(79, 140, 255);
    private static final Color PRIMARY_HOVER  = new Color(50, 110, 240);
    private static final Color SUCCESS         = new Color(34, 197, 94);
    private static final Color TEXT_PRIMARY    = new Color(240, 245, 255);
    private static final Color TEXT_SECONDARY = new Color(150, 170, 210);
    private static final Color INPUT_BG        = new Color(30, 40, 65);
    private static final Color INPUT_BORDER    = new Color(60, 75, 110);
    private static final Color INPUT_FOCUS     = new Color(100, 180, 255);

    private final DefaultListModel<CameraDetector.DetectedCamera> model = new DefaultListModel<>();
    private final JList<CameraDetector.DetectedCamera> list = new JList<>(model);
    //private final JList<CameraDetector.detectAll> list = new JList<>(model)
    private final JTextField txtServer = new JTextField("localhost:9000");
    private final JTextField txtNameOverride = new JTextField();

    private final JTextField txtManualId = new JTextField();
    private final JTextField txtManualNombre = new JTextField();
    private final JTextField txtManualHost = new JTextField("localhost");
    private final JSpinner spManualPort = new JSpinner(new SpinnerNumberModel(9000, 1, 65535, 1));

    private final JButton btnRefrescar = createPrimaryButton("Refrescar Cámaras PC");
    private final JButton btnRegistrarSeleccion = createPrimaryButton("Registrar Seleccionadas");
    private final JButton btnRegistrarTodas = createPrimaryButton("Registrar Todas");
    private final JButton btnRegistrarManual = createPrimaryButton("Registrar Manual");

    private final Runnable onRegistered;

    public RegistrarCamaraPanel() {
        this(null);
    }

    public RegistrarCamaraPanel(Runnable onRegistered) {
        this.onRegistered = onRegistered;
        initUI();
        onRefrescar(null);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Registro de Cámaras", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG_CARD);
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 15));

        tabs.addTab("Detectadas en PC", buildDetectadasPanel());
        tabs.addTab("Registro Manual", buildManualPanel());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildDetectadasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 0, 8, 0);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Lista
        JLabel lDetect = new JLabel("Cámaras detectadas en esta PC:");
        lDetect.setForeground(TEXT_SECONDARY);
        lDetect.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setBackground(CARD_BG);
        list.setForeground(TEXT_PRIMARY);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        list.setCellRenderer(new DetectedCameraRenderer());

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1));

        // Campos
        JLabel lName = new JLabel("Nombre personalizado (solo si seleccionas 1):");
        lName.setForeground(TEXT_SECONDARY);
        styleInput(txtNameOverride);

        JLabel lServer = new JLabel("Servidor Ingest (host:puerto):");
        lServer.setForeground(TEXT_SECONDARY);
        styleInput(txtServer);

        // Layout
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        form.add(lDetect, c);

        c.gridy++; c.weighty = 1.0; c.fill = GridBagConstraints.BOTH;
        form.add(scroll, c);

        c.gridy++; c.weighty = 0; c.gridwidth = 1;
        form.add(lName, c);
        c.gridx = 1;
        form.add(txtNameOverride, c);

        c.gridx = 0; c.gridy++;
        form.add(lServer, c);
        c.gridx = 1;
        form.add(txtServer, c);

        // Botones
        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnRefrescar);
        btnPanel.add(btnRegistrarSeleccion);
        btnPanel.add(btnRegistrarTodas);

        c.gridx = 0; c.gridy++; c.gridwidth = 2; c.insets = new Insets(20, 0, 0, 0);
        form.add(btnPanel, c);

        panel.add(form, BorderLayout.CENTER);

        btnRefrescar.addActionListener(this::onRefrescar);
        btnRegistrarSeleccion.addActionListener(this::onRegistrarSeleccionadas);
        btnRegistrarTodas.addActionListener(this::onRegistrarTodas);

        return panel;
    }

    private JPanel buildManualPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 0, 10, 0);
        c.fill = GridBagConstraints.HORIZONTAL;

        styleInput(txtManualId);
        styleInput(txtManualNombre);
        styleInput(txtManualHost);
        spManualPort.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        addField(form, c, "ID Cámara (ej: PC_CAM_0)", txtManualId, 0);
        addField(form, c, "Nombre de la cámara", txtManualNombre, 1);
        addField(form, c, "Server Host", txtManualHost, 2);
        addField(form, c, "Server Port", spManualPort, 3);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false);
        south.add(btnRegistrarManual);
        panel.add(form, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);

        btnRegistrarManual.addActionListener(this::onRegistrarManual);

        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints c, String label, JComponent field, int row) {
        JLabel l = new JLabel(label);
        l.setForeground(TEXT_SECONDARY);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        c.gridx = 0; c.gridy = row; c.weightx = 0.0;
        panel.add(l, c);
        c.gridx = 1; c.weightx = 1.0;
        panel.add(field, c);
    }

    private void styleInput(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_FOCUS, 2),
                        new EmptyBorder(9, 11, 9, 11)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_BORDER, 1),
                        new EmptyBorder(10, 12, 10, 12)
                ));
            }
        });
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
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(PRIMARY_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(PRIMARY); }
        });
        return btn;
    }

    private void onRefrescar(ActionEvent e) {
        model.clear();
        List<CameraDetector.DetectedCamera> detected = CameraDetector.detectAll();
        detected.forEach(model::addElement);

        if (model.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se detectaron cámaras en esta PC.", "Sin cámaras", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onRegistrarSeleccionadas(ActionEvent e) {
        List<CameraDetector.DetectedCamera> selected = list.getSelectedValuesList();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona al menos una cámara.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        registrarLista(selected, txtNameOverride.getText().trim());
    }

    private void onRegistrarTodas(ActionEvent e) {
        if (model.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay cámaras detectadas para registrar.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<CameraDetector.DetectedCamera> all = Collections.list(model.elements());
        registrarLista(all, txtNameOverride.getText().trim());
    }

    private void onRegistrarManual(ActionEvent e) {
        String id = txtManualId.getText().trim();
        String nombre = txtManualNombre.getText().trim();
        String host = txtManualHost.getText().trim();
        int port = (Integer) spManualPort.getValue();

        if (id.isBlank() || nombre.isBlank() || host.isBlank()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        registrarCamaraManual(id, nombre, host, port);
    }

    private void registrarLista(List<CameraDetector.DetectedCamera> cams, String nameOverride) {
        String token = AppContext.getInstance().getToken();
        Usuario usuario = AppContext.getInstance().getUsuario();
        if (token == null || usuario == null || usuario.getId() == null) {
            JOptionPane.showMessageDialog(this, "Sesión no válida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String server = txtServer.getText().trim();
        String[] parts = server.split(":");
        String host = parts[0].trim();
        int ingestPort = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 9000;
        int viewPort = ingestPort + 1;

        CamaraServicio servicio = new CamaraServicio(new CamaraApi(token));
        int registrados = 0;

        for (CameraDetector.DetectedCamera dc : cams) {
            try {
                Camara cam = servicio.obtenerCamaraPooled();
                cam.setId(dc.getId());
                cam.setNombre(cams.size() == 1 && !nameOverride.isEmpty() ? nameOverride : dc.getName());
                cam.setServerHost(host);
                cam.setServerPort(ingestPort);
                cam.setUsuarioId(usuario.getId());
                cam.setTipo("LOCAL");

                Integer idx = null;
                try { idx = Integer.parseInt(dc.getId().replace("PC_CAM_", "")); } catch (Exception ignored) {}
                cam.setIndiceLocal(idx);

                Camara saved = servicio.guardarCamara(cam);
                if (saved != null) {
                    Camera runtime = new Camera(saved.getId(), saved.getNombre(), saved.getServerHost(), viewPort);
                    CameraManager.get().register(runtime);
                    registrados++;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        JOptionPane.showMessageDialog(this,
                "Se registraron " + registrados + " de " + cams.size() + " cámaras.",
                "Registro completado", JOptionPane.INFORMATION_MESSAGE);

        if (onRegistered != null) onRegistered.run();
    }

    private void registrarCamaraManual(String id, String nombre, String host, int port) {
        String token = AppContext.getInstance().getToken();
        Usuario usuario = AppContext.getInstance().getUsuario();
        if (token == null || usuario == null || usuario.getId() == null) {
            JOptionPane.showMessageDialog(this, "Sesión no válida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CamaraServicio servicio = new CamaraServicio(new CamaraApi(token));
        try {
            Camara cam = servicio.obtenerCamaraPooled();
            cam.setId(id);
            cam.setNombre(nombre);
            cam.setServerHost(host);
            cam.setServerPort(port);
            cam.setUsuarioId(usuario.getId());
            cam.setTipo("LOCAL");

            Integer idx = null;
            try { idx = Integer.parseInt(id.replace("PC_CAM_", "")); } catch (Exception ignored) {}
            if (idx == null) {
                JOptionPane.showMessageDialog(this, "El ID debe tener formato PC_CAM_X", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            cam.setIndiceLocal(idx);

            Camara saved = servicio.guardarCamara(cam);
            if (saved != null) {
                int viewPort = port + 1;
                Camera runtime = new Camera(saved.getId(), saved.getNombre(), saved.getServerHost(), viewPort);
                CameraManager.get().register(runtime);

                JOptionPane.showMessageDialog(this, "Cámara registrada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                if (onRegistered != null) onRegistered.run();

                // Limpiar campos
                txtManualId.setText("");
                txtManualNombre.setText("");
                txtManualHost.setText("localhost");
                spManualPort.setValue(9000);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Renderer bonito para la lista
    private static class DetectedCameraRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof CameraDetector.DetectedCamera dc) {
                setText(dc.getName() + "  →  " + dc.getId());
            }
            setBorder(new EmptyBorder(8, 12, 8, 12));
            return this;
        }
    }
}