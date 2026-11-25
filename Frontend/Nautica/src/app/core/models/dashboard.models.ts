/** Respuestas de /api/dashboard (calculadas con SQL en el backend). */
export interface KpiDashboard {
  totalSolicitudes: number;
  pendientesVerificacion: number;
  pendientesAprobacion: number;
  sociosActivos: number;
  tasaAprobacion: number;
  tasaNoAptos: number;
  promedioHorasAtencion: number | null;
  medianaHorasAtencion: number | null;
  desviacionHorasAtencion: number | null;
  edadPromedio: number | null;
  reincidentes: number;
  correlacionEdadEmbarcaciones: number | null;
}

export interface SerieTemporal {
  fecha: string;
  cantidad: number;
  aprobadas: number;
  mediaMovil: number;
  acumulado: number;
}

export interface TiempoAtencion {
  clasificacion: string;
  descripcion: string;
  cantidad: number;
  minimo: number | null;
  q1: number | null;
  mediana: number | null;
  q3: number | null;
  maximo: number | null;
  promedio: number | null;
  desviacion: number | null;
}

export interface MapaCalor {
  diaSemana: number;
  dia: string;
  franja: string;
  cantidad: number;
  porcentaje: number;
}

export interface PerfilEdad {
  rango: string;
  clasificacion: string;
  descripcion: string;
  cantidad: number;
  porcentaje: number;
}

export interface DashboardDatos {
  kpis: KpiDashboard;
  serie: SerieTemporal[];
  tiempos: TiempoAtencion[];
  mapaCalor: MapaCalor[];
  perfilEdad: PerfilEdad[];
}
