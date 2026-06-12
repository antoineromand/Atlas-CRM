import { computed, inject, Injectable, OnDestroy, signal } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { finalize, Subject } from 'rxjs';
import { ClientService } from '../../../core/services/client/client.service';
import {
  ClientContactResponse,
  CreateClientContactPayload,
  UpdateClientContactPayload,
} from '../../../core/interface/client.interface';
import { NotificationService } from '../../../core/services/notification/notification.service';

type ClientContactDrawerMode = 'create' | 'edit';

type ClientContactFormControls = {
  firstName: FormControl<string>;
  lastName: FormControl<string>;
  email: FormControl<string>;
  phone: FormControl<string>;
  jobTitle: FormControl<string>;
  primary: FormControl<boolean>;
};

export type ClientContactFormControlName = keyof ClientContactFormControls;

export interface ClientContactMutationEvent {
  kind: 'saved' | 'deleted';
  clientId: string;
  contactId: string;
}

@Injectable()
export class ClientContactEditorFacade implements OnDestroy {
  private readonly clientService = inject(ClientService);
  private readonly notificationService = inject(NotificationService);

  private drawerCloseTimer: ReturnType<typeof setTimeout> | null = null;

  readonly drawerOpen = signal(false);
  readonly drawerVisible = signal(false);
  readonly drawerMode = signal<ClientContactDrawerMode>('create');
  readonly currentClientId = signal<string | null>(null);
  readonly editingContactId = signal<string | null>(null);
  readonly isSaving = signal(false);
  readonly mutationCompleted = new Subject<ClientContactMutationEvent>();

  readonly isEditing = computed(() => this.drawerMode() === 'edit');

