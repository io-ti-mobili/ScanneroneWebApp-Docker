import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ClassificaService } from '../../services/classifica.service';
import { UserProfileDto } from '../../dto/user-profile.dto';

@Component({
  selector: 'app-profile',
  imports: [CommonModule],
  template: `
    <div class="profile-container" *ngIf="profile">
      <!-- Bottone Indietro -->
      <button class="back-btn" (click)="goBack()">
        <i class="pi pi-arrow-left"></i> Torna alla Classifica
      </button>

      <!-- Intestazione Profilo -->
      <div class="profile-header">
        <div class="user-main">
          <div class="avatar-large">{{ profile.avatar }}</div>
          <div class="user-details">
            <h1 class="username">{{ profile.username }}</h1>
            <p class="subtitle">Attivo da {{ profile.joinDate }} &middot; {{ profile.location }}</p>
          </div>
        </div>

        <div class="score-main">
          <h1 class="score-large">{{ profile.score }}</h1>
          <p class="score-subtitle">punti totali &middot; #{{ profile.globalRank }} globale</p>
        </div>
      </div>

      <!-- Griglia delle Card -->
      <div class="cards-grid">
        
        <!-- Card: Le tue reti -->
        <div class="profile-card">
          <h2 class="card-title">Le tue reti</h2>
          
          <div class="stat-row">
            <span class="stat-label">Caricate totali</span>
            <span class="stat-value font-bold">{{ profile.reti.totali | number:'1.0-0' }}</span>
          </div>
          
          <div class="stat-row">
            <span class="stat-label">Uniche (nuove per il DB)</span>
            <span class="stat-value blue-text">{{ profile.reti.uniche | number:'1.0-0' }}</span>
          </div>
          
          <div class="stat-row">
            <span class="stat-label">Accuracy media</span>
            <span class="stat-value font-bold">{{ profile.reti.accuracy }}</span>
          </div>
          
          <div class="stat-row no-border">
            <span class="stat-label">Con indirizzo completo</span>
            <span class="stat-value font-bold">{{ profile.reti.indirizzoCompleto }}</span>
          </div>
        </div>

        <!-- Card: Copertura geografica -->
        <div class="profile-card">
          <h2 class="card-title">Copertura geografica</h2>
          
          <div class="stat-row">
            <span class="stat-label">Paesi</span>
            <span class="stat-value font-bold">{{ profile.geografia.paesi }}</span>
          </div>
          
          <div class="stat-row">
            <span class="stat-label">Regioni</span>
            <span class="stat-value font-bold">{{ profile.geografia.regioni }}</span>
          </div>
          
          <div class="stat-row">
            <span class="stat-label">Città</span>
            <span class="stat-value font-bold">{{ profile.geografia.citta }}</span>
          </div>
          
          <div class="stat-row no-border">
            <span class="stat-label">Nuove città scoperte</span>
            <span class="stat-value blue-text">{{ profile.geografia.nuoveCitta }}</span>
          </div>
        </div>

      </div>
    </div>

    <!-- Spinner se sta caricando -->
    <div class="loading-state" *ngIf="!profile">
      <i class="pi pi-spin pi-spinner" style="font-size: 2rem; color: #3b82f6;"></i>
    </div>
  `,
  styles: [`
    .profile-container {
      max-width: 900px;
      margin: 0 auto;
      padding: 1rem 0;
    }
    .back-btn {
      background: transparent;
      border: none;
      color: #a0a0a0;
      cursor: pointer;
      font-size: 0.95rem;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 2rem;
      transition: color 0.2s;
    }
    .back-btn:hover {
      color: #fff;
    }
    
    /* Header Profilo */
    .profile-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 3rem;
      flex-wrap: wrap;
      gap: 2rem;
    }
    .user-main {
      display: flex;
      align-items: center;
      gap: 1.5rem;
    }
    .avatar-large {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background-color: #bfdbfe; /* celeste chiaro */
      color: #1e3a8a; /* blu scuro */
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.75rem;
      font-weight: bold;
    }
    .username {
      color: #ffffff;
      font-size: 1.75rem;
      font-weight: 500;
      margin: 0 0 0.25rem 0;
    }
    .subtitle {
      color: #a0a0a0;
      margin: 0;
      font-size: 0.95rem;
    }
    
    .score-main {
      text-align: right;
    }
    .score-large {
      color: #3b82f6; /* Blu */
      font-size: 2.5rem;
      font-weight: 500;
      margin: 0 0 0.25rem 0;
    }
    .score-subtitle {
      color: #a0a0a0;
      margin: 0;
      font-size: 0.95rem;
    }

    /* Griglia delle Card */
    .cards-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 1.5rem;
    }
    @media (min-width: 768px) {
      .cards-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }

    /* Stile delle singole Card */
    .profile-card {
      background-color: #282828;
      border: 1px solid #333;
      border-radius: 12px;
      padding: 1.5rem;
    }
    .card-title {
      color: #ffffff;
      font-size: 1.2rem;
      font-weight: 500;
      margin: 0 0 1.5rem 0;
    }

    /* Righe statistiche dentro la card */
    .stat-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem 0;
      border-bottom: 1px solid #3a3a3a;
    }
    .stat-row.no-border {
      border-bottom: none;
      padding-bottom: 0;
    }
    .stat-label {
      color: #d1d5db; /* grigio chiaro */
      font-size: 1rem;
    }
    .stat-value {
      font-size: 1.1rem;
      color: #ffffff;
    }
    .font-bold {
      font-weight: 600;
    }
    .blue-text {
      color: #3b82f6;
      font-weight: 600;
    }

    .loading-state {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 400px;
    }
  `]
})
export class Profile implements OnInit {
  profile: UserProfileDto | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private classificaService: ClassificaService
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const username = params.get('username');
      if (username) {
        this.classificaService.getUserProfile(username).subscribe(data => {
          this.profile = data;
        });
      }
    });
  }

  goBack() {
    this.router.navigate(['/classifica']);
  }
}
