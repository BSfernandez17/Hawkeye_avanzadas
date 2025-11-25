import axios from "axios";

// Tipos para tipar correctamente la solicitud y la respuesta
export interface LoginRequest {
  email: string;
  contrasena: string;
}
// ✅ Crear un usuario nuevo
export interface CrearUsuarioPayload {
  nombre: string;
  email: string;
  contrasena: string;

}

export interface AuthResponse {
  token: string;
}

// Tipo seguro para la respuesta de error del backend
export interface ErrorResponse {
  message?: string;
  [key: string]: unknown;
}

// ✅ Base URL general del backend (sin el /login)
// En desarrollo usamos el proxy de Vite apuntando a "/auth" para evitar CORS.
// En producción puedes definir VITE_API_URL en .env para apuntar al dominio real.
const API_URL = import.meta.env?.VITE_API_URL ?? "/auth";

/**
 * Inicia sesión y devuelve el token JWT.
 */
export const loginUser = async (
  credentials: LoginRequest
): Promise<AuthResponse> => {
  try {
    const response = await axios.post<AuthResponse>(
      `${API_URL}/login`,
      credentials
    );
    return response.data;
  } catch (error: unknown) {
    // ✅ Manejo de error claro y seguro con axios
    if (
      axios.isAxiosError(error) &&
      error.response?.data &&
      (error.response.data as ErrorResponse).message
    ) {
      throw new Error(String((error.response.data as ErrorResponse).message));
    }
    // Si es una Error estándar con mensaje
    if (error instanceof Error && error.message) {
      throw new Error(error.message);
    }
    throw new Error("Error al iniciar sesión. Intenta nuevamente.");
  }
};
export const crearUsuario = async (payload: CrearUsuarioPayload):Promise<AuthResponse> => {
  const response = await axios.post( `${API_URL}/register`, payload);
  return response.data;
};
