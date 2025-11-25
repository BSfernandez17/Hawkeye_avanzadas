import React from 'react'
import { CamarasPorUsuario } from '../../components/CamarasPorUsuario'
import { useParams } from 'react-router-dom';

export const ReportesUsuario = () => {
    const { id } = useParams<{ id?: string }>();

  return (
    <CamarasPorUsuario id={id} />
  )
}

export default ReportesUsuario