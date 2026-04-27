import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
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
  `]
})
export class Navbar {}
