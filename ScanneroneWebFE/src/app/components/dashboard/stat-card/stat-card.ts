import { Component, Input } from '@angular/core';
import { SimpleStatDto } from '../../../dto/simple-stat.dto';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-stat-card',
  imports: [CommonModule],
  template: `
    <div class="stat-card">
      <div class="stat-title">{{ stat.title }}</div>
      <div class="stat-value">{{ stat.rawNumber }}</div>
      <div class="stat-desc">{{ stat.description }}</div>
    </div>
  `,
  styles: [`
    .stat-card {
      background-color: #282828;
      border-radius: 12px;
      padding: 1.5rem;
      border: 1px solid #333;
    }
    .stat-title {
      color: #a0a0a0;
      font-size: 0.9rem;
      margin-bottom: 0.5rem;
    }
    .stat-value {
      color: #fff;
      font-size: 2rem;
      font-weight: 600;
      margin-bottom: 0.5rem;
    }
    .stat-desc {
      color: #a0a0a0;
      font-size: 0.85rem;
    }
  `]
})
export class StatCard {
  @Input() stat!: SimpleStatDto;
}
