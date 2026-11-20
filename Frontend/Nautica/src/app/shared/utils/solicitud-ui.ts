import { HttpErrorResponse } from '@angular/common/http';

import { Solicitud } from '../../core/models/solicitud.models';


export const ESTADOS: Record<string, { etiqueta: string; clase: string }> = {
  PENDIENTE: { etiqueta: 'Pendiente de verificación', clase: 'text-bg-warning' },
  VERIFICADA: { etiqueta: 'Pendiente de aprobación', clase: 'text-bg-info' },
  NO_APTA: { etiqueta: 'No apta', clase: 'text-bg-secondary' },
  APROBADA: { etiqueta: 'Aprobada', clase: 'text-bg-success' },
  DESAPROBADA: { etiqueta: 'Desaprobada', clase: 'text-bg-danger' },
};

export const CLASIFICACIONES: Record<string, { etiqueta: string; clase: string; descripcion: string }> = {
  PAGADOR: {
    etiqueta: 'Pagador',
    clase: 'text-bg-success',
    descripcion: 'Paga puntualmente. Pasa a aprobación del Jefe de Atención al Cliente.',
  },
  PAGADOR_ESPORADICO: {
    etiqueta: 'Pagador esporádico',
    clase: 'text-bg-warning',
    descripcion: 'Paga con atrasos. La solicitud queda como NO APTA.',
  },
  RENUENTE: {
    etiqueta: 'Renuente a pago',
    clase: 'text-bg-danger',
    descripcion: 'No paga sus obligaciones. La solicitud queda como NO APTA.',
  },
};


export function codigoCorto(codigo: string | null | undefined): string {
  return (codigo ?? '').substring(0, 8).toUpperCase();
}

export function etiquetaEstado(estado: string): string {
  return ESTADOS[estado]?.etiqueta ?? estado;
}

export function claseEstado(estado: string): string {
  return ESTADOS[estado]?.clase ?? 'text-bg-light';
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

export function antecedentesHtml(lista: Solicitud[]): string {
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
