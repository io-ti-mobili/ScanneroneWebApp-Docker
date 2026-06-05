import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SelectModule } from 'primeng/select';
import { StatCard } from './stat-card/stat-card';
import { ChartCard } from './chart-card/chart-card';
import { DashboardService } from '../../services/dashboard.service';
import { SimpleStatDto } from '../../dto/simple-stat.dto';
import { ChartDataDto } from '../../dto/chart-data.dto';

import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, StatCard, ChartCard, FormsModule, SelectModule, TranslateModule],
  templateUrl: './dashboard.html',
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
  categoryCardTitle: string = 'Tipologia rete (Global)';
  bandData: ChartDataDto[] = [];
  categoryData: ChartDataDto[] = [];

  constructor(
    private dashboardService: DashboardService,
    private cdr: ChangeDetectorRef,
    private translate: TranslateService
  ) {
    this.translate.onLangChange.subscribe(() => {
      this.refreshTranslations();
    });
    // Ensure initial load
    this.translate.get('DASHBOARD.TITLE').subscribe(() => {
      this.refreshTranslations();
    });
  }

  refreshTranslations() {
    this.onHierarchicalGeoChange({
        country: this.lastGeoFilter.country, 
        region: this.lastGeoFilter.region 
    });
    this.onCardFilterChange(this.lastWpa3Filter, 'wpa3');
    this.onCardFilterChange(this.lastInsecureFilter, 'insecure');
    this.onCardFilterChange(this.lastCategoryFilter, 'category');
    this.refreshStats();
  }

  lastGeoFilter: any = {};
  lastWpa3Filter: any = {};
  lastInsecureFilter: any = {};
  lastCategoryFilter: any = {};

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
        { title: this.translate.instant('DASHBOARD.STATS.NETWORKS_TITLE'), rawNumber: data.totalNetworks?.toLocaleString() || '0', description: this.translate.instant('DASHBOARD.STATS.NETWORKS_DESC') },
        { title: this.translate.instant('DASHBOARD.STATS.USERS_TITLE'), rawNumber: data.totalUsers?.toLocaleString() || '0', description: this.translate.instant('DASHBOARD.STATS.USERS_DESC') },
        { title: this.translate.instant('DASHBOARD.STATS.CITIES_TITLE'), rawNumber: data.totalCities?.toLocaleString() || '0', description: `${this.translate.instant('DASHBOARD.STATS.IN')} ${data.totalCountries || 0} ${this.translate.instant('DASHBOARD.STATS.COUNTRIES')}` },
        { title: this.translate.instant('DASHBOARD.STATS.OPEN_TITLE'), rawNumber: (data.openNetworkPercent ? data.openNetworkPercent.toFixed(1) : '0') + '%', description: this.translate.instant('DASHBOARD.STATS.OPEN_DESC') }
      ];
      this.cdr.detectChanges();
    });
  }

  onHierarchicalGeoChange(filter: { country?: string, region?: string }) {
    this.lastGeoFilter = filter;
    const country = filter.country === 'Global' ? undefined : filter.country;
    const region = filter.region || undefined;

    if (!country) {
        this.geoChartTitle = this.translate.instant('DASHBOARD.CHART.TOP_COUNTRIES');
        this.dashboardService.getTopCountriesChart().subscribe(res => { this.geoHierarchicalData = res; this.cdr.detectChanges(); });
    } else if (!region) {
        this.geoChartTitle = `${this.translate.instant('DASHBOARD.CHART.TOP_REGIONS_IN')} ${country}`;
        this.dashboardService.getTopRegionsChart(country).subscribe(res => { this.geoHierarchicalData = res; this.cdr.detectChanges(); });
    } else {
        this.geoChartTitle = `${this.translate.instant('DASHBOARD.CHART.TOP_CITIES_IN')} ${region}`;
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
                label: this.translate.instant('SEC.' + d.label.replace(/\s+/g, '').toUpperCase()), 
                value: total > 0 ? Math.round(((d.value as number) / total) * 100 * 10) / 10 : 0 
            }));
            this.cdr.detectChanges();
        });
        break;
      case 'discoveries':
        this.dashboardService.getDiscoveriesChart(country, region).subscribe(data => { this.discoveriesData = data; this.cdr.detectChanges(); });
        break;
      case 'wpa3':
        this.lastWpa3Filter = filter;
        this.wpa3CardTitle = !country ? this.translate.instant('DASHBOARD.CHART.WPA3_COUNTRIES') : (!region ? `${this.translate.instant('DASHBOARD.CHART.WPA3_REGIONS')} ${country}` : `${this.translate.instant('DASHBOARD.CHART.WPA3_CITIES')} ${region}`);
        this.dashboardService.getTopCitiesWpa3(country, region).subscribe(res => { this.topCitiesWpa3Data = res; this.cdr.detectChanges(); });
        break;
      case 'insecure':
        this.lastInsecureFilter = filter;
        this.insecureCardTitle = !country ? this.translate.instant('DASHBOARD.CHART.INSECURE_COUNTRIES') : (!region ? `${this.translate.instant('DASHBOARD.CHART.INSECURE_REGIONS')} ${country}` : `${this.translate.instant('DASHBOARD.CHART.INSECURE_CITIES')} ${region}`);
        this.dashboardService.getTopInsecureCities(country, region).subscribe(res => { this.topInsecureCitiesData = res; this.cdr.detectChanges(); });
        break;
      case 'category':
        this.lastCategoryFilter = filter;
        this.categoryCardTitle = !country ? this.translate.instant('DASHBOARD.CHART.CAT_GLOBAL') : (!region ? `${this.translate.instant('DASHBOARD.CHART.CAT_IN')} ${country}` : `${this.translate.instant('DASHBOARD.CHART.CAT_IN')} ${region}`);
        this.dashboardService.getGlobalStats(country, region).subscribe((data: any) => {
            if (data?.categoryDistribution) {
                const total = Array.isArray(data.categoryDistribution) ? 0 : Object.values(data.categoryDistribution).reduce((acc: any, curr: any) => acc + curr, 0) as number;
                this.categoryData = Object.entries(data.categoryDistribution).map(([label, value]) => ({
                    label: this.translate.instant('CAT.' + label.toUpperCase()),
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
