import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthStateService } from '../services/auth/auth-state.service';
import { AuthSessionService } from '../services/auth/auth-session.service';

export const authGuard: CanMatchFn = () => {
  const authStateService = inject(AuthStateService);
  const authSessionService = inject(AuthSessionService);
  const router = inject(Router);

  if (authStateService.isAuthenticated()) {
    return true;
  }

  return authSessionService.ensureSession().pipe(
    map((hasSession) => (hasSession ? true : router.createUrlTree(['/login'])))
  );
};

export const guestGuard: CanMatchFn = () => {
  const authStateService = inject(AuthStateService);
  const authSessionService = inject(AuthSessionService);
  const router = inject(Router);

  if (authStateService.isAuthenticated()) {
    return router.createUrlTree(['/dashboard']);
  }

  return authSessionService.ensureSession().pipe(
    map((hasSession) => (hasSession ? router.createUrlTree(['/dashboard']) : true))
  );
};
