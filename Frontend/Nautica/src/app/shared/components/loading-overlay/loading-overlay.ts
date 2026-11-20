import { Component, inject } from '@angular/core';

import { LoadingService } from '../../../core/services/loading.service';


@Component({
  selector: 'app-loading-overlay',
  standalone: true,
  template: `
    @if (loading.cargando()) {
      <div class="overlay" role="status" aria-label="Cargando">
        <div class="spinner-border text-light" style="width: 3rem; height: 3rem"></div>
        <span class="mt-3 text-light fw-semibold">Cargando...</span>
      </div>
    }
  `,
  styles: `
    .overlay {
      position: fixed;
      inset: 0;
      z-index: 1990;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: rgba(11, 45, 66, 0.55);
      backdrop-filter: blur(2px);
    }
  `,
})
export class LoadingOverlay {
  readonly loading = inject(LoadingService);
}
