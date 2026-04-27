import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SelectModule } from 'primeng/select';
import { StatCard } from './stat-card/stat-card';
import { ChartCard } from './chart-card/chart-card';
import { DashboardService } from '../../services/dashboard.service';
import { SimpleStatDto } from '../../dto/simple-stat.dto';
import { ChartDataDto } from '../../dto/chart-data.dto';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, StatCard, ChartCard, FormsModule, SelectModule],
  template: `
    <div class="dashboard-container">
      <div class="dash-header">
        <h2 class="dash-title">Network Dashboard</h2>
      </div>

      <div class="stats-grid">
        <app-stat-card *ngFor="let stat of stats" [stat]="stat"></app-stat-card>
      </div>

      <div class="charts-grid mt-4">
        <!-- Sicurezza -->
        <app-chart-card title="Sicurezza reti" 
                        chartType="pie" 
                        [data]="securityData" 
                        [showGeoFilters]="true"
                        (filterChange)="onCardFilterChange($event, 'security')"></app-chart-card>

        <!-- Scoperte -->
        <app-chart-card title="Scoperte giornaliere" 
                        chartType="bar" 
                        [data]="discoveriesData" 
                        color="#93c5fd" 
                        [showGeoFilters]="true"
                        (filterChange)="onCardFilterChange($event, 'discoveries')"></app-chart-card>
        
        <!-- Dinamica Geografica (Hierarchical) -->
        <app-chart-card [title]="geoChartTitle" 
                        chartType="list" 
                        [data]="geoHierarchicalData" 
                        [color]="'#818cf8'"
                        [showGeoFilters]="true"
                        (filterChange)="onHierarchicalGeoChange($event)"></app-chart-card>
        
        <!-- Top WPA3 -->
        <app-chart-card [title]="wpa3CardTitle" 
                        chartType="list" 
                        [data]="topCitiesWpa3Data" 
                        color="#3b82f6"
                        [showGeoFilters]="true"
                        (filterChange)="onCardFilterChange($event, 'wpa3')"></app-chart-card>
        
        <!-- Low Security -->
        <app-chart-card [title]="insecureCardTitle" 
                        chartType="list" 
                        [data]="topInsecureCitiesData" 
                        [isPercent]="true" 
                        color="#ef4444"
                        [showGeoFilters]="true"
                        (filterChange)="onCardFilterChange($event, 'insecure')"></app-chart-card>

        <!-- Categorie -->
        <app-chart-card [title]="categoryCardTitle" 
                        chartType="list" 
                        [data]="categoryData" 
                        [isPercent]="true" 
                        color="#f59e0b"
                        [showGeoFilters]="true"
                        (filterChange)="onCardFilterChange($event, 'category')"></app-chart-card>

        <!-- Banda -->
        <app-chart-card title="Banda — 2.4 / 5 / 6 GHz" 
                        chartType="list" 
                        [data]="bandData"
                        [showGeoFilters]="true"
                        (filterChange)="onCardFilterChange($event, 'band')"></app-chart-card>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      max-width: 1400px;
      margin: 0 auto;
    }
    .dash-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
    }
    .dash-title {
      color: white;
      margin: 0;
      font-size: 1.5rem;
    }
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(1, 1fr);
      gap: 1rem;
    }
    @media (min-width: 640px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
    @media (min-width: 1024px) { .stats-grid { grid-template-columns: repeat(4, 1fr); } }
    
    .charts-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 1.5rem;
    }
    @media (min-width: 768px) { .charts-grid { grid-template-columns: repeat(2, 1fr); } }
    .mt-4 { margin-top: 1.5rem; }
  `]
})
export class Dashboard implements OnInit {
  stats: SimpleStatDto[] = [];
  securityData: ChartDataDto[] = [];
  discoveriesData: ChartDataDto[] = [];
  geoHierarchicalData: ChartDataDto[] = [];
  geoChartTitle: string = 'Top Nazioni per reti';
  topCitiesWpa3Data: ChartDataDto[] = [];
  wpa3CardTitle: string = 'Top Nazioni WPA3';
  topInsecureCitiesData: ChartDataDto[] = [];
  insecureCardTitle: string = 'Top Nazioni meno sicure';
  categoryCardTitle: string = 'Tipologia location (Global)';
  bandData: ChartDataDto[] = [];
  categoryData: ChartDataDto[] = [];

  constructor(
    private dashboardService: DashboardService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.refreshAll();
  }

  refreshAll() {
    this.onCardFilterChange({}, 'security');
    this.onCardFilterChange({}, 'discoveries');
    this.onHierarchicalGeoChange({});
    this.onCardFilterChange({}, 'wpa3');
    this.onCardFilterChange({}, 'insecure');
    this.onCardFilterChange({}, 'category');
    this.onCardFilterChange({}, 'band');
    this.refreshStats();
  }

