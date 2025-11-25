import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { crearUsuario, type CrearUsuarioPayload } from "../api/Auth";
import { useAuth } from "../context/AuthContex";

export default function RegistroUsuario() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [form, setForm] = useState<CrearUsuarioPayload>({
    nombre: "",
    email: "",
    contrasena: "",
  } as CrearUsuarioPayload);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccess("");



    if (!form.nombre || !form.email || !form.contrasena) {
      setError("Completa nombre, correo y contraseña.");
      return;
    }

    setLoading(true);
    try {
      const data= await crearUsuario( form);
      setSuccess("Usuario creado correctamente.");
      login(data.token)
      setTimeout(() => navigate("/Reportes"), 800);
    } catch (err) {
      console.error("Error al crear usuario:", err);
      let msg = "No se pudo crear el usuario.";
      if (err instanceof Error && err.message) msg = err.message;
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 px-4 py-8">
      <div className="max-w-lg mx-auto bg-white p-6 rounded-lg shadow">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-xl font-semibold text-gray-800">Registrar usuario</h1>
          <Link
            to="/Reportes"
            className="text-sm text-indigo-600 hover:text-indigo-700"
          >
            Volver a usuarios
          </Link>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Nombre
            </label>
            <input
              name="nombre"
              value={form.nombre}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
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
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
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
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="••••••••"
              required
            />
          </div>



          <button
            type="submit"
            disabled={loading}
            className="w-full inline-flex justify-center py-2 px-4 rounded-md text-white bg-indigo-600 hover:bg-indigo-700 disabled:opacity-60"
          >
            {loading ? "Creando..." : "Crear usuario"}
          </button>
        </form>

        {error && (
          <p className="mt-3 text-sm text-red-600 text-center">{error}</p>
        )}
        {success && (
          <p className="mt-3 text-sm text-green-600 text-center">{success}</p>
        )}
      </div>
    </div>
  );
}
