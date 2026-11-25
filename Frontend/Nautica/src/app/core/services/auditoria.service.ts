import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { EventoAuditoria } from '../models/auditoria.models';

@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  private readonly http = inject(HttpClient);

  eventos(): Observable<EventoAuditoria[]> {
    return this.http.get<EventoAuditoria[]>(`${environment.apiUrl}/auditoria/eventos`);
  }
}
