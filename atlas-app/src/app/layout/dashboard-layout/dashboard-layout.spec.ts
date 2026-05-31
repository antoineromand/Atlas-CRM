import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { DashboardLayoutComponent } from './dashboard-layout';
import { AuthService } from '../../core/services/auth/auth.service';
import { AuthStateService } from '../../core/services/auth/auth-state.service';

describe('DashboardLayoutComponent', () => {
  let component: DashboardLayoutComponent;
  let fixture: ComponentFixture<DashboardLayoutComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let authStateServiceSpy: jasmine.SpyObj<AuthStateService>;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['logout']);
    authServiceSpy.logout.and.returnValue(of({ message: 'ok' }));

    authStateServiceSpy = jasmine.createSpyObj<AuthStateService>('AuthStateService', ['clear']);

    await TestBed.configureTestingModule({
      imports: [DashboardLayoutComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: AuthStateService, useValue: authStateServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardLayoutComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should logout and redirect to login', () => {
    component.logout();

    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(authStateServiceSpy.clear).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
