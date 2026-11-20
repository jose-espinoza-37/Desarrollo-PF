import { Component, OnInit, TemplateRef, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap/modal';
import { NgxPaginationModule } from 'ngx-pagination';

import { SolicitudService } from '../../../core/services/solicitud.service';
import { ToastService } from '../../../core/services/toast.service';
import { Clasificacion, Solicitud } from '../../../core/models/solicitud.models';
import { REGEX } from '../../../shared/validators/regex';
import {
  CLASIFICACIONES,
  antecedentesDe,
  claseEstado,
  codigoCorto,
  etiquetaEstado,
  mensajeError,
} from '../../../shared/utils/solicitud-ui';


@Component({
  selector: 'app-solicitudes-verificar',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule, NgxPaginationModule],
  templateUrl: './solicitudes-verificar.html',
  styleUrl: '../solicitudes.scss',
})
export class SolicitudesVerificar implements OnInit {
  private readonly solicitudService = inject(SolicitudService);
  private readonly toast = inject(ToastService);
  private readonly modal = inject(NgbModal);

  readonly CLASIFICACIONES = CLASIFICACIONES;
  readonly opciones = Object.entries(CLASIFICACIONES) as [Clasificacion, (typeof CLASIFICACIONES)[string]][];

  readonly seleccionada = signal<Solicitud | null>(null);
  readonly etiquetaEstado = etiquetaEstado;
  readonly codigoCorto = codigoCorto;
  readonly claseEstado = claseEstado;


  private readonly todas = signal<Solicitud[]>([]);

  readonly solicitudes = computed(() =>
    this.todas()
      .filter((s) => s.estado === 'PENDIENTE')
      .sort((a, b) => a.fechaSolicitud.localeCompare(b.fechaSolicitud)),
  );

  readonly antecedentes = computed(() => {
    const s = this.seleccionada();
    return s ? antecedentesDe(this.todas(), s) : [];
  });
  readonly cargado = signal(false);
  readonly filtro = signal('');
  readonly pagina = signal(1);
  readonly guardando = signal(false);
  private modalRef: NgbModalRef | null = null;

  readonly filtroControl = new FormControl('', {
    nonNullable: true,
    validators: [Validators.maxLength(50), Validators.pattern(/^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\s-]*$/)],
  });


  readonly filtradas = computed(() => {
    const texto = this.filtro().trim().toLowerCase();
    if (!texto) return this.solicitudes();
    return this.solicitudes().filter((s) =>
      `${s.nombres} ${s.apellidos} ${s.dni} ${s.codigo}`.toLowerCase().includes(texto),
    );
  });

  readonly duplicadas = computed(() => this.solicitudes().filter((s) => s.dniDuplicado).length);

  readonly form = new FormGroup({
    clasificacion: new FormControl<Clasificacion | ''>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^(PAGADOR|PAGADOR_ESPORADICO|RENUENTE)$/)],
    }),
    resultadoVerificacion: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(10), Validators.maxLength(500), Validators.pattern(REGEX.TEXTO)],
    }),
  });

  constructor() {
    this.filtroControl.valueChanges.subscribe((v) => {
      if (this.filtroControl.valid) {
        this.filtro.set(v);
        this.pagina.set(1);
      }
    });
  }

  ngOnInit(): void {
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

  abrir(solicitud: Solicitud, plantilla: TemplateRef<unknown>): void {
    this.seleccionada.set(solicitud);
    this.form.reset({ clasificacion: '', resultadoVerificacion: '' });
    this.modalRef = this.modal.open(plantilla, { size: 'lg', centered: true, backdrop: 'static' });
  }

  invalido(campo: 'clasificacion' | 'resultadoVerificacion'): boolean {
    const c = this.form.controls[campo];
    return c.invalid && (c.touched || c.dirty);
  }

  guardar(): void {
    const s = this.seleccionada();
    if (!s) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const datos = this.form.getRawValue();
    this.guardando.set(true);
    this.solicitudService
      .verificar(s.id, {
        clasificacion: datos.clasificacion as Clasificacion,
        resultadoVerificacion: datos.resultadoVerificacion.trim(),
      })
      .subscribe({
        next: (r) => {
          this.guardando.set(false);
          this.modalRef?.close();
          if (r.estado === 'VERIFICADA') {
            this.toast.exito(`Solicitud ${codigoCorto(r.codigo)} verificada: pasa a aprobación del Jefe.`);
          } else {
            this.toast.advertencia(`Solicitud ${codigoCorto(r.codigo)} marcada como NO APTA (no es pagador).`);
          }
          this.todas.update((lista) => lista.map((x) => (x.id === r.id ? r : x)));
        },
        error: (err: HttpErrorResponse) => {
          this.guardando.set(false);
          this.toast.error(mensajeError(err, 'No se pudo registrar la verificación.'));
        },
      });
  }

  edad(fecha: string): number {
    const n = new Date(fecha + 'T00:00:00');
    const hoy = new Date();
    let anios = hoy.getFullYear() - n.getFullYear();
    if (hoy.getMonth() < n.getMonth() || (hoy.getMonth() === n.getMonth() && hoy.getDate() < n.getDate())) anios--;
    return anios;
  }
}
