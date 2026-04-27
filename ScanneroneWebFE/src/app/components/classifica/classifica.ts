import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SelectModule } from 'primeng/select';
import { PaginatorModule } from 'primeng/paginator';
import { InputTextModule } from 'primeng/inputtext';
import { ClassificaService } from '../../services/classifica.service';
import { DashboardService } from '../../services/dashboard.service';
import { LeaderboardEntryDto } from '../../dto/leaderboard-entry.dto';
import { CountryDto } from '../../dto/country.dto';

@Component({
  selector: 'app-classifica',
  imports: [CommonModule, FormsModule, SelectModule, PaginatorModule, InputTextModule],
  template: `
    <div class="classifica-container">
      <div class="filters-header">
        <div class="filters">
          <button class="filter-btn" [class.active]="activeCategory === 'global'" (click)="setCategory('global')">Global Score</button>
          <button class="filter-btn" [class.active]="activeCategory === 'discovery'" (click)="setCategory('discovery')">Scopritori</button>
          <button class="filter-btn" [class.active]="activeCategory === 'travelers'" (click)="setCategory('travelers')">Esploratori</button>
        </div>

        <div class="geo-filters">
           <p-select [options]="countryOptions" 
                    [(ngModel)]="selectedCountry" 
                    (onChange)="onCountryChangeLocal()" 
                    styleClass="p-dropdown-sm custom-geo"></p-select>

           <p-select *ngIf="selectedCountry !== 'Global' && regionOptions.length > 0"
                    [options]="regionOptions" 
                    [(ngModel)]="selectedRegion" 
                    (onChange)="onRegionChangeLocal()" 
                    styleClass="p-dropdown-sm custom-geo"></p-select>
        </div>
      </div>

      <div class="leaderboard-card">
        <div class="leaderboard-row" *ngFor="let entry of paginatedLeaderboard; let isLast=last; let i=index" [class.border-bottom]="!isLast">
          <div class="rank" [ngClass]="getRankColor(entry.rank)">{{ entry.rank }}</div>
          
          <div class="user-info">
            <div class="username-container">
              <div class="username">{{ entry.username }}</div>
              <div class="user-uuid">{{ entry.deviceToken }}</div>
            </div>
            <div class="location-stats">
              {{ entry.citiesCovered }} città &middot; {{ entry.uniqueDiscovered | number:'1.0-0' }} reti uniche
            </div>
          </div>
          
          <div class="score-info">
            <div class="score">{{ entry.score | number }} pt</div>
            <div class="accuracy" *ngIf="entry.avgAccuracy">accuracy media {{ entry.avgAccuracy | number:'1.1-1' }}m</div>
          </div>
        </div>

        <p-paginator 
          [first]="first" 
          [rows]="rows" 
          [totalRecords]="totalRecords" 
          (onPageChange)="onPageChange($event)"
          [showCurrentPageReport]="true"
          currentPageReportTemplate="{first} - {last} di {totalRecords}"
          styleClass="custom-paginator">
        </p-paginator>
      </div>
    </div>
  `,
  styles: [`
    .classifica-container {
      max-width: 900px;
      margin: 0 auto;
    }
    .filters-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
      flex-wrap: wrap;
      gap: 1rem;
    }
    .geo-filters {
      display: flex;
      gap: 0.75rem;
    }
    ::ng-deep .custom-geo.p-select {
        background: #282828 !important;
        border: 1px solid #333 !important;
        border-radius: 8px !important;
        min-width: 120px;
    }
    ::ng-deep .custom-geo .p-select-label {
        color: #e0e0e0 !important;
        font-size: 0.9rem !important;
        padding: 0.4rem 0.75rem !important;
    }
    .filters {
      display: flex;
      gap: 0.5rem;
    }
    .filter-btn {
      background-color: transparent;
      border: 1px solid #444;
      color: #e0e0e0;
      padding: 0.5rem 1rem;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      font-size: 0.95rem;
    }
    .filter-btn:hover {
      background-color: #333;
    }
    .filter-btn.active {
      background-color: #333;
      border-color: #666;
      color: white;
    }
    .leaderboard-card {
      background-color: #282828;
      border-radius: 12px;
      border: 1px solid #333;
      overflow: hidden;
    }
    .leaderboard-row {
      display: flex;
      align-items: center;
      padding: 1.25rem 1.5rem;
      transition: background-color 0.2s;
    }
    .border-bottom {
      border-bottom: 1px solid #333;
    }
    .rank {
      width: 2rem;
      font-weight: bold;
      font-size: 1.1rem;
    }
    .rank.gold { color: #f59e0b; }
    .rank.silver { color: #a1a1aa; }
    .rank.bronze { color: #d97706; }
    .rank.normal { color: #a0a0a0; }
    
    .user-info {
      flex-grow: 1;
      margin-left: 1.25rem;
    }
    .username {
      color: white;
      font-weight: 500;
      font-size: 1.1rem;
    }
    .user-uuid {
      color: #666;
      font-size: 0.75rem;
      font-family: monospace;
      margin-top: 0.1rem;
    }
    .location-stats {
      color: #a0a0a0;
      font-size: 0.85rem;
      margin-top: 0.4rem;
    }
    
    .score-info {
      text-align: right;
    }
    .score {
      color: #3b82f6;
      font-weight: 500;
      font-size: 1.1rem;
    }
    .accuracy {
      color: #a0a0a0;
      font-size: 0.85rem;
      margin-top: 0.2rem;
    }

    ::ng-deep .custom-dropdown.p-select {
        background: transparent !important;
        border: 1px solid #444 !important;
        border-radius: 8px !important;
        min-width: 140px;
    }
    ::ng-deep .custom-dropdown .p-select-label {
        color: #e0e0e0 !important;
        padding: 0.5rem 1rem !important;
        font-size: 0.95rem;
    }
    ::ng-deep .custom-dropdown .p-select-trigger {
        color: #a0a0a0 !important;
        width: 2.5rem !important;
    }
    ::ng-deep .custom-dropdown.p-select:hover {
        background: #333 !important;
    }
    ::ng-deep .p-select-list-container {
        background: #282828 !important;
        border: 1px solid #444 !important;
        border-radius: 8px !important;
        margin-top: 4px;
    }
    ::ng-deep .p-select-option {
        color: #e0e0e0 !important;
        background: transparent;
        padding: 0.75rem 1rem !important;
        transition: background-color 0.2s;
    }
    ::ng-deep .p-select-option:hover {
        background: #333 !important;
    }
    ::ng-deep .p-select-option.p-select-option-selected {
        background: #3b82f6 !important;
        color: white !important;
    }

    ::ng-deep .custom-paginator.p-paginator {
      background: transparent !important;
      border: none !important;
      border-top: 1px solid #333 !important;
      padding: 1rem !important;
      color: #a0a0a0 !important;
    }
    ::ng-deep .custom-paginator .p-paginator-current {
      color: #a0a0a0 !important;
    }
    ::ng-deep .custom-paginator .p-paginator-page, 
    ::ng-deep .custom-paginator .p-paginator-next, 
    ::ng-deep .custom-paginator .p-paginator-prev, 
    ::ng-deep .custom-paginator .p-paginator-first, 
    ::ng-deep .custom-paginator .p-paginator-last {
      background: transparent !important;
      border: 1px solid transparent !important;
      color: #a0a0a0 !important;
      border-radius: 8px !important;
      min-width: 2.5rem !important;
      height: 2.5rem !important;
    }
    ::ng-deep .custom-paginator .p-paginator-page:hover, 
    ::ng-deep .custom-paginator .p-paginator-next:hover, 
    ::ng-deep .custom-paginator .p-paginator-prev:hover {
      background: #333 !important;
      color: #fff !important;
    }
    ::ng-deep .custom-paginator .p-paginator-page.p-paginator-page-selected {
      background: #3b82f6 !important;
      color: #fff !important;
      font-weight: bold !important;
    }
  `]
})
export class Classifica implements OnInit {
  paginatedLeaderboard: LeaderboardEntryDto[] = [];
  totalRecords: number = 0;
  rows: number = 50;
  first: number = 0;
  activeCategory: string = 'global';
  
