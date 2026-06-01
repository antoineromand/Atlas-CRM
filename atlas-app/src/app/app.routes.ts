import { Routes } from '@angular/router';
import { PublicLayoutComponent } from './layout/public-layout/public-layout';
import { DashboardLayoutComponent } from './layout/dashboard-layout/dashboard-layout';
import { authGuard, guestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        canMatch: [guestGuard],
        loadComponent: () =>
          import('./features/landing/page/landing-page-component/landing-page-component')
            .then((m) => m.LandingPageComponent),
      },
      {
        path: 'register',
        canMatch: [guestGuard],
        loadComponent: () =>
          import('./features/auth/page/register-page-component/register-page-component')
            .then((m) => m.RegisterPageComponent),
      },
      {
        path: 'login',
        canMatch: [guestGuard],
        loadComponent: () =>
          import('./features/auth/page/login-page-component/login-page-component')
            .then((m) => m.LoginPageComponent),
      },
    ],
  },
  {
    path: 'dashboard',
    canMatch: [authGuard],
    component: DashboardLayoutComponent,
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/dashboard/pages/main-dashboard-page-component/main-dashboard-page-component')
            .then((m) => m.MainDashboardPageComponent),
      },
      {
        path: 'settings',
        loadComponent: () =>
          import('./features/settings/page/settings-page-component/settings-page-component')
            .then((m) => m.SettingsPageComponent),
      },
    ],
  },
];
