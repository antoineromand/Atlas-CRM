import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BrandMarkComponent } from '../../../../shared/ui/brand-mark/brand-mark.component';
import { ScreenCardComponent } from '../../../../shared/ui/screen-card/screen-card.component';

@Component({
  selector: 'app-login-page-component',
  standalone: true,
  imports: [BrandMarkComponent, RouterLink, ScreenCardComponent],
  templateUrl: './login-page-component.html',
  styleUrl: './login-page-component.scss',
})
export class LoginPageComponent {}
