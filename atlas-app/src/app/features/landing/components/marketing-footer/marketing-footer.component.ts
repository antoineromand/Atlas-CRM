import { Component } from '@angular/core';
import { BrandMarkComponent } from '../../../../shared/ui/brand-mark/brand-mark.component';

@Component({
  selector: 'app-marketing-footer',
  standalone: true,
  imports: [BrandMarkComponent],
  templateUrl: './marketing-footer.component.html',
  styleUrl: './marketing-footer.component.scss',
})
export class MarketingFooterComponent {}
