import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslateService, TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive, TranslateModule],
  templateUrl: './navbar.html',
  styles: [`
    .nav-container {
      display: flex;
      align-items: center;
      background-color: #1a1a1a;
      padding: 1rem 2rem;
      border-bottom: 1px solid #333;
    }
    .brand {
      color: white;
      font-weight: bold;
      font-size: 1.25rem;
      margin-right: 2rem;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      cursor: pointer;
      text-decoration: none;
    }
    .nav-links {
      display: flex;
      flex-grow: 1;
    }
    .nav-button {
      background-color: transparent;
      border: 1px solid #444;
      color: #a0a0a0;
      padding: 0.5rem 1.25rem;
      border-radius: 8px;
      margin-right: 0.5rem;
      cursor: pointer;
      text-decoration: none;
      transition: all 0.2s;
    }
    .nav-button:hover {
      background-color: #333;
    }
    .nav-button.active {
      background-color: #2a2a2a;
      color: white;
      border-color: #555;
    }
    .lang-switch {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-left: auto;
    }
    .lang-switch button {
      background: none;
      border: none;
      color: #a0a0a0;
      cursor: pointer;
      font-size: 0.9rem;
      font-weight: bold;
      padding: 0.25rem;
    }
    .lang-switch button:hover {
      color: #fff;
    }
    .lang-switch button.active {
      color: #3b82f6;
    }
    .sep {
      color: #444;
    }
  `]
})
export class Navbar {
  currentLang: string;

  constructor(public translate: TranslateService) {
    this.currentLang = translate.currentLang || translate.getDefaultLang() || 'it';
  }

  switchLang(lang: string) {
    this.translate.use(lang);
    this.currentLang = lang;
  }
}
