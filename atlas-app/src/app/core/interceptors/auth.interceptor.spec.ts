import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthStateService } from '../services/auth/auth-state.service';
import { NotificationService } from '../services/notification/notification.service';

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

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authStateService: AuthStateService;
  let routerSpy: jasmine.SpyObj<Router>;
  let notificationService: NotificationService;

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigateByUrl']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: Router, useValue: routerSpy },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authStateService = TestBed.inject(AuthStateService);
    notificationService = TestBed.inject(NotificationService);
  });

  afterEach(() => {
    httpMock.verify();
    authStateService.clear();
    notificationService.clear();
  });

  it('should attach the access token to authenticated requests', () => {
    authStateService.setAccessToken(createJwt(60));

    http.get('/api/private').subscribe((response) => {
      expect(response).toEqual({ ok: true });
    });

    const req = httpMock.expectOne('/api/private');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${authStateService.getAccessToken()}`);
    req.flush({ ok: true });
  });

  it('should refresh the access token and retry the request after a 401', () => {
    authStateService.setAccessToken(createJwt(-60));
    let result: unknown;

    http.get('/api/private').subscribe((response) => {
      result = response;
    });

    const initialReq = httpMock.expectOne('/api/private');
    expect(initialReq.request.headers.get('Authorization')).toBeNull();
    initialReq.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    const refreshReq = httpMock.expectOne('http://localhost:3000/api/v1/authentication/refresh-token');
    expect(refreshReq.request.withCredentials).toBeTrue();
    refreshReq.flush({ accessToken: 'new-access-token', refreshToken: 'new-refresh-token' });

    const retriedReq = httpMock.expectOne('/api/private');
    expect(retriedReq.request.headers.get('Authorization')).toBe('Bearer new-access-token');
    retriedReq.flush({ ok: true });

    expect(result).toEqual({ ok: true });
    expect(authStateService.getAccessToken()).toBe('new-access-token');
  });

  it('should refresh the access token and retry the request after a 403', () => {
    authStateService.setAccessToken(createJwt(-60));
    let result: unknown;

    http.get('/api/private').subscribe((response) => {
      result = response;
    });

    const initialReq = httpMock.expectOne('/api/private');
    expect(initialReq.request.headers.get('Authorization')).toBeNull();
    initialReq.flush({ message: 'Forbidden' }, { status: 403, statusText: 'Forbidden' });

    const refreshReq = httpMock.expectOne('http://localhost:3000/api/v1/authentication/refresh-token');
    expect(refreshReq.request.withCredentials).toBeTrue();
    refreshReq.flush({ accessToken: 'new-access-token', refreshToken: 'new-refresh-token' });

    const retriedReq = httpMock.expectOne('/api/private');
    expect(retriedReq.request.headers.get('Authorization')).toBe('Bearer new-access-token');
    retriedReq.flush({ ok: true });

    expect(result).toEqual({ ok: true });
    expect(authStateService.getAccessToken()).toBe('new-access-token');
  });

  it('should redirect to login when refresh fails', () => {
    authStateService.setAccessToken(createJwt(-60));
    let errorResponse: unknown;

    http.get('/api/private').subscribe({
      error: (error) => {
        errorResponse = error;
      },
    });

    const initialReq = httpMock.expectOne('/api/private');
    initialReq.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    const refreshReq = httpMock.expectOne('http://localhost:3000/api/v1/authentication/refresh-token');
    refreshReq.flush(
      { code: 'INVALID_TOKEN', message: 'The token is invalid or expired.' },
      { status: 401, statusText: 'Unauthorized' }
    );

    expect(errorResponse).toBeTruthy();
    expect(authStateService.getAccessToken()).toBeNull();
    expect(notificationService.notifications().length).toBe(1);
    expect(routerSpy.navigateByUrl).toHaveBeenCalledWith('/login');
  });
});
