package org.example.View;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.ConexionApi.UsuarioApi;
import org.example.Model.Usuario;
import org.example.Utils.AppContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class LoginView {

    private final JFrame frame;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JButton registerButton;
    private final JToggleButton showPasswordToggle;
    private final JLabel statusLabel;
    private final JProgressBar progress;

    // ===== COLORES CORREGIDOS Y OPTIMIZADOS =====
    private final Color BG_MAIN           = new Color(10, 15, 28);
    private final Color BG_CARD           = new Color(20, 28, 48);
    private final Color PRIMARY           = new Color(79, 140, 255);
    private final Color PRIMARY_HOVER     = new Color(50, 110, 240);
    private final Color PRIMARY_DISABLED  = new Color(60, 70, 90);
    private final Color TEXT_PRIMARY      = new Color(240, 245, 255);
    private final Color TEXT_SECONDARY    = new Color(150, 170, 210);
    private final Color INPUT_BG          = new Color(30, 40, 65);
    private final Color INPUT_BORDER      = new Color(60, 75, 110);
    private final Color INPUT_FOCUS       = new Color(100, 180, 255);
    private final Color OUTLINE_HOVER     = new Color(35, 45, 75);
    private final Color DANGER            = new Color(248, 113, 113);

    public LoginView() {
        setLookAndFeel();

        frame = new JFrame("HawkEye - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(560, 420));
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(BG_MAIN);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(true);
        root.setBackground(BG_MAIN);
        root.setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel header = buildHeader();
        JPanel card = buildCardWrapper();
        JPanel form = buildForm();
        JPanel footer = buildFooter();

        card.add(form, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        frame.setContentPane(root);

        emailField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Iniciar sesión");
        registerButton = new JButton("Registrarse");
        showPasswordToggle = new JToggleButton("Mostrar");
        statusLabel = new JLabel(" ");
        progress = new JProgressBar();

        wireComponents(form, footer);
        wireActions();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
        } catch (Exception ignored) {}
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("HawkEye");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        title.setForeground(new Color(147, 197, 253));

        JLabel subtitle = new JLabel("Gestor inteligente de cámaras de seguridad");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(TEXT_SECONDARY);

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);
        return header;
    }

    private JPanel buildCardWrapper() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 50, 80), 1),
                new EmptyBorder(24, 24, 24, 24)
        ));
        return card;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        return form;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(new EmptyBorder(16, 0, 0, 0));
        return footer;
    }

    private void styleInput(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                new EmptyBorder(12, 14, 12, 14)
        ));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_FOCUS, 2),
                        new EmptyBorder(11, 13, 11, 13)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_BORDER, 1),
                        new EmptyBorder(12, 14, 12, 14)
                ));
            }
        });
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(PRIMARY_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(PRIMARY);
            }
        });
    }

    private void styleOutlineButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 2),
                new EmptyBorder(12, 20, 12, 20)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setContentAreaFilled(true);
                    btn.setBackground(OUTLINE_HOVER);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setContentAreaFilled(false);
                    btn.setBackground(null);
                }
            }
        });
    }

    private void wireComponents(JPanel form, JPanel footer) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 0, 8, 0);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailLabel.setForeground(TEXT_SECONDARY);

        JLabel passLabel = new JLabel("Contraseña");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passLabel.setForeground(TEXT_SECONDARY);

        styleInput(emailField);
        styleInput(passwordField);

        showPasswordToggle.setFocusPainted(false);
        showPasswordToggle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showPasswordToggle.setForeground(TEXT_PRIMARY);
        showPasswordToggle.setBackground(new Color(40, 50, 70));
        showPasswordToggle.setBorder(new EmptyBorder(8, 12, 8, 12));
        showPasswordToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        stylePrimaryButton(loginButton);
        styleOutlineButton(registerButton);

        // Email
        c.gridx = 0; c.gridy = 0; c.weightx = 1.0;
        form.add(emailLabel, c);
        c.gridy = 1;
        form.add(emailField, c);

        // Contraseña + toggle
        c.gridy = 2;
        form.add(passLabel, c);

        JPanel passRow = new JPanel(new BorderLayout(8, 0));
        passRow.setOpaque(false);
        passRow.add(passwordField, BorderLayout.CENTER);
        passRow.add(showPasswordToggle, BorderLayout.EAST);

        c.gridy = 3;
        form.add(passRow, c);

        // Botones
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        c.gridy = 4; c.insets = new Insets(20, 0, 0, 0);
        form.add(buttonPanel, c);

        // Footer
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        progress.setIndeterminate(true);
        progress.setVisible(false);
        progress.setForeground(PRIMARY);

        footer.add(progress);
        footer.add(Box.createVerticalStrut(8));
        footer.add(statusLabel);
    }

    private void wireActions() {
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateButtons(); }
            public void removeUpdate(DocumentEvent e) { updateButtons(); }
            public void changedUpdate(DocumentEvent e) { updateButtons(); }
        };
        emailField.getDocument().addDocumentListener(dl);
        passwordField.getDocument().addDocumentListener(dl);
        updateButtons();

        showPasswordToggle.addActionListener(e -> {
            if (showPasswordToggle.isSelected()) {
                passwordField.setEchoChar((char) 0);
                showPasswordToggle.setText("Ocultar");
            } else {
                passwordField.setEchoChar('•');
                showPasswordToggle.setText("Mostrar");
            }
        });

        loginButton.addActionListener(e -> doLogin());
        registerButton.addActionListener(e -> showRegisterDialog());

        frame.getRootPane().setDefaultButton(loginButton);
    }

    private void updateButtons() {
        boolean filled = !emailField.getText().trim().isEmpty() && passwordField.getPassword().length > 0;
        loginButton.setEnabled(filled);
        loginButton.setBackground(filled ? PRIMARY : PRIMARY_DISABLED);
    }

    private void setLoading(boolean loading, String message) {
        loginButton.setEnabled(!loading);
        registerButton.setEnabled(!loading);
        emailField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
        showPasswordToggle.setEnabled(!loading);
        progress.setVisible(loading);
        statusLabel.setText(message != null ? message : " ");
        statusLabel.setForeground(loading ? TEXT_SECONDARY : DANGER);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        setLoading(true, "Autenticando...");

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                try { TimeUnit.MILLISECONDS.sleep(150); } catch (Exception ignored) {}
                return login(email, password);
            }
            @Override protected void done() {
                try {
                    if (Boolean.TRUE.equals(get())) {
                        frame.dispose();
                        SwingUtilities.invokeLater(CameraListView::new);
                    } else {
                        setLoading(false, null);
                    }
                } catch (Exception ex) {
                    setLoading(false, "Error de red");
                }
            }
        }.execute();
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(frame, "Registrarse", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(frame);
        dialog.getContentPane().setBackground(BG_MAIN);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 0, 8, 0);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField();
        JTextField emailRegField = new JTextField();
        JPasswordField passRegField = new JPasswordField();

        styleInput(nameField);
        styleInput(emailRegField);
        styleInput(passRegField);

        c.gridx = 0; c.gridy = 0; c.weightx = 1.0;
        panel.add(new JLabel("Nombre"), c);
        c.gridy++; panel.add(nameField, c);

        c.gridy++;
        panel.add(new JLabel("Email"), c);
        c.gridy++; panel.add(emailRegField, c);

        c.gridy++;
        panel.add(new JLabel("Contraseña"), c);
        c.gridy++; panel.add(passRegField, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);

        JButton okBtn = new JButton("Registrar");
        JButton cancelBtn = new JButton("Cancelar");
        stylePrimaryButton(okBtn);
        styleOutlineButton(cancelBtn);

        buttons.add(cancelBtn);
        buttons.add(okBtn);

        c.gridy++; c.insets = new Insets(20, 0, 0, 0);
        panel.add(buttons, c);

        okBtn.addActionListener(e -> {
            String nombre = nameField.getText().trim();
            String email = emailRegField.getText().trim();
            String pass = new String(passRegField.getPassword());

            if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Completa todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            UsuarioApi api = new UsuarioApi();
            if (api.registrarUsuario(nombre, email, pass)) {
                JOptionPane.showMessageDialog(dialog, "¡Registro exitoso! Ya puedes iniciar sesión.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al registrar usuario", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private boolean login(String email, String password) {
        try {
            UsuarioApi api = new UsuarioApi();
            String token = api.autenticarUsuario(email, password);

            if (token == null || token.isBlank()) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Credenciales inválidas", "Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setForeground(DANGER);
                    statusLabel.setText("Usuario o contraseña incorrectos");
                });
                return false;
            }

            String[] parts = token.split("\\.");
            if (parts.length < 2) return false;

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(payload).getAsJsonObject();

            int id = json.has("id") ? json.get("id").getAsInt() : -1;
            boolean aprobado = json.has("status") && json.get("status").getAsBoolean();

            if (!aprobado) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Tu cuenta está pendiente de aprobación", "Acceso denegado", JOptionPane.WARNING_MESSAGE);
                    statusLabel.setForeground(DANGER);
                    statusLabel.setText("Pendiente de aprobación");
                });
                return false;
            }

            Usuario usuario = new Usuario(id, null, null, null, null, false, null);
            AppContext.getInstance().setUsuario(usuario);
            AppContext.setJwtToken(token);

            return true;

        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setForeground(DANGER);
                statusLabel.setText("Error de conexión: " + ex.getMessage());
                JOptionPane.showMessageDialog(frame, "Error de red: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            });
            ex.printStackTrace();
            return false;
        }
    }

    // Para probar rápidamente
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginView::new);
    }
}