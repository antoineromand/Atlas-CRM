import { TestBed } from '@angular/core/testing';
import { AuthStateService } from './auth-state.service';

describe('AuthStateService', () => {
  let service: AuthStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthStateService);
  });

  it('should start empty', () => {
    expect(service.getAccessToken()).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
  });

  it('should store and clear the access token in memory', () => {
    service.setAccessToken('access-token');

    expect(service.getAccessToken()).toBe('access-token');
    expect(service.isAuthenticated()).toBeTrue();

    service.clear();

    expect(service.getAccessToken()).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
  });
});
