import { Component, OnInit, inject, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { finalize } from 'rxjs';
import { AccountService } from '../../../../core/services/account/account.service';
import {
  AccountResponse,
  UpdateAccountPayload,
} from '../../../../core/interface/account.interface';
import { NotificationService } from '../../../../core/services/notification/notification.service';
import { SettingsAccountFormComponent } from '../../components/settings-account-form-component/settings-account-form-component';
import { SettingsSummaryCardComponent } from '../../components/settings-summary-card-component/settings-summary-card-component';

interface SettingsFormValue {
  firstName: FormControl<string | null>;
  lastName: FormControl<string | null>;
  companyName: FormControl<string | null>;
  siretNumber: FormControl<string | null>;
  vatNumber: FormControl<string | null>;
  billingEmail: FormControl<string | null>;
  billingAddressLine1: FormControl<string | null>;
  billingAddressLine2: FormControl<string | null>;
  billingPostalCode: FormControl<string | null>;
  billingCity: FormControl<string | null>;
  billingCountry: FormControl<string | null>;
}

@Component({
  selector: 'app-settings-page-component',
  standalone: true,
  imports: [ReactiveFormsModule, SettingsAccountFormComponent, SettingsSummaryCardComponent],
  templateUrl: './settings-page-component.html',
  styleUrl: './settings-page-component.scss',
})
export class SettingsPageComponent implements OnInit {
  private readonly accountService = inject(AccountService);
  private readonly notificationService = inject(NotificationService);

  protected readonly account = signal<AccountResponse | null>(null);
  protected readonly isLoading = signal(true);
  protected readonly isSaving = signal(false);
  protected readonly loadError = signal<string | null>(null);

  protected readonly settingsForm = new FormGroup<SettingsFormValue>({
    firstName: new FormControl<string | null>('', {
      validators: [Validators.required, Validators.minLength(2), Validators.maxLength(120)],
      nonNullable: false,
    }),
    lastName: new FormControl<string | null>('', {
      validators: [Validators.required, Validators.minLength(2), Validators.maxLength(120)],
      nonNullable: false,
    }),
    companyName: new FormControl<string | null>('', {
      validators: [Validators.maxLength(200)],
      nonNullable: false,
    }),
    siretNumber: new FormControl<string | null>('', {
      validators: [Validators.maxLength(14)],
      nonNullable: false,
    }),
    vatNumber: new FormControl<string | null>('', {
      validators: [Validators.maxLength(32)],
      nonNullable: false,
    }),
    billingEmail: new FormControl<string | null>('', {
      validators: [Validators.email, Validators.maxLength(200)],
      nonNullable: false,
    }),
    billingAddressLine1: new FormControl<string | null>('', {
      validators: [Validators.maxLength(255)],
      nonNullable: false,
    }),
    billingAddressLine2: new FormControl<string | null>('', {
      validators: [Validators.maxLength(255)],
      nonNullable: false,
    }),
    billingPostalCode: new FormControl<string | null>('', {
      validators: [Validators.maxLength(20)],
      nonNullable: false,
    }),
    billingCity: new FormControl<string | null>('', {
      validators: [Validators.maxLength(120)],
      nonNullable: false,
    }),
    billingCountry: new FormControl<string | null>('', {
      validators: [Validators.maxLength(120)],
      nonNullable: false,
    }),
  });

  ngOnInit(): void {
    this.loadAccount();
  }

  protected reload(): void {
    this.loadAccount();
  }

  protected onSubmit(): void {
    if (this.settingsForm.invalid) {
      this.settingsForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);

    const payload: UpdateAccountPayload = {
      firstName: this.normalize(this.settingsForm.controls.firstName.value),
      lastName: this.normalize(this.settingsForm.controls.lastName.value),
      companyName: this.normalize(this.settingsForm.controls.companyName.value),
      siretNumber: this.normalize(this.settingsForm.controls.siretNumber.value),
      vatNumber: this.normalize(this.settingsForm.controls.vatNumber.value),
      billingEmail: this.normalize(this.settingsForm.controls.billingEmail.value),
      billingAddressLine1: this.normalize(this.settingsForm.controls.billingAddressLine1.value),
      billingAddressLine2: this.normalize(this.settingsForm.controls.billingAddressLine2.value),
      billingPostalCode: this.normalize(this.settingsForm.controls.billingPostalCode.value),
      billingCity: this.normalize(this.settingsForm.controls.billingCity.value),
      billingCountry: this.normalize(this.settingsForm.controls.billingCountry.value),
    };

    this.accountService
      .updateMyAccount(payload)
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
        next: (account) => {
          this.account.set(account);
          this.patchForm(account);
          this.notificationService.success('Account updated.');
        },
        error: (error) => {
          const message = error?.error?.message ?? 'Unable to save account changes.';
          this.notificationService.error(message);
        },
      });
  }

  private loadAccount(): void {
    this.isLoading.set(true);
    this.loadError.set(null);
    this.settingsForm.disable({ emitEvent: false });

    this.accountService
      .getMyAccount()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (account) => {
          this.account.set(account);
          this.patchForm(account);
          this.settingsForm.enable({ emitEvent: false });
        },
        error: (error) => {
          const message = error?.error?.message ?? 'Unable to load account details.';
          this.loadError.set(message);
          this.notificationService.error(message, 'Settings unavailable');
        },
      });
  }

  private patchForm(account: AccountResponse): void {
    this.settingsForm.patchValue(
      {
        firstName: account.firstName,
        lastName: account.lastName,
        companyName: account.companyName ?? '',
        siretNumber: account.siretNumber ?? '',
        vatNumber: account.vatNumber ?? '',
        billingEmail: account.billingEmail ?? '',
        billingAddressLine1: account.billingAddressLine1 ?? '',
        billingAddressLine2: account.billingAddressLine2 ?? '',
        billingPostalCode: account.billingPostalCode ?? '',
        billingCity: account.billingCity ?? '',
        billingCountry: account.billingCountry ?? '',
      },
      { emitEvent: false }
    );
    this.settingsForm.markAsPristine();
    this.settingsForm.markAsUntouched();
  }

  private normalize(value: string | null | undefined): string | null {
    const trimmed = value?.trim() ?? '';
    return trimmed.length > 0 ? trimmed : null;
  }
}
