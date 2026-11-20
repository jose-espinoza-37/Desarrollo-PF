import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, Modulo, PerfilUsuario, UsuarioSesion } from '../models/auth.models';
import { StorageService } from './storage.service';

const CLAVE_TOKEN = 'poseidon_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly storage = inject(StorageService);


  private readonly _usuario = signal<UsuarioSesion | null>(this.leerSesionGuardada());

  readonly usuario = this._usuario.asReadonly();
  readonly estaAutenticado = computed(() => this._usuario() !== null);
  readonly modulos = computed<Modulo[]>(() =>
    [...(this._usuario()?.modulos ?? [])].sort((a, b) => a.orden - b.orden),
  );

  login(datos: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, datos)
      .pipe(tap((respuesta) => this.guardarSesion(respuesta.accessToken)));
  }

  obtenerPerfil(): Observable<PerfilUsuario> {
    return this.http.get<PerfilUsuario>(`${environment.apiUrl}/usuarios/me`);
  }

  logout(redirigir = true): void {
    this.storage.removeItem(CLAVE_TOKEN);
    this._usuario.set(null);
    if (redirigir) {
      this.router.navigate(['/login']);
    }
  }

  getToken(): string | null {
    const token = this.storage.getItem(CLAVE_TOKEN);
    if (!token) return null;

    const datos = this.decodificar(token);
    if (!datos || datos.exp * 1000 <= Date.now()) {
      this.storage.removeItem(CLAVE_TOKEN);
      this._usuario.set(null);
      return null;
    }
    return token;
  }


  tieneAcceso(url: string): boolean {
    const ruta = url.split('?')[0].split('#')[0];
    return this.modulos().some((m) => ruta === m.ruta || ruta.startsWith(m.ruta + '/'));
  }

  rutaInicial(): string {
    return this.modulos()[0]?.ruta ?? '/';
  }

  private guardarSesion(token: string): void {
    this.storage.setItem(CLAVE_TOKEN, token);
    this._usuario.set(this.decodificar(token));
  }

  private leerSesionGuardada(): UsuarioSesion | null {
    const token = this.storage.getItem(CLAVE_TOKEN);
    if (!token) return null;
    const datos = this.decodificar(token);
    if (!datos || datos.exp * 1000 <= Date.now()) {
      this.storage.removeItem(CLAVE_TOKEN);
      return null;
    }
    return datos;
  }


  private decodificar(token: string): UsuarioSesion | null {
    try {
      const payload = token.split('.')[1];
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const binario = atob(base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '='));
      const bytes = Uint8Array.from(binario, (c) => c.charCodeAt(0));
      const json = JSON.parse(new TextDecoder().decode(bytes));
      return {
        id: json.id,
        correo: json.sub,
        nombres: json.nombres,
        rol: json.rol,
        rolNombre: json.rolNombre,
        modulos: json.modulos ?? [],
        exp: json.exp,
      };
    } catch {
      return null;
    }
  }
}
