import { Component, Input, OnInit, OnChanges, ChangeDetectorRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartModule } from 'primeng/chart';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { ChartDataDto } from '../../../dto/chart-data.dto';
import { DashboardService } from '../../../services/dashboard.service';

import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-chart-card',
  imports: [CommonModule, ChartModule, SelectModule, FormsModule, TranslateModule],
  templateUrl: './chart-card.html',
  styles: [`
    .chart-card {
      background-color: #282828;
      border-radius: 12px;
      padding: 1.5rem;
      border: 1px solid #333;
      height: 100%;
      display: flex;
      flex-direction: column;
    }
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start; /* Changed from center to accommodate wrapping */
      margin-bottom: 1.5rem;
      flex-wrap: wrap; /* Make it wrap on small screens */
      gap: 0.75rem;
    }
    .card-title {
      color: #fff;
      font-size: 1.05rem;
      font-weight: 500;
      margin: 0;
      opacity: 0.9;
      width: 100%; /* Default to full width on mobile */
    }
    @media (min-width: 640px) {
      .card-header {
        align-items: center;
      }
      .card-title {
        width: auto;
      }
    }
    
    .header-filters {
      display: flex;
      gap: 0.5rem;
      width: 100%;
    }
    @media (min-width: 640px) {
      .header-filters {
        width: auto;
      }
    }

    .chart-container {
      position: relative;
      flex-grow: 1;
    }
    
    /* List Layout Styles */
    .list-container {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
      flex-grow: 1;
      padding-bottom: 0.5rem;
    }
    .list-item {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .item-label {
      color: #a0a0a0;
      font-size: 0.9rem;
      width: 70px;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
    }
    .item-bar-container {
      flex-grow: 1;
      height: 6px;
      background-color: #333;
      border-radius: 10px;
      overflow: hidden;
    }
    .item-bar {
      height: 100%;
      border-radius: 10px;
      transition: width 0.6s ease-out;
    }
    .item-value {
      color: #a0a0a0;
      font-size: 0.9rem;
      width: 45px;
      text-align: right;
    }

    /* Footer Stats Styles */
    .footer-stats {
      margin-top: 1.25rem;
      border-top: 1px solid #333;
      padding-top: 1.25rem;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .footer-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .footer-label {
      color: #a0a0a0;
      font-size: 0.95rem;
    }
    .footer-value {
      color: #fff;
      font-weight: bold;
      font-size: 1rem;
    }

    ::ng-deep .custom-dropdown .p-select {
        background: #333 !important;
        border: 1px solid #444 !important;
        border-radius: 6px !important;
        width: 100%;
    }
    @media (min-width: 640px) {
      ::ng-deep .custom-dropdown .p-select {
          width: auto;
      }
    }
    ::ng-deep .custom-geo-card.p-select {
        background: #1a1a1a !important;
        border: 1px solid #444 !important;
        border-radius: 6px !important;
        min-width: 100px;
        flex: 1; /* Allow to grow on mobile */
    }
    ::ng-deep .custom-geo-card .p-select-label {
        color: #e0e0e0 !important;
        font-size: 0.8rem !important;
        padding: 0.25rem 0.5rem !important;
    }
  `]
})
export class ChartCard implements OnInit, OnChanges {
  @Input() 
  set data(value: ChartDataDto[]) {
    this._data = value || [];
    this.initChart();
  }
  get data() { return this._data; }
  private _data: ChartDataDto[] = [];

  @Input() title!: string;
  @Input() chartType: 'bar' | 'line' | 'pie' | 'doughnut' | 'list' = 'bar';
  @Input() showDropdown: boolean = false;
  @Input() showGeoFilters: boolean = false;
  @Input() horizontal: boolean = false;
  @Input() color: string = '#3b82f6';
  @Input() childStats: any[] = [];
  @Input() isPercent: boolean = false;

  @Output() filterChange = new EventEmitter<{ country?: string, region?: string }>();

  // Simple country dropdown (old mode)
  countries: string[] = [];
  selectedCountrySimple: string | null = null;

