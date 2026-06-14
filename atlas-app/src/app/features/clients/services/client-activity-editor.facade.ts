import { computed, inject, Injectable, OnDestroy, signal } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { finalize, Subject } from 'rxjs';
import { ClientService } from '../../../core/services/client/client.service';
import {
  ClientActivityResponse,
  CreateClientActivityPayload,
  UpdateClientActivityPayload,
} from '../../../core/interface/client.interface';
import { NotificationService } from '../../../core/services/notification/notification.service';

type ClientActivityDrawerMode = 'create' | 'edit';

type ClientActivityFormControls = {
  activityType: FormControl<string>;
  title: FormControl<string>;
  description: FormControl<string>;
  occurredAt: FormControl<string>;
};

export type ClientActivityFormControlName = keyof ClientActivityFormControls;

export interface ClientActivityMutationEvent {
  kind: 'saved' | 'deleted';
  clientId: string;
  activityId: string;
}

@Injectable()
export class ClientActivityEditorFacade implements OnDestroy {
  private readonly clientService = inject(ClientService);
  private readonly notificationService = inject(NotificationService);

  private drawerCloseTimer: ReturnType<typeof setTimeout> | null = null;

  readonly drawerOpen = signal(false);
  readonly drawerVisible = signal(false);
  readonly drawerMode = signal<ClientActivityDrawerMode>('create');
  readonly currentClientId = signal<string | null>(null);
  readonly editingActivityId = signal<string | null>(null);
  readonly isSaving = signal(false);
  readonly mutationCompleted = new Subject<ClientActivityMutationEvent>();

  readonly isEditing = computed(() => this.drawerMode() === 'edit');
  readonly activityTypeOptions = [
    { value: 'call', label: 'Call' },
    { value: 'email', label: 'Email' },
    { value: 'meeting', label: 'Meeting' },
    { value: 'note', label: 'Note' },
    { value: 'task', label: 'Task' },
    { value: 'follow_up', label: 'Follow up' },
    { value: 'status_change', label: 'Status change' },
  ] as const;

  readonly activityForm = new FormGroup<ClientActivityFormControls>({
    activityType: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(32)],
    }),
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(2), Validators.maxLength(200)],
    }),
    description: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(4000)],
    }),
    occurredAt: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  openCreateDrawer(clientId: string): void {
    this.resetDrawerState();
    this.currentClientId.set(clientId);
    this.drawerMode.set('create');
    this.patchActivityForm(null);
    this.openDrawer();
  }

  openEditDrawer(clientId: string, activity: ClientActivityResponse): void {
    this.resetDrawerState();
    this.currentClientId.set(clientId);
    this.drawerMode.set('edit');
    this.editingActivityId.set(activity.id);
    this.patchActivityForm(activity);
    this.openDrawer();
  }

  closeDrawer(): void {
    this.drawerOpen.set(false);
    this.isSaving.set(false);
    this.drawerMode.set('create');
    this.currentClientId.set(null);
    this.editingActivityId.set(null);
    this.clearDrawerCloseTimer();
    this.drawerCloseTimer = setTimeout(() => {
      this.drawerVisible.set(false);
      this.drawerCloseTimer = null;
    }, 240);
    this.activityForm.markAsPristine();
    this.activityForm.markAsUntouched();
  }

  submitActivity(): void {
    if (this.isSaving() || this.activityForm.invalid) {
      this.activityForm.markAllAsTouched();
      return;
    }

    const clientId = this.currentClientId();
    if (!clientId) {
      this.notificationService.error('Missing client context.', 'Activities unavailable');
      return;
    }

    this.isSaving.set(true);

    if (this.editingActivityId()) {
      const activityId = this.editingActivityId()!;
      this.clientService
        .updateClientActivity(clientId, activityId, this.buildUpdatePayload())
        .pipe(finalize(() => this.isSaving.set(false)))
        .subscribe({
          next: () => {
            this.notificationService.success('Activity updated.');
            this.mutationCompleted.next({ kind: 'saved', clientId, activityId });
            this.closeDrawer();
          },
          error: (error: unknown) => {
            const message = this.extractErrorMessage(error, 'Unable to update activity.');
            this.notificationService.error(message, 'Activities unavailable');
          },
        });
      return;
    }

    this.clientService
      .createClientActivity(clientId, this.buildCreatePayload())
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
        next: (activity) => {
          this.notificationService.success('Activity created.');
          this.mutationCompleted.next({ kind: 'saved', clientId, activityId: activity.id });
          this.closeDrawer();
        },
        error: (error: unknown) => {
          const message = this.extractErrorMessage(error, 'Unable to create activity.');
          this.notificationService.error(message, 'Activities unavailable');
        },
      });
  }

  hasActivityFieldError(controlName: ClientActivityFormControlName): boolean {
    const control = this.activityForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  activityFieldError(controlName: ClientActivityFormControlName): string {
    const control = this.activityForm.controls[controlName];

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
    this.clearDrawerCloseTimer();
    this.mutationCompleted.complete();
  }

  private buildCreatePayload(): CreateClientActivityPayload {
    return {
      activityType: this.normalizeRequired(this.activityForm.controls.activityType.value),
      title: this.normalizeRequired(this.activityForm.controls.title.value),
      description: this.normalizeOptional(this.activityForm.controls.description.value),
      occurredAt: this.toIsoDateTime(this.activityForm.controls.occurredAt.value),
    };
  }

  private buildUpdatePayload(): UpdateClientActivityPayload {
    return {
      activityType: this.normalizeRequired(this.activityForm.controls.activityType.value),
      title: this.normalizeRequired(this.activityForm.controls.title.value),
      description: this.normalizeOptional(this.activityForm.controls.description.value),
      occurredAt: this.toIsoDateTime(this.activityForm.controls.occurredAt.value),
    };
  }

  private patchActivityForm(activity: ClientActivityResponse | null): void {
    this.activityForm.patchValue(
      activity
        ? {
            activityType: activity.activityType ?? 'call',
            title: activity.title ?? '',
            description: activity.description ?? '',
            occurredAt: this.formatDateTimeLocal(activity.occurredAt),
          }
        : {
            activityType: 'call',
            title: '',
            description: '',
            occurredAt: this.formatDateTimeLocal(new Date().toISOString()),
          },
      { emitEvent: false },
    );

    this.activityForm.markAsPristine();
    this.activityForm.markAsUntouched();
  }

  private formatDateTimeLocal(value: string | null | undefined): string {
    if (!value) {
      return '';
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return '';
    }

    const pad = (input: number): string => `${input}`.padStart(2, '0');
    return [
      date.getFullYear(),
      '-',
      pad(date.getMonth() + 1),
      '-',
      pad(date.getDate()),
      'T',
      pad(date.getHours()),
      ':',
      pad(date.getMinutes()),
    ].join('');
  }

  private toIsoDateTime(value: string): string {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      throw new Error('occurredAt is invalid');
    }

    return date.toISOString();
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
    this.editingActivityId.set(null);
    this.activityForm.reset({
      activityType: 'call',
      title: '',
      description: '',
      occurredAt: this.formatDateTimeLocal(new Date().toISOString()),
    });
  }

  private clearDrawerCloseTimer(): void {
    if (this.drawerCloseTimer) {
      clearTimeout(this.drawerCloseTimer);
      this.drawerCloseTimer = null;
    }
  }

  private extractErrorMessage(error: unknown, fallback: string): string {
    const response = error as { error?: { message?: string } } | null | undefined;
    return response?.error?.message ?? fallback;
  }
}
