/** Catálogos que vienen de la base de datos (GET /api/catalogos/...). */
export interface EstadoCatalogo {
  codigo: string;
  descripcion: string;
  orden: number;
}

export interface ClasificacionCatalogo {
  codigo: string;
  descripcion: string;
  detalle: string;
  apto: boolean;
  orden: number;
}

/** Tipos y estados de embarcación (tablas tipo_embarcacion y estado_embarcacion). */
export interface ItemCatalogo {
  codigo: string;
  descripcion: string;
  orden: number;
}
