import { Component, Input } from '@angular/core';

type StatTone = 'primary' | 'secondary' | 'accent' | 'danger';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  templateUrl: './stat-card.component.html',
  styleUrl: './stat-card.component.scss',
})
export class StatCardComponent {
  @Input() icon = '';
  @Input() badge = '';
  @Input() label = '';
  @Input() value = '';
  @Input() footer = '';
  @Input() meta = '';
  @Input() tone: StatTone = 'primary';

  get footerText(): string {
    return this.footer || this.meta;
  }
}
