package org.example.View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import org.example.Controllers.UsuarioControlador;
import org.example.Controllers.ControladorAcciones;

public class InterfazInicio extends JPanel {

    private final UsuarioControlador usuarioControlador;
    private final ControladorAcciones acciones;

    public InterfazInicio(UsuarioControlador usuarioControlador, ControladorAcciones acciones) {
        this.usuarioControlador = usuarioControlador;
        this.acciones = acciones;

        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("HawkEye", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(new Color(173, 216, 230));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(0, 1, 12, 12));
        center.setBackground(new Color(45, 45, 45));
        center.setBorder(new EmptyBorder(30, 80, 30, 80));

        JButton btnCamaras = styledButton("Gestión de Cámaras");
        btnCamaras.addActionListener(e -> SwingUtilities.invokeLater(CameraListView::new));

        JButton btnPerfil = styledButton("Perfil");
        btnPerfil.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Funcionalidad de perfil pendiente",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        JButton btnSalir = styledButton("Cerrar Sesión");
        btnSalir.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "¿Deseas cerrar sesión?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION
            );
            if (r == JOptionPane.YES_OPTION) {
                try {
                    if (acciones != null) acciones.cerrarSesion();
                } catch (Exception ignored) {
                }
                Window w = SwingUtilities.getWindowAncestor(this);
                if (w != null) w.dispose();
                SwingUtilities.invokeLater(() -> {
                    JFrame f = new JFrame("HawkEye");
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.setSize(800, 600);
                    f.setLocationRelativeTo(null);
                    f.setVisible(true);
                });
            }
        });

        center.add(btnCamaras);
        center.add(btnPerfil);
        center.add(btnSalir);

        add(center, BorderLayout.CENTER);
    }

    private JButton styledButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 18));
        b.setBackground(new Color(100, 149, 237));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(12, 18, 12, 18));
        return b;
    }
}
