import { TestBed } from '@angular/core/testing';
import { AuthStateService } from './auth-state.service';

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

describe('AuthStateService', () => {
  let service: AuthStateService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthStateService);
  });

  it('should start empty', () => {
    expect(service.getAccessToken()).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
  });

  it('should store and clear the access token in memory', () => {
    const token = createJwt(60);
    service.setAccessToken(token);

    expect(service.getAccessToken()).toBe(token);
    expect(service.isAuthenticated()).toBeTrue();
    expect(localStorage.getItem('atlas.access_token')).toBe(token);

    service.clear();

    expect(service.getAccessToken()).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
    expect(localStorage.getItem('atlas.access_token')).toBeNull();
  });

  it('should treat expired tokens as unauthenticated', () => {
    service.setAccessToken(createJwt(-60));

    expect(service.getAccessToken()).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
    expect(localStorage.getItem('atlas.access_token')).toBeNull();
  });
});