  readonly contactForm = new FormGroup<ClientContactFormControls>({
    firstName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(2), Validators.maxLength(120)],
    }),
    lastName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(2), Validators.maxLength(120)],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.email, Validators.maxLength(200)],
    }),
    phone: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(30)],
    }),
    jobTitle: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(120)],
    }),
    primary: new FormControl(false, {
      nonNullable: true,
    }),
  });

  openCreateDrawer(clientId: string, primaryByDefault = false): void {
    this.resetDrawerState();
    this.currentClientId.set(clientId);
    this.drawerMode.set('create');
    this.patchContactForm(null, primaryByDefault);
    this.openDrawer();
  }

  openEditDrawer(clientId: string, contact: ClientContactResponse): void {
    this.resetDrawerState();
    this.currentClientId.set(clientId);
    this.drawerMode.set('edit');
    this.editingContactId.set(contact.id);
    this.patchContactForm(contact);
    this.openDrawer();
  }

  closeDrawer(): void {
    this.drawerOpen.set(false);
    this.isSaving.set(false);
    this.drawerMode.set('create');
    this.currentClientId.set(null);
    this.editingContactId.set(null);
    this.clearDrawerCloseTimer();
    this.drawerCloseTimer = setTimeout(() => {
      this.drawerVisible.set(false);
      this.drawerCloseTimer = null;
    }, 240);
    this.contactForm.markAsPristine();
    this.contactForm.markAsUntouched();
  }

  submitContact(): void {
    if (this.isSaving() || this.contactForm.invalid) {
      this.contactForm.markAllAsTouched();
      return;
    }

    const clientId = this.currentClientId();
    if (!clientId) {
      this.notificationService.error('Missing client context.', 'Contacts unavailable');
      return;
    }

    this.isSaving.set(true);

    if (this.editingContactId()) {
      const contactId = this.editingContactId()!;
      this.clientService
        .updateClientContact(clientId, contactId, this.buildUpdatePayload())
        .pipe(finalize(() => this.isSaving.set(false)))
        .subscribe({
          next: () => {
            this.notificationService.success('Contact updated.');
            this.mutationCompleted.next({ kind: 'saved', clientId, contactId });
            this.closeDrawer();
          },
          error: (error: unknown) => {
            const message = this.extractErrorMessage(error, 'Unable to update contact.');
            this.notificationService.error(message, 'Contacts unavailable');
          },
        });
      return;
    }

    this.clientService
      .createClientContact(clientId, this.buildCreatePayload())
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
        next: (contact) => {
          this.notificationService.success('Contact created.');
          this.mutationCompleted.next({ kind: 'saved', clientId, contactId: contact.id });
          this.closeDrawer();
        },
        error: (error: unknown) => {
          const message = this.extractErrorMessage(error, 'Unable to create contact.');
          this.notificationService.error(message, 'Contacts unavailable');
        },
      });
  }

  hasContactFieldError(controlName: ClientContactFormControlName): boolean {
    const control = this.contactForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  contactFieldError(controlName: ClientContactFormControlName): string {
    const control = this.contactForm.controls[controlName];

    if (!control.errors) {
      return '';
    }

    if (control.errors['required']) {
      return 'This field is required.';
    }

    if (control.errors['email']) {
      return 'Use a valid email address.';
    }

    if (control.errors['minlength']) {
      return `Use at least ${control.errors['minlength'].requiredLength} characters.`;
    }

    if (control.errors['maxlength']) {
      return `Use at most ${control.errors['maxlength'].requiredLength} characters.`;
    }

    return 'Please check this field.';
  }

  ngOnDestroy(): void {
    this.clearDrawerCloseTimer();
    this.mutationCompleted.complete();
  }

  private buildCreatePayload(): CreateClientContactPayload {
    return {
      firstName: this.normalizeRequired(this.contactForm.controls.firstName.value),
      lastName: this.normalizeRequired(this.contactForm.controls.lastName.value),
      email: this.normalizeOptional(this.contactForm.controls.email.value),
      phone: this.normalizeOptional(this.contactForm.controls.phone.value),
      jobTitle: this.normalizeOptional(this.contactForm.controls.jobTitle.value),
      primary: this.contactForm.controls.primary.value,
    };
  }

  private buildUpdatePayload(): UpdateClientContactPayload {
    return {
      firstName: this.normalizeRequired(this.contactForm.controls.firstName.value),
      lastName: this.normalizeRequired(this.contactForm.controls.lastName.value),
      email: this.normalizeOptional(this.contactForm.controls.email.value),
      phone: this.normalizeOptional(this.contactForm.controls.phone.value),
      jobTitle: this.normalizeOptional(this.contactForm.controls.jobTitle.value),
      primary: this.contactForm.controls.primary.value,
    };
  }

  private patchContactForm(contact: ClientContactResponse | null, primaryByDefault = false): void {
    this.contactForm.patchValue(
      contact
        ? {
            firstName: contact.firstName ?? '',
            lastName: contact.lastName ?? '',
            email: contact.email ?? '',
            phone: contact.phone ?? '',
            jobTitle: contact.jobTitle ?? '',
            primary: contact.primary,
          }
        : {
            firstName: '',
            lastName: '',
            email: '',
            phone: '',
            jobTitle: '',
            primary: primaryByDefault,
          },
      { emitEvent: false },
    );

    this.contactForm.markAsPristine();
    this.contactForm.markAsUntouched();
  }

  private normalizeRequired(value: string): string {
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : '';
  }

  private normalizeOptional(value: string): string | null {
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : null;
  }

  private openDrawer(): void {
    this.clearDrawerCloseTimer();
    this.drawerVisible.set(true);
    queueMicrotask(() => this.drawerOpen.set(true));
  }

  private resetDrawerState(): void {
    this.clearDrawerCloseTimer();
    this.drawerVisible.set(false);
    this.drawerOpen.set(false);
    this.drawerMode.set('create');
    this.currentClientId.set(null);
    this.editingContactId.set(null);
    this.isSaving.set(false);
  }

  private clearDrawerCloseTimer(): void {
    if (!this.drawerCloseTimer) {
      return;
    }

    clearTimeout(this.drawerCloseTimer);
    this.drawerCloseTimer = null;
  }

  private extractErrorMessage(error: unknown, fallback: string): string {
    const response = error as { error?: { message?: string } } | null | undefined;
    return response?.error?.message ?? fallback;
  }
}
