export interface CambioAuditoria {
  campo: string;
  valorAnterior: string | null;
  valorNuevo: string | null;
}

/** Evento de la bitácora de auditoría (GET /api/auditoria/eventos). */
export interface EventoAuditoria {
  idEvento: number;
  fechaEvento: string;
  modulo: string | null;
  operacion: string;
  nombreTabla: string;
  idRegistro: string;
  idUsuario: number;
  usuario: string;
  ipOrigen: string | null;
  metodoHttp: string | null;
  endpoint: string | null;
  resultado: 'SUCCESS' | 'FAILURE';
  motivo: string | null;
  cambios: CambioAuditoria[];
}
