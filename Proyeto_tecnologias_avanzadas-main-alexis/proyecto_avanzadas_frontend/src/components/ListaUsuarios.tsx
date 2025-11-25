import { Link } from "react-router-dom";

const ListaUsuarios = ({ usuarios = [], loading = false, error = "" }) => {
  return (
    <div className="bg-[#23283a] p-8 rounded-xl shadow-lg border border-[#23283a]">
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-3xl font-bold text-blue-300">Usuarios</h2>
        <Link
          to="/usuarios/registrar"
          className="inline-flex items-center px-4 py-2 rounded-lg bg-blue-600 text-white text-base font-semibold shadow hover:bg-blue-700 transition"
        >
          Registrar usuario
        </Link>
      </div>
      {loading ? (
        <div className="flex items-center justify-center p-8">
          <svg
            className="animate-spin h-8 w-8 text-blue-400"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            ></circle>
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
            ></path>
          </svg>
          <p className="text-blue-200">Cargando usuarios...</p>
        </div>
      ) : error ? (
        <p className="text-red-500 text-center font-semibold">{error}</p>
      ) : usuarios.length === 0 ? (
        <p className="text-center text-blue-200">No hay usuarios disponibles.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full border-collapse bg-[#23283a] rounded-lg shadow border border-[#23283a]">
            <thead>
              <tr>
                <th className="text-left border-b border-blue-700 px-4 py-2 bg-[#23283a] text-sm font-semibold text-blue-200">ID</th>
                <th className="text-left border-b border-blue-700 px-4 py-2 bg-[#23283a] text-sm font-semibold text-blue-200">Nombre</th>
                <th className="text-left border-b border-blue-700 px-4 py-2 bg-[#23283a] text-sm font-semibold text-blue-200">Email</th>
                <th className="text-left border-b border-blue-700 px-4 py-2 bg-[#23283a] text-sm font-semibold text-blue-200">Rol</th>
                <th className="text-left border-b border-blue-700 px-4 py-2 bg-[#23283a] text-sm font-semibold text-blue-200">IP</th>
                <th className="text-left border-b border-blue-700 px-4 py-2 bg-[#23283a] text-sm font-semibold text-blue-200">Status</th>
                <th className="text-left border-b border-blue-700 px-4 py-2 bg-[#23283a] text-sm font-semibold text-blue-200">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {usuarios.map((usuario) => (
                <tr
                  key={usuario.id}
                  className="odd:bg-[#23283a] even:bg-[#20232f] hover:bg-[#20232f] transition"
                >
                  <td className="px-4 py-2 border-b text-sm text-blue-100 font-semibold">{usuario.id}</td>
                  <td className="px-4 py-2 border-b text-sm text-blue-100">{usuario.nombre}</td>
                  <td className="px-4 py-2 border-b text-sm text-blue-100">{usuario.email}</td>
                  <td className="px-4 py-2 border-b text-sm text-blue-100">{usuario.rol}</td>
                  <td className="px-4 py-2 border-b text-sm text-blue-100">{usuario.ip}</td>
                  <td className="px-4 py-2 border-b text-sm text-blue-100">{usuario.status ? "activo" : "pendiente"}</td>
                  <td className="px-4 py-2 border-b text-sm">
                    <Link 
                      to={`/usuarios/VerReporte/${usuario.id}`}
                      className="inline-flex items-center px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-semibold shadow hover:bg-blue-700 transition mr-2">
                      Ver Reporte
                    </Link>
                    <Link
                      to={`/usuarios/editar/${usuario.id}`}
                      className="inline-flex items-center px-4 py-2 rounded-lg bg-green-600 text-white text-sm font-semibold shadow hover:bg-green-700 transition"
                      aria-label={`Editar usuario ${usuario.nombre}`}
                    >
                      Editar
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default ListaUsuarios;
