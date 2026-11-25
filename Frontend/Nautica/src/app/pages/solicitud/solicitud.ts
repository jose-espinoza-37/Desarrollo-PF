import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import Swal from 'sweetalert2';

import { SolicitudService } from '../../core/services/solicitud.service';
import { ToastService } from '../../core/services/toast.service';
import { Seguimiento } from '../../core/models/solicitud.models';
import { REGEX, camposIguales, edadMinima, soloDigitos } from '../../shared/validators/regex';
import { CatalogoService } from '../../core/services/catalogo.service';
import { ConCambiosPendientes } from '../../core/guards/cambios-pendientes.guard';
import { claseEstado, mensajeError } from '../../shared/utils/solicitud-ui';


@Component({
  selector: 'app-solicitud',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './solicitud.html',
  styleUrl: './solicitud.scss',
})
export class SolicitudPage implements ConCambiosPendientes {
  private readonly solicitudService = inject(SolicitudService);
  private readonly toast = inject(ToastService);

  readonly soloDigitos = soloDigitos;
  private readonly catalogo = inject(CatalogoService);
  readonly etiquetaEstado = (codigo: string) => this.catalogo.etiquetaEstado(codigo);
  readonly claseEstado = claseEstado;

  readonly enviando = signal(false);
  readonly verPassword = signal(false);
  readonly seguimiento = signal<Seguimiento | null>(null);
  readonly hoy = new Date().toISOString().substring(0, 10);

  readonly form = new FormGroup(
    {
      nombres: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(2), Validators.maxLength(80), Validators.pattern(REGEX.NOMBRE)],
      }),
      apellidos: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(2), Validators.maxLength(80), Validators.pattern(REGEX.NOMBRE)],
      }),
      dni: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(REGEX.DNI)],
      }),
      fechaNacimiento: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, edadMinima(18)],
      }),
      correo: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.maxLength(150), Validators.pattern(REGEX.CORREO)],
      }),
      celular: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(REGEX.CELULAR)],
      }),
      direccion: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(5), Validators.maxLength(200), Validators.pattern(REGEX.DIRECCION)],
      }),
      poseeEmbarcacion: new FormControl(false, { nonNullable: true }),
      cantidadEmbarcaciones: new FormControl(
        { value: 1, disabled: true },
        { nonNullable: true, validators: [Validators.required, Validators.min(1), Validators.max(20), Validators.pattern(/^\d+$/)] },
      ),
      password: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(REGEX.PASSWORD)],
      }),
      confirmarPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      aceptaPoliticas: new FormControl(false, { nonNullable: true, validators: [Validators.requiredTrue] }),
    },
    { validators: camposIguales('password', 'confirmarPassword') },
  );

  readonly formSeguimiento = new FormGroup({
    codigo: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(REGEX.UUID)],
    }),
  });

  /** Usado por cambiosPendientesGuard: hay datos escritos y no se está enviando. */
  tieneCambiosPendientes(): boolean {
    return this.form.dirty && !this.enviando();
  }

  constructor() {
    this.catalogo.cargar();

    this.form.controls.poseeEmbarcacion.valueChanges.subscribe((posee) => {
      const cantidad = this.form.controls.cantidadEmbarcaciones;
      if (posee) {
        cantidad.enable();
      } else {
        cantidad.setValue(1);
        cantidad.disable();
      }
    });
  }

  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  error(campo: string, tipo: string): boolean {
    return !!this.form.get(campo)?.hasError(tipo);
  }

  get passwordsNoCoinciden(): boolean {
    const c = this.form.controls.confirmarPassword;
    return this.form.hasError('noCoinciden') && (c.touched || c.dirty);
  }

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.advertencia('Revisa los campos marcados en rojo.');
      return;
    }

    const v = this.form.getRawValue();
    this.enviando.set(true);
    this.solicitudService
      .registrar({
        nombres: v.nombres.trim(),
        apellidos: v.apellidos.trim(),
        dni: v.dni,
        fechaNacimiento: v.fechaNacimiento,
        correo: v.correo.trim().toLowerCase(),
        celular: v.celular,
        direccion: v.direccion.trim(),
        poseeEmbarcacion: v.poseeEmbarcacion,
        cantidadEmbarcaciones: v.poseeEmbarcacion ? Number(v.cantidadEmbarcaciones) : 0,
        password: v.password,
      })
      .subscribe({
        next: (r) => {
          this.enviando.set(false);
          this.form.reset();
          // Constancia con número de seguimiento (criterio de aceptación de la HU08)
          Swal.fire({
            icon: 'success',
            title: '¡Solicitud registrada!',
            html: `
              <p>Tu solicitud fue enviada al área de Atención al Cliente.</p>
              <p class="mb-1">Tu código de seguimiento es:</p>
              <p style="font-family:monospace;font-size:1rem;font-weight:700;color:#0b2d42;word-break:break-all;
                        background:#f5f7f8;border-radius:8px;padding:10px">${r.codigo}</p>
              <p class="small text-muted">Guárdalo para consultar el estado de tu solicitud.</p>`,
            showDenyButton: true,
            denyButtonText: 'Copiar código',
            denyButtonColor: '#176b87',
            preDeny: () => {
              navigator.clipboard?.writeText(r.codigo);
              Swal.showValidationMessage('Código copiado');
              return false;
            },
            confirmButtonText: 'Entendido',
            confirmButtonColor: '#0b2d42',
          });
          this.formSeguimiento.setValue({ codigo: r.codigo });
        },
        error: (err: HttpErrorResponse) => {
          this.enviando.set(false);
          this.toast.error(mensajeError(err, 'No se pudo registrar la solicitud.'));
        },
      });
  }

  consultar(): void {
    if (this.formSeguimiento.invalid) {
      this.formSeguimiento.markAllAsTouched();
      return;
    }
    this.seguimiento.set(null);
    this.solicitudService.seguimiento(this.formSeguimiento.getRawValue().codigo.trim().toLowerCase()).subscribe({
      next: (s) => this.seguimiento.set(s),
      error: (err: HttpErrorResponse) => this.toast.error(mensajeError(err, 'No se encontró la solicitud.')),
    });
  }
}
