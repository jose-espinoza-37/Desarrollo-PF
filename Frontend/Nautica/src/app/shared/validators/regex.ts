import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';


export const REGEX = {

  CORREO: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,


  PASSWORD: /^(?=.*[A-Za-z])(?=.*\d)[^\s]{8,72}$/,


  NOMBRE: /^[A-Za-zÁÉÍÓÚáéíóúÑñÜü]+(?:[ '\-][A-Za-zÁÉÍÓÚáéíóúÑñÜü]+)*$/,


  DNI: /^\d{8}$/,


  CELULAR: /^9\d{8}$/,


  TEXTO: /^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñÜü\s.,;:¿?¡!()'"\-#/°]+$/,


  UUID: /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/,


  DIRECCION: /^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñÜü\s.,#°\-/]+$/,
} as const;


export function soloDigitos(control: AbstractControl | null): void {
  if (!control) return;
  const valor = String(control.value ?? '');
  const limpio = valor.replace(/\D/g, '');
  if (limpio !== valor) {
    control.setValue(limpio);
  }
}

export function edadMinima(edad: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const nacimiento = new Date(control.value + 'T00:00:00');
    if (isNaN(nacimiento.getTime())) return { fechaInvalida: true };
    const hoy = new Date();
    if (nacimiento > hoy) return { fechaFutura: true };
    let anios = hoy.getFullYear() - nacimiento.getFullYear();
    const m = hoy.getMonth() - nacimiento.getMonth();
    if (m < 0 || (m === 0 && hoy.getDate() < nacimiento.getDate())) anios--;
    return anios < edad ? { menorDeEdad: true } : null;
  };
}

export function camposIguales(campo: string, confirmacion: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const a = grupo.get(campo)?.value;
    const b = grupo.get(confirmacion)?.value;
    return a && b && a !== b ? { noCoinciden: true } : null;
  };
}
