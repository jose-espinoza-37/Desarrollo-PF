import { Injectable } from '@angular/core';


@Injectable({ providedIn: 'root' })
export class StorageService {
  getItem(clave: string): string | null {
    try {
      return localStorage.getItem(clave);
    } catch {
      return null;
    }
  }

  setItem(clave: string, valor: string): void {
    try {
      localStorage.setItem(clave, valor);
    } catch {

    }
  }

  removeItem(clave: string): void {
    try {
      localStorage.removeItem(clave);
    } catch {

    }
  }
}
