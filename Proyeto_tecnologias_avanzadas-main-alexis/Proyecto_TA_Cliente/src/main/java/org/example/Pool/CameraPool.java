package org.example.Pool;

import org.example.Transmision.Camera;
import java.util.*;

public class CameraPool {
    private static final CameraPool INSTANCE = new CameraPool();
    private final Queue<Camera> availableCameras = new LinkedList<>();
    private final Set<Camera> inUseCameras = new HashSet<>();

    private CameraPool() {}

    public static CameraPool getInstance() {
        return INSTANCE;
    }

    public synchronized Camera acquireCamera() {
        if (availableCameras.isEmpty()) {
            return null; // No cameras available
        }
        Camera camera = availableCameras.poll();
        inUseCameras.add(camera);
        return camera;
    }

    public synchronized void releaseCamera(Camera camera) {
        if (inUseCameras.remove(camera)) {
            availableCameras.offer(camera);
        }
    }

    public synchronized void addCamera(Camera camera) {
        if (!availableCameras.contains(camera) && !inUseCameras.contains(camera)) {
            availableCameras.offer(camera);
        }
    }

    public synchronized int getAvailableCount() {
        return availableCameras.size();
    }
}