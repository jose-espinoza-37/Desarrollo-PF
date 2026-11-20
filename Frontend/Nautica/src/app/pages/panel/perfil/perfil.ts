import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';

import { AuthService } from '../../../core/services/auth.service';
import { PerfilUsuario } from '../../../core/models/auth.models';


@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [DatePipe],
  template: `
    <h2 class="titulo-pagina"><i class="bi bi-person-circle"></i> Mi perfil</h2>

    @if (perfil(); as p) {
      <div class="card tarjeta">
        <div class="card-body">
          <div class="row g-4">
            <div class="col-md-6">
              <div class="dato-label">Nombres</div>
              <div class="dato-valor">{{ p.nombres }}</div>
            </div>
            <div class="col-md-6">
              <div class="dato-label">Apellidos</div>
              <div class="dato-valor">{{ p.apellidos }}</div>
            </div>
            <div class="col-md-6">
              <div class="dato-label">Correo electrónico</div>
              <div class="dato-valor">{{ p.correo }}</div>
            </div>
            <div class="col-md-6">
              <div class="dato-label">Perfil</div>
              <div class="dato-valor"><span class="badge text-bg-primary">{{ p.rolNombre }}</span></div>
            </div>
            <div class="col-md-6">
              <div class="dato-label">Miembro desde</div>
              <div class="dato-valor">{{ p.fechaCreacion | date: 'dd/MM/yyyy HH:mm' }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="card tarjeta mt-4">
        <div class="card-body">
          <h3 class="h6 fw-bold mb-3">Opciones habilitadas para tu perfil</h3>
          <ul class="list-group list-group-flush">
            @for (m of auth.modulos(); track m.codigo) {
              <li class="list-group-item d-flex align-items-center gap-2">
                <i class="bi {{ m.icono }}"></i> {{ m.nombre }}
                <code class="ms-auto small">{{ m.ruta }}</code>
              </li>
            }
          </ul>
        </div>
      </div>
    }
  `,
  styles: `
    .titulo-pagina { color: var(--primary-color); font-weight: 750; font-size: 1.5rem; margin-bottom: 1.25rem; }
    .tarjeta { border: none; border-radius: 14px; box-shadow: var(--shadow-soft); }
    .dato-label { color: var(--text-muted); font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.5px; }
    .dato-valor { color: var(--text-dark); font-weight: 600; }
  `,
})
export class Perfil implements OnInit {
  readonly auth = inject(AuthService);
  readonly perfil = signal<PerfilUsuario | null>(null);

  ngOnInit(): void {
    this.auth.obtenerPerfil().subscribe((datos) => this.perfil.set(datos));
  }
}
