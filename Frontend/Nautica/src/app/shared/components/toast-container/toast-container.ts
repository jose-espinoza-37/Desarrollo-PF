import { Component, inject } from '@angular/core';
import { NgbToast } from '@ng-bootstrap/ng-bootstrap/toast';

import { ToastService } from '../../../core/services/toast.service';

const ICONOS: Record<string, string> = {
  success: 'bi-check-circle-fill',
  danger: 'bi-x-octagon-fill',
  warning: 'bi-exclamation-triangle-fill',
  info: 'bi-info-circle-fill',
};

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [NgbToast],
  template: `
    <div class="toast-zona" aria-live="polite" aria-atomic="true">
      @for (toast of toastService.toasts(); track toast.id) {
        <ngb-toast
          [class]="'text-bg-' + toast.tipo + ' border-0 shadow'"
          [autohide]="true"
          [delay]="toast.duracion"
          (hidden)="toastService.quitar(toast.id)"
        >
          <div class="d-flex align-items-center gap-2">
            <i class="bi {{ icono(toast.tipo) }}"></i>
            <span class="flex-grow-1">{{ toast.mensaje }}</span>
            <button
              type="button"
              class="btn-close btn-close-white ms-2"
              aria-label="Cerrar"
              (click)="toastService.quitar(toast.id)"
            ></button>
          </div>
        </ngb-toast>
      }
    </div>
  `,
  styles: `
    .toast-zona {
      position: fixed;
      bottom: 1rem;
      right: 1rem;
      z-index: 2000;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      max-width: 360px;
    }
  `,
})
export class ToastContainer {
  readonly toastService = inject(ToastService);

  icono(tipo: string): string {
    return ICONOS[tipo] ?? ICONOS['info'];
  }
}
