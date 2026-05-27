import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, finalize, map, Observable, of, shareReplay, switchMap, throwError, tap } from 'rxjs';
import { AuthStateService } from '../services/auth/auth-state.service';
import { AuthService } from '../services/auth/auth.service';
import { NotificationService } from '../services/notification/notification.service';

let refreshInFlight$: Observable<string> | null = null;

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const authState = inject(AuthStateService);
  const notificationService = inject(NotificationService);
  const router = inject(Router);

  if (request.url.includes('/api/v1/authentication')) {
    return next(request);
  }

  const accessToken = authState.getAccessToken();
  const authorizedRequest = accessToken
    ? request.clone({
      setHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
    })
    : request;

  return next(authorizedRequest).pipe(
    catchError((error) => {
      if (error?.status !== 401 || request.url.includes('/api/v1/authentication')) {
        return throwError(() => error);
      }

      return getRefreshedToken(authService, authState, notificationService, router).pipe(
        switchMap((newAccessToken) =>
          next(
            request.clone({
              setHeaders: {
                Authorization: `Bearer ${newAccessToken}`,
              },
            })
          )
        ),
        catchError((refreshError) => throwError(() => refreshError))
      );
    })
  );
};

function getRefreshedToken(
  authService: AuthService,
  authState: AuthStateService,
  notificationService: NotificationService,
  router: Router
): Observable<string> {
  if (!refreshInFlight$) {
    refreshInFlight$ = authService.refreshToken().pipe(
      tap((tokens) => {
        authState.setAccessToken(tokens.accessToken);
      }),
      map((tokens) => tokens.accessToken),
      catchError((error) => {
        authState.clear();
        notificationService.info('Your session expired. Please sign in again.', 'Session expired');
        void router.navigateByUrl('/login');
        return throwError(() => error);
      }),
      finalize(() => {
        refreshInFlight$ = null;
      }),
      shareReplay(1)
    );
  }

  return refreshInFlight$!;
}