  refreshStats() {
    this.dashboardService.getGlobalStats().subscribe((data: any) => {
      if (!data) return;
      this.stats = [
        { title: 'Reti nel database', rawNumber: data.totalNetworks?.toLocaleString() || '0', description: 'Totale reti caricate' },
        { title: 'Utenti attivi', rawNumber: data.totalUsers?.toLocaleString() || '0', description: 'Community Scannerone' },
        { title: 'Città mappate', rawNumber: data.totalCities?.toLocaleString() || '0', description: `In ${data.totalCountries || 0} nazioni` },
        { title: 'Reti Open', rawNumber: (data.openNetworkPercent ? data.openNetworkPercent.toFixed(1) : '0') + '%', description: 'Senza protezione' }
      ];
      this.cdr.detectChanges();
    });
  }

  onHierarchicalGeoChange(filter: { country?: string, region?: string }) {
    const country = filter.country === 'Global' ? undefined : filter.country;
    const region = filter.region || undefined;

    if (!country) {
        this.geoChartTitle = 'Top Nazioni per reti';
        this.dashboardService.getTopCountriesChart().subscribe(res => { this.geoHierarchicalData = res; this.cdr.detectChanges(); });
    } else if (!region) {
        this.geoChartTitle = `Top Regioni in ${country}`;
        this.dashboardService.getTopRegionsChart(country).subscribe(res => { this.geoHierarchicalData = res; this.cdr.detectChanges(); });
    } else {
        this.geoChartTitle = `Top Città in ${region}`;
        this.dashboardService.getTopCitiesChart(country, region).subscribe(res => { this.geoHierarchicalData = res; this.cdr.detectChanges(); });
    }
  }

  onCardFilterChange(filter: { country?: string, region?: string }, cardType: string) {
    const region = filter.region || undefined;
    const country = filter.country === 'Global' ? undefined : filter.country;

    switch (cardType) {
      case 'security':
        this.dashboardService.getSecurityChart(country, region).subscribe(data => {
            const total = data.reduce((acc, curr) => acc + (curr.value as number), 0);
            this.securityData = data.map(d => ({ 
                label: d.label, 
                value: total > 0 ? Math.round(((d.value as number) / total) * 100 * 10) / 10 : 0 
            }));
            this.cdr.detectChanges();
        });
        break;
      case 'discoveries':
        this.dashboardService.getDiscoveriesChart(country, region).subscribe(data => { this.discoveriesData = data; this.cdr.detectChanges(); });
        break;
      case 'wpa3':
        this.wpa3CardTitle = !country ? 'Top Nazioni WPA3' : (!region ? `Top Regioni WPA3 in ${country}` : `Top Città WPA3 in ${region}`);
        this.dashboardService.getTopCitiesWpa3(country, region).subscribe(res => { this.topCitiesWpa3Data = res; this.cdr.detectChanges(); });
        break;
      case 'insecure':
        this.insecureCardTitle = !country ? 'Top Nazioni meno sicure' : (!region ? `Top Regioni meno sicure in ${country}` : `Top Città meno sicure in ${region}`);
        this.dashboardService.getTopInsecureCities(country, region).subscribe(res => { this.topInsecureCitiesData = res; this.cdr.detectChanges(); });
        break;
      case 'category':
        this.categoryCardTitle = !country ? 'Tipologia location (Global)' : (!region ? `Tipologia in ${country}` : `Tipologia in ${region}`);
        this.dashboardService.getGlobalStats(country, region).subscribe((data: any) => {
            if (data?.categoryDistribution) {
                const total = Array.isArray(data.categoryDistribution) ? 0 : Object.values(data.categoryDistribution).reduce((acc: any, curr: any) => acc + curr, 0) as number;
                this.categoryData = Object.entries(data.categoryDistribution).map(([label, value]) => ({
                    label: label.charAt(0).toUpperCase() + label.slice(1).toLowerCase(),
                    value: total > 0 ? Math.round(((value as number) / total) * 100 * 10) / 10 : 0
                }));
            }
            this.cdr.detectChanges();
        });
        break;
      case 'band':
        this.dashboardService.getBandChart(country, region).subscribe(data => {
            const total = data.reduce((acc, curr) => acc + (curr.value as number), 0);
            this.bandData = data.map(d => ({ 
                label: d.label, 
                value: total > 0 ? Math.round(((d.value as number) / total) * 100 * 10) / 10 : 0 
            }));
            this.cdr.detectChanges();
        });
        break;
    }
  }
}
