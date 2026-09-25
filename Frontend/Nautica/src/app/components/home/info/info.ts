import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  imports: [CommonModule],
  selector: 'app-info',
  standalone: true,
  styleUrl: './info.scss',
  templateUrl: './info.html',
})
export class Info {
  servicios = [
    {
      nombre: 'Amarre de embarcaciones (Rada)',
      imagen: 'assets/imagenes/servicios/1.png',
    },
    {
      nombre: 'Limpieza de nave',
      imagen: 'assets/imagenes/servicios/2.png',
    },
    {
      nombre: 'Cabotaje y traslado de nave',
      imagen: 'assets/imagenes/servicios/3.png',
    },
    {
      nombre: 'Cafetería e instructores',
      imagen: 'assets/imagenes/servicios/4.png',
    },
  ];

  instalaciones = [
    {
      nombre: 'Piscinas',
      imagen: 'assets/imagenes/instalaciones/1.png',
    },
    {
      nombre: 'Salones de relajación',
      imagen: 'assets/imagenes/instalaciones/2.png',
    },
    {
      nombre: 'Salones de fiestas',
      imagen: 'assets/imagenes/instalaciones/3.png',
    },
  ];
}