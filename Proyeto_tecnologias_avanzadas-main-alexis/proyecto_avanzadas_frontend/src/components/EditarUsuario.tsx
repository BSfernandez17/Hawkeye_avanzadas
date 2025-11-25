import { useNavigate, Link, useParams, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContex";
import { editarUsuario, obtenerUsuarioPorId } from "../api/Usuarios.api";

interface EditarUsuario {
  nombre: string;
  email: string;
  contrasena?: string;
  rol: "ADMIN" | "USER";
  status?: boolean;
  ip?: string;
}

export const EditarUsuario = () => {
  const navigate = useNavigate();
  const { token } = useAuth();
  const { id } = useParams<{ id?: string }>();
  const location = useLocation();

  interface LocationState {
    usuario?: {
      nombre?: string;
      email?: string;
      rol?: "ADMIN" | "USER";
      status?: boolean;
      ip?: string;
    };
  }

  const state = location.state as LocationState | null;

  const [form, setForm] = useState<EditarUsuario>({
    nombre: "",
    email: "",
    contrasena: "",
    rol: "USER",
    status: false,
    ip: "",
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);

  // Cargar datos del usuario
  useEffect(() => {
    const cargarUsuario = async () => {
      try {
        if (state?.usuario) {
          // Si viene desde location.state
          setForm({
            nombre: state.usuario.nombre ?? "",
            email: state.usuario.email ?? "",
            contrasena: "",
            rol: state.usuario.rol ?? "USER",
            status: state.usuario.status ?? false,
            ip: state.usuario.ip ?? "",
          });
        } else if (id && token) {
          // Si NO viene state, traerlo del backend
          const usuario = await obtenerUsuarioPorId(token, Number(id));
          setForm({
            nombre: usuario.nombre ?? "",
            email: usuario.email ?? "",
            contrasena: "",
            rol: usuario.rol ?? "USER",
            status: usuario.status ?? false,
            ip: usuario.ip ?? "",
          });
        } else {
          setError("No se encontró información del usuario.");
        }
      } catch (err) {
        console.error("Error cargando usuario:", err);
        setError("Error al cargar los datos del usuario.");
      } finally {
        setInitialLoading(false);
      }
    };

    cargarUsuario();
  }, [state, id, token]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    // Convertir status del selector a boolean
    if (name === "status") {
      setForm((prev) => ({ ...prev, status: value === "true" }));
      return;
    }
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!form.nombre || !form.email) {
      setError("Completa nombre y correo.");
      return;
    }

    setLoading(true);
    try {
      const payload = { ...form };
      if (!payload.contrasena) delete payload.contrasena;

      await editarUsuario(token ?? "", payload, Number(id!));
      setSuccess("Usuario editado correctamente.");
      setTimeout(() => navigate("/Reportes"), 1000);
    } catch (err) {
      console.error("Error al editar usuario:", err);
      setError("No se pudo editar el usuario.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 px-4 py-8">
      <div className="max-w-lg mx-auto bg-white p-6 rounded-lg shadow">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-xl font-semibold text-gray-800">
            Editar usuario
          </h1>
          <Link
            to="/Reportes"
            className="text-sm text-indigo-600 hover:text-indigo-700"
          >
            Volver a usuarios
          </Link>
        </div>

        {initialLoading ? (
          <p className="text-sm text-gray-500">Cargando usuario...</p>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Nombre
              </label>
              <input
                name="nombre"
                value={form.nombre}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="Nombre completo"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Correo
              </label>
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="correo@ejemplo.com"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Contraseña
              </label>
              <input
                type="password"
                name="contrasena"
                value={form.contrasena}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="(dejar vacío para mantener la actual)"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Rol
                </label>
                <select
                  name="rol"
                  value={form.rol}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                >
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Estado</label>
                <select
                  name="status"
                  value={form.status ? "true" : "false"}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                >
                  <option value="true">Activo</option>
                  <option value="false">Inactivo</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                IP registrada
              </label>
              <input
                readOnly
                value={form.ip || "(sin IP)"}
                className="w-full px-3 py-2 bg-gray-100 border border-gray-300 rounded-md text-gray-600"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full inline-flex justify-center py-2 px-4 rounded-md text-white bg-indigo-600 hover:bg-indigo-700 disabled:opacity-60"
            >
              {loading ? "Editando..." : "Guardar cambios"}
            </button>
          </form>
        )}

        {error && <p className="mt-3 text-sm text-red-600 text-center">{error}</p>}
        {success && (
          <p className="mt-3 text-sm text-green-600 text-center">{success}</p>
        )}
      </div>
    </div>
  );
};
