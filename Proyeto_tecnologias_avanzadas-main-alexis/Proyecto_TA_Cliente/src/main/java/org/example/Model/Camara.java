package org.example.Model;

import org.example.Pool.IPoolableObject;

public class Camara implements IPoolableObject {

    private String id;
    private Usuario usuario;
    private String nombre;
    private String serverHost;
    private Integer serverPort;
    private String tipo;
    private Integer indiceLocal;
    private String streamUrl;

    public Camara() {
    }

    public Camara(String id, Usuario usuario, String nombre, String serverHost, Integer serverPort, String tipo, Integer indiceLocal, String streamUrl) {
        this.id = id;
        this.usuario = usuario;
        this.nombre = nombre;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.tipo = tipo;
        this.indiceLocal = indiceLocal;
        this.streamUrl = streamUrl;
    }

    public static class Builder {
        private String id;
        private Usuario usuario;
        private String nombre;
        private String serverHost;
        private Integer serverPort;
        private String tipo;
        private Integer indiceLocal;
        private String streamUrl;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder usuario(Usuario usuario) {
            this.usuario = usuario;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder serverHost(String serverHost) {
            this.serverHost = serverHost;
            return this;
        }

        public Builder serverPort(Integer serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public Builder tipo(String tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder indiceLocal(Integer indiceLocal) {
            this.indiceLocal = indiceLocal;
            return this;
        }

        public Builder streamUrl(String streamUrl) {
            this.streamUrl = streamUrl;
            return this;
        }

        public Camara build() {
            return new Camara(id, usuario, nombre, serverHost, serverPort, tipo, indiceLocal, streamUrl);
        }
    }

    public String getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getServerHost() {
        return serverHost;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public String getTipo() {
        return tipo;
    }

    public Integer getIndiceLocal() {
        return indiceLocal;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setUsuarioId(Integer userId) {
        if (this.usuario == null) {
            this.usuario = new Usuario(userId, null, null, null, null, false, null);
        } else {
            this.usuario.setId(userId);
        }
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setIndiceLocal(Integer indiceLocal) {
        this.indiceLocal = indiceLocal;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    @Override
    public void operation() {
        this.usuario = null;
        this.nombre = null;
        this.serverHost = null;
        this.serverPort = null;
        this.tipo = null;
        this.indiceLocal = null;
        this.streamUrl = null;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(id == null ? "null" : "\"" + escapeJson(id) + "\"").append(",");
        sb.append("\"usuario\":");
        if (usuario == null || usuario.getId() == null) {
            sb.append("null");
        } else {
            sb.append("{\"id\":").append(usuario.getId()).append("}");
        }
        sb.append(",");
        sb.append("\"nombre\":").append(nombre == null ? "null" : "\"" + escapeJson(nombre) + "\"").append(",");
        sb.append("\"serverHost\":").append(serverHost == null ? "null" : "\"" + escapeJson(serverHost) + "\"").append(",");
        sb.append("\"serverPort\":").append(serverPort == null ? "null" : serverPort).append(",");
        sb.append("\"tipo\":").append(tipo == null ? "null" : "\"" + escapeJson(tipo) + "\"").append(",");
        sb.append("\"indiceLocal\":").append(indiceLocal == null ? "null" : indiceLocal).append(",");
        sb.append("\"streamUrl\":").append(streamUrl == null ? "null" : "\"" + escapeJson(streamUrl) + "\"");
        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return null;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }
}
