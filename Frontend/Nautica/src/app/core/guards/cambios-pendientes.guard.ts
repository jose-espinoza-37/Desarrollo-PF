import { CanDeactivateFn } from '@angular/router';
import Swal from 'sweetalert2';

/** Pantallas con un formulario que se puede perder al salir. */
export interface ConCambiosPendientes {
  tieneCambiosPendientes(): boolean;
}

/**
 * canDeactivate: si el formulario tiene datos sin enviar,
 * pide confirmación antes de salir de la pantalla.
 */
export const cambiosPendientesGuard: CanDeactivateFn<ConCambiosPendientes> = async (componente) => {
  if (!componente.tieneCambiosPendientes()) {
    return true;
  }
  const respuesta = await Swal.fire({
    icon: 'warning',
    title: '¿Salir sin enviar tu solicitud?',
    text: 'Los datos que ingresaste en el formulario se perderán.',
    showCancelButton: true,
    confirmButtonText: 'Sí, salir',
    cancelButtonText: 'Seguir completando',
    confirmButtonColor: '#0b2d42',
    cancelButtonColor: '#647783',
  });
  return respuesta.isConfirmed;
};
