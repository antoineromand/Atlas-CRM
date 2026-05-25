import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-screen-card',
  standalone: true,
  templateUrl: './screen-card.component.html',
  styleUrl: './screen-card.component.scss',
})
export class ScreenCardComponent {
  @Input() imageSrc = '';
  @Input() imageAlt = '';
  @Input() title = '';
  @Input() description = '';
  @Input() frameLabel = 'Stitch';
}