  // Hierarchical local filters
  countryOptions: string[] = ['Global'];
  regionOptions: string[] = [];
  selectedCountry: string = 'Global';
  selectedRegion: string = 'Regions';

  chartDataObj: any;
  chartOptions: any;

  private palette = [
    '#84cc16', // Lime (Sicurezza)
    '#3b82f6', // Blue
    '#ef4444', // Red
    '#f59e0b', // Amber
    '#6366f1', // Indigo
    '#ec4899', // Pink
  ];

  constructor(
      private dashboardService: DashboardService,
      private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (this.showDropdown && !this.showGeoFilters) {
      this.dashboardService.getCountries().subscribe(res => {
        this.countries = res;
        this.selectedCountrySimple = (this.countries && this.countries.length > 0) ? this.countries[0] : null;
      });
    }

    if (this.showGeoFilters) {
        this.dashboardService.getCountries().subscribe(res => {
            this.countryOptions = ['Global', ...res];
        });
    }

    this.initChart();
  }

  ngOnChanges() {
    this.initChart();
  }

  onCountryChangeLocal() {
    this.regionOptions = [];
    this.selectedRegion = 'Regions';

    if (this.selectedCountry !== 'Global') {
        this.dashboardService.getRegions(this.selectedCountry).subscribe(res => {
            this.regionOptions = ['Regions', ...res];
            this.cdr.detectChanges();
        });
        this.filterChange.emit({ country: this.selectedCountry });
    } else {
        this.filterChange.emit({});
    }
  }

  onRegionChangeLocal() {
      const region = this.selectedRegion === 'Regions' ? undefined : this.selectedRegion;
      this.filterChange.emit({ country: this.selectedCountry, region: region });
  }

  onCountryChange() {}

  getPercent(value: number): number {
    if (this.isPercent) return value;
    const max = Math.max(...this.data.map(d => d.value), 100);
    return (value / max) * 100;
  }

  formatValue(value: number): string {
    if (this.isPercent) return value.toFixed(1) + '%';
    if (value >= 1000) return (value / 1000).toFixed(1) + 'k';
    return value.toString();
  }

  getPaletteColor(index: number): string {
    if (this.chartType === 'list' && this.color !== '#3b82f6' && index === 0) return this.color;
    if (this.title.toLowerCase().includes('banda')) {
        const bandPalette = ['#99f6e4', '#10b981', '#065f46'];
        return bandPalette[index] || this.palette[index % this.palette.length];
    }
    return this.palette[index % this.palette.length];
  }

  initChart() {
    setTimeout(() => {
        const isCircular = this.chartType === 'pie' || this.chartType === 'doughnut';

        this.chartDataObj = {
          labels: this.data.map(d => d.label),
          datasets: [
            {
              label: this.title,
              data: this.data.map(d => d.value),
              backgroundColor: isCircular ? this.palette : this.color,
              borderColor: '#1a1a1a',
              borderWidth: isCircular ? 2 : 0,
              borderRadius: isCircular ? 0 : 4,
              barThickness: this.horizontal ? 8 : 20
            }
          ]
        };

        const textColor = '#a0a0a0';
        const surfaceBorder = '#333333';

        this.chartOptions = {
          indexAxis: this.horizontal ? 'y' : 'x',
          maintainAspectRatio: false,
          plugins: {
            legend: { 
              display: isCircular,
              position: 'right',
              labels: { color: textColor, usePointStyle: true, font: { size: 11 } }
            },
            tooltip: {
                backgroundColor: '#1a1a1a',
                titleColor: '#fff',
                bodyColor: '#a0a0a0',
                borderColor: '#333',
                borderWidth: 1
            }
          },
          scales: isCircular ? {} : {
            x: {
              display: !this.horizontal,
              ticks: { color: textColor, font: { size: 10 } },
              grid: { color: this.horizontal ? surfaceBorder : 'transparent', drawBorder: false }
            },
            y: {
              display: this.horizontal,
              ticks: { color: textColor, font: { size: 10 } },
              grid: { color: !this.horizontal ? surfaceBorder : 'transparent', drawBorder: false }
            }
          }
        };
        
        this.cdr.detectChanges();
    }, 0);
  }
}
