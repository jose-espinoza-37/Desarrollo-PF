export type EstadoEmbarcacion = 'PENDIENTE' | 'VERIFICADA' | 'OBSERVADA';
export type TipoDocumento = 'MATRICULA' | 'CERTIFICADO' | 'OTRO';

export interface DocumentoEmbarcacion {
  id: number;
  tipoDocumento: TipoDocumento;
  nombreArchivo: string;
  contentType: string;
  tamano: number;
  fechaCarga: string;
}

/** Ficha de "Registro de Embarcación" (HU11). */
export interface Embarcacion {
  id: number;
  nombre: string;
  matricula: string;
  tipo: string;
  eslora: number;
  manga: number;
  calado: number;
  anioFabricacion: number;
  estado: EstadoEmbarcacion;
  observaciones: string | null;
  fechaRegistro: string;
  fechaVerificacion: string | null;
  verificadoPor: string | null;
  socioId: number;
  socioNombre: string;
  socioCorreo: string;
  radaId: number | null;
  radaCodigo: string | null;
  radaMuelle: string | null;
  radaTarifaMensual: number | null;
  fechaAsignacion: string | null;
  documentos: DocumentoEmbarcacion[];
}

export interface EmbarcacionRequest {
  nombre: string;
  matricula: string;
  tipo: string;
  eslora: number;
  manga: number;
  calado: number;
  anioFabricacion: number;
}

export interface VerificacionEmbarcacionRequest {
  resultado: 'VERIFICADA' | 'OBSERVADA';
  observaciones: string;
}

/** HU13 */
export interface Rada {
  id: number;
  codigo: string;
  muelle: string;
  esloraMax: number;
  mangaMax: number;
  caladoMax: number;
  tarifaMensual: number;
  estado: 'LIBRE' | 'OCUPADA';
  embarcacionId: number | null;
  embarcacionNombre: string | null;
  embarcacionMatricula: string | null;
  socioNombre: string | null;
}

export interface RadaCompatible {
  id: number;
  codigo: string;
  muelle: string;
  esloraMax: number;
  mangaMax: number;
  caladoMax: number;
  tarifaMensual: number;
  holguraEslora: number;
  holguraManga: number;
  aprovechamiento: number;
  ranking: number;
}

export interface AsignacionRada {
  id: number;
  radaCodigo: string;
  embarcacionNombre: string;
  embarcacionMatricula: string;
  socioNombre: string;
  fechaAsignacion: string;
  asignadoPor: string;
  fechaLiberacion: string | null;
  liberadoPor: string | null;
  motivoLiberacion: string | null;
  activa: boolean;
}
