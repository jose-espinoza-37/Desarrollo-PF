import { HttpErrorResponse } from '@angular/common/http';

import { Solicitud } from '../../core/models/solicitud.models';


/** Color del badge de cada estado (solo presentación; las etiquetas vienen del catálogo). */
export const CLASE_ESTADO: Record<string, string> = {
  PENDIENTE: 'text-bg-warning',
  VERIFICADA: 'text-bg-info',
  NO_APTA: 'text-bg-secondary',
  APROBADA: 'text-bg-success',
  DESAPROBADA: 'text-bg-danger',
};

/** Color del badge de cada clasificación de pago. */
export const CLASE_CLASIFICACION: Record<string, string> = {
  PAGADOR: 'text-bg-success',
  PAGADOR_ESPORADICO: 'text-bg-warning',
  RENUENTE: 'text-bg-danger',
};


export function codigoCorto(codigo: string | null | undefined): string {
  return (codigo ?? '').substring(0, 8).toUpperCase();
}

/** Color del badge del estado de la ficha de embarcación. */
export const CLASE_ESTADO_EMBARCACION: Record<string, string> = {
  PENDIENTE: 'text-bg-warning',
  VERIFICADA: 'text-bg-success',
  OBSERVADA: 'text-bg-danger',
};

/** Abre en una pestaña nueva el archivo descargado (PDF o imagen). */
export function abrirArchivo(blob: Blob): void {
  const url = URL.createObjectURL(blob);
  window.open(url, '_blank');
  setTimeout(() => URL.revokeObjectURL(url), 60_000);
}

/** Escapa texto que se inserta en HTML generado (constancias). */
export function escaparHtml(texto: string | number | null | undefined): string {
  return String(texto ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

export function claseEstado(estado: string): string {
  return CLASE_ESTADO[estado] ?? 'text-bg-light';
}

export function claseClasificacion(codigo: string | null): string {
  return CLASE_CLASIFICACION[codigo ?? ''] ?? 'text-bg-light';
}


export function mensajeError(err: HttpErrorResponse, porDefecto = 'Ocurrió un error inesperado.'): string {
  if (err.status === 0) return 'No se pudo conectar con el servidor.';
  const cuerpo = err.error;
  if (cuerpo?.mensajes && typeof cuerpo.mensajes === 'object') {
    return Object.values(cuerpo.mensajes).join(' ');
  }
  return cuerpo?.mensaje ?? porDefecto;
}


export function antecedentesDe(todas: Solicitud[], s: Solicitud): Solicitud[] {
  return todas
    .filter((x) => x.dni === s.dni && x.id !== s.id)
    .sort((a, b) => b.fechaSolicitud.localeCompare(a.fechaSolicitud));
}

export function antecedentesHtml(lista: Solicitud[], etiquetaEstado: (codigo: string) => string): string {
  const esc = (t: string | null) =>
    (t ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  const filas = lista
    .map(
      (a) => `<tr>
        <td style="font-family:monospace" title="${esc(a.codigo)}">${codigoCorto(a.codigo)}</td>
        <td>${new Date(a.fechaSolicitud).toLocaleDateString('es-PE')}</td>
        <td>${esc(etiquetaEstado(a.estado))}</td>
        <td style="text-align:left">${esc(a.motivoDesaprobacion ?? a.resultadoVerificacion ?? '-')}</td>
      </tr>`,
    )
    .join('');
  return `<table style="width:100%;font-size:0.85rem;border-collapse:collapse" class="table table-sm">
    <thead><tr><th>Código</th><th>Fecha</th><th>Estado</th><th style="text-align:left">Motivo / verificación</th></tr></thead>
    <tbody>${filas}</tbody></table>`;
}
