import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { EMPTY, catchError, throwError } from 'rxjs';
import Swal from 'sweetalert2';

import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';


const RUTAS_PUBLICAS = ['/auth/login', '/auth/registro'];

let mostrandoSesionExpirada = false;


export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const toast = inject(ToastService);

  const esPublica = RUTAS_PUBLICAS.some((ruta) => req.url.includes(ruta));
  const token = esPublica ? null : auth.getToken();

  const peticion = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(peticion).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && !esPublica) {
        if (error.status === 401) {
          if (!mostrandoSesionExpirada) {
            mostrandoSesionExpirada = true;
            auth.logout(false);
            Swal.fire({
              icon: 'warning',
              title: 'Sesión expirada',
              text: 'Tu sesión ha caducado. Inicia sesión nuevamente.',
              confirmButtonText: 'Iniciar sesión',
              confirmButtonColor: '#0b2d42',
              allowOutsideClick: false,
            }).then(() => {
              mostrandoSesionExpirada = false;
              auth.logout(true);
            });
          }
          return EMPTY;
        }
        if (error.status === 403) {
          toast.advertencia('No tienes permisos para realizar esta acción.');
        }
      }
      return throwError(() => error);
    }),
  );
};
