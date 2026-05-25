import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BrandMarkComponent } from '../../../../shared/ui/brand-mark/brand-mark.component';

export interface MarketingLink {
  label: string;
  href: string;
}

@Component({
  selector: 'app-marketing-header',
  standalone: true,
  imports: [BrandMarkComponent, RouterLink],
  templateUrl: './marketing-header.component.html',
  styleUrl: './marketing-header.component.scss',
})
export class MarketingHeaderComponent {
  @Input() links: readonly MarketingLink[] = [];
  @Input() ctaLabel = 'Join us';
  @Input() ctaHref = '/login';
}
