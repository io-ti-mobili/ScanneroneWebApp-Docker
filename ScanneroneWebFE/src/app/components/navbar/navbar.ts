import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  imports: [CommonModule, RouterLink, RouterLinkActive, TranslateModule],
  templateUrl: './navbar.html',
  styles: [`
    .nav-container {
      display: flex;
      align-items: center;
      background-color: #1a1a1a;
      padding: 1rem 2rem;
      border-bottom: 1px solid #333;
      position: relative;
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
    
    /* Mobile Menu Toggle Button */
    .mobile-menu-btn {
      display: none;
      background: none;
      border: none;
      color: white;
      font-size: 1.5rem;
      cursor: pointer;
      padding: 0.5rem;
      margin-left: 1rem;
    }

    /* Desktop Navigation */
    .nav-menu {
      display: flex;
      align-items: center;
      flex-grow: 1;
    }
    .nav-links {
      display: flex;
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
      white-space: nowrap;
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

    /* Mobile Responsive Styles */
    @media (max-width: 768px) {
      .nav-container {
        padding: 1rem;
        flex-wrap: wrap;
      }
      .brand {
        margin-right: auto;
      }
      .mobile-menu-btn {
        display: block;
      }
      .nav-menu {
        display: none; /* Hidden by default on mobile */
        flex-direction: column;
        width: 100%;
        margin-top: 1rem;
        gap: 1rem;
      }
      .nav-menu.mobile-open {
        display: flex;
      }
      .nav-links {
        flex-direction: column;
        width: 100%;
        gap: 0.5rem;
      }
      .nav-button {
        margin-right: 0;
        width: 100%;
        text-align: center;
      }
      .lang-switch {
        margin-left: 0;
        justify-content: center;
        width: 100%;
        padding-top: 0.5rem;
        border-top: 1px solid #333;
      }
    }
  `]
})
export class Navbar {
  currentLang: string;
  isMobileMenuOpen = false;

  constructor(public translate: TranslateService) {
    this.currentLang = translate.currentLang || translate.getDefaultLang() || 'it';
  }

  switchLang(lang: string) {
    this.translate.use(lang);
    this.currentLang = lang;
    this.isMobileMenuOpen = false;
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
}
