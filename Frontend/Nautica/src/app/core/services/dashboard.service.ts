import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  DashboardDatos,
  KpiDashboard,
  MapaCalor,
  PerfilEdad,
  SerieTemporal,
  TiempoAtencion,
} from '../models/dashboard.models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/dashboard`;

  /** Pide los 5 endpoints en paralelo para el periodo indicado (últimos N días). */
  cargar(dias: number): Observable<DashboardDatos> {
    const params = new HttpParams().set('dias', dias);
    return forkJoin({
      kpis: this.http.get<KpiDashboard>(`${this.url}/kpis`, { params }),
      serie: this.http.get<SerieTemporal[]>(`${this.url}/serie-temporal`, { params }),
      tiempos: this.http.get<TiempoAtencion[]>(`${this.url}/tiempos-atencion`, { params }),
      mapaCalor: this.http.get<MapaCalor[]>(`${this.url}/mapa-calor`, { params }),
      perfilEdad: this.http.get<PerfilEdad[]>(`${this.url}/perfil-edad`, { params }),
    });
  }
}
