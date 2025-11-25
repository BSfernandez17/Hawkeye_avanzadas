import axios from "axios";
import type { Usuario } from "../model";

export interface Camara {
  id: number;
  nombre: string;
  ip: string;
  ubicacion?: string;
  estado?: boolean;
  usuario?: Usuario;
}

export interface CamaraDTO {
  id: number;
  usuario: Usuario;
  nombre: string;
  ip: string;
  ubicacion?: string;
  estado?: boolean;
}

// En desarrollo usamos el proxy de Vite con rutas relativas
const API_BASE = import.meta.env?.VITE_API_BASE ?? "/api";
const API_URL = `${API_BASE}/camaras`;

export const createAuthAxios = (token: string) => {
  return axios.create({
    baseURL: API_URL,
    headers: { Authorization: `Bearer ${token}` },
  });
};

// Guardar nueva cámara
export const guardarCamara = async (token: string, camara: Omit<Camara, "id">) => {
  const axiosAuth = createAuthAxios(token);
  const response = await axiosAuth.post("/guardarCamara", camara);
  return response.data;
};

// Obtener cámaras por id de usuario
export const obtenerCamarasPorUsuario = async (token: string, id: number) => {
  const axiosAuth = createAuthAxios(token);
  const response = await axiosAuth.get(`/obtenerCamarasPorUsuario/${id}`);
  return response.data as CamaraDTO[];
};

// Obtener cámara por id de usuario y ip (query params)
export const obtenerCamaraPorUsuarioYip = async (token: string, id: number, ip: string) => {
  const axiosAuth = createAuthAxios(token);
  const response = await axiosAuth.get(`/obtenerCamaraPorUsuarioYip`, { params: { id, ip } });
  return response.data as CamaraDTO;
};

// Obtener cámara por id
export const obtenerCamaraPorId = async (token: string, id: number) => {
  const axiosAuth = createAuthAxios(token);
  const response = await axiosAuth.get(`/obtenerCamaraPorId/${id}`);
  return response.data as Camara;
};

// Eliminar cámara por id
export const eliminarCamara = async (token: string, id: number) => {
  const axiosAuth = createAuthAxios(token);
  const response = await axiosAuth.delete(`/eliminarCamara/${id}`);
  return response.data;
};

export default {
  guardarCamara,
  obtenerCamarasPorUsuario,
  obtenerCamaraPorUsuarioYip,
  obtenerCamaraPorId,
  eliminarCamara,
};
