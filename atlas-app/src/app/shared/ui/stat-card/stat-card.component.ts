import { Component, Input } from '@angular/core';

type StatTone = 'primary' | 'secondary' | 'accent' | 'danger';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  templateUrl: './stat-card.component.html',
  styleUrl: './stat-card.component.scss',
})
export class StatCardComponent {
  @Input() label = '';
  @Input() value = '';
  @Input() meta = '';
  @Input() tone: StatTone = 'primary';
}
