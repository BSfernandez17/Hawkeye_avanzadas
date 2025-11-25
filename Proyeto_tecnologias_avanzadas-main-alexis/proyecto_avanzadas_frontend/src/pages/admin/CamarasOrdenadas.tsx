import React from "react";

interface CamaraGlobal {
  id: number;
  nombre: string;
  usuario: string;
  cantidadArchivos: number;
}

// TODO: Reemplazar con datos reales desde la API
const camarasEjemplo: CamaraGlobal[] = [
  { id: 101, nombre: "PC_CAM_0", usuario: "Usuario 1", cantidadArchivos: 10 },
  { id: 102, nombre: "PC_CAM_1", usuario: "Usuario 2", cantidadArchivos: 7 },
  { id: 103, nombre: "PC_CAM_2", usuario: "Usuario 3", cantidadArchivos: 3 },
];

const CamarasOrdenadas = () => {
  const camarasOrdenadas = [...camarasEjemplo].sort((a, b) => b.cantidadArchivos - a.cantidadArchivos);

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">Cámaras ordenadas por cantidad de archivos</h2>
      <table className="min-w-full border">
        <thead>
          <tr className="bg-gray-200">
            <th className="px-4 py-2">Cámara</th>
            <th className="px-4 py-2">Usuario</th>
            <th className="px-4 py-2">Cantidad de archivos</th>
          </tr>
        </thead>
        <tbody>
          {camarasOrdenadas.map((camara) => (
            <tr key={camara.id}>
              <td className="border px-4 py-2">{camara.nombre}</td>
              <td className="border px-4 py-2">{camara.usuario}</td>
              <td className="border px-4 py-2">{camara.cantidadArchivos}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default CamarasOrdenadas;
