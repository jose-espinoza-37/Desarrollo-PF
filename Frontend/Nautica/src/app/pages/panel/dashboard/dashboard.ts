import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexFill,
  ApexLegend,
  ApexMarkers,
  ApexPlotOptions,
  ApexStroke,
  ApexTooltip,
  ApexXAxis,
  ApexYAxis,
  ChartComponent,
} from 'ng-apexcharts';

import { DashboardService } from '../../../core/services/dashboard.service';
import { ToastService } from '../../../core/services/toast.service';
import { DashboardDatos } from '../../../core/models/dashboard.models';
import { mensajeError } from '../../../shared/utils/solicitud-ui';

// Paleta categórica en orden fijo (azul, naranja, aqua) y rampa secuencial azul para el heatmap
const SERIE = ['#2a78d6', '#eb6834', '#1baf7a'];
const SECUENCIAL = ['#eef4fc', '#b7d3f6', '#86b6ef', '#5598e7', '#2a78d6', '#1c5cab', '#0d366b'];
const TEXTO = '#243746';
const TEXTO_SUAVE = '#647783';
const FUENTE = "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif";
const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'set', 'oct', 'nov', 'dic'];

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [ChartComponent, DecimalPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly toast = inject(ToastService);

  readonly periodos = [30, 60, 90, 180, 365];
  readonly dias = signal(90);
  readonly datos = signal<DashboardDatos | null>(null);
  readonly tablas = signal<Record<string, boolean>>({});
  readonly colores = SERIE;

  readonly sinEtiquetas: ApexDataLabels = { enabled: false };
  readonly grid = { borderColor: '#eef2f4', strokeDashArray: 0, xaxis: { lines: { show: false } } };
  readonly leyenda: ApexLegend = {
    position: 'top',
    horizontalAlign: 'left',
    fontFamily: FUENTE,
    labels: { colors: TEXTO },
    markers: { size: 6 },
  };

  // ======================================================================
  //  Chart 1: serie temporal (área + líneas) con media móvil de 7 días
  // ======================================================================
  readonly serieSeries = computed<ApexAxisChartSeries>(() => {
    const serie = this.datos()?.serie ?? [];
    return [
      { name: 'Solicitudes del día', type: 'area', data: serie.map((d) => d.cantidad) },
      { name: 'Media móvil 7 días', type: 'line', data: serie.map((d) => d.mediaMovil) },
    ];
  });
  readonly serieXaxis = computed<ApexXAxis>(() => ({
    categories: (this.datos()?.serie ?? []).map((d) => this.fechaCorta(d.fecha)),
    tickAmount: 10,
    axisBorder: { show: false },
    axisTicks: { show: false },
    labels: { rotate: 0, hideOverlappingLabels: true, style: { colors: TEXTO_SUAVE, fontFamily: FUENTE } },
    tooltip: { enabled: false },
  }));
  readonly serieChart: ApexChart = {
    type: 'line',
    height: 320,
    fontFamily: FUENTE,
    toolbar: { show: false },
    zoom: { enabled: false },
    animations: { enabled: true, speed: 400 },
  };
  readonly serieStroke: ApexStroke = { curve: ['straight', 'smooth'], width: [1.5, 3] };
  readonly serieFill: ApexFill = { type: 'solid', opacity: [0.18, 1] };
  readonly serieYaxis: ApexYAxis = {
    min: 0,
    forceNiceScale: true,
    labels: { formatter: (v: number) => (Number.isInteger(v) ? String(v) : v.toFixed(1)), style: { colors: TEXTO_SUAVE } },
  };
  readonly serieTooltip: ApexTooltip = { shared: true, intersect: false };

  // ======================================================================
  //  Chart 2: box plot del tiempo de atención (horas) por clasificación
  // ======================================================================
  readonly cajaSeries = computed<ApexAxisChartSeries>(() => [
    {
      name: 'Horas de atención',
      type: 'boxPlot',
      data: (this.datos()?.tiempos ?? [])
        .filter((t) => t.cantidad > 0)
        .map((t) => ({ x: t.descripcion, y: [t.minimo ?? 0, t.q1 ?? 0, t.mediana ?? 0, t.q3 ?? 0, t.maximo ?? 0] })),
    },
  ]);
  readonly cajaChart: ApexChart = { type: 'boxPlot', height: 320, fontFamily: FUENTE, toolbar: { show: false } };
  readonly cajaPlot: ApexPlotOptions = {
    bar: { horizontal: true, barHeight: '45%' },
    boxPlot: { colors: { upper: '#86b6ef', lower: '#cde2fb' } },
  };
  readonly cajaStroke: ApexStroke = { width: 1.5, colors: [SERIE[0]] };
  readonly cajaXaxis: ApexXAxis = {
    title: { text: 'Horas desde el registro hasta la resolución', style: { color: TEXTO_SUAVE, fontWeight: 500 } },
    labels: { style: { colors: TEXTO_SUAVE } },
  };
  readonly cajaYaxis: ApexYAxis = { labels: { style: { colors: TEXTO, fontSize: '13px' } } };

  // ======================================================================
  //  Chart 3: heatmap día de la semana x franja horaria
  // ======================================================================
  readonly calorSeries = computed<ApexAxisChartSeries>(() => {
    const celdas = this.datos()?.mapaCalor ?? [];
    const dias = [...new Set(celdas.map((c) => c.dia))];
    // ApexCharts dibuja la primera serie abajo: se invierte para que el lunes quede arriba
    return dias.reverse().map((dia) => ({
      name: dia,
      data: celdas.filter((c) => c.dia === dia).map((c) => ({ x: c.franja, y: c.cantidad })),
    }));
  });
  readonly calorChart: ApexChart = { type: 'heatmap', height: 320, fontFamily: FUENTE, toolbar: { show: false } };
  readonly calorPlot = computed<ApexPlotOptions>(() => {
    const max = Math.max(1, ...(this.datos()?.mapaCalor ?? []).map((c) => c.cantidad));
    const paso = max / (SECUENCIAL.length - 1);
    return {
      heatmap: {
        radius: 3,
        enableShades: false,
        colorScale: {
          ranges: SECUENCIAL.map((color, i) => ({
            from: i === 0 ? 0 : Math.floor((i - 1) * paso) + 1,
            to: i === 0 ? 0 : Math.ceil(i * paso),
            color,
            // Texto blanco sobre los tonos oscuros de la rampa
            foreColor: i >= 4 ? '#ffffff' : '#0b2d42',
            name: i === 0 ? 'Sin registros' : undefined,
          })),
        },
      },
    };
  });
  readonly calorLabels: ApexDataLabels = { enabled: true, style: { fontSize: '12px' } };
  readonly calorStroke: ApexStroke = { width: 2, colors: ['#ffffff'] };
  readonly calorLegend: ApexLegend = { show: false };
  readonly calorTooltip: ApexTooltip = { y: { formatter: (v: number) => `${v} solicitud(es)` } };

  // ======================================================================
  //  Chart 4: radar de la clasificación de pago por rango de edad
  // ======================================================================
  readonly radarCategorias = computed(() => [...new Set((this.datos()?.perfilEdad ?? []).map((p) => p.rango))]);
  readonly radarSeries = computed<ApexAxisChartSeries>(() => {
    const perfil = this.datos()?.perfilEdad ?? [];
    const clasificaciones = [...new Set(perfil.map((p) => p.descripcion))];
    return clasificaciones.map((nombre) => ({
      name: nombre,
      data: perfil.filter((p) => p.descripcion === nombre).map((p) => p.porcentaje),
    }));
  });
  readonly radarXaxis = computed<ApexXAxis>(() => ({
    categories: this.radarCategorias(),
    labels: { style: { colors: Array(5).fill(TEXTO), fontSize: '12px' } },
  }));
  readonly radarChart: ApexChart = { type: 'radar', height: 400, fontFamily: FUENTE, toolbar: { show: false } };
  readonly radarYaxis: ApexYAxis = { show: false, max: 100, tickAmount: 4 };
  readonly radarPlot: ApexPlotOptions = { radar: { size: 140, polygons: { strokeColors: '#e3e9ee', connectorColors: '#e3e9ee' } } };
  readonly radarStroke: ApexStroke = { width: 2 };
  readonly radarFill: ApexFill = { opacity: 0.12 };
  readonly radarMarkers: ApexMarkers = { size: 4 };
  readonly radarTooltip: ApexTooltip = { y: { formatter: (v: number) => `${v}% del rango` } };

  // ======================================================================
  //  Lectura de los resultados (se muestra debajo de cada chart)
  // ======================================================================
  readonly picoDiario = computed(() => {
    const serie = this.datos()?.serie ?? [];
    return serie.reduce((max, d) => (d.cantidad > (max?.cantidad ?? -1) ? d : max), serie[0]);
  });
  readonly franjaMayor = computed(() => {
    const celdas = this.datos()?.mapaCalor ?? [];
    return celdas.reduce((max, c) => (c.cantidad > (max?.cantidad ?? -1) ? c : max), celdas[0]);
  });
  readonly clasificacionMasLenta = computed(() => {
    const tiempos = (this.datos()?.tiempos ?? []).filter((t) => t.cantidad > 0);
    return tiempos.reduce((max, t) => ((t.mediana ?? 0) > (max?.mediana ?? -1) ? t : max), tiempos[0]);
  });
  readonly rangoMayorRiesgo = computed(() => {
    const noAptos = (this.datos()?.perfilEdad ?? []).filter((p) => p.clasificacion !== 'PAGADOR');
    const porRango = new Map<string, number>();
    noAptos.forEach((p) => porRango.set(p.rango, (porRango.get(p.rango) ?? 0) + p.porcentaje));
    return [...porRango.entries()].sort((a, b) => b[1] - a[1])[0];
  });

  ngOnInit(): void {
    this.cargar();
  }

  cambiarPeriodo(dias: number): void {
    this.dias.set(dias);
    this.cargar();
  }

  cargar(): void {
    this.dashboardService.cargar(this.dias()).subscribe({
      next: (d) => this.datos.set(d),
      error: (err: HttpErrorResponse) => this.toast.error(mensajeError(err, 'No se pudo cargar el dashboard.')),
    });
  }

  alternarTabla(id: string): void {
    this.tablas.update((t) => ({ ...t, [id]: !t[id] }));
  }

  /** Interpretación de la r de Pearson según su valor absoluto. */
  fuerzaCorrelacion(r: number | null): string {
    if (r === null) return 'sin datos';
    const a = Math.abs(r);
    const sentido = r >= 0 ? 'positiva' : 'negativa';
    if (a < 0.1) return 'nula';
    if (a < 0.3) return `débil ${sentido}`;
    if (a < 0.5) return `moderada ${sentido}`;
    return `fuerte ${sentido}`;
  }

  fechaCorta(iso: string): string {
    const [, m, d] = iso.split('-');
    return `${Number(d)} ${MESES[Number(m) - 1]}`;
  }
}
