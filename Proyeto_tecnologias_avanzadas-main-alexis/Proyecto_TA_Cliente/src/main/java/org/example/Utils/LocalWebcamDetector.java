package org.example.Utils;

import java.util.ArrayList;
import java.util.List;
import com.github.sarxos.webcam.Webcam;

public class LocalWebcamDetector {

    public record DetectedCamera(String id, String name) {}

    public static List<DetectedCamera> listLocalCameras() {
        List<Webcam> cams = Webcam.getWebcams();
        List<DetectedCamera> out = new ArrayList<>();
        int i = 0;
        for (Webcam w : cams) {
            String id = "PC_CAM_" + i;
            String name = w.getName();
            out.add(new DetectedCamera(id, name));
            i++;
        }
        return out;
    }
}
