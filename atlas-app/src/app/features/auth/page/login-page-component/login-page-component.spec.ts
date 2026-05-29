import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginPageComponent } from './login-page-component';
import { AuthService } from '../../../../core/services/auth/auth.service';
import { AuthStateService } from '../../../../core/services/auth/auth-state.service';
import { NotificationService } from '../../../../core/services/notification/notification.service';
import { Router } from '@angular/router';

describe('LoginPageComponent', () => {
  let component: LoginPageComponent;
  let fixture: ComponentFixture<LoginPageComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let authStateServiceSpy: jasmine.SpyObj<AuthStateService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['login']);
    authStateServiceSpy = jasmine.createSpyObj<AuthStateService>('AuthStateService', [
      'setAccessToken',
      'clear',
    ]);
    notificationServiceSpy = jasmine.createSpyObj<NotificationService>('NotificationService', [
      'success',
      'error',
      'info',
      'dismiss',
      'clear',
    ]);
    await TestBed.configureTestingModule({
      imports: [LoginPageComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: AuthStateService, useValue: authStateServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginPageComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not submit when the form is invalid', () => {
    (component as any).onSubmit();

    expect(authServiceSpy.login).not.toHaveBeenCalled();
    expect((component as any).loginForm.get('email')?.touched).toBeTrue();
    expect((component as any).loginForm.get('password')?.touched).toBeTrue();
  });

  it('should store the access token and navigate after a successful login', () => {
    authServiceSpy.login.and.returnValue(
      of({ accessToken: 'access-token', refreshToken: 'refresh-token' })
    );
    (component as any).loginForm.setValue({
      email: 'jane@studio.com',
      password: 'Password123!',
    });

    (component as any).onSubmit();

    expect(authServiceSpy.login).toHaveBeenCalledWith({
      email: 'jane@studio.com',
      password: 'Password123!',
    });
    expect(authStateServiceSpy.setAccessToken).toHaveBeenCalledWith('access-token');
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Welcome back.');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/dashboard');
    expect((component as any).isSubmitting()).toBeFalse();
  });

  it('should show an error notification when login fails', () => {
    authServiceSpy.login.and.returnValue(
      throwError(() => ({ error: { message: 'Invalid credentials' } }))
    );
    (component as any).loginForm.setValue({
      email: 'jane@studio.com',
      password: 'Password123!',
    });

    (component as any).onSubmit();

    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Invalid credentials');
    expect(router.navigateByUrl).not.toHaveBeenCalled();
    expect((component as any).isSubmitting()).toBeFalse();
  });
});
