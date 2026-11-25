export type EstadoSolicitud = 'PENDIENTE' | 'VERIFICADA' | 'NO_APTA' | 'APROBADA' | 'DESAPROBADA';
export type Clasificacion = 'PAGADOR' | 'PAGADOR_ESPORADICO' | 'RENUENTE';


export interface SolicitudRequest {
  nombres: string;
  apellidos: string;
  dni: string;
  fechaNacimiento: string;
  correo: string;
  celular: string;
  direccion: string;
  poseeEmbarcacion: boolean;
  cantidadEmbarcaciones: number;
  password: string;
}

export interface SolicitudCreada {
  mensaje: string;
  codigo: string;
  estado: EstadoSolicitud;
  fechaSolicitud: string;
}

export interface Seguimiento {
  codigo: string;
  nombres: string;
  estado: EstadoSolicitud;
  fechaSolicitud: string;
  fechaResolucion: string | null;
  motivoDesaprobacion: string | null;
}


export interface Solicitud {
  id: number;
  codigo: string;
  nombres: string;
  apellidos: string;
  dni: string;
  fechaNacimiento: string;
  correo: string;
  celular: string;
  direccion: string;
  poseeEmbarcacion: boolean;
  cantidadEmbarcaciones: number;
  estado: EstadoSolicitud;
  clasificacion: Clasificacion | null;
  resultadoVerificacion: string | null;
  motivoDesaprobacion: string | null;
  fechaSolicitud: string;
  fechaVerificacion: string | null;
  fechaResolucion: string | null;
  verificadoPor: string | null;
  resueltoPor: string | null;
  dniDuplicado: boolean;
  solicitudesConMismoDni: number;
}

export interface VerificacionRequest {
  clasificacion: Clasificacion;
  resultadoVerificacion: string;
}


