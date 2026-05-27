import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { AuthService } from './core/services/auth/auth.service';
import { AuthStateService } from './core/services/auth/auth-state.service';
import { catchError, firstValueFrom, of, tap } from 'rxjs';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAppInitializer(() => {
      const authService = inject(AuthService);
      const authStateService = inject(AuthStateService);

      return firstValueFrom(
        authService.refreshToken().pipe(
          tap((tokens) => {
            authStateService.setAccessToken(tokens.accessToken);
          }),
          catchError(() => of(null))
        )
      );
    })
  ]
};
