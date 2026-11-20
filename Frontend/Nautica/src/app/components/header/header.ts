import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  desplazarA(event: Event): void {
    const enlace = event.currentTarget as HTMLAnchorElement;
    const destinoId = enlace.getAttribute('href');

    // Solo procesamos enlaces internos, como #servicios.
    if (!destinoId || !destinoId.startsWith('#')) {
      return;
    }

    const destino = document.querySelector(destinoId);

    // Si la sección no está en esta página (ej. estamos en /login),
    // volvemos al Home y el router desplaza hasta la sección.
    if (!destino) {
      event.preventDefault();
      this.router.navigate(['/'], { fragment: destinoId.substring(1) });
      return;
    }

    event.preventDefault();

    const inicio = window.scrollY;
    const final =
      destino.getBoundingClientRect().top + window.scrollY;

    const duracion = 1800;
    const tiempoInicio = performance.now();

    // Actualizamos la dirección de la página sin provocar un salto.
    history.pushState(null, '', destinoId);

    const desplazar = (tiempoActual: number): void => {
      const progreso = Math.min(
        (tiempoActual - tiempoInicio) / duracion,
        1
      );

      // Aceleración y desaceleración suaves.
      const curva =
        progreso < 0.5
          ? 4 * progreso ** 3
          : 1 - Math.pow(-2 * progreso + 2, 3) / 2;

      window.scrollTo(
        0,
        inicio + (final - inicio) * curva
      );

      if (progreso < 1) {
        requestAnimationFrame(desplazar);
      }
    };

    requestAnimationFrame(desplazar);
  }
}