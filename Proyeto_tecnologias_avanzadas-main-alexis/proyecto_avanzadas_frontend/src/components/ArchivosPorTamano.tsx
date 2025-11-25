import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContex";
import axios from "axios";

interface FotoMeta {
  id: number;
  titulo: string;
  tipoMime: string;
  nombreArchivo: string;
  camaraId: string;
  camaraNombre: string;
  propietarioId: number;
  propietarioNombre: string;
  propietarioEmail: string;
  tamanoBytes?: number;
}

interface VideoMeta {
  id: number;
  titulo: string;
  tipoMime: string;
  nombreArchivo: string;
  fechaGrabacion: string;
  tamanoBytes: number;
  idCamara: string;
  nombreCamara: string;
  idUsuario: number;
  nombreUsuario: string;
  emailUsuario: string;
}
const ArchivosPorTamano = () => {

  const { token } = useAuth();
  const [fotos, setFotos] = useState<FotoMeta[]>([]);
  const [videos, setVideos] = useState<VideoMeta[]>([]);
  const [fotoUrls, setFotoUrls] = useState<{ [id: number]: string }>({});
  const [videoUrls, setVideoUrls] = useState<{ [id: number]: string }>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) {
      setError("No autenticado");
      setLoading(false);
      return;
    }
    Promise.all([
      axios.get<FotoMeta[]>("/api/imagenes", { headers: { Authorization: `Bearer ${token}` } }),
      axios.get<VideoMeta[]>("/api/videos/listar-todo", { headers: { Authorization: `Bearer ${token}` } })
    ])
      .then(async ([fotosRes, videosRes]) => {
        const fotosData = (fotosRes.data ?? []).sort((a, b) => (b.tamanoBytes ?? 0) - (a.tamanoBytes ?? 0));
        const videosData = (videosRes.data ?? []).sort((a, b) => b.tamanoBytes - a.tamanoBytes);
        setFotos(fotosData);
        setVideos(videosData);

        // Descargar imágenes y videos como blobs y crear URLs
        const fotoBlobPromises = fotosData.map(foto =>
          axios.get(`/api/imagenes/${foto.id}`, {
            responseType: "blob",
            headers: { Authorization: `Bearer ${token}` }
          }).then(res => [foto.id, URL.createObjectURL(res.data)])
        );
        const videoBlobPromises = videosData.map(video =>
          axios.get(`/api/videos/${video.id}`, {
            responseType: "blob",
            headers: { Authorization: `Bearer ${token}` }
          }).then(res => [video.id, URL.createObjectURL(res.data)])
        );
        const fotoBlobs = await Promise.all(fotoBlobPromises);
        const videoBlobs = await Promise.all(videoBlobPromises);
        setFotoUrls(Object.fromEntries(fotoBlobs));
        setVideoUrls(Object.fromEntries(videoBlobs));

        setLoading(false);
      })
      .catch(() => {
        setError("Error al cargar archivos");
        setLoading(false);
      });
  }, [token]);

  if (loading) return <div>Cargando...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div className="flex gap-8">
      <div className="flex-1">
        <h2 className="text-2xl font-bold mb-4 text-blue-300">Fotos (mayor a menor)</h2>
        <table className="min-w-full bg-[#23283a] rounded-lg overflow-hidden shadow border border-[#23283a]">
          <thead className="bg-[#23283a] text-blue-200">
            <tr>
              <th className="px-4 py-2">Vista</th>
              <th className="px-4 py-2">Archivo</th>
              <th className="px-4 py-2">Tamaño</th>
              <th className="px-4 py-2">Propietario</th>
              <th className="px-4 py-2">Cámara</th>
            </tr>
          </thead>
          <tbody>
            {fotos.map((foto) => (
              <tr key={foto.id} className="border-b border-[#23283a] hover:bg-[#20232f]">
                <td className="px-4 py-2">
                  {fotoUrls[foto.id] ? (
                    <img
                      src={fotoUrls[foto.id]}
                      alt={foto.titulo}
                      className="max-w-[120px] max-h-[80px] rounded shadow"
                    />
                  ) : (
                    <span className="text-gray-400">Cargando...</span>
                  )}
                </td>
                <td className="px-4 py-2">{foto.nombreArchivo}</td>
                <td className="px-4 py-2">{foto.tamanoBytes ?? "-"}</td>
                <td className="px-4 py-2">{foto.propietarioNombre} <br /> <span className="text-xs text-blue-200">{foto.propietarioEmail}</span></td>
                <td className="px-4 py-2">{foto.camaraNombre}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="flex-1">
        <h2 className="text-2xl font-bold mb-4 text-blue-300">Videos (mayor a menor)</h2>
        <table className="min-w-full bg-[#23283a] rounded-lg overflow-hidden shadow border border-[#23283a]">
          <thead className="bg-[#23283a] text-blue-200">
            <tr>
              <th className="px-4 py-2">Vista</th>
              <th className="px-4 py-2">Archivo</th>
              <th className="px-4 py-2">Tamaño</th>
              <th className="px-4 py-2">Propietario</th>
              <th className="px-4 py-2">Cámara</th>
            </tr>
          </thead>
          <tbody>
            {videos.map((video) => (
              <tr key={video.id} className="border-b border-[#23283a] hover:bg-[#20232f]">
                <td className="px-4 py-2">
                  {videoUrls[video.id] ? (
                    <video
                      src={videoUrls[video.id]}
                      controls
                      className="max-w-[120px] max-h-[80px] rounded shadow"
                    />
                  ) : (
                    <span className="text-gray-400">Cargando...</span>
                  )}
                </td>
                <td className="px-4 py-2">{video.nombreArchivo}</td>
                <td className="px-4 py-2">{video.tamanoBytes}</td>
                <td className="px-4 py-2">{video.nombreUsuario} <br /> <span className="text-xs text-blue-200">{video.emailUsuario}</span></td>
                <td className="px-4 py-2">{video.nombreCamara}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ArchivosPorTamano;
