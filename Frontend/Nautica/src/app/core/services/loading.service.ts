import { Injectable, computed, signal } from '@angular/core';


@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly peticionesActivas = signal(0);
  readonly cargando = computed(() => this.peticionesActivas() > 0);

  mostrar(): void {
    this.peticionesActivas.update((n) => n + 1);
  }

  ocultar(): void {
    this.peticionesActivas.update((n) => Math.max(0, n - 1));
  }
}
