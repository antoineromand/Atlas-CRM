import {Component, inject} from '@angular/core';
import {RegisterHeaderComponent} from '../../components/register-header-component/register-header-component';
import {RegisterFormComponent} from '../../components/register-form-component/register-form-component';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {RegisterCommand} from '../../../../core/interface/auth.interface';
import {AuthService} from '../../../../core/services/auth/auth.service';
import {NotificationService} from '../../../../core/services/notification/notification.service';

@Component({
  selector: 'app-register-page-component',
  standalone: true,
  imports: [RegisterHeaderComponent, RegisterFormComponent, ReactiveFormsModule],
  templateUrl: './register-page-component.html',
  styleUrl: './register-page-component.scss',
})
export class RegisterPageComponent {
    authService = inject(AuthService);
    notificationService = inject(NotificationService);
    registerForm = new FormGroup({
      email: new FormControl<string | null>(null, [Validators.required, Validators.minLength(2), Validators.email]),
      password: new FormControl<string | null>(null, [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(
          /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[#?!@$%^&*+\-]).{8,}$/
        ),
      ]),
      firstName: new FormControl<string | null>(null, [Validators.required, Validators.minLength(2)]),
      lastName: new FormControl<string | null>(null, [Validators.required, Validators.minLength(2)])
    });

    onSubmit(isValid: boolean) {
      if (!isValid) {
        return;
      }
      const request: RegisterCommand = this.registerForm.value as RegisterCommand;
      this.authService.register(request).subscribe({
        next: (data) => {
          this.notificationService.success(data.message || 'Account created successfully.');
        },
        error: (error) => {
          const message = error?.error?.message ?? 'Registration failed. Please try again.';
          this.notificationService.error(message);
        },
      });
    }
}
