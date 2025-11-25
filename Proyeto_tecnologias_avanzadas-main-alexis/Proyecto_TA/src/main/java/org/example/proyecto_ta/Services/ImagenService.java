package org.example.proyecto_ta.Services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.example.proyecto_ta.Repositories.ImagenRepositorio;
import org.example.proyecto_ta.Repositories.VideoRepositorio;
import org.example.proyecto_ta.model.Camara;
import org.example.proyecto_ta.model.Imagen;
import org.example.proyecto_ta.model.Video;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ImagenService {
    private static final int MAX_FILES_PER_USER = 1000;

    private final ImagenRepositorio imagenRepositorio;
    private final CamaraService camaraServicio;
    private final VideoRepositorio videoRepositorio;
    private final FrameExtractorService frameExtractorService;
    private final ImageFilterService imageFilterService;
    private final ImagenStorageService imagenStorageService;
    private final RedisTemplate<String, byte[]> redisTemplateBytes;
    
    private final java.util.concurrent.ConcurrentHashMap<Long, java.util.concurrent.atomic.AtomicInteger> archivosEnProceso = new java.util.concurrent.ConcurrentHashMap<>();
    public ImagenService(
            ImagenRepositorio imagenRepositorio,
            CamaraService camaraServicio,
            VideoRepositorio videoRepositorio,
            FrameExtractorService frameExtractorService,
            ImageFilterService imageFilterService,
            ImagenStorageService imagenStorageService,
            RedisTemplate<String, byte[]> redisTemplateBytes
    ){
        this.imagenRepositorio = imagenRepositorio;
        this.camaraServicio = camaraServicio;
        this.videoRepositorio = videoRepositorio;
        this.frameExtractorService = frameExtractorService;
        this.imageFilterService = imageFilterService;
        this.imagenStorageService = imagenStorageService;
        this.redisTemplateBytes = redisTemplateBytes;
    }

    public Imagen capturarDesdeVideo(
            Long videoId,
            Double segundo,
            String filtro,
            Integer porcentajeEscala,
            Float brillo,
            Integer rotacion
    ) throws Exception {

        Video video = videoRepositorio.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video no existe id=" + videoId));

        Camara camara = video.getCamara();
        String cameraId = camara != null ? camara.getId() : null;

        // Reservar 1 slot para esta operación (evita condiciones de carrera)
        Long usuarioId = camara != null && camara.getUsuario() != null ? camara.getUsuario().getId() : null;
        boolean reserved = true;
        if (usuarioId != null) {
            reserved = tryReserveSlots(usuarioId, 1);
            if (!reserved) {
                throw new IllegalStateException("Límite de archivos alcanzado para usuario=" + usuarioId + " (reservas)");
            }
        }
        Path tempDir = Files.createTempDirectory("capturas");
        Path tempMp4 = tempDir.resolve("video_" + videoId + ".mp4");
        Files.write(tempMp4, video.getVideo());

        Path framePath = frameExtractorService.extractFrame(tempMp4, segundo, tempDir);

        BufferedImage frameImg = ImageIO.read(framePath.toFile());
        if (frameImg == null) throw new RuntimeException("No se pudo leer frame.");

        BufferedImage filtered = imageFilterService.aplicarFiltros(frameImg, filtro, porcentajeEscala, brillo, rotacion);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(filtered, "jpg", baos);
        byte[] jpgBytes = baos.toByteArray();

        Path diskPath = imagenStorageService.saveCapture(camara, video, cameraId, jpgBytes);
        String nombreArchivo = diskPath.getFileName().toString();

        Imagen img = new Imagen();
        img.setCamara(camara);
        img.setTitulo("Captura video " + videoId);
        img.setImagen(jpgBytes);
        img.setFiltro(filtro != null ? filtro : "NONE");
        img.setTipoMime("image/jpeg");
        img.setNombreArchivo(nombreArchivo);

        Imagen persisted = imagenRepositorio.save(img);

        String redisKey = "img:" + persisted.getId();
        redisTemplateBytes.opsForValue().set(redisKey, jpgBytes, 2, TimeUnit.HOURS);

        // liberar reserva y limpiar en finally
        try {
            return persisted;
        } finally {
            if (usuarioId != null && reserved) releaseSlots(usuarioId, 1);
            try { Files.deleteIfExists(framePath); } catch (Exception ignored) {}
            try { Files.deleteIfExists(tempMp4); } catch (Exception ignored) {}
            try { Files.deleteIfExists(tempDir); } catch (Exception ignored) {}
        }
    }

    public Imagen capturarDesdeLiveBytes(
            String cameraId,
            byte[] jpgBytes,
            String filtro,
            Integer porcentajeEscala,
            Float brillo,
            Integer rotacion
    ) throws Exception {

        if (cameraId == null || cameraId.isBlank()) {
            throw new IllegalArgumentException("cameraId requerido");
        }

        Camara camara = camaraServicio.obtenerCamaraPorId(cameraId)
                .orElseThrow(() -> new IllegalArgumentException("Cámara no existe id=" + cameraId));

        // Reservar 1 slot para esta operation (live capture)
        Long usuarioIdLive = camara != null && camara.getUsuario() != null ? camara.getUsuario().getId() : null;
        boolean reservedLive = true;
        if (usuarioIdLive != null) {
            reservedLive = tryReserveSlots(usuarioIdLive, 1);
            if (!reservedLive) {
                throw new IllegalStateException("Límite de archivos alcanzado para usuario=" + usuarioIdLive + " (reservas)");
            }
        }

        BufferedImage frameImg = ImageIO.read(new ByteArrayInputStream(jpgBytes));
        if (frameImg == null) throw new RuntimeException("No se pudo leer JPG LIVE");

        BufferedImage filtered = imageFilterService.aplicarFiltros(frameImg, filtro, porcentajeEscala, brillo, rotacion);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(filtered, "jpg", baos);
        byte[] filteredJpg = baos.toByteArray();

        Path diskPath = imagenStorageService.saveCapture(camara, null, cameraId, filteredJpg);
        String nombreArchivo = diskPath.getFileName().toString();

        Imagen img = new Imagen();
        img.setCamara(camara);
        img.setTitulo("Captura LIVE " + cameraId);
        img.setImagen(filteredJpg);
        img.setFiltro(filtro != null ? filtro : "NONE");
        img.setTipoMime("image/jpeg");
        img.setNombreArchivo(nombreArchivo);

        Imagen persisted = imagenRepositorio.save(img);

        String redisKey = "img:" + persisted.getId();
        redisTemplateBytes.opsForValue().set(redisKey, filteredJpg, 2, TimeUnit.HOURS);

        try {
            return persisted;
        } finally {
            try {
                if (usuarioIdLive != null && reservedLive) releaseSlots(usuarioIdLive, 1);
            } catch (Exception e) {
                System.err.println("Error liberando slots: " + e.getMessage());
            }
        }
    }

    public List<Imagen> aplicarTodosLosFiltrosYGuardarDesdeLive(
            String cameraId,
            byte[] jpgBytes,
            Integer porcentajeEscala,
            Float brillo
    ) throws Exception {

        if (cameraId == null || cameraId.isBlank()) {
            throw new IllegalArgumentException("cameraId requerido");
        }

        Camara camara = camaraServicio.obtenerCamaraPorId(cameraId)
                .orElseThrow(() -> new IllegalArgumentException("Cámara no existe id=" + cameraId));

        BufferedImage baseImg = ImageIO.read(new ByteArrayInputStream(jpgBytes));
        if (baseImg == null) throw new RuntimeException("No se pudo leer JPG LIVE");

        String tituloBase = "Captura LIVE " + cameraId;

        // Reservar slots para las imágenes que se van a crear
        Long usuarioIdBulk = camara != null && camara.getUsuario() != null ? camara.getUsuario().getId() : null;
        int toCreate = 6; // specs count in aplicarTodosLosFiltrosYGuardar
        boolean reservedBulk = true;
        if (usuarioIdBulk != null) {
            reservedBulk = tryReserveSlots(usuarioIdBulk, toCreate);
            if (!reservedBulk) {
                throw new IllegalStateException("Guardar todas las capturas excede el límite de archivos para usuario=" + usuarioIdBulk + " (reservas)");
            }
        }

        try {
            return aplicarTodosLosFiltrosYGuardar(baseImg, camara, null, cameraId, tituloBase, porcentajeEscala, brillo);
        } finally {
            if (usuarioIdBulk != null && reservedBulk) releaseSlots(usuarioIdBulk, toCreate);
        }
    }

    public List<Imagen> aplicarTodosLosFiltrosYGuardarDesdeVideo(
            Long videoId,
            Double segundo,
            Integer porcentajeEscala,
            Float brillo
    ) throws Exception {

        Video video = videoRepositorio.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video no existe id=" + videoId));

        Camara camara = video.getCamara();
        String cameraId = camara != null ? camara.getId() : null;

        Path tempDir = Files.createTempDirectory("capturas");
        Path tempMp4 = tempDir.resolve("video_" + videoId + ".mp4");
        Files.write(tempMp4, video.getVideo());

        Path framePath = frameExtractorService.extractFrame(tempMp4, segundo, tempDir);

        BufferedImage baseImg = ImageIO.read(framePath.toFile());
        if (baseImg == null) throw new RuntimeException("No se pudo leer frame.");

        String tituloBase = "Captura video " + videoId;

        // Reservar slots para las imágenes que se van a crear
        Long usuarioIdBulk2 = camara != null && camara.getUsuario() != null ? camara.getUsuario().getId() : null;
        int toCreate2 = 6; // specs count
        boolean reservedBulk2 = true;
        if (usuarioIdBulk2 != null) {
            reservedBulk2 = tryReserveSlots(usuarioIdBulk2, toCreate2);
            if (!reservedBulk2) {
                throw new IllegalStateException("Guardar todas las capturas excede el límite de archivos para usuario=" + usuarioIdBulk2 + " (reservas)");
            }
        }

        List<Imagen> res = null;
        try {
            res = aplicarTodosLosFiltrosYGuardar(baseImg, camara, video, cameraId, tituloBase, porcentajeEscala, brillo);
        } finally {
            if (usuarioIdBulk2 != null && reservedBulk2) releaseSlots(usuarioIdBulk2, toCreate2);
            try { Files.deleteIfExists(framePath); } catch (Exception ignored) {}
            try { Files.deleteIfExists(tempMp4); } catch (Exception ignored) {}
            try { Files.deleteIfExists(tempDir); } catch (Exception ignored) {}
        }

        return res;
    }

    public void guardarImagen(Imagen imagen){
        imagenRepositorio.save(imagen);
    }
public List<Imagen> listarTodas() {
    return imagenRepositorio.findAll();
}

    public List<Imagen> obtenerPorCamaraId(String idCamara){
        return imagenRepositorio.findByCamara_Id(idCamara);
    }

    public void eliminarPorId(Long id){
        imagenRepositorio.deleteById(id);
        redisTemplateBytes.delete("img:" + id);
    }

    public List<Imagen> obtenerImagenPorUsuario(Long usuarioId) {
        List<Camara> camaras = camaraServicio.obtenerCamarasPorUsuario(usuarioId);
        List<Imagen> imagenes = new ArrayList<>();
        for (Camara camara : camaras) {
            imagenes.addAll(obtenerPorCamaraId(camara.getId()));
        }
        return imagenes;
    }

    public Optional<Imagen> obtenerImagenPorId(Long id) {
        return imagenRepositorio.findById(id);
    }

    public byte[] obtenerBytesImagen(Long id) {
        String key = "img:" + id;
        byte[] cached = redisTemplateBytes.opsForValue().get(key);
        if (cached != null) return cached;

        Optional<Imagen> img = imagenRepositorio.findById(id);
        if (img.isEmpty()) return null;

        byte[] bytes = img.get().getImagen();
        redisTemplateBytes.opsForValue().set(key, bytes, 2, TimeUnit.HOURS);
        return bytes;
    }

    private List<Imagen> aplicarTodosLosFiltrosYGuardar(
            BufferedImage baseImg,
            Camara camara,
            Video video,
            String cameraId,
            String tituloBase,
            Integer porcentajeEscala,
            Float brillo
    ) throws Exception {

        List<FiltroSpec> specs = new ArrayList<>();
        specs.add(new FiltroSpec("GRISES", "GRISES", null, null, null));
        specs.add(new FiltroSpec("REDUCIR", "REDUCIR", porcentajeEscala != null ? porcentajeEscala : 50, null, null));
        specs.add(new FiltroSpec("BRILLO", "BRILLO", null, brillo != null ? brillo : 0.3f, null));
        specs.add(new FiltroSpec("ROTAR_45", "ROTAR", null, null, 45));
        specs.add(new FiltroSpec("ROTAR_90", "ROTAR", null, null, 90));
        specs.add(new FiltroSpec("ROTAR_180", "ROTAR", null, null, 180));

        List<Imagen> out = new ArrayList<>();

        for (FiltroSpec spec : specs) {
            BufferedImage filtered = imageFilterService.aplicarFiltros(
                    baseImg,
                    spec.filtroAplicar,
                    spec.porcentajeEscala,
                    spec.brillo,
                    spec.rotacion
            );

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(filtered, "jpg", baos);
            byte[] bytes = baos.toByteArray();

            Path diskPath = imagenStorageService.saveCapture(camara, video, cameraId, bytes);
            String nombreArchivo = diskPath.getFileName().toString();

            Imagen img = new Imagen();
            img.setCamara(camara);
            img.setTitulo(tituloBase + " - " + spec.filtroGuardar);
            img.setImagen(bytes);
            img.setFiltro(spec.filtroGuardar);
            img.setTipoMime("image/jpeg");
            img.setNombreArchivo(nombreArchivo);

            Imagen persisted = imagenRepositorio.save(img);
            redisTemplateBytes.opsForValue().set("img:" + persisted.getId(), bytes, 2, TimeUnit.HOURS);

            out.add(persisted);
        }

        return out;
    }

    private static class FiltroSpec {
        private final String filtroGuardar;
        private final String filtroAplicar;
        private final Integer porcentajeEscala;
        private final Float brillo;
        private final Integer rotacion;

        private FiltroSpec(String filtroGuardar, String filtroAplicar, Integer porcentajeEscala, Float brillo, Integer rotacion) {
            this.filtroGuardar = filtroGuardar;
            this.filtroAplicar = filtroAplicar;
            this.porcentajeEscala = porcentajeEscala;
            this.brillo = brillo;
            this.rotacion = rotacion;
        }
    }

    public List<Imagen> obtenerImagenesPorUsuario(Long usuarioId) {
        return imagenRepositorio.findByCamara_Usuario_Id(usuarioId);
    }

    // Reserva atomica en memoria para evitar condiciones de carrera entre peticiones concurrentes
    private boolean tryReserveSlots(Long usuarioId, int slots) {
        final java.util.concurrent.atomic.AtomicBoolean success = new java.util.concurrent.atomic.AtomicBoolean(false);
        archivosEnProceso.compute(usuarioId, (k, v) -> {
            int reserved = (v == null) ? 0 : v.get();
            // contar los archivos ya persistidos (videos + images)
            int existing = countFilesForUsuario(usuarioId);
            if (existing + reserved + slots > MAX_FILES_PER_USER) {
                success.set(false);
                return v;
            }
            if (v == null) v = new java.util.concurrent.atomic.AtomicInteger(0);
            v.addAndGet(slots);
            success.set(true);
            System.out.println("ImagenService: reserved slots for usuario=" + usuarioId + " reservedNow=" + v.get() + " existingFiles=" + existing);
            return v;
        });
        return success.get();
    }

    private void releaseSlots(Long usuarioId, int slots) {
        archivosEnProceso.computeIfPresent(usuarioId, (k, v) -> {
            int updated = v.addAndGet(-slots);
            if (updated <= 0) return null;
            return v;
        });
    }

    private int countFilesForUsuario(Long usuarioId) {
        int images = obtenerImagenesPorUsuario(usuarioId).size();
        // contar videos por cámaras del usuario
        int videos = 0;
        List<Camara> cams = camaraServicio.obtenerCamarasPorUsuario(usuarioId);
        for (Camara c : cams) {
            videos += videoRepositorio.findByCamara_Id(c.getId()).size();
        }
        return images + videos;
    }
}
