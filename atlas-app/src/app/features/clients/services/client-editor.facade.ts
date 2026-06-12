import { computed, inject, Injectable, OnDestroy, signal } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, Subject, Subscription } from 'rxjs';
import { ClientPageFacade } from './client-page.facade';
import { ClientService } from '../../../core/services/client/client.service';
import {
  ClientDetailResponse,
  ClientResponse,
  ClientStatus,
  CreateClientPayload,
  CreateClientResponse,
} from '../../../core/interface/client.interface';
import { NotificationService } from '../../../core/services/notification/notification.service';

type ClientDrawerMode = 'create' | 'edit';

type ClientFormControls = {
  companyName: FormControl<string>;
  status: FormControl<ClientStatus>;
  notes: FormControl<string>;
};

export type ClientFormControlName = keyof ClientFormControls;

export interface ClientMutationEvent {
  kind: 'saved';
  clientId: string;
}

@Injectable()
export class ClientEditorFacade implements OnDestroy {
  private readonly clientPageFacade = inject(ClientPageFacade, { optional: true });
  private readonly clientService = inject(ClientService);
  private readonly notificationService = inject(NotificationService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private loadClientSubscription: Subscription | null = null;
  private drawerCloseTimer: ReturnType<typeof setTimeout> | null = null;
  private shouldSyncQueryParams = false;

  readonly drawerOpen = signal(false);
  readonly drawerVisible = signal(false);
  readonly drawerLoading = signal(false);
  readonly drawerMode = signal<ClientDrawerMode>('create');
  readonly editingClientId = signal<string | null>(null);
  readonly isSaving = signal(false);
  readonly mutationCompleted = new Subject<ClientMutationEvent>();

  readonly clientStatusOptions: readonly { value: ClientStatus; label: string }[] = [
    { value: 'prospect', label: 'Prospect' },
    { value: 'active', label: 'Active' },
    { value: 'inactive', label: 'Inactive' },
    { value: 'archived', label: 'Archived' },
  ];

  readonly clientForm = new FormGroup<ClientFormControls>({
    companyName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(2), Validators.maxLength(200)],
    }),
    status: new FormControl<ClientStatus>('prospect', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    notes: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(4000)],
    }),
  });

  readonly isEditing = computed(() => this.drawerMode() === 'edit');

  openCreateDrawer(syncQueryParams = true): void {
    this.shouldSyncQueryParams = syncQueryParams;
    this.cancelClientLoad();
    this.editingClientId.set(null);
    this.patchClientForm(null);
    this.drawerLoading.set(false);
    this.drawerMode.set('create');
    this.openDrawer();

    if (syncQueryParams) {
      void this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { create: '1', edit: null },
        queryParamsHandling: 'merge',
      });
    }
  }

  openEditDrawer(clientId: string, syncQueryParams = true): void {
    this.shouldSyncQueryParams = syncQueryParams;
    this.cancelClientLoad();
    this.drawerMode.set('edit');
    this.editingClientId.set(clientId);
    this.drawerLoading.set(true);
    this.openDrawer();

    if (syncQueryParams) {
      void this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { create: null, edit: clientId },
        queryParamsHandling: 'merge',
      });
    }

    this.loadClientSubscription = this.clientService
      .getClientById(clientId)
      .pipe(
        finalize(() => {
          this.drawerLoading.set(false);
          this.loadClientSubscription = null;
        }),
      )
      .subscribe({
        next: (detail) => {
          this.patchClientForm(detail.client);
        },
        error: (error: unknown) => {
          const message = this.extractErrorMessage(error, 'Unable to load client for editing.');
          this.notificationService.error(message, 'Clients unavailable');
          this.closeDrawer();
        },
      });
  }

  closeDrawer(): void {
    this.cancelClientLoad();
    this.drawerOpen.set(false);
    this.drawerLoading.set(false);
    this.drawerMode.set('create');
    this.editingClientId.set(null);
    this.clearDrawerCloseTimer();
    this.drawerCloseTimer = setTimeout(() => {
      this.drawerVisible.set(false);
      this.drawerCloseTimer = null;
    }, 240);
    this.clientForm.markAsPristine();
    this.clientForm.markAsUntouched();

    if (this.shouldSyncQueryParams) {
      void this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { create: null, edit: null },
        queryParamsHandling: 'merge',
      });
    }

    this.shouldSyncQueryParams = false;
  }

  submitClient(): void {
    if (this.drawerLoading() || this.clientForm.invalid) {
      this.clientForm.markAllAsTouched();
      return;
    }

    const payload = this.buildPayload();
    this.isSaving.set(true);

    if (this.editingClientId()) {
      this.clientService
        .updateClient(this.editingClientId()!, payload)
        .pipe(finalize(() => this.isSaving.set(false)))
        .subscribe({
          next: (updatedClient) => {
            this.notificationService.success('Client updated.');
            this.closeDrawer();
            this.clientPageFacade?.reload();
            this.mutationCompleted.next({ kind: 'saved', clientId: updatedClient.id });
          },
          error: (error: unknown) => {
            const message = this.extractErrorMessage(error, 'Unable to update client.');
            this.notificationService.error(message, 'Clients unavailable');
          },
        });
      return;
    }

    this.clientService
      .createClient(payload)
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
        next: (response: CreateClientResponse) => {
          this.notificationService.success(response.message || 'Client created.');
          this.closeDrawer();
          this.clientPageFacade?.goToPage(1);
          this.clientPageFacade?.reload();
          this.mutationCompleted.next({ kind: 'saved', clientId: response.clientId });
          void this.router.navigate(['/dashboard/clients', response.clientId]);
        },
        error: (error: unknown) => {
          const message = this.extractErrorMessage(error, 'Unable to create client.');
          this.notificationService.error(message, 'Clients unavailable');
        },
      });
  }

  hasClientFieldError(controlName: ClientFormControlName): boolean {
    const control = this.clientForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  clientFieldError(controlName: ClientFormControlName): string {
    const control = this.clientForm.controls[controlName];

    if (!control.errors) {
      return '';
    }

    if (control.errors['required']) {
      return 'This field is required.';
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
    this.cancelClientLoad();
    this.clearDrawerCloseTimer();
    this.mutationCompleted.complete();
  }

  private buildPayload(): CreateClientPayload {
    return {
      companyName: this.normalize(this.clientForm.controls.companyName.value) ?? '',
      status: this.clientForm.controls.status.value,
      notes: this.normalize(this.clientForm.controls.notes.value),
    };
  }

  private patchClientForm(client: ClientDetailResponse['client'] | ClientResponse | null): void {
    this.clientForm.patchValue(
      client
        ? {
            companyName: client.companyName ?? '',
            status: client.status,
            notes: client.notes ?? '',
          }
        : {
            companyName: '',
            status: 'prospect',
            notes: '',
          },
      { emitEvent: false },
    );

    this.clientForm.markAsPristine();
    this.clientForm.markAsUntouched();
  }

  private normalize(value: string | null | undefined): string | null {
    const trimmed = value?.trim() ?? '';
    return trimmed.length > 0 ? trimmed : null;
  }

  private openDrawer(): void {
    this.clearDrawerCloseTimer();
    this.drawerVisible.set(true);
    queueMicrotask(() => this.drawerOpen.set(true));
  }

  private clearDrawerCloseTimer(): void {
    if (!this.drawerCloseTimer) {
      return;
    }

    clearTimeout(this.drawerCloseTimer);
    this.drawerCloseTimer = null;
  }

  private cancelClientLoad(): void {
    if (!this.loadClientSubscription) {
      return;
    }

    this.loadClientSubscription.unsubscribe();
    this.loadClientSubscription = null;
  }

  private extractErrorMessage(error: unknown, fallback: string): string {
    const response = error as { error?: { message?: string } } | null | undefined;
    return response?.error?.message ?? fallback;
  }
}
