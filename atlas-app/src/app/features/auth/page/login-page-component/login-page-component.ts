import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { BrandMarkComponent } from '../../../../shared/ui/brand-mark/brand-mark.component';
import { AuthService } from '../../../../core/services/auth/auth.service';
import { AuthStateService } from '../../../../core/services/auth/auth-state.service';
import { LoginCommand } from '../../../../core/interface/auth.interface';
import { NotificationService } from '../../../../core/services/notification/notification.service';

@Component({
  selector: 'app-login-page-component',
  standalone: true,
  imports: [BrandMarkComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './login-page-component.html',
  styleUrl: './login-page-component.scss',
})
export class LoginPageComponent {
  private readonly authService = inject(AuthService);
  private readonly authStateService = inject(AuthStateService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);

  protected readonly isSubmitting = signal(false);

  protected readonly loginForm = new FormGroup({
    email: new FormControl<string | null>(null, [
      Validators.required,
      Validators.email,
      Validators.minLength(2),
    ]),
    password: new FormControl<string | null>(null, [
      Validators.required,
      Validators.minLength(8),
    ]),
  });

  protected onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const request = this.loginForm.getRawValue() as LoginCommand;

    this.authService
      .login(request)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: (tokens) => {
          this.authStateService.setAccessToken(tokens.accessToken);
          this.notificationService.success('Welcome back.');
          void this.router.navigateByUrl('/dashboard');
        },
        error: (error) => {
          const message = error?.error?.message ?? 'Unable to sign in. Please verify your credentials.';
          this.notificationService.error(message);
        },
      });
  }

  protected hasError(name: string): boolean {
    const control = this.loginForm.get(name);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  protected errorMessage(name: string): string {
    const control = this.loginForm.get(name);

    if (!control || !control.errors) {
      return '';
    }

    if (control.errors['required']) {
      return 'This field is required.';
    }

    if (control.errors['email']) {
      return 'Enter a valid email address.';
    }

    if (control.errors['minlength']) {
      return `Must be at least ${control.errors['minlength'].requiredLength} characters long.`;
    }

    return 'Please check this field.';
  }
}
