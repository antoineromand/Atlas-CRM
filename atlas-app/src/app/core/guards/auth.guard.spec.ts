import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { firstValueFrom, of, Observable } from 'rxjs';
import { AuthStateService } from '../services/auth/auth-state.service';
import { AuthSessionService } from '../services/auth/auth-session.service';
import { authGuard, guestGuard } from './auth.guard';

function createJwt(expSecondsFromNow: number): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/g, '');
  const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + expSecondsFromNow }))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/g, '');

  return `${header}.${payload}.signature`;
}

describe('auth guards', () => {
  let authStateService: AuthStateService;
  let authSessionServiceSpy: jasmine.SpyObj<AuthSessionService>;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    authSessionServiceSpy = jasmine.createSpyObj<AuthSessionService>('AuthSessionService', [
      'ensureSession',
    ]);

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthSessionService, useValue: authSessionServiceSpy },
      ],
    });

    authStateService = TestBed.inject(AuthStateService);
    router = TestBed.inject(Router);
  });

  it('should redirect unauthenticated users to login when refresh fails', async () => {
    authSessionServiceSpy.ensureSession.and.returnValue(of(false));

    const result = await TestBed.runInInjectionContext(() =>
      firstValueFrom(authGuard({} as never, [] as never) as Observable<unknown>)
    );

    expect(result).toEqual(router.createUrlTree(['/login']));
  });

  it('should allow authenticated users on dashboard', () => {
    authStateService.setAccessToken(createJwt(60));

    const result = TestBed.runInInjectionContext(() => authGuard({} as never, [] as never));

    expect(result).toBeTrue();
  });

  it('should redirect authenticated users away from public pages', () => {
    authStateService.setAccessToken(createJwt(60));

    const result = TestBed.runInInjectionContext(() => guestGuard({} as never, [] as never));

    expect(result).toEqual(router.createUrlTree(['/dashboard']));
  });

  it('should redirect public users to dashboard when refresh succeeds', async () => {
    authSessionServiceSpy.ensureSession.and.returnValue(of(true));

    const result = await TestBed.runInInjectionContext(() =>
      firstValueFrom(guestGuard({} as never, [] as never) as Observable<unknown>)
    );

    expect(result).toEqual(router.createUrlTree(['/dashboard']));
  });
});
