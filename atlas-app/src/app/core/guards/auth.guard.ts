import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { AuthStateService } from '../services/auth/auth-state.service';

export const authGuard: CanMatchFn = () => {
  const authStateService = inject(AuthStateService);
  const router = inject(Router);

  return authStateService.isAuthenticated() ? true : router.createUrlTree(['/login']);
};

export const guestGuard: CanMatchFn = () => {
  const authStateService = inject(AuthStateService);
  const router = inject(Router);

  return authStateService.isAuthenticated() ? router.createUrlTree(['/dashboard']) : true;
};
