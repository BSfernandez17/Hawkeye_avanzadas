package org.example.Controllers;

import static org.example.Configuracion.Configuracion.*;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class ImagenControlador {

    private static final int API_PORT = 9003;
    private static final int MAX_HEADER_BYTES = 2048;

    public CapturaRespuesta capturarDesdeVideoTcp(
            Long videoId,
            Double segundo,
            String filtro,
            Integer porcentajeEscala,
            Float brillo,
            Integer rotacion
    ) throws Exception {

        String header =
                "CAPTURE:" + videoId +
                        "|SEC:" + (segundo != null ? segundo : 0.0) +
                        "|FILTER:" + (filtro != null ? filtro : "NONE") +
                        "|SCALE:" + (porcentajeEscala != null ? porcentajeEscala : 100) +
                        "|BRIGHT:" + (brillo != null ? brillo : 0.0f) +
                        "|ROT:" + (rotacion != null ? rotacion : 0) +
                        "\n";

        byte[] hb = header.getBytes(StandardCharsets.UTF_8);
        if (hb.length > MAX_HEADER_BYTES) throw new IllegalArgumentException("Header demasiado grande");

        try (Socket socket = new Socket(ipServidor, API_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {

            socket.setTcpNoDelay(true);

            out.writeInt(hb.length);
            out.write(hb);
            out.flush();

            String status = in.readUTF();
            if (status.startsWith("REJECTED:")) {
                throw new IllegalStateException(status.substring("REJECTED:".length()).trim());
            }
            if (!status.startsWith("OK:")) {
                throw new IllegalStateException("Respuesta servidor: " + status);
            }

            long imgId = Long.parseLong(status.substring(3).trim());

            int len = in.readInt();
            if (len <= 0) throw new RuntimeException("Servidor devolvió imagen vacía");

            byte[] jpg = new byte[len];
            in.readFully(jpg);

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(jpg));
            if (img == null) throw new RuntimeException("No se pudo decodificar JPG");

            return new CapturaRespuesta(imgId, jpg, img);
        }
    }

    public CapturaMultipleRespuesta capturarTodosFiltrosDesdeVideoTcp(
            Long videoId,
            Double segundo,
            Integer porcentajeEscala,
            Float brillo
    ) throws Exception {

        String header =
                "CAPTURE_ALL:" + videoId +
                        "|SEC:" + (segundo != null ? segundo : 0.0) +
                        "|SCALE:" + (porcentajeEscala != null ? porcentajeEscala : 50) +
                        "|BRIGHT:" + (brillo != null ? brillo : 0.3f) +
                        "\n";

        byte[] hb = header.getBytes(StandardCharsets.UTF_8);
        if (hb.length > MAX_HEADER_BYTES) throw new IllegalArgumentException("Header demasiado grande");

        try (Socket socket = new Socket(ipServidor, API_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {

            socket.setTcpNoDelay(true);

            out.writeInt(hb.length);
            out.write(hb);
            out.flush();

            String status = in.readUTF();
            if (status.startsWith("REJECTED:")) {
                throw new IllegalStateException(status.substring("REJECTED:".length()).trim());
            }
            if (!status.startsWith("OK_ALL:")) {
                throw new IllegalStateException("Respuesta servidor: " + status);
            }

            int count = Integer.parseInt(status.substring("OK_ALL:".length()).trim());
            List<Long> ids = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                ids.add(in.readLong());
            }

            return new CapturaMultipleRespuesta(ids);
        }
    }

    public CapturaRespuesta guardarImagenLiveTcp(
            String cameraId,
            byte[] jpgOriginal,
            String filtro,
            Integer porcentajeEscala,
            Float brillo,
            Integer rotacion
    ) throws Exception {

        String header =
                "SAVE_IMAGE:" +
                        "CAMERA:" + cameraId +
                        "|FILTER:" + (filtro != null ? filtro : "NONE") +
                        "|SCALE:" + (porcentajeEscala != null ? porcentajeEscala : 100) +
                        "|BRIGHT:" + (brillo != null ? brillo : 0.0f) +
                        "|ROT:" + (rotacion != null ? rotacion : 0) +
                        "\n";

        byte[] hb = header.getBytes(StandardCharsets.UTF_8);
        if (hb.length > MAX_HEADER_BYTES) throw new IllegalArgumentException("Header demasiado grande");

        try (Socket socket = new Socket(ipServidor, API_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {

            socket.setTcpNoDelay(true);

            out.writeInt(hb.length);
            out.write(hb);
            out.writeInt(jpgOriginal.length);
            out.write(jpgOriginal);
            out.flush();

            String status = in.readUTF();
            if (status.startsWith("REJECTED:")) {
                throw new IllegalStateException(status.substring("REJECTED:".length()).trim());
            }
            if (!status.startsWith("OK:")) {
                throw new IllegalStateException("Respuesta servidor: " + status);
            }

            long imgId = Long.parseLong(status.substring(3).trim());

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(jpgOriginal));
            if (img == null) img = null;

            return new CapturaRespuesta(imgId, jpgOriginal, img);
        }
    }

    public CapturaMultipleRespuesta guardarTodosFiltrosLiveTcp(
            String cameraId,
            byte[] jpgOriginal,
            Integer porcentajeEscala,
            Float brillo
    ) throws Exception {

        String header =
                "SAVE_ALL_FILTERS:" +
                        "CAMERA:" + cameraId +
                        "|SCALE:" + (porcentajeEscala != null ? porcentajeEscala : 50) +
                        "|BRIGHT:" + (brillo != null ? brillo : 0.3f) +
                        "\n";

        byte[] hb = header.getBytes(StandardCharsets.UTF_8);
        if (hb.length > MAX_HEADER_BYTES) throw new IllegalArgumentException("Header demasiado grande");

        try (Socket socket = new Socket(ipServidor, API_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {

            socket.setTcpNoDelay(true);

            out.writeInt(hb.length);
            out.write(hb);
            out.writeInt(jpgOriginal.length);
            out.write(jpgOriginal);
            out.flush();

            String status = in.readUTF();
            if (status.startsWith("REJECTED:")) {
                throw new IllegalStateException(status.substring("REJECTED:".length()).trim());
            }
            if (!status.startsWith("OK_ALL:")) {
                throw new IllegalStateException("Respuesta servidor: " + status);
            }

            int count = Integer.parseInt(status.substring("OK_ALL:".length()).trim());
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                ids.add(in.readLong());
            }

            return new CapturaMultipleRespuesta(ids);
        }
    }

    public static class CapturaRespuesta {
        private final long imagenId;
        private final byte[] bytesJpg;
        private final BufferedImage imagen;

        public CapturaRespuesta(long imagenId, byte[] bytesJpg, BufferedImage imagen) {
            this.imagenId = imagenId;
            this.bytesJpg = bytesJpg;
            this.imagen = imagen;
        }

        public long getImagenId() {
            return imagenId;
        }

        public byte[] getBytesJpg() {
            return bytesJpg;
        }

        public BufferedImage getImagen() {
            return imagen;
        }
    }

    public static class CapturaMultipleRespuesta {
        private final List<Long> imagenesIds;

        public CapturaMultipleRespuesta(List<Long> imagenesIds) {
            this.imagenesIds = imagenesIds;
        }

        public List<Long> getImagenesIds() {
            return imagenesIds;
        }
    }
}
