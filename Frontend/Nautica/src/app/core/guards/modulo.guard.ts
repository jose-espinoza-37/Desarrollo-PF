import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';


export const moduloGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const toast = inject(ToastService);

  if (auth.tieneAcceso(state.url)) {
    return true;
  }

  toast.advertencia('Tu perfil no tiene acceso a esa opción.');
  return router.parseUrl(auth.rutaInicial());
};
