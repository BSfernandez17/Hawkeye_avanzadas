package org.example.proyecto_ta.Services;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

@Service
public class ImageFilterService {

    public BufferedImage aplicarFiltros(BufferedImage src, String filtro, Integer porcentajeEscala, Float brillo, Integer rotacion) {
        BufferedImage out = src;

        String f = filtro != null ? filtro.trim().toUpperCase() : "NONE";

        if (f.equals("GRISES") || f.equals("GRAY") || f.equals("GREY") || f.equals("GRAYSCALE")) {
            out = aGrises(out);
        } else if (f.equals("REDUCIR") || f.equals("SCALE") || f.equals("RESIZE") || f.equals("REDUCE")) {
            out = reducir(out, porcentajeEscala != null ? porcentajeEscala : 50);
        } else if (f.equals("BRILLO") || f.equals("BRIGHT") || f.equals("BRIGHTNESS")) {
            out = brillo(out, normalizarBrillo(brillo));
        } else if (f.equals("ROTAR") || f.equals("ROTATE")) {
            out = rotar(out, rotacion != null ? rotacion : 90);
        }

        if (brillo != null && (f.equals("NONE") || !(f.equals("BRILLO") || f.equals("BRIGHT") || f.equals("BRIGHTNESS")))) {
            float factor = normalizarBrillo(brillo);
            if (factor != 1.0f) out = brillo(out, factor);
        }

        if (rotacion != null && rotacion != 0 && (f.equals("NONE") || !(f.equals("ROTAR") || f.equals("ROTATE")))) {
            out = rotar(out, rotacion);
        }

        if (porcentajeEscala != null && porcentajeEscala != 100 && (f.equals("NONE") || !(f.equals("REDUCIR") || f.equals("SCALE") || f.equals("RESIZE") || f.equals("REDUCE")))) {
            out = reducir(out, porcentajeEscala);
        }

        return out;
    }

    private float normalizarBrillo(Float brillo) {
        if (brillo == null) return 1.0f;
        float b = brillo;
        if (b >= -2.0f && b <= 2.0f) {
            float factor = 1.0f + b;
            if (factor < 0.1f) factor = 0.1f;
            return factor;
        }
        if (b < 0.1f) b = 0.1f;
        return b;
    }

    private BufferedImage aGrises(BufferedImage src) {
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return gray;
    }

    private BufferedImage reducir(BufferedImage src, int porcentaje) {
        if (porcentaje <= 0) porcentaje = 50;
        if (porcentaje > 100) porcentaje = 100;

        int w = Math.max(1, src.getWidth() * porcentaje / 100);
        int h = Math.max(1, src.getHeight() * porcentaje / 100);

        Image scaled = src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = out.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        return out;
    }

    private BufferedImage brillo(BufferedImage src, float factor) {
        RescaleOp op = new RescaleOp(factor, 0, null);
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        op.filter(src, out);
        return out;
    }

    private BufferedImage rotar(BufferedImage src, int grados) {
        int g = grados % 360;
        if (g < 0) g += 360;

        double rads = Math.toRadians(g);
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));
        int w = src.getWidth();
        int h = src.getHeight();
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newW, newH, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate(newW / 2.0, newH / 2.0);
        at.rotate(rads);
        at.translate(-w / 2.0, -h / 2.0);
        g2d.drawImage(src, at, null);
        g2d.dispose();
        return rotated;
    }
}
