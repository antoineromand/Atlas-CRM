import { inject, Injectable } from '@angular/core';
import { map, Observable, of, shareReplay, catchError, finalize, tap } from 'rxjs';
import { AuthStateService } from './auth-state.service';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class AuthSessionService {
  private readonly authService = inject(AuthService);
  private readonly authStateService = inject(AuthStateService);
  private refreshInFlight$: Observable<string> | null = null;

  refreshAccessToken(): Observable<string> {
    const accessToken = this.authStateService.getAccessToken();

    if (accessToken) {
      return of(accessToken);
    }

    if (!this.refreshInFlight$) {
      this.refreshInFlight$ = this.authService.refreshToken().pipe(
        tap((tokens) => this.authStateService.setAccessToken(tokens.accessToken)),
        map((tokens) => tokens.accessToken),
        finalize(() => {
          this.refreshInFlight$ = null;
        }),
        shareReplay(1)
      );
    }

    return this.refreshInFlight$;
  }

  ensureSession(): Observable<boolean> {
    if (this.authStateService.isAuthenticated()) {
      return of(true);
    }

    return this.refreshAccessToken().pipe(
      map(() => true),
      catchError(() => {
        this.authStateService.clear();
        return of(false);
      })
    );
  }
}
