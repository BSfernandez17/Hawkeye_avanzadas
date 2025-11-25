import axios from "axios";

export interface ArchivoFoto {
  id: string;
  nombre: string;
  tamano: number;
  propietario: string;
  camara: string;
  tipo: "foto";
}

export interface ArchivoVideo {
  id: string;
  nombre: string;
  tamano: number;
  propietario: string;
  camara: string;
  tipo: "video";
}

export type Archivo = ArchivoFoto | ArchivoVideo;

const API_BASE = import.meta.env?.VITE_API_BASE ?? "/api";

export const getArchivosPorTamano = async (token: string) => {
  const response = await axios.get(`${API_BASE}/report/archivos-por-tamano`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return response.data as Archivo[];
};

export const getFotos = async (token: string) => {
  const response = await axios.get(`${API_BASE}/imagenes`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return response.data as ArchivoFoto[];
};

export const getVideos = async (token: string) => {
  const response = await axios.get(`${API_BASE}/videos/camara/all`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return response.data as ArchivoVideo[];
};
