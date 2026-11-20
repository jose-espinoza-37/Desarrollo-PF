import { Injectable, signal } from '@angular/core';

export type TipoToast = 'success' | 'danger' | 'warning' | 'info';

export interface Toast {
  id: number;
  mensaje: string;
  tipo: TipoToast;
  duracion: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private siguienteId = 1;
  readonly toasts = signal<Toast[]>([]);

  mostrar(mensaje: string, tipo: TipoToast = 'info', duracion = 3500): void {
    const toast: Toast = { id: this.siguienteId++, mensaje, tipo, duracion };
    this.toasts.update((lista) => [...lista, toast]);
  }

  exito(mensaje: string): void { this.mostrar(mensaje, 'success'); }
  error(mensaje: string): void { this.mostrar(mensaje, 'danger', 5000); }
  advertencia(mensaje: string): void { this.mostrar(mensaje, 'warning', 4500); }
  info(mensaje: string): void { this.mostrar(mensaje, 'info'); }

  limpiar(): void {
    this.toasts.set([]);
  }

  quitar(id: number): void {
    this.toasts.update((lista) => lista.filter((t) => t.id !== id));
  }
}
