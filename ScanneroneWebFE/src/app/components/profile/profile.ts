import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ClassificaService } from '../../services/classifica.service';
import { UserProfileDto } from '../../dto/user-profile.dto';

import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, TranslateModule],
  templateUrl: './profile.html',
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