  // Hierarchical local filters
  countryOptions: string[] = ['Global'];
  regionOptions: string[] = [];
  selectedCountry: string = 'Global';
  selectedRegion: string = 'Regions';

  constructor(
    private classificaService: ClassificaService,
    private dashboardService: DashboardService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}
  
  ngOnInit() {
    this.fetchTotalCount();
    this.loadData();
    this.loadCountries();
  }

  loadCountries() {
    this.dashboardService.getCountries().subscribe(res => {
        this.countryOptions = ['Global', ...res];
    });
  }

  onCountryChangeLocal() {
    this.regionOptions = [];
    this.selectedRegion = 'Regions';

    if (this.selectedCountry !== 'Global') {
        this.dashboardService.getRegions(this.selectedCountry).subscribe(res => {
            this.regionOptions = ['Regions', ...res];
            this.cdr.detectChanges();
        });
    }
    this.first = 0; // Reset pagination
    this.fetchTotalCount(); // Aggiorna il totale per il nuovo filtro
    this.loadData();
  }

  onRegionChangeLocal() {
    this.first = 0; // Reset pagination
    this.fetchTotalCount(); // Aggiorna il totale per il nuovo filtro
    this.loadData();
  }

  fetchTotalCount() {
    const country = this.selectedCountry === 'Global' ? undefined : this.selectedCountry;
    const region = this.selectedRegion === 'Regions' ? undefined : this.selectedRegion;

    this.dashboardService.getGlobalStats(country, region).subscribe((data: any) => {
      if (data) {
        this.totalRecords = data.totalUsers || 0;
        this.cdr.detectChanges();
      }
    });
  }

  setCategory(category: string) {
    this.activeCategory = category;
    this.first = 0; 
    this.fetchTotalCount(); // Assicuriamoci che il totale sia corretto per la categoria (se il backend lo supporta)
    this.loadData();
  }

  loadData() {
    const country = this.selectedCountry === 'Global' ? undefined : this.selectedCountry;
    const region = this.selectedRegion === 'Regions' ? undefined : this.selectedRegion;

    this.classificaService.getLeaderboard(
        this.activeCategory, 
        this.rows, 
        this.first,
        country,
        region
    ).subscribe(data => {
        this.paginatedLeaderboard = data;
        this.cdr.detectChanges();
    });
  }

  onPageChange(event: any) {
    this.first = event.first;
    this.loadData();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  getRankColor(rank: number): string {
    if (rank === 1) return 'gold';
    if (rank === 2) return 'silver';
    if (rank === 3) return 'bronze';
    return 'normal';
  }
}
