import React, { useState } from "react";
import { Link } from "react-router-dom";

interface Camara {
  id: number;
  nombre: string;
  cantidadArchivos: number;
  archivos: { id: number; nombre: string; tipo: "video" | "imagen"; url: string }[];
}

interface Usuario {
  id: number;
  nombre: string;
  camaras: Camara[];
}

// TODO: Reemplazar con datos reales desde la API
const usuariosEjemplo: Usuario[] = [
  {
    id: 1,
    nombre: "Usuario 1",
    camaras: [
      {
        id: 101,
        nombre: "Camara PC_CAM_0",
        cantidadArchivos: 3,
        archivos: [
          { id: 1, nombre: "video1.mp4", tipo: "video", url: "#" },
          { id: 2, nombre: "img1.jpg", tipo: "imagen", url: "#" },
          { id: 3, nombre: "video2.mp4", tipo: "video", url: "#" },
        ],
      },
    ],
  },
  {
    id: 2,
    nombre: "Usuario 2",
    camaras: [
      {
        id: 102,
        nombre: "Camara PC_CAM_1",
        cantidadArchivos: 2,
        archivos: [
          { id: 4, nombre: "video3.mp4", tipo: "video", url: "#" },
          { id: 5, nombre: "img2.jpg", tipo: "imagen", url: "#" },
        ],
      },
    ],
  },
];

const QReporteCamarasPorUsuario = () => {
  const [camaraSeleccionada, setCamaraSeleccionada] = useState<Camara | null>(null);

  return (
    <div>
      <h3 className="text-lg font-bold mb-2">Cámaras por usuario</h3>
      {usuariosEjemplo.map((usuario) => (
        <div key={usuario.id} className="mb-4 p-2 border rounded">
          <div className="font-semibold">{usuario.nombre}</div>
          {usuario.camaras.map((camara) => (
            <div key={camara.id} className="ml-4 mb-2">
              <span className="font-medium">{camara.nombre}</span> - Archivos: {camara.cantidadArchivos}
              <Link
                to={`/archivos-camara/${camara.id}`}
                className="ml-2 px-2 py-1 bg-blue-500 text-white rounded"
              >
                Ver archivos
              </Link>
            </div>
          ))}
        </div>
      ))}
      {camaraSeleccionada && (
        <div className="mt-4 p-2 border-t">
          <h4 className="font-bold">Archivos de {camaraSeleccionada.nombre}</h4>
          <ul className="list-disc pl-6">
            {camaraSeleccionada.archivos.map((archivo) => (
              <li key={archivo.id}>
                {archivo.tipo === "video" ? "🎬" : "🖼️"} {archivo.nombre}
                <a href={archivo.url} className="ml-2 text-blue-600 underline" target="_blank" rel="noopener noreferrer">
                  Ver
                </a>
              </li>
            ))}
          </ul>
          <button className="mt-2 px-2 py-1 bg-gray-300 rounded" onClick={() => setCamaraSeleccionada(null)}>
            Cerrar
          </button>
        </div>
      )}
    </div>
  );
};

export default QReporteCamarasPorUsuario;
