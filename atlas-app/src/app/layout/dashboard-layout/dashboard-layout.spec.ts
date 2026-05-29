import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';

import { DashboardLayoutComponent } from './dashboard-layout';
import { AuthService } from '../../core/services/auth/auth.service';
import { AuthStateService } from '../../core/services/auth/auth-state.service';

describe('DashboardLayoutComponent', () => {
  let component: DashboardLayoutComponent;
  let fixture: ComponentFixture<DashboardLayoutComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let authStateServiceSpy: jasmine.SpyObj<AuthStateService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['logout']);
    authServiceSpy.logout.and.returnValue(of({ message: 'ok' }));

    authStateServiceSpy = jasmine.createSpyObj<AuthStateService>('AuthStateService', ['clear']);
    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [DashboardLayoutComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: AuthStateService, useValue: authStateServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should logout and redirect to login', () => {
    component.logout();

    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(authStateServiceSpy.clear).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });
});
