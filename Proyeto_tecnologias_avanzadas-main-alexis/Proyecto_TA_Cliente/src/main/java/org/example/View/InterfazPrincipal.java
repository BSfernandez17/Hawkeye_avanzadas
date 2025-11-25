package org.example.View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import org.example.Controllers.ControladorAcciones;
import org.example.Model.GestorSesion;

public class InterfazPrincipal extends JPanel {

    private final ControladorAcciones acciones;
    private final GestorSesion sesion;

    public InterfazPrincipal(ControladorAcciones acciones, GestorSesion sesion) {
        this.acciones = acciones;
        this.sesion = sesion;

        setLayout(new BorderLayout());
        setBackground(new Color(35, 35, 35));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(35, 35, 35));
        top.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("HawkEye - Panel Principal");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(173, 216, 230));
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(new Color(35, 35, 35));

        JButton btnInicio = styledButtonSmall("Inicio");
        btnInicio.addActionListener(e -> mostrarInicio());

        JButton btnCamaras = styledButtonSmall("Cámaras");
        btnCamaras.addActionListener(e -> SwingUtilities.invokeLater(CameraListView::new));

        JButton btnSalir = styledButtonSmall("Cerrar Sesión");
        btnSalir.addActionListener(e -> {
            try {
                if (acciones != null) acciones.cerrarSesion();
            } catch (Exception ignored) {
            }
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
        });

        actions.add(btnInicio);
        actions.add(btnCamaras);
        actions.add(btnSalir);

        top.add(actions, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        mostrarInicio();
    }

    private void mostrarInicio() {
        removeCenterIfAny();
        add(new InterfazInicio(null, acciones), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void removeCenterIfAny() {
        BorderLayout bl = (BorderLayout) getLayout();
        Component c = bl.getLayoutComponent(BorderLayout.CENTER);
        if (c != null) remove(c);
    }

    private JButton styledButtonSmall(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setBackground(new Color(100, 149, 237));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 12, 6, 12));
        return b;
    }
}
