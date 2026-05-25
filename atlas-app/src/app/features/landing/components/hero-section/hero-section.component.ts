import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ScreenCardComponent } from '../../../../shared/ui/screen-card/screen-card.component';
import { StatCardComponent } from '../../../../shared/ui/stat-card/stat-card.component';

export interface HeroStat {
  label: string;
  value: string;
  meta: string;
  tone: 'primary' | 'secondary' | 'accent' | 'danger';
}

@Component({
  selector: 'app-hero-section',
  standalone: true,
  imports: [RouterLink, ScreenCardComponent, StatCardComponent],
  templateUrl: './hero-section.component.html',
  styleUrl: './hero-section.component.scss',
})
export class HeroSectionComponent {
  @Input() eyebrow = '';
  @Input() title = '';
  @Input() description = '';
  @Input() primaryLabel = '';
  @Input() secondaryLabel = '';
  @Input() primaryHref = '/login';
  @Input() secondaryHref = '#dashboard';
  @Input() screenSrc = '/atlas/screens/dashboard/screen.png';
  @Input() screenAlt = 'Atlas CRM dashboard screenshot';
  @Input() screenLabel = 'Dashboard';
  @Input() stats: readonly HeroStat[] = [];
}
