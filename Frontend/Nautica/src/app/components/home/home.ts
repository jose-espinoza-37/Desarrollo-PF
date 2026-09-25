import { Component } from '@angular/core';
import { Hero } from './hero/hero';
import { Info } from './info/info';

@Component({
  imports: [Hero, Info],
  selector: 'app-home',
  standalone: true,
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {}