import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthStateService } from '../services/auth/auth-state.service';
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
  let router: Router;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideRouter([])],
    });

    authStateService = TestBed.inject(AuthStateService);
    router = TestBed.inject(Router);
  });

  it('should redirect unauthenticated users to login', () => {
    const result = TestBed.runInInjectionContext(() => authGuard({} as never, [] as never));

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
});
