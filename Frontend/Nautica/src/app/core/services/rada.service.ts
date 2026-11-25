import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AsignacionRada, Embarcacion, Rada, RadaCompatible } from '../models/embarcacion.models';

/** HU13: radas, sugerencias, asignación, liberación e historial. */
@Injectable({ providedIn: 'root' })
export class RadaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/radas`;

  listar(): Observable<Rada[]> {
    return this.http.get<Rada[]>(this.url);
  }

  porAsignar(): Observable<Embarcacion[]> {
    return this.http.get<Embarcacion[]>(`${this.url}/por-asignar`);
  }

  compatibles(embarcacionId: number): Observable<RadaCompatible[]> {
    return this.http.get<RadaCompatible[]>(`${this.url}/compatibles/${embarcacionId}`);
  }

  asignar(radaId: number, embarcacionId: number): Observable<Rada> {
    return this.http.post<Rada>(`${this.url}/${radaId}/asignar`, { embarcacionId });
  }

  liberar(radaId: number, motivo: string): Observable<Rada> {
    return this.http.put<Rada>(`${this.url}/${radaId}/liberar`, { motivo });
  }

  historial(): Observable<AsignacionRada[]> {
    return this.http.get<AsignacionRada[]>(`${this.url}/historial`);
  }
}
