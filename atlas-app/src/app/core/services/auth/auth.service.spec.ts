import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should send login requests with credentials', () => {
    const payload = { email: 'jane@studio.com', password: 'Password123!' };
    const response = { accessToken: 'access', refreshToken: 'refresh' };

    service.login(payload).subscribe((tokens) => {
      expect(tokens).toEqual(response);
    });

    const req = httpMock.expectOne('http://localhost:3000/api/v1/authentication/sign-in');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBeTrue();
    expect(req.request.body).toEqual(payload);
    req.flush(response);
  });

  it('should send refresh token requests with credentials', () => {
    const response = { accessToken: 'new-access', refreshToken: 'new-refresh' };

    service.refreshToken().subscribe((tokens) => {
      expect(tokens).toEqual(response);
    });

    const req = httpMock.expectOne('http://localhost:3000/api/v1/authentication/refresh-token');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBeTrue();
    req.flush(response);
  });

  it('should send logout requests with credentials', () => {
    service.logout().subscribe((response) => {
      expect(response).toEqual({ message: 'Sign out successful.' });
    });

    const req = httpMock.expectOne('http://localhost:3000/api/v1/authentication/sign-out');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBeTrue();
    req.flush({ message: 'Sign out successful.' });
  });
});
