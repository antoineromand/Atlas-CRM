import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-testimonial-band',
  standalone: true,
  templateUrl: './testimonial-band.component.html',
  styleUrl: './testimonial-band.component.scss',
})
export class TestimonialBandComponent {
  @Input() items: readonly TestimonialItem[] = [];
}

export interface TestimonialItem {
  quote: string;
  author: string;
  role: string;
}
