import { Routes } from '@angular/router';
import { Home } from './components/home/home';
import { Contacto} from './components/home/contacto/contacto'

export const routes: Routes = [
  {
    path: '',
    component: Home 
  },
  {
    path: 'contacto',
    component: Contacto
  }
];