import { Component, computed, signal } from '@angular/core';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexFill,
  ApexGrid,
  ApexStroke,
  ApexTooltip,
  ApexXAxis,
  ApexYAxis,
  NgApexchartsModule,
} from 'ng-apexcharts';

type IncomeRangeKey = '6m' | '12m';

interface IncomeRangeOption {
  label: string;
  value: IncomeRangeKey;
}

interface IncomeDataset {
  categories: string[];
  values: number[];
}

@Component({
  selector: 'app-income-report-card',
  standalone: true,
  imports: [NgApexchartsModule],
  templateUrl: './income-report-card.component.html',
  styleUrl: './income-report-card.component.scss',
})
export class IncomeReportCardComponent {
  protected readonly title = 'Monthly Income';
  protected readonly subtitle = 'Average revenue by month';
  protected readonly rangeOptions: readonly IncomeRangeOption[] = [
    { label: 'Last 6 months', value: '6m' },
    { label: 'Last 12 months', value: '12m' },
  ];

  protected readonly selectedRange = signal<IncomeRangeKey>('6m');

  private readonly datasets: Record<IncomeRangeKey, IncomeDataset> = {
    '6m': {
      categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
      values: [38, 42, 36, 48, 44, 52],
    },
    '12m': {
      categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
      values: [38, 42, 36, 48, 44, 52, 49, 57, 53, 60, 58, 66],
    },
  };

  protected readonly chartSeries = computed<ApexAxisChartSeries>(() => [
    {
      name: 'Revenue',
      data: this.datasets[this.selectedRange()].values,
    },
  ]);

  protected readonly chartOptions = computed<{
    chart: ApexChart;
    stroke: ApexStroke;
    fill: ApexFill;
    grid: ApexGrid;
    tooltip: ApexTooltip;
    xaxis: ApexXAxis;
    yaxis: ApexYAxis;
  }>(() => {
    const dataset = this.datasets[this.selectedRange()];

    return {
      chart: {
        type: 'area',
        width: '100%',
        height: '100%',
        redrawOnParentResize: true,
        redrawOnWindowResize: true,
        toolbar: { show: false },
        zoom: { enabled: false },
        animations: {
          enabled: true,
          easing: 'easeinout',
          speed: 450,
        },
        fontFamily: 'var(--font-body)',
        background: 'transparent',
      },
      stroke: {
        curve: 'smooth',
        width: 3,
      },
      fill: {
        type: 'gradient',
        gradient: {
          shadeIntensity: 0.35,
          opacityFrom: 0.32,
          opacityTo: 0.02,
          stops: [0, 90, 100],
        },
      },
      grid: {
        borderColor: 'rgba(197, 198, 205, 0.45)',
        strokeDashArray: 4,
        padding: {
          left: 0,
          right: 0,
          top: 8,
          bottom: 0,
        },
      },
      tooltip: {
        theme: 'light',
        style: {
          fontSize: '12px',
          fontFamily: 'var(--font-body)',
        },
        y: {
          formatter: (value: number) => `$${value.toFixed(0)}k`,
        },
      },
      xaxis: {
        categories: dataset.categories,
        axisBorder: {
          color: 'rgba(197, 198, 205, 0.5)',
        },
        axisTicks: {
          color: 'rgba(197, 198, 205, 0.5)',
        },
        labels: {
          style: {
            colors: '#54647a',
            fontFamily: 'var(--font-label)',
            fontSize: '12px',
          },
        },
      },
      yaxis: {
        min: 0,
        tickAmount: 4,
        labels: {
          style: {
            colors: '#54647a',
            fontFamily: 'var(--font-label)',
            fontSize: '12px',
          },
          formatter: (value: number) => `$${value.toFixed(0)}k`,
        },
      },
    };
  });

  onRangeChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as IncomeRangeKey;
    this.selectedRange.set(value);
  }
}
