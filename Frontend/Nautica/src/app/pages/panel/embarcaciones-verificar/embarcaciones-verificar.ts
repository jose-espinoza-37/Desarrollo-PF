import { Component, OnInit, TemplateRef, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap/modal';
import { NgxPaginationModule } from 'ngx-pagination';

import { CatalogoService } from '../../../core/services/catalogo.service';
import { EmbarcacionService } from '../../../core/services/embarcacion.service';
import { ToastService } from '../../../core/services/toast.service';
import { Embarcacion } from '../../../core/models/embarcacion.models';
import { REGEX } from '../../../shared/validators/regex';
import { CLASE_ESTADO_EMBARCACION, abrirArchivo, mensajeError } from '../../../shared/utils/solicitud-ui';

type Pestana = 'PENDIENTE' | 'OBSERVADA' | 'VERIFICADA';

/** HU12: Servicios Navieros contrasta la ficha con el registro de Capitanías. */
@Component({
  selector: 'app-embarcaciones-verificar',
  standalone: true,
  imports: [DatePipe, DecimalPipe, ReactiveFormsModule, NgxPaginationModule],
  templateUrl: './embarcaciones-verificar.html',
  styleUrl: '../solicitudes.scss',
})
export class EmbarcacionesVerificar implements OnInit {
  private readonly embarcacionService = inject(EmbarcacionService);
  private readonly catalogo = inject(CatalogoService);
  private readonly toast = inject(ToastService);
  private readonly modal = inject(NgbModal);

  readonly etiquetaTipo = (c: string) => this.catalogo.etiquetaTipoEmbarcacion(c);
  readonly etiquetaEstado = (c: string) => this.catalogo.etiquetaEstadoEmbarcacion(c);
  readonly claseEstado = (c: string) => CLASE_ESTADO_EMBARCACION[c] ?? 'text-bg-light';

  readonly todas = signal<Embarcacion[]>([]);
  readonly cargado = signal(false);
  readonly pestana = signal<Pestana>('PENDIENTE');
  readonly pagina = signal(1);
  readonly seleccionada = signal<Embarcacion | null>(null);
  readonly guardando = signal(false);
  private modalRef: NgbModalRef | null = null;

  readonly lista = computed(() => this.todas().filter((e) => e.estado === this.pestana()));
  readonly conteo = computed(() => ({
    PENDIENTE: this.todas().filter((e) => e.estado === 'PENDIENTE').length,
    OBSERVADA: this.todas().filter((e) => e.estado === 'OBSERVADA').length,
    VERIFICADA: this.todas().filter((e) => e.estado === 'VERIFICADA').length,
  }));

  readonly form = new FormGroup({
    resultado: new FormControl<'VERIFICADA' | 'OBSERVADA' | ''>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^(VERIFICADA|OBSERVADA)$/)],
    }),
    observaciones: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
    contrastado: new FormControl(false, { nonNullable: true, validators: [Validators.requiredTrue] }),
  });

  constructor() {
    // Si la ficha se observa, las observaciones pasan a ser obligatorias (para que el socio subsane)
    this.form.controls.resultado.valueChanges.subscribe((r) => {
      const obs = this.form.controls.observaciones;
      obs.setValidators(
        r === 'OBSERVADA'
          ? [Validators.required, Validators.minLength(10), Validators.maxLength(500), Validators.pattern(REGEX.TEXTO)]
          : [Validators.maxLength(500), Validators.pattern(/^[^<>{}]*$/)],
      );
      obs.updateValueAndValidity();
    });
  }

  ngOnInit(): void {
    this.catalogo.cargar();
    this.cargar();
  }

  cargar(): void {
    this.embarcacionService.listar().subscribe({
      next: (lista) => {
        this.todas.set(lista);
        this.cargado.set(true);
      },
      error: (err: HttpErrorResponse) => this.toast.error(mensajeError(err)),
    });
  }

  cambiarPestana(p: Pestana): void {
    this.pestana.set(p);
    this.pagina.set(1);
  }

  abrir(e: Embarcacion, plantilla: TemplateRef<unknown>): void {
    this.seleccionada.set(e);
    this.form.reset({ resultado: '', observaciones: '', contrastado: false });
    this.modalRef = this.modal.open(plantilla, { size: 'lg', centered: true, backdrop: 'static' });
  }

  ver(documentoId: number): void {
    this.embarcacionService.descargarDocumento(documentoId).subscribe({
      next: (blob) => abrirArchivo(blob),
      error: () => this.toast.error('No se pudo abrir el documento.'),
    });
  }

  invalido(campo: 'resultado' | 'observaciones' | 'contrastado'): boolean {
    const c = this.form.controls[campo];
    return c.invalid && (c.touched || c.dirty);
  }

  guardar(): void {
    const e = this.seleccionada();
    if (!e) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.guardando.set(true);
    this.embarcacionService
      .verificar(e.id, { resultado: v.resultado as 'VERIFICADA' | 'OBSERVADA', observaciones: v.observaciones.trim() })
      .subscribe({
        next: (r) => {
          this.guardando.set(false);
          this.modalRef?.close();
          this.todas.update((lista) => lista.map((x) => (x.id === r.id ? r : x)));
          if (r.estado === 'VERIFICADA') {
            this.toast.exito(`"${r.nombre}" verificada: ya puede asignarse a una rada.`);
          } else {
            this.toast.advertencia(`"${r.nombre}" observada: el socio verá la observación para subsanarla.`);
          }
        },
        error: (err: HttpErrorResponse) => {
          this.guardando.set(false);
          this.toast.error(mensajeError(err, 'No se pudo registrar la verificación.'));
        },
      });
  }
}
