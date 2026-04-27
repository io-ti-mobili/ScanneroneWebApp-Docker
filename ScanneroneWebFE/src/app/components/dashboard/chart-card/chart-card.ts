import { Component, Input, OnInit, OnChanges, ChangeDetectorRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartModule } from 'primeng/chart';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { ChartDataDto } from '../../../dto/chart-data.dto';
import { DashboardService } from '../../../services/dashboard.service';

@Component({
  selector: 'app-chart-card',
  imports: [CommonModule, ChartModule, SelectModule, FormsModule],
  template: `
    <div class="chart-card">
      <div class="card-header">
        <h3 class="card-title">{{ title }}</h3>
        
        <!-- Cascading Geo Filters -->
        <div class="header-filters" *ngIf="showGeoFilters">
          <p-select [options]="countryOptions" 
                   [(ngModel)]="selectedCountry" 
                   (onChange)="onCountryChangeLocal()" 
                   styleClass="p-dropdown-xs custom-geo-card"></p-select>

           <p-select *ngIf="selectedCountry !== 'Global' && regionOptions.length > 0"
                   [options]="regionOptions" 
                   [(ngModel)]="selectedRegion" 
                   (onChange)="onRegionChangeLocal()" 
                   styleClass="p-dropdown-xs custom-geo-card"></p-select>
        </div>

        <p-select *ngIf="showDropdown && !showGeoFilters" [options]="countries" [(ngModel)]="selectedCountrySimple" (onChange)="onCountryChange()" placeholder="Seleziona" styleClass="p-dropdown-sm" class="custom-dropdown"></p-select>
      </div>

      <!-- Render as Custom List of Bars -->
      <div class="list-container" *ngIf="chartType === 'list'">
        <div class="list-item" *ngFor="let item of data; let i=index">
          <div class="item-label">{{ item.label }}</div>
          <div class="item-bar-container">
            <div class="item-bar" [style.width.%]="getPercent(item.value)" [style.background-color]="getPaletteColor(i)"></div>
          </div>
          <div class="item-value">{{ formatValue(item.value) }}</div>
        </div>
      </div>

      <!-- Render as Standard Chart -->
      <div class="chart-container" *ngIf="chartType !== 'list'">
        <p-chart [type]="chartType" [data]="chartDataObj" [options]="chartOptions" [height]="'200px'"></p-chart>
      </div>

      <!-- Optional Footer Stats -->
      <div class="footer-stats" *ngIf="childStats && childStats.length > 0">
        <div class="footer-item" *ngFor="let stat of childStats">
          <span class="footer-label">{{ stat.title }}</span>
          <span class="footer-value">{{ stat.rawNumber }}</span>
        </div>
      </div>
    </div>
  `,
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
      align-items: center;
      margin-bottom: 1.5rem;
    }
    .card-title {
      color: #fff;
      font-size: 1.05rem;
      font-weight: 500;
      margin: 0;
      opacity: 0.9;
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
    }
    ::ng-deep .custom-geo-card.p-select {
        background: #1a1a1a !important;
        border: 1px solid #444 !important;
        border-radius: 6px !important;
        min-width: 100px;
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
