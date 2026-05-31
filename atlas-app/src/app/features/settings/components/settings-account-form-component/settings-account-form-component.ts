import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

interface AccountFieldConfig {
  controlName: string;
  label: string;
  type?: string;
  placeholder?: string;
  autocomplete?: string;
  help?: string;
}

@Component({
  selector: 'app-settings-account-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './settings-account-form-component.html',
  styleUrl: './settings-account-form-component.scss',
})
export class SettingsAccountFormComponent {
  @Input({ required: true }) form!: FormGroup;
  @Input() loading = false;
  @Input() saving = false;
  @Output() submitted = new EventEmitter<void>();

  protected readonly identityFields: readonly AccountFieldConfig[] = [
    {
      controlName: 'firstName',
      label: 'First name',
      placeholder: 'Jane',
      autocomplete: 'given-name',
    },
    {
      controlName: 'lastName',
      label: 'Last name',
      placeholder: 'Doe',
      autocomplete: 'family-name',
    },
    {
      controlName: 'companyName',
      label: 'Company name',
      placeholder: 'Atlas Studio',
      autocomplete: 'organization',
    },
    {
      controlName: 'siretNumber',
      label: 'SIRET number',
      placeholder: '12345678901234',
      autocomplete: 'off',
      help: 'Optional, used for French invoicing.',
    },
    {
      controlName: 'vatNumber',
      label: 'VAT number',
      placeholder: 'FR12345678901',
      autocomplete: 'off',
      help: 'Optional, keep it in sync with your tax registration.',
    },
  ];

  protected readonly billingFields: readonly AccountFieldConfig[] = [
    {
      controlName: 'billingEmail',
      label: 'Billing email',
      type: 'email',
      placeholder: 'billing@example.com',
      autocomplete: 'email',
    },
    {
      controlName: 'billingAddressLine1',
      label: 'Address line 1',
      placeholder: '10 rue de Paris',
      autocomplete: 'address-line1',
    },
    {
      controlName: 'billingAddressLine2',
      label: 'Address line 2',
      placeholder: 'Building, floor, apartment',
      autocomplete: 'address-line2',
    },
    {
      controlName: 'billingPostalCode',
      label: 'Postal code',
      placeholder: '75000',
      autocomplete: 'postal-code',
    },
    {
      controlName: 'billingCity',
      label: 'City',
      placeholder: 'Paris',
      autocomplete: 'address-level2',
    },
    {
      controlName: 'billingCountry',
      label: 'Country',
      placeholder: 'France',
      autocomplete: 'country',
    },
  ];

  submit(): void {
    this.submitted.emit();
  }

  hasError(controlName: string): boolean {
    const control = this.form?.get(controlName);

    return !!control && control.invalid && (control.dirty || control.touched);
  }

  errorMessage(controlName: string): string {
    const control = this.form?.get(controlName);

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

    if (control.errors['maxlength']) {
      return `Must be at most ${control.errors['maxlength'].requiredLength} characters long.`;
    }

    return 'Please check this field.';
  }
}
