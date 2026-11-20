import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { ToastContainer } from './shared/components/toast-container/toast-container';
import { LoadingOverlay } from './shared/components/loading-overlay/loading-overlay';

@Component({
  imports: [RouterOutlet, ToastContainer, LoadingOverlay],
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {}
