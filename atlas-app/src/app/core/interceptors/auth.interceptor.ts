import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthStateService } from '../services/auth/auth-state.service';
import { AuthSessionService } from '../services/auth/auth-session.service';
import { NotificationService } from '../services/notification/notification.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authSessionService = inject(AuthSessionService);
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

      return authSessionService.refreshAccessToken().pipe(
        switchMap((newAccessToken) =>
          next(
            request.clone({
              setHeaders: {
                Authorization: `Bearer ${newAccessToken}`,
              },
            })
          )
        ),
        catchError((refreshError) => {
          authState.clear();
          notificationService.info('Your session expired. Please sign in again.', 'Session expired');
          void router.navigateByUrl('/login');
          return throwError(() => refreshError);
        })
      );
    })
  );
};
