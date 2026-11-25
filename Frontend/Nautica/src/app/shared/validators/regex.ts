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

  /** Nombre de la nave: letras, números, espacios y guiones. */
  NOMBRE_NAVE: /^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñÜü]+(?:[ \-][A-Za-z0-9ÁÉÍÓÚáéíóúÑñÜü]+)*$/,

  /** Matrícula: dos letras, 4 a 6 dígitos y dos letras (ej. CO-12345-RE). */
  MATRICULA: /^[A-Z]{2}-\d{4,6}-[A-Z]{2}$/,

  /** Medida en metros con hasta 2 decimales (eslora, manga, calado). */
  MEDIDA: /^\d{1,2}(\.\d{1,2})?$/,

  /** Año de 4 dígitos. */
  ANIO: /^(19[5-9]\d|20\d{2})$/,
} as const;

/** Archivo PDF, JPG o PNG de hasta 5 MB (documentos de la nave). */
export function archivoValido(archivo: File | null): string | null {
  if (!archivo) return 'Selecciona un archivo.';
  if (!['application/pdf', 'image/jpeg', 'image/png'].includes(archivo.type)) return 'Solo se aceptan archivos PDF, JPG o PNG.';
  if (archivo.size > 5 * 1024 * 1024) return 'El archivo no puede superar los 5 MB.';
  return null;
}


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
