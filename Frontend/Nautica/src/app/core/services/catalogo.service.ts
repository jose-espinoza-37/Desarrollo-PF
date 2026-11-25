import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { forkJoin } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ClasificacionCatalogo, EstadoCatalogo, ItemCatalogo } from '../models/catalogo.models';

/** Catálogos leídos de la BD (se piden una sola vez). */
@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/catalogos`;
  private solicitado = false;

  readonly estados = signal<EstadoCatalogo[]>([]);
  readonly clasificaciones = signal<ClasificacionCatalogo[]>([]);
  readonly tiposEmbarcacion = signal<ItemCatalogo[]>([]);
  readonly estadosEmbarcacion = signal<ItemCatalogo[]>([]);

  cargar(): void {
    if (this.solicitado) return;
    this.solicitado = true;
    forkJoin({
      estados: this.http.get<EstadoCatalogo[]>(`${this.url}/estados`),
      clasificaciones: this.http.get<ClasificacionCatalogo[]>(`${this.url}/clasificaciones`),
      tiposEmbarcacion: this.http.get<ItemCatalogo[]>(`${this.url}/tipos-embarcacion`),
      estadosEmbarcacion: this.http.get<ItemCatalogo[]>(`${this.url}/estados-embarcacion`),
    }).subscribe({
      next: (r) => {
        this.estados.set(r.estados);
        this.clasificaciones.set(r.clasificaciones);
        this.tiposEmbarcacion.set(r.tiposEmbarcacion);
        this.estadosEmbarcacion.set(r.estadosEmbarcacion);
      },
      error: () => (this.solicitado = false),
    });
  }

  etiquetaEstado(codigo: string | null | undefined): string {
    return this.estados().find((e) => e.codigo === codigo)?.descripcion ?? codigo ?? '';
  }

  etiquetaClasificacion(codigo: string | null | undefined): string {
    return this.clasificaciones().find((c) => c.codigo === codigo)?.descripcion ?? codigo ?? '';
  }

  etiquetaTipoEmbarcacion(codigo: string | null | undefined): string {
    return this.tiposEmbarcacion().find((t) => t.codigo === codigo)?.descripcion ?? codigo ?? '';
  }

  etiquetaEstadoEmbarcacion(codigo: string | null | undefined): string {
    return this.estadosEmbarcacion().find((e) => e.codigo === codigo)?.descripcion ?? codigo ?? '';
  }
}
