// src/api/Usuarios.api.ts
import axios from "axios";
export interface EditarUsuario {
  nombre: string;
  email: string;
  contrasena?: string;
  rol: 'ADMIN' | 'USER';
  status?: boolean;
  ip?: string;
}






// En desarrollo, usamos el proxy de Vite con rutas relativas para evitar CORS
// En producción, puedes definir VITE_API_BASE (p.ej. https://mi-dominio.com/api)
const API_BASE = import.meta.env?.VITE_API_BASE ?? "/api";
const API_URL = `${API_BASE}/usuarios`;

// ✅ Crea una instancia de axios que añade el token automáticamente
export const createAuthAxios = (token: string) => {
  const instance = axios.create({
    baseURL: API_URL,
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return instance;
};

// ✅ Obtener todos los usuarios
export const getUsuarios = async (token: string) => {
  const axiosAuth = createAuthAxios(token);
  const response = await axiosAuth.get("/obtenerUsuarios");
  return response.data;
};

//Editar usuarios 
export const editarUsuario=async(token:string,usuario:EditarUsuario,id:number)=>{
  const axiosAuth=createAuthAxios(token)
  const response = await axiosAuth.put(`/editarUsuario/${id}`, usuario);
  return response.data;
}
export const obtenerUsuarioPorId = async (token: string, id: number) => {

  const axiosAuth=createAuthAxios(token);
  const response=await axiosAuth.get(`/obtenerUsuarioPorId/${id}`)
  return response.data;

};




