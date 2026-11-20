import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { REGEX } from '../../shared/validators/regex';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly verPassword = signal(false);
  readonly enviando = signal(false);
  readonly errorServidor = signal<string | null>(null);

  readonly form = new FormGroup({
    correo: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(150), Validators.pattern(REGEX.CORREO)],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(8), Validators.maxLength(72)],
    }),
  });

  invalido(campo: 'correo' | 'password'): boolean {
    const control = this.form.controls[campo];
    return control.invalid && (control.touched || control.dirty);
  }

  ingresar(): void {
    this.errorServidor.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: (respuesta) => {
        this.enviando.set(false);
        this.toast.limpiar();
        this.toast.exito(`Bienvenido(a), ${respuesta.nombres}.`);
        const volver = this.route.snapshot.queryParamMap.get('volver');
        const destino = volver && this.auth.tieneAcceso(volver) ? volver : this.auth.rutaInicial();
        this.router.navigateByUrl(destino);
      },
      error: (err: HttpErrorResponse) => {
        this.enviando.set(false);
        const mensaje =
          err.status === 0
            ? 'No se pudo conectar con el servidor. Verifica que el backend esté activo.'
            : (err.error?.mensaje ?? 'El correo electrónico o la contraseña son incorrectos.');
        this.errorServidor.set(mensaje);
        this.toast.error(mensaje);
      },
    });
  }
}
