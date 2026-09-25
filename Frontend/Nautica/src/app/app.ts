import { Component } from '@angular/core';
import { Header } from './components/header/header';

@Component({
  imports: [Header],
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {}