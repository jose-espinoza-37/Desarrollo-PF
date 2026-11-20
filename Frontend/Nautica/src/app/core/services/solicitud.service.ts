import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  EstadoSolicitud,
  Seguimiento,
  Solicitud,
  SolicitudCreada,
  SolicitudRequest,
  VerificacionRequest,
} from '../models/solicitud.models';

@Injectable({ providedIn: 'root' })
export class SolicitudService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/solicitudes`;

  registrar(datos: SolicitudRequest): Observable<SolicitudCreada> {
    return this.http.post<SolicitudCreada>(this.url, datos);
  }


  seguimiento(codigo: string): Observable<Seguimiento> {
    return this.http.get<Seguimiento>(`${this.url}/seguimiento/${encodeURIComponent(codigo)}`);
  }


  listar(estado?: EstadoSolicitud): Observable<Solicitud[]> {
    const params = estado ? new HttpParams().set('estado', estado) : undefined;
    return this.http.get<Solicitud[]>(this.url, { params });
  }


  verificar(id: number, datos: VerificacionRequest): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.url}/${id}/verificar`, datos);
  }


  aprobar(id: number): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.url}/${id}/aprobar`, {});
  }

  desaprobar(id: number, motivo: string): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.url}/${id}/desaprobar`, { motivo });
  }
}
