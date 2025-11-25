package org.example.Transmision;

public class Camera {
    private final String id;
    private final String name;
    private final String serverHost;
    private final int serverPort;

    public Camera(String id, String name, String serverHost, int serverPort) {
        this.id = id;
        this.name = name;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getServerHost() { return serverHost; }
    public int getServerPort() { return serverPort; }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
