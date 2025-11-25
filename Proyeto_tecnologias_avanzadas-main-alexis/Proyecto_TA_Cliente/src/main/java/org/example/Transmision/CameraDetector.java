package org.example.Transmision;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;

import java.util.ArrayList;
import java.util.List;

public class CameraDetector {

    public static class DetectedCamera {
        private final String id;
        private final String name;
        private final String type;

        public DetectedCamera(String id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return name + " [" + type + "] (" + id + ")";
        }
    }

    public static List<DetectedCamera> detectAll() {
        return detectWebcams();
    }

    public static List<DetectedCamera> detectWebcams() {
        List<DetectedCamera> list = new ArrayList<>();
        try {
            List<Webcam> webcams = Webcam.getWebcams();
            int idx = 0;
            for (Webcam w : webcams) {
                String id = "PC_CAM_" + idx;
                String name = w.getName() != null ? w.getName() : ("Webcam " + idx);
                list.add(new DetectedCamera(id, name, "PC"));
                idx++;
            }
        } catch (WebcamException ex) {
            ex.printStackTrace();
        }
        return list;
    }
}
