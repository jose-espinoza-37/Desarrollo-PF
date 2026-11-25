import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { NgxPaginationModule } from 'ngx-pagination';
import Swal from 'sweetalert2';

import { CatalogoService } from '../../../core/services/catalogo.service';
import { RadaService } from '../../../core/services/rada.service';
import { ToastService } from '../../../core/services/toast.service';
import { AsignacionRada, Embarcacion, Rada, RadaCompatible } from '../../../core/models/embarcacion.models';
import { REGEX } from '../../../shared/validators/regex';
import { escaparHtml, mensajeError } from '../../../shared/utils/solicitud-ui';

type Pestana = 'asignar' | 'historial';

/** HU13: asignar la rada según las medidas de la nave, liberar y ver el historial. */
@Component({
  selector: 'app-radas',
  standalone: true,
  imports: [DatePipe, DecimalPipe, NgxPaginationModule],
  templateUrl: './radas.html',
  styleUrls: ['../solicitudes.scss', './radas.scss'],
})
export class Radas implements OnInit {
  private readonly radaService = inject(RadaService);
  private readonly catalogo = inject(CatalogoService);
  private readonly toast = inject(ToastService);

  readonly etiquetaTipo = (c: string) => this.catalogo.etiquetaTipoEmbarcacion(c);

  readonly pestana = signal<Pestana>('asignar');
  readonly radas = signal<Rada[]>([]);
  readonly porAsignar = signal<Embarcacion[]>([]);
  readonly historial = signal<AsignacionRada[]>([]);
  readonly seleccionada = signal<Embarcacion | null>(null);
  readonly compatibles = signal<RadaCompatible[]>([]);
  readonly buscando = signal(false);
  readonly paginaHistorial = signal(1);

  readonly muelles = computed(() => {
    const grupos = new Map<string, Rada[]>();
    this.radas().forEach((r) => grupos.set(r.muelle, [...(grupos.get(r.muelle) ?? []), r]));
    return [...grupos.entries()].map(([muelle, lista]) => ({ muelle, lista }));
  });
  readonly ocupadas = computed(() => this.radas().filter((r) => r.estado === 'OCUPADA').length);
  readonly ocupacion = computed(() => (this.radas().length ? (100 * this.ocupadas()) / this.radas().length : 0));
  readonly idsCompatibles = computed(() => new Set(this.compatibles().map((c) => c.id)));

  ngOnInit(): void {
    this.catalogo.cargar();
    this.cargar();
  }

  cargar(): void {
    this.radaService.listar().subscribe({
      next: (r) => this.radas.set(r),
      error: (err: HttpErrorResponse) => this.toast.error(mensajeError(err)),
    });
    this.radaService.porAsignar().subscribe({ next: (e) => this.porAsignar.set(e) });
    this.radaService.historial().subscribe({ next: (h) => this.historial.set(h) });
  }

  /** Al elegir una nave se piden las radas compatibles (consulta SQL en el backend). */
  elegir(e: Embarcacion): void {
    this.seleccionada.set(e);
    this.compatibles.set([]);
    this.buscando.set(true);
    this.radaService.compatibles(e.id).subscribe({
      next: (lista) => {
        this.compatibles.set(lista);
        this.buscando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.buscando.set(false);
        this.toast.error(mensajeError(err));
      },
    });
  }

  async asignar(rada: RadaCompatible): Promise<void> {
    const e = this.seleccionada();
    if (!e) return;
    const r = await Swal.fire({
      icon: 'question',
      title: `¿Asignar la rada ${rada.codigo}?`,
      html: `<b>${escaparHtml(e.nombre)}</b> (${escaparHtml(e.matricula)}) de ${escaparHtml(e.socioNombre)}<br>
             Muelle ${escaparHtml(rada.muelle)} · aprovechamiento ${rada.aprovechamiento}% · S/ ${rada.tarifaMensual.toFixed(2)} al mes`,
      showCancelButton: true,
      confirmButtonText: 'Sí, asignar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#0b2d42',
    });
    if (!r.isConfirmed) return;
    this.radaService.asignar(rada.id, e.id).subscribe({
      next: () => {
        this.toast.exito(`Rada ${rada.codigo} asignada a "${e.nombre}". El socio ya puede ver su constancia.`);
        this.seleccionada.set(null);
        this.compatibles.set([]);
        this.cargar();
      },
      error: (err: HttpErrorResponse) => this.toast.error(mensajeError(err, 'No se pudo asignar la rada.')),
    });
  }

  async liberar(rada: Rada): Promise<void> {
    if (rada.estado !== 'OCUPADA') return;
    const r = await Swal.fire({
      icon: 'warning',
      title: `Liberar la rada ${rada.codigo}`,
      html: `Ocupada por <b>${escaparHtml(rada.embarcacionNombre)}</b> (${escaparHtml(rada.embarcacionMatricula)}).<br>
             Indica el motivo; quedará en el historial.`,
      input: 'textarea',
      inputPlaceholder: 'Ej. El socio trasladó su embarcación a otro club.',
      inputAttributes: { maxlength: '300' },
      showCancelButton: true,
      confirmButtonText: 'Liberar rada',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#e34948',
      inputValidator: (valor) => {
        const texto = (valor ?? '').trim();
        if (texto.length < 5) return 'El motivo debe tener al menos 5 caracteres.';
        if (!REGEX.TEXTO.test(texto)) return 'El motivo contiene caracteres no permitidos.';
        return null;
      },
    });
    if (!r.isConfirmed) return;
    this.radaService.liberar(rada.id, String(r.value).trim()).subscribe({
      next: () => {
        this.toast.info(`Rada ${rada.codigo} liberada.`);
        this.cargar();
      },
      error: (err: HttpErrorResponse) => this.toast.error(mensajeError(err, 'No se pudo liberar la rada.')),
    });
  }
}
