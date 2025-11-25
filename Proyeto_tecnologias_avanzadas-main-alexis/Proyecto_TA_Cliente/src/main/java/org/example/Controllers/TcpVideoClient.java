package org.example.Controllers;

import static org.example.Configuracion.Configuracion.ipServidor;

import org.example.Utils.VideoMeta;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TcpVideoClient {

    private static final int MAX_HEADER_BYTES = 2048;

    private final String host;
    private final int port;

    public TcpVideoClient() {
        this(ipServidor, 9003);
    }

    public TcpVideoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public List<VideoMeta> listarPorCamara(String cameraId) throws Exception {
        try (Socket socket = new Socket(host, port)) {
            socket.setTcpNoDelay(true);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            String header = "LIST_VIDEOS:" + cameraId;
            byte[] h = header.getBytes(StandardCharsets.UTF_8);
            if (h.length > MAX_HEADER_BYTES) throw new IllegalArgumentException("Header demasiado grande");

            out.writeInt(h.length);
            out.write(h);
            out.flush();

            int count = in.readInt();
            List<VideoMeta> metas = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                long id = in.readLong();
                String titulo = in.readUTF();
                String tipoMime = in.readUTF();
                String nombreArchivo = in.readUTF();
                metas.add(new VideoMeta(id, titulo, tipoMime, nombreArchivo));
            }
            return metas;
        }
    }
}
