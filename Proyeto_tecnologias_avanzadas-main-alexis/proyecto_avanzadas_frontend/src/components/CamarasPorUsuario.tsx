import React, { useEffect, useState } from 'react'
import type { CamaraDTO } from '../api/Camaras.api'
import * as CamarasApi from '../api/Camaras.api'
import { useAuth } from '../context/AuthContex'

type CamarasPorUsuarioProps = {
  id?: string
}

export const CamarasPorUsuario: React.FC<CamarasPorUsuarioProps> = ({ id }) => {
  const { token } = useAuth();
  const [camaras, setCamaras] = useState<CamaraDTO[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    if (!token) {
      setError('No autenticado. Inicia sesión para ver las cámaras.');
      return;
    }

    const fetchCamaras = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await CamarasApi.obtenerCamarasPorUsuario(token, Number(id));
        setCamaras(data);
      } catch (e: unknown) {
        const msg = e instanceof Error ? e.message : String(e);
        setError(msg || 'Error al obtener cámaras.');
      } finally {
        setLoading(false);
      }
    };

    fetchCamaras();
  }, [id, token]);

  return (
    <div>
      <h2 className="text-lg font-semibold mb-2">Cámaras del usuario {id ?? ''}</h2>

      {!token && (<div className="text-yellow-700">Debes iniciar sesión para ver las cámaras.</div>)}

      {loading && <div>Cargando cámaras...</div>}
      {error && <div className="text-red-600">{error}</div>}

      {!loading && camaras && camaras.length === 0 && <div>No hay cámaras registradas para este usuario.</div>}

      {!loading && camaras && camaras.length > 0 && (
        <ul className="space-y-4">
          {camaras.map((c) => (
            <li key={c.id} className="border border-[#23283a] rounded-lg p-4 bg-[#23283a] shadow hover:bg-[#20232f]">
              <div className="font-bold text-blue-200 text-lg mb-1">{c.nombre}</div>
              <div className="text-sm">IP: <span className="text-blue-100">{c.ip}</span></div>
              <div className="text-sm">Ubicación: <span className="text-blue-100">{c.ubicacion ?? '-'}</span></div>
              <div className="text-sm">Estado: <span className={c.estado ? "text-green-400" : "text-red-400"}>{c.estado ? 'Activo' : 'Inactivo'}</span></div>
              <a
                href={`/archivos-camara/${c.id}`}
                className="inline-block mt-3 px-4 py-2 bg-blue-600 text-white rounded-lg shadow hover:bg-blue-700 transition font-semibold"
              >
                Ver archivos
              </a>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default CamarasPorUsuario
