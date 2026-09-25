import { Component } from '@angular/core';
import { Hero } from './hero/hero';

@Component({
  imports: [Hero],
  selector: 'app-home',
  standalone: true,
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {}