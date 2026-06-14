import { computed, inject, Injectable, OnDestroy, signal } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { ClientService } from '../../../core/services/client/client.service';
import { ClientResponse } from '../../../core/interface/client.interface';
import {
  CreateMissionPayload,
  MissionPriority,
  MissionResponse,
  MissionStatus,
} from '../../../core/interface/mission.interface';
import { MissionService } from '../../../core/services/mission/mission.service';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { MissionPageFacade } from './mission-page.facade';

const MISSION_STATUS_FLOW: readonly { value: MissionStatus; label: string }[] = [
  { value: 'created', label: 'Created' },
  { value: 'analysed', label: 'Analysed' },
  { value: 'planned', label: 'Planned' },
  { value: 'started', label: 'Started' },
  { value: 'in_progress', label: 'In progress' },
  { value: 'finalized', label: 'Finalized' },
  { value: 'shipped', label: 'Shipped' },
  { value: 'completed', label: 'Completed' },
];

const MISSION_PRIORITY_FLOW: readonly { value: MissionPriority; label: string }[] = [
  { value: 'low', label: 'Low' },
  { value: 'medium', label: 'Medium' },
  { value: 'high', label: 'High' },
];

type MissionFormControls = {
  title: FormControl<string>;
  roleInProject: FormControl<string>;
  description: FormControl<string>;
  clientId: FormControl<string>;
  priority: FormControl<MissionPriority>;
  startDate: FormControl<string>;
  deadline: FormControl<string>;
};

export type MissionFormControlName = keyof MissionFormControls;

@Injectable()
export class MissionEditorFacade implements OnDestroy {
  private readonly missionPageFacade = inject(MissionPageFacade);
  private readonly missionService = inject(MissionService);
  private readonly clientService = inject(ClientService);
  private readonly notificationService = inject(NotificationService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private drawerCloseTimer: ReturnType<typeof setTimeout> | null = null;

  readonly isSaving = signal(false);
  readonly isDeleting = signal(false);
  readonly drawerVisible = signal(false);
  readonly drawerOpen = signal(false);
  readonly deleteTarget = signal<MissionResponse | null>(null);
  readonly editingMissionId = signal<string | null>(null);
  readonly statusUpdatingMissionId = signal<string | null>(null);
  readonly clients = signal<ClientResponse[]>([]);
  readonly clientsLoaded = signal(false);
  readonly clientsLoading = signal(false);

  readonly missionForm = new FormGroup<MissionFormControls>({
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(3), Validators.maxLength(200)],
    }),
    roleInProject: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(150)],
    }),
    description: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(4000)],
    }),
    clientId: new FormControl('', {
      nonNullable: true,
    }),
    priority: new FormControl<MissionPriority>('medium', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    startDate: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    deadline: new FormControl('', {
      nonNullable: true,
    }),
  });

  readonly missionPriorityOptions = MISSION_PRIORITY_FLOW;

  readonly isCreating = computed(() => this.editingMissionId() === null);

  initialize(): void {
    this.loadClients();
  }

  openCreateDrawer(syncQueryParams = true): void {
    this.openDrawer();
    this.editingMissionId.set(null);
    this.deleteTarget.set(null);
    this.patchMissionForm(null);

    if (syncQueryParams) {
      void this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { create: '1' },
        queryParamsHandling: 'merge',
      });
    }
  }

  openEditDrawer(mission: MissionResponse): void {
    this.openDrawer();
    this.editingMissionId.set(mission.id);
    this.deleteTarget.set(null);
    this.patchMissionForm(mission);

    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { create: null },
      queryParamsHandling: 'merge',
    });
  }

  closeDrawer(): void {
    this.drawerOpen.set(false);
    this.clearDrawerCloseTimer();
    this.drawerCloseTimer = setTimeout(() => {
      this.drawerVisible.set(false);
      this.editingMissionId.set(null);
      this.drawerCloseTimer = null;
    }, 240);
    this.missionForm.markAsPristine();
    this.missionForm.markAsUntouched();

    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { create: null },
      queryParamsHandling: 'merge',
    });
  }

  submitMission(): void {
    if (this.missionForm.invalid) {
      this.missionForm.markAllAsTouched();
      return;
    }

    const payload = this.buildPayload();
    this.isSaving.set(true);

    if (this.editingMissionId()) {
      this.missionService.updateMission(this.editingMissionId()!, payload).pipe(finalize(() => this.isSaving.set(false))).subscribe({
        next: () => {
          this.notificationService.success('Mission updated.');
          this.closeDrawer();
          this.missionPageFacade.refreshCurrentPage();
          this.missionPageFacade.reloadSummary();
        },
        error: (error: any) => {
          const message = error?.error?.message ?? 'Unable to save mission.';
          this.notificationService.error(message);
        },
      });
      return;
    }

    this.missionService.createMission(payload).pipe(finalize(() => this.isSaving.set(false))).subscribe({
      next: () => {
        this.notificationService.success('Mission created.');
        this.closeDrawer();
        this.missionPageFacade.resetToFirstPage();
        this.missionPageFacade.reloadSummary();
      },
      error: (error: any) => {
        const message = error?.error?.message ?? 'Unable to save mission.';
        this.notificationService.error(message);
      },
    });
  }

  requestDelete(mission: MissionResponse): void {
    this.deleteTarget.set(mission);
  }

  cancelDelete(): void {
    this.deleteTarget.set(null);
  }

  confirmDelete(): void {
    const target = this.deleteTarget();

    if (!target) {
      return;
    }

    this.isDeleting.set(true);

    this.missionService.deleteMission(target.id).pipe(finalize(() => this.isDeleting.set(false))).subscribe({
      next: () => {
        this.notificationService.success('Mission deleted.');
        this.deleteTarget.set(null);
        this.missionPageFacade.refreshCurrentPage();
        this.missionPageFacade.reloadSummary();
      },
      error: (error: any) => {
        const message = error?.error?.message ?? 'Unable to delete mission.';
        this.notificationService.error(message);
      },
    });
  }

  canMoveMissionStatus(status: MissionStatus, direction: 'up' | 'down'): boolean {
    return this.getAdjacentMissionStatus(status, direction) !== null && this.statusUpdatingMissionId() === null;
  }

  moveMissionStatus(mission: MissionResponse, direction: 'up' | 'down'): void {
    const nextStatus = this.getAdjacentMissionStatus(mission.status, direction);

    if (!nextStatus || this.statusUpdatingMissionId() !== null) {
      return;
    }

    this.statusUpdatingMissionId.set(mission.id);

    this.missionService
      .updateMissionStatus(mission.id, { status: nextStatus })
      .pipe(finalize(() => this.statusUpdatingMissionId.set(null)))
      .subscribe({
        next: () => {
          this.notificationService.success('Mission status updated.');
          this.missionPageFacade.refreshCurrentPage();
          this.missionPageFacade.reloadSummary();
        },
        error: (error: any) => {
          const message = error?.error?.message ?? 'Unable to update mission status.';
          this.notificationService.error(message);
        },
      });
  }

  hasMissionFieldError(controlName: MissionFormControlName): boolean {
    const control = this.missionForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  missionFieldError(controlName: MissionFormControlName): string {
    const control = this.missionForm.controls[controlName];

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

  clientLabel(clientId: string | null): string {
    if (!clientId) {
      return 'No client';
    }

    const client = this.clients().find((item) => item.id === clientId);
    if (!client) {
      return 'Unknown client';
    }

    const contactParts = [client.primaryContactFirstName, client.primaryContactLastName].filter(Boolean);
    return contactParts.length > 0 ? `${client.companyName} · ${contactParts.join(' ')}` : client.companyName;
  }

  ngOnDestroy(): void {
    this.clearDrawerCloseTimer();
  }

  private loadClients(): void {
    if (this.clientsLoading() || this.clientsLoaded()) {
      return;
    }

    this.clientsLoading.set(true);
    const collectedClients: ClientResponse[] = [];

    const loadPage = (page: number): void => {
      this.clientService.listMyClients(null, null, page, 50).subscribe({
        next: (result) => {
          collectedClients.push(...result.items);

          if (result.hasNext) {
            loadPage(page + 1);
            return;
          }

          this.clients.set(collectedClients);
          this.clientsLoaded.set(true);
          this.clientsLoading.set(false);
        },
        error: (error: any) => {
          this.clientsLoading.set(false);
          const message = error?.error?.message ?? 'Unable to load clients.';
          this.notificationService.error(message, 'Clients unavailable');
        },
      });
    };

    loadPage(1);
  }

  private patchMissionForm(mission: MissionResponse | null): void {
    this.missionForm.patchValue(
      mission
        ? {
            title: mission.title,
            roleInProject: mission.roleInProject ?? '',
            description: mission.description ?? '',
            clientId: mission.clientId ?? '',
            priority: mission.priority,
            startDate: mission.startDate,
            deadline: mission.deadline ?? '',
          }
        : {
            title: '',
            roleInProject: '',
            description: '',
            clientId: '',
            priority: 'medium',
            startDate: '',
            deadline: '',
          },
      { emitEvent: false }
    );

    this.missionForm.markAsPristine();
    this.missionForm.markAsUntouched();
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

  private buildPayload(): CreateMissionPayload {
    return {
      title: this.normalize(this.missionForm.controls.title.value) ?? '',
      roleInProject: this.normalize(this.missionForm.controls.roleInProject.value),
      description: this.normalize(this.missionForm.controls.description.value),
      clientId: this.normalize(this.missionForm.controls.clientId.value),
      priority: this.missionForm.controls.priority.value,
      startDate: this.normalize(this.missionForm.controls.startDate.value) ?? '',
      deadline: this.normalize(this.missionForm.controls.deadline.value),
    };
  }

  private normalize(value: string | null | undefined): string | null {
    const trimmed = value?.trim() ?? '';
    return trimmed.length > 0 ? trimmed : null;
  }

  private getAdjacentMissionStatus(currentStatus: MissionStatus, direction: 'up' | 'down'): MissionStatus | null {
    const currentIndex = MISSION_STATUS_FLOW.findIndex((option) => option.value === currentStatus);
    if (currentIndex < 0) {
      return null;
    }

    const nextIndex = direction === 'up' ? currentIndex + 1 : currentIndex - 1;
    if (nextIndex < 0 || nextIndex >= MISSION_STATUS_FLOW.length) {
      return null;
    }

    return MISSION_STATUS_FLOW[nextIndex].value;
  }
}
