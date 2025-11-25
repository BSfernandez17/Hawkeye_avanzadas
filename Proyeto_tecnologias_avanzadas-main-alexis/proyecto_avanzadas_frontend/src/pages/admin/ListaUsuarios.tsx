import { useEffect, useState } from 'react';
import ListaUsuarios from '../../components/ListaUsuarios';
import { getUsuarios } from '../../api/Usuarios.api';
import { useAuth } from '../../context/AuthContex';

const ListaUsuariosPage = () => {
  const { token } = useAuth();
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchUsuarios = async () => {
      if (!token) {
        setError('Debes iniciar sesión para ver los usuarios.');
        setLoading(false);
        return;
      }
      try {
        const data = await getUsuarios(token);
        setUsuarios(data);
      } catch (err) {
        setError('Error al obtener usuarios (token inválido o expirado).');
      } finally {
        setLoading(false);
      }
    };
    fetchUsuarios();
  }, [token]);

  return <ListaUsuarios usuarios={usuarios} loading={loading} error={error} />;
};

export default ListaUsuariosPage;