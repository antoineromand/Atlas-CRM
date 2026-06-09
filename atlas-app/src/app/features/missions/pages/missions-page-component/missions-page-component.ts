import { Component, DestroyRef, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { StatCardComponent } from '../../../../shared/ui/stat-card/stat-card.component';
import { MissionPageFacade } from '../../services/mission-page.facade';
import { MissionService } from '../../../../core/services/mission/mission.service';
import {
  CreateMissionPayload,
  MissionPriority,
  MissionResponse,
  MissionStatus,
} from '../../../../core/interface/mission.interface';
import { NotificationService } from '../../../../core/services/notification/notification.service';

type MissionViewMode = 'cards' | 'list';
type MissionPaginationItem = {
  key: string;
  kind: 'page' | 'ellipsis';
  page: number;
};

interface MissionFormControls {
  title: FormControl<string>;
  roleInProject: FormControl<string>;
  description: FormControl<string>;
  status: FormControl<MissionStatus>;
  priority: FormControl<MissionPriority>;
  startDate: FormControl<string>;
  deadline: FormControl<string>;
}

interface MissionStatCard {
  icon: string;
  badge: string;
  label: string;
  value: string;
  footer: string;
  tone: 'primary' | 'secondary' | 'accent' | 'danger';
}

@Component({
  selector: 'app-missions-page-component',
  standalone: true,
  imports: [ReactiveFormsModule, StatCardComponent],
  templateUrl: './missions-page-component.html',
  styleUrl: './missions-page-component.scss',
})
export class MissionsPageComponent implements OnInit, OnDestroy {
  private readonly missionPageFacade = inject(MissionPageFacade);
  private readonly missionService = inject(MissionService);
  private readonly notificationService = inject(NotificationService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private drawerCloseTimer: ReturnType<typeof setTimeout> | null = null;

  protected readonly missions = this.missionPageFacade.missions;
  protected readonly pagination = this.missionPageFacade.pagination;
  protected readonly isLoading = this.missionPageFacade.isLoading;
  protected readonly isSaving = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly loadError = this.missionPageFacade.loadError;
  protected readonly searchTerm = this.missionPageFacade.searchTerm;
  protected readonly searchWarning = this.missionPageFacade.searchWarning;
  protected readonly viewMode = this.missionPageFacade.viewMode;
  protected readonly drawerVisible = signal(false);
  protected readonly drawerOpen = signal(false);
  protected readonly deleteTarget = signal<MissionResponse | null>(null);
  protected readonly editingMissionId = signal<string | null>(null);
  protected readonly isRefreshing = this.missionPageFacade.isRefreshing;

  protected readonly missionStats = computed<MissionStatCard[]>(() => {
    const summary = this.missionPageFacade.summary();

    return [
      {
        icon: 'rocket_launch',
        badge: '+1 this week',
        label: 'Active missions',
        value: String(summary?.activeMissions ?? 0),
        footer: 'Current workload in progress',
        tone: 'accent',
      },
      {
        icon: 'schedule',
        badge: 'Deadline watch',
        label: 'Due soon',
        value: String(summary?.dueSoonMissions ?? 0),
        footer: 'Within the next 7 days',
        tone: 'danger',
      },
      {
        icon: 'task_alt',
        badge: 'Done',
        label: 'Completed',
        value: String(summary?.completedMissions ?? 0),
        footer: 'Closed and archived in the board',
        tone: 'secondary',
      },
      {
        icon: 'priority_high',
        badge: 'Focus',
        label: 'High priority',
        value: String(summary?.highPriorityMissions ?? 0),
        footer: 'Needs attention first',
        tone: 'primary',
      },
    ];
  });

  protected readonly missionForm = new FormGroup<MissionFormControls>({
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
    status: new FormControl<MissionStatus>('not_started', {
      nonNullable: true,
      validators: [Validators.required],
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

  protected readonly missionStatusOptions: readonly { value: MissionStatus; label: string }[] = [
    { value: 'not_started', label: 'Not started' },
    { value: 'in_progress', label: 'In progress' },
    { value: 'completed', label: 'Completed' },
  ];

  protected readonly missionPriorityOptions: readonly { value: MissionPriority; label: string }[] = [
    { value: 'low', label: 'Low' },
    { value: 'medium', label: 'Medium' },
    { value: 'high', label: 'High' },
  ];

  ngOnInit(): void {
    this.missionPageFacade.initialize();

    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      if (params.get('create') === '1') {
        this.openCreateDrawer(false);
      }
    });
  }

  ngOnDestroy(): void {
    this.clearDrawerCloseTimer();
  }

  protected reload(): void {
    this.missionPageFacade.reload();
    this.missionPageFacade.reloadSummary();
  }

  protected setSearchTerm(value: string): void {
    this.missionPageFacade.setSearchTerm(value);
  }

  protected clearSearch(): void {
    this.missionPageFacade.clearSearch();
  }

  protected setViewMode(mode: MissionViewMode): void {
    this.missionPageFacade.setViewMode(mode);
  }

  protected openCreateDrawer(syncQueryParams = true): void {
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

  protected openEditDrawer(mission: MissionResponse): void {
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

  protected closeDrawer(): void {
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

  protected submitMission(): void {
    if (this.missionForm.invalid) {
      this.missionForm.markAllAsTouched();
      return;
    }

    const payload = this.buildPayload();
    this.isSaving.set(true);

    if (this.editingMissionId()) {
      this.missionService
        .updateMission(this.editingMissionId()!, payload)
        .pipe(finalize(() => this.isSaving.set(false)))
        .subscribe({
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

    this.missionService
      .createMission(payload)
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
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

  protected requestDelete(mission: MissionResponse): void {
    this.deleteTarget.set(mission);
  }

  protected cancelDelete(): void {
    this.deleteTarget.set(null);
  }

  protected confirmDelete(): void {
    const target = this.deleteTarget();

    if (!target) {
      return;
    }

    this.isDeleting.set(true);

    this.missionService
      .deleteMission(target.id)
      .pipe(finalize(() => this.isDeleting.set(false)))
      .subscribe({
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

  protected statusLabel(status: MissionStatus): string {
    switch (status) {
      case 'completed':
        return 'Completed';
      case 'in_progress':
        return 'In progress';
      default:
        return 'Not started';
    }
  }

  protected priorityLabel(priority: MissionPriority): string {
    switch (priority) {
      case 'high':
        return 'High';
      case 'medium':
        return 'Medium';
      default:
        return 'Low';
    }
  }

  protected formatDate(value: string | null): string {
    if (!value) {
      return 'No deadline';
    }

    const date = this.parseDate(value);
    if (!date) {
      return value;
    }

    return new Intl.DateTimeFormat('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    }).format(date);
  }

  protected formatRelativeStatus(mission: MissionResponse): string {
    if (mission.status === 'completed') {
      return 'Done and ready for invoicing.';
    }

    if (!mission.deadline) {
      return 'No deadline set yet.';
    }

    const deadline = this.parseDate(mission.deadline);
    if (!deadline) {
      return 'No deadline set yet.';
    }

    const diffDays = Math.ceil((deadline.getTime() - Date.now()) / (1000 * 60 * 60 * 24));

    if (diffDays < 0) {
      return `${Math.abs(diffDays)} day${Math.abs(diffDays) > 1 ? 's' : ''} late`;
    }

    if (diffDays === 0) {
      return 'Due today';
    }

    return `${diffDays} day${diffDays > 1 ? 's' : ''} remaining`;
  }

  protected missionProgress(mission: MissionResponse): number {
    switch (mission.status) {
      case 'completed':
        return 92;
      case 'in_progress':
        return 60;
      default:
        return mission.priority === 'high' ? 35 : 20;
    }
  }

  protected trackMission(_: number, mission: MissionResponse): string {
    return mission.id;
  }

  protected hasMissionFieldError(controlName: keyof MissionFormControls): boolean {
    const control = this.missionForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  protected missionFieldError(controlName: keyof MissionFormControls): string {
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

  protected get isCreating(): boolean {
    return this.editingMissionId() === null;
  }

  protected goToPage(page: number): void {
    this.missionPageFacade.goToPage(page);
  }

  protected goToPreviousPage(): void {
    this.missionPageFacade.goToPreviousPage();
  }

  protected goToNextPage(): void {
    this.missionPageFacade.goToNextPage();
  }

  protected paginationItems(): MissionPaginationItem[] {
    return this.missionPageFacade.paginationItems();
  }

  private patchMissionForm(mission: MissionResponse | null): void {
    this.missionForm.patchValue(
      mission
        ? {
            title: mission.title,
            roleInProject: mission.roleInProject ?? '',
            description: mission.description ?? '',
            status: mission.status,
            priority: mission.priority,
            startDate: mission.startDate,
            deadline: mission.deadline ?? '',
          }
        : {
            title: '',
            roleInProject: '',
            description: '',
            status: 'not_started',
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
      status: this.missionForm.controls.status.value,
      priority: this.missionForm.controls.priority.value,
      startDate: this.normalize(this.missionForm.controls.startDate.value) ?? '',
      deadline: this.normalize(this.missionForm.controls.deadline.value),
    };
  }

  private normalize(value: string | null | undefined): string | null {
    const trimmed = value?.trim() ?? '';
    return trimmed.length > 0 ? trimmed : null;
  }

  private parseDate(value: string | null): Date | null {
    if (!value) {
      return null;
    }

    const parsed = new Date(`${value}T00:00:00`);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }

}
