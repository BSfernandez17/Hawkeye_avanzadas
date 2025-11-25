import React, { useEffect, useState } from "react";
import axios from "axios";
import { useAuth } from "../context/AuthContex";

interface CamaraArchivo {
  idCamara: string;
  nombreCamara: string;
  cantidadArchivos: number;
}

const CamarasOrdenadas = () => {
  const [camaras, setCamaras] = useState<CamaraArchivo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const { token } = useAuth();
  useEffect(() => {
    if (!token) {
      setError("No autenticado");
      setLoading(false);
      return;
    }
    axios.get("/api/camaras/reporte/camaras-por-archivos", {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
      .then((res) => {
        setCamaras(res.data);
        setLoading(false);
      })
      .catch(() => {
        setError("Error al cargar las cámaras");
        setLoading(false);
      });
  }, [token]);

  if (loading) return <div>Cargando...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div>
      <h2>Cámaras ordenadas por cantidad de archivos</h2>
      <table>
        <thead>
          <tr>
            <th>ID Cámara</th>
            <th>Nombre</th>
            <th>Cantidad de Archivos</th>
          </tr>
        </thead>
        <tbody>
          {camaras.map((camara) => (
            <tr key={camara.idCamara}>
              <td>{camara.idCamara}</td>
              <td>{camara.nombreCamara}</td>
              <td>{camara.cantidadArchivos}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default CamarasOrdenadas;
