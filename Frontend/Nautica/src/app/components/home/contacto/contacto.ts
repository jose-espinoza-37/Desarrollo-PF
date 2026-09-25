import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

@Component({
  imports: [CommonModule, ReactiveFormsModule],
  selector: 'app-contacto',
  standalone: true,
  styleUrl: './contacto.scss',
  templateUrl: './contacto.html',
})
export class Contacto {
  formulario: FormGroup;

  constructor(private fb: FormBuilder) {
    this.formulario = this.fb.group({
      nombre: ['', Validators.required],
      correo: ['', [Validators.required, Validators.email]],
      mensaje: ['', Validators.required],
    });
  }

  enviar(): void {
    if (this.formulario.valid) {
      console.log('Formulario de contacto:', this.formulario.value);

      alert('Gracias por tu mensaje, te contactaremos pronto.');

      this.formulario.reset();
    }
  }
}