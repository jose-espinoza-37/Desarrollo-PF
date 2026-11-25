import { Component, OnInit, TemplateRef, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap/modal';

import { CatalogoService } from '../../../core/services/catalogo.service';
import { EmbarcacionService } from '../../../core/services/embarcacion.service';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';
import { Embarcacion, TipoDocumento } from '../../../core/models/embarcacion.models';
import { REGEX, archivoValido } from '../../../shared/validators/regex';
import {
  CLASE_ESTADO_EMBARCACION,
  abrirArchivo,
  escaparHtml,
  mensajeError,
} from '../../../shared/utils/solicitud-ui';

const ANIO_ACTUAL = new Date().getFullYear();

/** HU11: el socio registra sus embarcaciones, carga documentos y ve el estado de cada ficha. */
@Component({
  selector: 'app-mis-embarcaciones',
  standalone: true,
  imports: [DatePipe, DecimalPipe, ReactiveFormsModule],
  templateUrl: './mis-embarcaciones.html',
  styleUrls: ['../solicitudes.scss', './mis-embarcaciones.scss'],
})
export class MisEmbarcaciones implements OnInit {
  private readonly embarcacionService = inject(EmbarcacionService);
  private readonly catalogo = inject(CatalogoService);
  private readonly toast = inject(ToastService);
  private readonly modal = inject(NgbModal);
  private readonly auth = inject(AuthService);

  readonly tipos = this.catalogo.tiposEmbarcacion;
  readonly etiquetaTipo = (c: string) => this.catalogo.etiquetaTipoEmbarcacion(c);
  readonly etiquetaEstado = (c: string) => this.catalogo.etiquetaEstadoEmbarcacion(c);
  readonly claseEstado = (c: string) => CLASE_ESTADO_EMBARCACION[c] ?? 'text-bg-light';
  readonly tiposDocumento: { codigo: TipoDocumento; etiqueta: string }[] = [
    { codigo: 'MATRICULA', etiqueta: 'Certificado de matrícula' },
    { codigo: 'CERTIFICADO', etiqueta: 'Certificado de navegabilidad' },
    { codigo: 'OTRO', etiqueta: 'Otro documento' },
  ];
  readonly anioActual = ANIO_ACTUAL;

  readonly embarcaciones = signal<Embarcacion[]>([]);
  readonly cargado = signal(false);
  readonly guardando = signal(false);
  readonly editando = signal<Embarcacion | null>(null);
  readonly subiendo = signal<number | null>(null);
  readonly observadas = computed(() => this.embarcaciones().filter((e) => e.estado === 'OBSERVADA'));
  private modalRef: NgbModalRef | null = null;

  readonly form = new FormGroup({
    nombre: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(2), Validators.maxLength(60), Validators.pattern(REGEX.NOMBRE_NAVE)],
    }),
    matricula: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(REGEX.MATRICULA)],
    }),
    tipo: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    eslora: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(REGEX.MEDIDA), Validators.min(2), Validators.max(40)],
    }),
    manga: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(REGEX.MEDIDA), Validators.min(0.8), Validators.max(12)],
    }),
    calado: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(REGEX.MEDIDA), Validators.min(0.2), Validators.max(6)],
    }),
    anioFabricacion: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(REGEX.ANIO), Validators.max(ANIO_ACTUAL)],
    }),
  });

  /** Archivo elegido en cada tarjeta, por id de embarcación. */
  private archivos = new Map<number, File>();

  ngOnInit(): void {
    this.catalogo.cargar();
    this.cargar();
  }

  cargar(): void {
    this.embarcacionService.misEmbarcaciones().subscribe({
      next: (lista) => {
        this.embarcaciones.set(lista);
        this.cargado.set(true);
        const obs = lista.filter((e) => e.estado === 'OBSERVADA').length;
        if (obs > 0) this.toast.advertencia(`Tienes ${obs} ficha(s) observada(s) por Servicios Navieros.`);
      },
      error: (err: HttpErrorResponse) => this.toast.error(mensajeError(err)),
    });
  }

  abrirFormulario(plantilla: TemplateRef<unknown>, embarcacion?: Embarcacion): void {
    this.editando.set(embarcacion ?? null);
    this.form.reset(
      embarcacion
        ? {
            nombre: embarcacion.nombre,
            matricula: embarcacion.matricula,
            tipo: embarcacion.tipo,
            eslora: String(embarcacion.eslora),
            manga: String(embarcacion.manga),
            calado: String(embarcacion.calado),
            anioFabricacion: String(embarcacion.anioFabricacion),
          }
        : undefined,
    );
    this.modalRef = this.modal.open(plantilla, { size: 'lg', centered: true, backdrop: 'static' });
  }

  /** La matrícula se escribe siempre en mayúsculas. */
  mayusculas(): void {
    const c = this.form.controls.matricula;
    const v = c.value.toUpperCase().replace(/\s/g, '');
    if (v !== c.value) c.setValue(v);
  }

  invalido(campo: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[campo];
    return c.invalid && (c.touched || c.dirty);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.advertencia('Revisa los campos marcados en rojo.');
      return;
    }
    const v = this.form.getRawValue();
    const datos = {
      nombre: v.nombre.trim(),
      matricula: v.matricula,
      tipo: v.tipo,
      eslora: Number(v.eslora),
      manga: Number(v.manga),
      calado: Number(v.calado),
      anioFabricacion: Number(v.anioFabricacion),
    };
    const actual = this.editando();
    const peticion = actual
      ? this.embarcacionService.subsanar(actual.id, datos)
      : this.embarcacionService.registrar(datos);

    this.guardando.set(true);
    peticion.subscribe({
      next: (e) => {
        this.guardando.set(false);
        this.modalRef?.close();
        this.embarcaciones.update((lista) =>
          actual ? lista.map((x) => (x.id === e.id ? e : x)) : [e, ...lista],
        );
        this.toast.exito(
          actual
            ? `Ficha de "${e.nombre}" corregida: vuelve a verificación.`
            : `Embarcación "${e.nombre}" registrada. Adjunta sus documentos.`,
        );
      },
      error: (err: HttpErrorResponse) => {
        this.guardando.set(false);
        this.toast.error(mensajeError(err, 'No se pudo guardar la ficha.'));
      },
    });
  }

  elegirArchivo(embarcacionId: number, evento: Event): void {
    const archivo = (evento.target as HTMLInputElement).files?.[0] ?? null;
    const error = archivoValido(archivo);
    if (error) {
      this.toast.advertencia(error);
      (evento.target as HTMLInputElement).value = '';
      this.archivos.delete(embarcacionId);
      return;
    }
    this.archivos.set(embarcacionId, archivo as File);
  }

  subir(e: Embarcacion, input: HTMLInputElement, tipoDocumento: string): void {
    if (!this.tiposDocumento.some((t) => t.codigo === tipoDocumento)) {
      this.toast.advertencia('Selecciona el tipo de documento.');
      return;
    }
    const archivo = this.archivos.get(e.id) ?? null;
    const error = archivoValido(archivo);
    if (error) {
      this.toast.advertencia(error);
      return;
    }
    this.subiendo.set(e.id);
    this.embarcacionService.subirDocumento(e.id, tipoDocumento as TipoDocumento, archivo as File).subscribe({
      next: (doc) => {
        this.subiendo.set(null);
        input.value = '';
        this.archivos.delete(e.id);
        this.embarcaciones.update((lista) =>
          lista.map((x) => (x.id === e.id ? { ...x, documentos: [...x.documentos, doc] } : x)),
        );
        this.toast.exito(`Documento "${doc.nombreArchivo}" cargado.`);
      },
      error: (err: HttpErrorResponse) => {
        this.subiendo.set(null);
        this.toast.error(mensajeError(err, 'No se pudo cargar el documento.'));
      },
    });
  }

  ver(documentoId: number): void {
    this.embarcacionService.descargarDocumento(documentoId).subscribe({
      next: (blob) => abrirArchivo(blob),
      error: () => this.toast.error('No se pudo abrir el documento.'),
    });
  }

  /** Constancia de asignación de rada (HU13) para el socio. */
  constancia(e: Embarcacion): void {
    const ventana = window.open('', '_blank', 'width=800,height=900');
    if (!ventana) {
      this.toast.advertencia('Permite las ventanas emergentes para ver la constancia.');
      return;
    }
    const socio = this.auth.usuario();
    const fecha = (iso: string | null) => (iso ? new Date(iso).toLocaleString('es-PE') : '-');
    ventana.document.write(`<!DOCTYPE html><html lang="es"><head><meta charset="utf-8">
      <title>Constancia de asignación de rada ${escaparHtml(e.radaCodigo)}</title>
      <style>
        body{font-family:'Segoe UI',Arial,sans-serif;color:#243746;max-width:720px;margin:40px auto;padding:0 24px}
        h1{font-size:1.3rem;color:#0b2d42;border-bottom:3px solid #c9a24d;padding-bottom:8px}
        table{width:100%;border-collapse:collapse;margin:16px 0}td{padding:8px;border-bottom:1px solid #e3e9ee}
        td:first-child{font-weight:600;width:40%}.pie{margin-top:40px;font-size:.85rem;color:#647783}
        button{margin-top:24px;padding:8px 16px;background:#0b2d42;color:#fff;border:0;border-radius:6px;cursor:pointer}
        @media print{button{display:none}}
      </style></head><body>
      <p style="letter-spacing:2px;font-size:.75rem">CLUB NÁUTICO POSEIDÓN DEL PERÚ</p>
      <h1>Constancia de asignación de rada</h1>
      <p>Se deja constancia de que la embarcación del socio indicado tiene asignado el siguiente lugar de amarre:</p>
      <table>
        <tr><td>Socio</td><td>${escaparHtml(socio?.nombres)}</td></tr>
        <tr><td>Embarcación</td><td>${escaparHtml(e.nombre)} (${escaparHtml(this.etiquetaTipo(e.tipo))})</td></tr>
        <tr><td>Matrícula</td><td>${escaparHtml(e.matricula)}</td></tr>
        <tr><td>Medidas (eslora × manga × calado)</td><td>${e.eslora} m × ${e.manga} m × ${e.calado} m</td></tr>
        <tr><td>Rada asignada</td><td><strong>${escaparHtml(e.radaCodigo)}</strong> · Muelle ${escaparHtml(e.radaMuelle)}</td></tr>
        <tr><td>Tarifa mensual de la rada</td><td>S/ ${Number(e.radaTarifaMensual ?? 0).toFixed(2)}</td></tr>
        <tr><td>Fecha de asignación</td><td>${fecha(e.fechaAsignacion)}</td></tr>
      </table>
      <p class="pie">Documento generado el ${new Date().toLocaleString('es-PE')} desde el sistema web del club.</p>
      <button onclick="window.print()">Imprimir / Guardar como PDF</button>
      </body></html>`);
    ventana.document.close();
  }

  tamano(bytes: number): string {
    return bytes > 1024 * 1024 ? `${(bytes / 1024 / 1024).toFixed(1)} MB` : `${Math.ceil(bytes / 1024)} KB`;
  }
}
