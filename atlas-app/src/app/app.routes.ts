import { Routes } from '@angular/router';
import { PublicLayoutComponent } from './layout/public-layout/public-layout';

export const routes: Routes = [
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/landing/page/landing-page-component/landing-page-component')
            .then(m => m.LandingPageComponent)
      },
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/page/login-page-component/login-page-component')
            .then(m => m.LoginPageComponent)
      },
    ],
  },
];
