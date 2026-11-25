import { inject } from '@angular/core';
import { Routes } from '@angular/router';

import { Home } from './components/home/home';
import { Contacto } from './components/home/contacto/contacto';
import { PublicLayout } from './layouts/public-layout/public-layout';
import { PanelLayout } from './layouts/panel-layout/panel-layout';
import { Login } from './pages/login/login';
import { authGuard, invitadoGuard } from './core/guards/auth.guard';
import { moduloGuard } from './core/guards/modulo.guard';
import { cambiosPendientesGuard } from './core/guards/cambios-pendientes.guard';
import { AuthService } from './core/services/auth.service';

export const routes: Routes = [
  // ---------- Sitio público ----------
  {
    path: '',
    component: PublicLayout,
    children: [
      { path: '', component: Home },
      { path: 'contacto', component: Contacto },
      { path: 'login', component: Login, canActivate: [invitadoGuard] },
      {
        path: 'solicitud',
        canDeactivate: [cambiosPendientesGuard],
        loadComponent: () => import('./pages/solicitud/solicitud').then((m) => m.SolicitudPage),
      },
    ],
  },

  // ---------- Sistema interno (requiere sesión) ----------
  {
    path: 'panel',
    component: PanelLayout,
    canActivate: [authGuard],
    children: [
      // /panel → primera opción del menú del rol
      { path: '', pathMatch: 'full', redirectTo: () => inject(AuthService).rutaInicial() },
      {
        path: 'dashboard',
        canActivate: [moduloGuard],
        loadComponent: () => import('./pages/panel/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'solicitudes/verificar',
        canActivate: [moduloGuard],
        loadComponent: () =>
          import('./pages/panel/solicitudes-verificar/solicitudes-verificar').then((m) => m.SolicitudesVerificar),
      },
      {
        path: 'solicitudes/aprobar',
        canActivate: [moduloGuard],
        loadComponent: () =>
          import('./pages/panel/solicitudes-aprobar/solicitudes-aprobar').then((m) => m.SolicitudesAprobar),
      },
      {
        path: 'mis-embarcaciones',
        canActivate: [moduloGuard],
        loadComponent: () =>
          import('./pages/panel/mis-embarcaciones/mis-embarcaciones').then((m) => m.MisEmbarcaciones),
      },
      {
        path: 'embarcaciones/verificar',
        canActivate: [moduloGuard],
        loadComponent: () =>
          import('./pages/panel/embarcaciones-verificar/embarcaciones-verificar').then((m) => m.EmbarcacionesVerificar),
      },
      {
        path: 'radas',
        canActivate: [moduloGuard],
        loadComponent: () => import('./pages/panel/radas/radas').then((m) => m.Radas),
      },
      {
        path: 'auditoria',
        canActivate: [moduloGuard],
        loadComponent: () => import('./pages/panel/auditoria/auditoria').then((m) => m.Auditoria),
      },
      {
        path: 'perfil',
        canActivate: [moduloGuard],
        loadComponent: () => import('./pages/panel/perfil/perfil').then((m) => m.Perfil),
      },
    ],
  },

  { path: '**', redirectTo: '' },
];
