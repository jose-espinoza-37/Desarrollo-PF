
export interface Modulo {
  codigo: string;
  nombre: string;
  ruta: string;
  icono: string;
  orden: number;
}

export interface LoginRequest {
  correo: string;
  password: string;
}


export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  id: number;
  nombres: string;
  apellidos: string;
  correo: string;
  rol: string;
  rolNombre: string;
  modulos: Modulo[];
}


export interface UsuarioSesion {
  id: number;
  correo: string;
  nombres: string;
  rol: string;
  rolNombre: string;
  modulos: Modulo[];
  exp: number;
}


export interface PerfilUsuario {
  id: number;
  nombres: string;
  apellidos: string;
  correo: string;
  rol: string;
  rolNombre: string;
  fechaCreacion: string;
}
