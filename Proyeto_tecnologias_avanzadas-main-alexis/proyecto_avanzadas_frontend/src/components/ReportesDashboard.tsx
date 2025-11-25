import React from "react";
import { Link } from "react-router-dom";

const ReportesDashboard = () => {
  return (
    <div className="space-y-6">
      <h2 className="text-xl font-bold mb-4">Reportes e Informes</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="p-4 border rounded">
          <div className="font-semibold mb-2">Cámara con más archivos enviados</div>
          <Link to="/admin/camaras-ordenadas" className="px-3 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">Ver reporte</Link>
        </div>
        <div className="p-4 border rounded">
          <div className="font-semibold mb-2">Archivos enviados: listar por tamaño y mostrar propietario</div>
          <Link to="/admin/archivos-por-tamano" className="px-3 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">Ver reporte</Link>
        </div>
        <div className="p-4 border rounded">
          <div className="font-semibold mb-2">Lista de usuarios</div>
          <Link to="/admin/lista-usuarios" className="px-3 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">Ver usuarios</Link>
        </div>
      </div>
    </div>
  );
};

export default ReportesDashboard;
