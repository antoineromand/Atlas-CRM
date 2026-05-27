import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { RegisterFormComponent } from './register-form-component';

describe('RegisterFormComponent', () => {
  let component: RegisterFormComponent;
  let fixture: ComponentFixture<RegisterFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterFormComponent],
      providers: [provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegisterFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit false and mark controls when invalid', () => {
    spyOn(component.submitEvent, 'emit');

    component.submit();

    expect(component.submitEvent.emit).toHaveBeenCalledWith(false);
    expect(component.form().get('email')?.touched).toBeTrue();
    expect(component.form().get('password')?.touched).toBeTrue();
    expect(component.form().get('firstName')?.touched).toBeTrue();
    expect(component.form().get('lastName')?.touched).toBeTrue();
  });

  it('should emit true when the form is valid', () => {
    spyOn(component.submitEvent, 'emit');
    component.form().setValue({
      email: 'jane@studio.com',
      password: 'Password123!',
      firstName: 'Jane',
      lastName: 'Doe',
    });

    component.submit();

    expect(component.submitEvent.emit).toHaveBeenCalledWith(true);
  });

  it('should return the expected error message', () => {
    const emailControl = component.form().get('email');
    emailControl?.setErrors({ required: true });
    emailControl?.markAsTouched();

    expect(component.errorMessage('email')).toBe('This field is required.');
  });
});
