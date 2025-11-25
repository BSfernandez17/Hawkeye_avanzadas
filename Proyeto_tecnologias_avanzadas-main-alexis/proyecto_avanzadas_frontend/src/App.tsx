import { Routes, Route } from 'react-router-dom';
import { Login, Home, Reportes, ReportesUsuario, EditarUsuario, RegistroUsuario } from './pages/Index';
import CamarasOrdenadas from './components/CamarasOrdenadas';
import ArchivosPorTamano from './components/ArchivosPorTamano';
import ListaUsuariosPage from './pages/admin/ListaUsuarios';
import ArchivosPorCamara from "./pages/ArchivosPorCamara";

function App() {
  return (
    <div className="min-h-screen bg-[#181e29] text-[#cfd8e3] font-sans">
      <div className="max-w-5xl mx-auto py-8 px-4">
        <Routes>
          <Route path='/' element={<Login />} />
          <Route path='Home' element={<Home />} />
          <Route path='Reportes' element={<Reportes />} />
          <Route path='ReportesUsuario' element={<ReportesUsuario />} />
          <Route path='usuarios/registrar' element={<RegistroUsuario />} />
          <Route path='usuarios/editar/:id' element={<EditarUsuario />} />
          <Route path='usuarios/VerReporte/:id' element={<ReportesUsuario />} />
          <Route path='admin/camaras-ordenadas' element={<CamarasOrdenadas />} />
          <Route path='admin/archivos-por-tamano' element={<ArchivosPorTamano />} />
          <Route path='admin/lista-usuarios' element={<ListaUsuariosPage />} />
          <Route path="/archivos-camara/:idCamara" element={<ArchivosPorCamara />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;
