import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { NgxPaginationModule } from 'ngx-pagination';
import Swal from 'sweetalert2';

import { SolicitudService } from '../../../core/services/solicitud.service';
import { ToastService } from '../../../core/services/toast.service';
import { Solicitud } from '../../../core/models/solicitud.models';
import { REGEX } from '../../../shared/validators/regex';
import { CatalogoService } from '../../../core/services/catalogo.service';
import {
  antecedentesDe,
  antecedentesHtml,
  claseClasificacion,
  claseEstado,
  codigoCorto,
  mensajeError,
} from '../../../shared/utils/solicitud-ui';

type Pestana = 'pendientes' | 'historial';


@Component({
  selector: 'app-solicitudes-aprobar',
  standalone: true,
  imports: [DatePipe, NgxPaginationModule],
  templateUrl: './solicitudes-aprobar.html',
  styleUrl: '../solicitudes.scss',
})
export class SolicitudesAprobar implements OnInit {
  private readonly solicitudService = inject(SolicitudService);
  private readonly toast = inject(ToastService);

  private readonly catalogo = inject(CatalogoService);

  readonly etiquetaEstado = (codigo: string) => this.catalogo.etiquetaEstado(codigo);
  readonly etiquetaClasificacion = (codigo: string | null) => this.catalogo.etiquetaClasificacion(codigo);
  readonly claseClasificacion = claseClasificacion;
  readonly codigoCorto = codigoCorto;
  readonly claseEstado = claseEstado;

  readonly pestana = signal<Pestana>('pendientes');
  readonly todas = signal<Solicitud[]>([]);
  readonly cargado = signal(false);
  readonly paginaPendientes = signal(1);
  readonly paginaHistorial = signal(1);

  readonly pendientes = computed(() =>
    this.todas()
      .filter((s) => s.estado === 'VERIFICADA')
      .sort((a, b) => a.fechaSolicitud.localeCompare(b.fechaSolicitud)),
  );
  readonly historial = computed(() =>
    this.todas().filter((s) => ['APROBADA', 'DESAPROBADA', 'NO_APTA'].includes(s.estado)),
  );

  antecedentes(s: Solicitud): Solicitud[] {
    return antecedentesDe(this.todas(), s);
  }

  verAntecedentes(s: Solicitud): void {
    Swal.fire({
      icon: 'warning',
      title: `Antecedentes del DNI ${s.dni}`,
      html: antecedentesHtml(this.antecedentes(s), this.etiquetaEstado),
      width: 720,
      confirmButtonText: 'Cerrar',
      confirmButtonColor: '#0b2d42',
    });
  }

  ngOnInit(): void {
    this.catalogo.cargar();
    this.cargar();
  }

  cargar(): void {
    this.solicitudService.listar().subscribe({
      next: (lista) => {
        this.todas.set(lista);
        this.cargado.set(true);
      },
      error: (err: HttpErrorResponse) => this.toast.error(mensajeError(err)),
    });
  }

  async aprobar(s: Solicitud): Promise<void> {
    const previas = this.antecedentes(s);
    const aviso = previas.length
      ? `<div style="margin-top:14px;padding:10px;border-radius:8px;background:#fdf2f2;color:#842029;text-align:left;font-size:0.9rem">
           <strong>Atención:</strong> este DNI tiene ${previas.length} solicitud(es) anterior(es).
           ${antecedentesHtml(previas, this.etiquetaEstado)}
         </div>`
      : '';
    const r = await Swal.fire({
      icon: previas.length ? 'warning' : 'question',
      title: '¿Aprobar solicitud?',
      width: previas.length ? 720 : undefined,
      html: `Se otorgará el rango de <strong>Socio</strong> a <strong>${this.escapar(s.nombres)} ${this.escapar(s.apellidos)}</strong>
             y se activará su usuario <strong>${this.escapar(s.correo)}</strong>.${aviso}`,
      showCancelButton: true,
      confirmButtonText: 'Sí, aprobar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#198754',
    });
    if (!r.isConfirmed) return;

    this.solicitudService.aprobar(s.id).subscribe({
      next: (actualizada) => {
        this.reemplazar(actualizada);
        this.toast.exito(`${s.nombres} ${s.apellidos} ahora es socio del club.`);
      },
      error: (err: HttpErrorResponse) => this.toast.error(mensajeError(err, 'No se pudo aprobar la solicitud.')),
    });
  }

