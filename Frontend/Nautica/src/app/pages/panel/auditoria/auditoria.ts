import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgxPaginationModule } from 'ngx-pagination';

import { AuditoriaService } from '../../../core/services/auditoria.service';
import { ToastService } from '../../../core/services/toast.service';
import { EventoAuditoria } from '../../../core/models/auditoria.models';
import { mensajeError } from '../../../shared/utils/solicitud-ui';

/** Bitácora de auditoría: quién hizo qué, cuándo, desde qué IP y qué campos cambió. */
@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule, NgxPaginationModule],
  templateUrl: './auditoria.html',
  styleUrl: '../solicitudes.scss',
})
export class Auditoria implements OnInit {
  private readonly auditoriaService = inject(AuditoriaService);
  private readonly toast = inject(ToastService);

  readonly operaciones = ['TODAS', 'INSERT', 'UPDATE', 'DELETE', 'LOGIN'];
  readonly eventos = signal<EventoAuditoria[]>([]);
  readonly cargado = signal(false);
  readonly operacion = signal('TODAS');
  readonly filtro = signal('');
  readonly pagina = signal(1);
  readonly abierto = signal<number | null>(null);

  readonly filtroControl = new FormControl('', {
    nonNullable: true,
    validators: [Validators.maxLength(60), Validators.pattern(/^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\s.@_-]*$/)],
  });

  readonly filtrados = computed(() => {
    const texto = this.filtro().trim().toLowerCase();
    return this.eventos().filter(
      (e) =>
        (this.operacion() === 'TODAS' || e.operacion === this.operacion()) &&
        (!texto || `${e.usuario} ${e.modulo} ${e.nombreTabla} ${e.endpoint}`.toLowerCase().includes(texto)),
    );
  });
  readonly fallidos = computed(() => this.eventos().filter((e) => e.resultado === 'FAILURE').length);

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
    this.auditoriaService.eventos().subscribe({
      next: (lista) => {
        this.eventos.set(lista);
        this.cargado.set(true);
      },
      error: (err: HttpErrorResponse) => this.toast.error(mensajeError(err, 'No se pudo cargar la auditoría.')),
    });
  }

  elegirOperacion(op: string): void {
    this.operacion.set(op);
    this.pagina.set(1);
  }

  alternar(id: number): void {
    this.abierto.update((actual) => (actual === id ? null : id));
  }

  claseOperacion(op: string): string {
    return (
      { INSERT: 'text-bg-success', UPDATE: 'text-bg-info', DELETE: 'text-bg-danger', LOGIN: 'text-bg-secondary' }[op] ??
      'text-bg-light'
    );
  }
}
