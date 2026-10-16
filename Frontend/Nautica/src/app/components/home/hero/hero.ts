
import { Component } from '@angular/core';

@Component({
  selector: 'app-hero',
  standalone: true,
  templateUrl: './hero.html',
  styleUrl: './hero.scss',
})
export class Hero {
  desplazarA(event: Event): void {
    const enlace = event.currentTarget as HTMLAnchorElement;
    const destinoId = enlace.getAttribute('href');

    if (!destinoId || !destinoId.startsWith('#')) {
      return;
    }

    const destino = document.querySelector(destinoId);

    if (!destino) {
      return;
    }

    event.preventDefault();

    const inicio = window.scrollY;
    const final =
      destino.getBoundingClientRect().top + window.scrollY;

    const duracion = 1800;
    const tiempoInicio = performance.now();

    history.pushState(null, '', destinoId);

    const desplazar = (tiempoActual: number): void => {
      const progreso = Math.min(
        (tiempoActual - tiempoInicio) / duracion,
        1
      );

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
