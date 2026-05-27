import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { RegisterPageComponent } from './register-page-component';
import { AuthService } from '../../../../core/services/auth/auth.service';
import { NotificationService } from '../../../../core/services/notification/notification.service';

describe('RegisterPageComponent', () => {
  let component: RegisterPageComponent;
  let fixture: ComponentFixture<RegisterPageComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['register']);
    notificationServiceSpy = jasmine.createSpyObj<NotificationService>('NotificationService', [
      'success',
      'error',
    ]);

    await TestBed.configureTestingModule({
      imports: [RegisterPageComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegisterPageComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should notify success after a valid register submission', () => {
    authServiceSpy.register.and.returnValue(of({ message: 'Registration completed successfully.' }));

    component.registerForm.setValue({
      email: 'jane@studio.com',
      password: 'Password123!',
      firstName: 'Jane',
      lastName: 'Doe',
    });

    component.onSubmit(true);

    expect(authServiceSpy.register).toHaveBeenCalledWith({
      email: 'jane@studio.com',
      password: 'Password123!',
      firstName: 'Jane',
      lastName: 'Doe',
    });
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Registration completed successfully.');
  });

  it('should notify an error when the register request fails', () => {
    authServiceSpy.register.and.returnValue(throwError(() => ({ error: { message: 'Email already used' } })));

    component.registerForm.setValue({
      email: 'jane@studio.com',
      password: 'Password123!',
      firstName: 'Jane',
      lastName: 'Doe',
    });

    component.onSubmit(true);

    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Email already used');
  });
});
