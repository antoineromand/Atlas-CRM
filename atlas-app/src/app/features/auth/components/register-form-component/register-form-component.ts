import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AbstractControl, FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-register-form-component',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './register-form-component.html',
  styleUrl: './register-form-component.scss',
})
export class RegisterFormComponent {
  form = input.required<FormGroup>();
  submitEvent = output<boolean>();

  submit() {
    if (!this.form().valid) {
      this.form().markAllAsTouched();
      this.submitEvent.emit(false);
      return;
    }

    this.submitEvent.emit(true);
  }

  control(name: string): AbstractControl | null {
    return this.form().get(name);
  }

  hasError(name: string): boolean {
    const control = this.control(name);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  errorMessage(name: string): string {
    const control = this.control(name);

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

    if (control.errors['pattern']) {
      return 'Must include uppercase, lowercase, a number, and a special character.';
    }

    return 'Please check this field.';
  }
}