  async desaprobar(s: Solicitud): Promise<void> {
    const r = await Swal.fire({
      icon: 'warning',
      title: 'Desaprobar solicitud',
      html: `Indica los motivos para que <strong>${this.escapar(s.nombres)}</strong> pueda subsanarlos.`,
      input: 'textarea',
      inputPlaceholder: 'Ej. El DNI adjunto es ilegible; debe presentar una copia legible.',
      inputAttributes: { maxlength: '500' },
      showCancelButton: true,
      confirmButtonText: 'Desaprobar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#dc3545',
      // Validación con expresión regular antes de enviar
      inputValidator: (valor) => {
        const texto = (valor ?? '').trim();
        if (!texto) return 'El motivo es obligatorio.';
        if (texto.length < 10) return 'El motivo debe tener al menos 10 caracteres.';
        if (texto.length > 500) return 'El motivo no puede superar los 500 caracteres.';
        if (!REGEX.TEXTO.test(texto)) return 'El motivo contiene caracteres no permitidos.';
        return null;
      },
    });
    if (!r.isConfirmed) return;

    this.solicitudService.desaprobar(s.id, String(r.value).trim()).subscribe({
      next: async (actualizada) => {
        this.reemplazar(actualizada);
        const doc = await Swal.fire({
          icon: 'info',
          title: 'Solicitud desaprobada',
          text: 'Se generó el documento con los motivos para entregar al postulante.',
          showCancelButton: true,
          confirmButtonText: 'Ver documento',
          cancelButtonText: 'Cerrar',
          confirmButtonColor: '#0b2d42',
        });
        if (doc.isConfirmed) this.imprimirConstancia(actualizada);
      },
      error: (err: HttpErrorResponse) => this.toast.error(mensajeError(err, 'No se pudo desaprobar la solicitud.')),
    });
  }


  imprimirConstancia(s: Solicitud): void {
    const ventana = window.open('', '_blank', 'width=800,height=900');
    if (!ventana) {
      this.toast.advertencia('Permite las ventanas emergentes para ver el documento.');
      return;
    }
    const fecha = (iso: string | null) => (iso ? new Date(iso).toLocaleDateString('es-PE') : '-');
    ventana.document.write(`<!DOCTYPE html><html lang="es"><head><meta charset="utf-8">
      <title>Constancia - Solicitud ${codigoCorto(s.codigo)}</title>
      <style>
        body{font-family:'Segoe UI',Arial,sans-serif;color:#243746;margin:48px;line-height:1.6}
        h1{color:#0b2d42;font-size:22px;margin:0} .club{letter-spacing:3px;font-size:11px;color:#647783}
        .linea{height:3px;background:#c9a45c;margin:16px 0 28px}
        table{width:100%;border-collapse:collapse;margin:16px 0} td{padding:6px 8px;border-bottom:1px solid #dce5e9}
        td:first-child{color:#647783;width:35%} .motivo{background:#fdf2f2;border-left:4px solid #dc3545;padding:12px 16px}
        .firma{margin-top:80px;text-align:center} .firma div{border-top:1px solid #243746;width:260px;margin:0 auto;padding-top:6px}
        @media print{button{display:none}}
      </style></head><body>
      <div class="club">CLUB NÁUTICO POSEIDÓN DEL PERÚ</div>
      <h1>Constancia de desaprobación de solicitud de inscripción</h1>
      <div class="linea"></div>
      <p>Se comunica al postulante que su solicitud de inscripción como socio <strong>no ha sido aprobada</strong>
         por los motivos que se detallan a continuación. Puede subsanarlos y presentar una nueva solicitud.</p>
      <table>
        <tr><td>Código de seguimiento</td><td style="font-family:monospace">${this.escapar(s.codigo)}</td></tr>
        <tr><td>Postulante</td><td>${this.escapar(s.nombres)} ${this.escapar(s.apellidos)}</td></tr>
        <tr><td>DNI</td><td>${this.escapar(s.dni)}</td></tr>
        <tr><td>Fecha de solicitud</td><td>${fecha(s.fechaSolicitud)}</td></tr>
        <tr><td>Fecha de resolución</td><td>${fecha(s.fechaResolucion)}</td></tr>
      </table>
      <p><strong>Motivos de la desaprobación:</strong></p>
      <div class="motivo">${this.escapar(s.motivoDesaprobacion ?? '')}</div>
      <div class="firma"><div>Jefe de Atención al Cliente<br><small>${this.escapar(s.resueltoPor ?? '')}</small></div></div>
      <p style="text-align:center;margin-top:40px"><button onclick="window.print()">Imprimir / Guardar como PDF</button></p>
      </body></html>`);
    ventana.document.close();
  }

  private reemplazar(actualizada: Solicitud): void {
    this.todas.update((lista) => lista.map((x) => (x.id === actualizada.id ? actualizada : x)));
  }

  private escapar(texto: string): string {
    return texto
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }
}
