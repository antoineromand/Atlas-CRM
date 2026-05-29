import { Routes } from '@angular/router';
import { PublicLayoutComponent } from './layout/public-layout/public-layout';
import { DashboardLayoutComponent } from './layout/dashboard-layout/dashboard-layout';

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
        path: 'register',
        loadComponent: () =>
          import('./features/auth/page/register-page-component/register-page-component')
            .then(m => m.RegisterPageComponent)
      },
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/page/login-page-component/login-page-component')
            .then(m => m.LoginPageComponent)
      },
    ],
  },
  {
    path: 'dashboard',
    component: DashboardLayoutComponent,
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/dashboard/pages/main-dashboard-page-component//main-dashboard-page-component')
            .then(m => m.MainDashboardPageComponent),
      }
    ],
  },
];
