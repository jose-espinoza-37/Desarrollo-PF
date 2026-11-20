import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { ToastService } from '../../../core/services/toast.service';
import { REGEX } from '../../../shared/validators/regex';

@Component({
  imports: [CommonModule, ReactiveFormsModule],
  selector: 'app-contacto',
  standalone: true,
  styleUrl: './contacto.scss',
  templateUrl: './contacto.html',
})
export class Contacto {
  private readonly toast = inject(ToastService);
  formulario: FormGroup;

  constructor(private fb: FormBuilder) {
    this.formulario = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(80), Validators.pattern(REGEX.NOMBRE)]],
      correo: ['', [Validators.required, Validators.maxLength(150), Validators.pattern(REGEX.CORREO)]],
      mensaje: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500), Validators.pattern(REGEX.TEXTO)]],
    });
  }

  invalido(campo: string): boolean {
    const control = this.formulario.get(campo);
    return !!control && control.invalid && control.touched;
  }

  error(campo: string, tipo: string): boolean {
    return !!this.formulario.get(campo)?.hasError(tipo);
  }

  enviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.toast.exito('Gracias por tu mensaje, te contactaremos pronto.');
    this.formulario.reset();
  }
}
