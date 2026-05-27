import { Component, HostBinding, Input } from '@angular/core';

@Component({
  selector: 'app-brand-mark',
  standalone: true,
  templateUrl: './brand-mark.component.html',
  styleUrl: './brand-mark.component.scss',
})
export class BrandMarkComponent {
  @Input() size = '2.5rem';

  @HostBinding('style.--brand-mark-size')
  get brandMarkSize() {
    return this.size;
  }
}
