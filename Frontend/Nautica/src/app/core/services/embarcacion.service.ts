import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  DocumentoEmbarcacion,
  Embarcacion,
  EmbarcacionRequest,
  EstadoEmbarcacion,
  TipoDocumento,
  VerificacionEmbarcacionRequest,
} from '../models/embarcacion.models';

/** HU11 (socio) y HU12 (Servicios Navieros). */
@Injectable({ providedIn: 'root' })
export class EmbarcacionService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/embarcaciones`;

  misEmbarcaciones(): Observable<Embarcacion[]> {
    return this.http.get<Embarcacion[]>(`${this.url}/mias`);
  }

  registrar(datos: EmbarcacionRequest): Observable<Embarcacion> {
    return this.http.post<Embarcacion>(this.url, datos);
  }

  subsanar(id: number, datos: EmbarcacionRequest): Observable<Embarcacion> {
    return this.http.put<Embarcacion>(`${this.url}/${id}`, datos);
  }

  /** Envía el archivo como multipart/form-data. */
  subirDocumento(id: number, tipoDocumento: TipoDocumento, archivo: File): Observable<DocumentoEmbarcacion> {
    const datos = new FormData();
    datos.append('tipoDocumento', tipoDocumento);
    datos.append('archivo', archivo);
    return this.http.post<DocumentoEmbarcacion>(`${this.url}/${id}/documentos`, datos);
  }

  /** El archivo se pide con el token (interceptor) y se abre como blob. */
  descargarDocumento(documentoId: number): Observable<Blob> {
    return this.http.get(`${this.url}/documentos/${documentoId}`, { responseType: 'blob' });
  }

  listar(estado?: EstadoEmbarcacion): Observable<Embarcacion[]> {
    const params = estado ? new HttpParams().set('estado', estado) : undefined;
    return this.http.get<Embarcacion[]>(this.url, { params });
  }

  verificar(id: number, datos: VerificacionEmbarcacionRequest): Observable<Embarcacion> {
    return this.http.put<Embarcacion>(`${this.url}/${id}/verificar`, datos);
  }
}
