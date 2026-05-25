import { Component, Input } from '@angular/core';

type FeatureTone = 'primary' | 'secondary' | 'accent';

@Component({
  selector: 'app-feature-card',
  standalone: true,
  templateUrl: './feature-card.component.html',
  styleUrl: './feature-card.component.scss',
})
export class FeatureCardComponent {
  @Input() icon = 'dashboard';
  @Input() title = '';
  @Input() description = '';
  @Input() tone: FeatureTone = 'primary';
}
