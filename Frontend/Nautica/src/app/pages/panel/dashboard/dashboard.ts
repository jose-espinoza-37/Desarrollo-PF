import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexFill,
  ApexLegend,
  ApexNonAxisChartSeries,
  ApexPlotOptions,
  ApexStroke,
  ApexTooltip,
  ApexXAxis,
  ApexYAxis,
  ChartComponent,
} from 'ng-apexcharts';

import { DashboardService } from '../../../core/services/dashboard.service';
import { ToastService } from '../../../core/services/toast.service';
import { DashboardResumen } from '../../../core/models/solicitud.models';
import { mensajeError } from '../../../shared/utils/solicitud-ui';


const SERIE = ['#2a78d6', '#eb6834', '#1baf7a', '#eda100', '#e87ba4'];
const TEXTO = '#243746';
const TEXTO_SUAVE = '#647783';
const FUENTE = "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif";


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

  readonly datos = signal<DashboardResumen | null>(null);
  readonly tablas = signal<Record<string, boolean>>({});
  readonly colores = SERIE;


  readonly areaSeries = computed<ApexAxisChartSeries>(() => [
    { name: 'Solicitudes', data: (this.datos()?.solicitudesPorDia ?? []).map((d) => d.cantidad) },
  ]);
  readonly areaXaxis = computed<ApexXAxis>(() => ({
    categories: (this.datos()?.solicitudesPorDia ?? []).map((d) => this.fechaCorta(d.fecha)),
    axisBorder: { show: false },
    axisTicks: { show: false },
    labels: { style: { colors: TEXTO_SUAVE, fontFamily: FUENTE } },
    tooltip: { enabled: false },
    crosshairs: { show: true, stroke: { color: '#b6c3cb', width: 1, dashArray: 0 } },
  }));
  readonly areaChart: ApexChart = {
    type: 'area',
    height: 300,
    fontFamily: FUENTE,
    toolbar: { show: false },
    zoom: { enabled: false },
    animations: { enabled: true, speed: 500 },
  };
  readonly areaStroke: ApexStroke = { curve: 'straight', width: 2 };
  readonly areaFill: ApexFill = {
    type: 'gradient',
    gradient: { shadeIntensity: 1, opacityFrom: 0.35, opacityTo: 0.02, stops: [0, 95] },
  };
  readonly areaYaxis: ApexYAxis = {
    min: 0,
    forceNiceScale: true,
    labels: { formatter: (v: number) => Math.round(v).toString(), style: { colors: TEXTO_SUAVE } },
  };
  readonly areaTooltip: ApexTooltip = {
    shared: true,
    intersect: false,
    y: { formatter: (v: number) => `${v} solicitud(es)` },
  };
  readonly sinEtiquetas: ApexDataLabels = { enabled: false };
  readonly grid = { borderColor: '#eef2f4', strokeDashArray: 0, xaxis: { lines: { show: false } } };


  readonly radialSeries = computed<ApexNonAxisChartSeries>(() =>
    (this.datos()?.porClasificacion ?? []).map((c) => c.porcentaje),
  );
  readonly radialLabels = computed(() => (this.datos()?.porClasificacion ?? []).map((c) => c.etiqueta));
  readonly radialChart: ApexChart = { type: 'radialBar', height: 330, fontFamily: FUENTE };
  readonly radialPlot = computed<ApexPlotOptions>(() => ({
    radialBar: {
      hollow: { size: '32%' },
      track: { background: '#eef2f4', margin: 6 },
      dataLabels: {
        name: { fontSize: '13px', color: TEXTO_SUAVE },
        value: { fontSize: '20px', fontWeight: 700, color: TEXTO, formatter: (v: number) => `${v}%` },
        total: {
          show: true,
          label: 'Verificados',
          color: TEXTO_SUAVE,
          formatter: () =>
            String((this.datos()?.porClasificacion ?? []).reduce((suma, c) => suma + c.cantidad, 0)),
        },
      },
    },
  }));
  readonly radialStroke: ApexStroke = { lineCap: 'round' };
  readonly radialLegend: ApexLegend = {
    show: true,
    position: 'bottom',
    fontFamily: FUENTE,
    labels: { colors: TEXTO },
    formatter: (nombre: string, opts?: { seriesIndex: number }) => {
      const c = this.datos()?.porClasificacion[opts?.seriesIndex ?? 0];
      return c ? `${nombre}: ${c.cantidad} (${c.porcentaje}%)` : nombre;
    },
  };


  readonly treemapSeries = computed<ApexAxisChartSeries>(() => [
    {
      name: 'Solicitudes',
      data: (this.datos()?.porEstado ?? [])
        .filter((e) => e.cantidad > 0)
        .map((e) => ({ x: e.etiqueta, y: e.cantidad, fillColor: this.colorEstado(e.codigo) })),
    },
  ]);
  readonly treemapChart: ApexChart = { type: 'treemap', height: 330, fontFamily: FUENTE, toolbar: { show: false } };
  readonly treemapPlot: ApexPlotOptions = {
    treemap: { distributed: true, enableShades: false, borderRadius: 4 },
  };

  readonly treemapLabels: ApexDataLabels = {
    enabled: true,
    style: { fontSize: '13px', fontWeight: 700, colors: ['#0b2d42'] },
    formatter: (texto, op) => [String(texto), String((op as { value?: number } | undefined)?.value ?? '')],
    offsetY: -2,
  };
  readonly treemapStroke: ApexStroke = { width: 2, colors: ['#ffffff'] };
  readonly treemapLegend: ApexLegend = { show: false };
  readonly treemapTooltip: ApexTooltip = { y: { formatter: (v: number) => `${v} solicitud(es)` } };

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.dashboardService.resumen().subscribe({
      next: (d) => this.datos.set(d),
      error: (err: HttpErrorResponse) => this.toast.error(mensajeError(err, 'No se pudo cargar el dashboard.')),
    });
  }

  alternarTabla(id: string): void {
    this.tablas.update((t) => ({ ...t, [id]: !t[id] }));
  }


  colorEstado(codigo: string): string {
    const orden = ['PENDIENTE', 'VERIFICADA', 'NO_APTA', 'APROBADA', 'DESAPROBADA'];
    return SERIE[Math.max(0, orden.indexOf(codigo))];
  }

  fechaCorta(iso: string): string {
    const [, m, d] = iso.split('-');
    const meses = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'set', 'oct', 'nov', 'dic'];
    return `${Number(d)} ${meses[Number(m) - 1]}`;
  }
}
