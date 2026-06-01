import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { StatCardComponent } from '../../../../shared/ui/stat-card/stat-card.component';
import { MissionService } from '../../../../core/services/mission/mission.service';
import {
  CreateMissionPayload,
  MissionPriority,
  MissionResponse,
  MissionStatus,
} from '../../../../core/interface/mission.interface';
import { NotificationService } from '../../../../core/services/notification/notification.service';

type MissionViewMode = 'cards' | 'list';

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
export class MissionsPageComponent implements OnInit {
  private readonly missionService = inject(MissionService);
  private readonly notificationService = inject(NotificationService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly missions = signal<MissionResponse[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly isSaving = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly loadError = signal<string | null>(null);
  protected readonly searchTerm = signal('');
  protected readonly viewMode = signal<MissionViewMode>('cards');
  protected readonly drawerOpen = signal(false);
  protected readonly deleteTarget = signal<MissionResponse | null>(null);
  protected readonly editingMissionId = signal<string | null>(null);
  private searchDebounceTimer: ReturnType<typeof setTimeout> | null = null;

  protected readonly missionStats = computed<MissionStatCard[]>(() => {
    const missions = this.missions();
    const today = new Date();
    const dueSoon = missions.filter((mission) => {
      if (mission.status === 'completed' || !mission.deadline) {
        return false;
      }

      const deadline = this.parseDate(mission.deadline);
      if (!deadline) {
        return false;
      }

      const diff = deadline.getTime() - today.getTime();
      return diff >= 0 && diff <= 1000 * 60 * 60 * 24 * 7;
    }).length;

    return [
      {
        icon: 'rocket_launch',
        badge: '+1 this week',
        label: 'Active missions',
        value: String(missions.filter((mission) => mission.status !== 'completed').length),
        footer: 'Current workload in progress',
        tone: 'accent',
      },
      {
        icon: 'schedule',
        badge: 'Deadline watch',
        label: 'Due soon',
        value: String(dueSoon),
        footer: 'Within the next 7 days',
        tone: 'danger',
      },
      {
        icon: 'task_alt',
        badge: 'Done',
        label: 'Completed',
        value: String(missions.filter((mission) => mission.status === 'completed').length),
        footer: 'Closed and archived in the board',
        tone: 'secondary',
      },
      {
        icon: 'priority_high',
        badge: 'Focus',
        label: 'High priority',
        value: String(missions.filter((mission) => mission.priority === 'high').length),
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
    this.loadMissions();

    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      if (params.get('create') === '1') {
        this.openCreateDrawer(false);
      }
    });
  }

  protected reload(): void {
    this.loadMissions(this.searchTerm());
  }

  protected setSearchTerm(value: string): void {
    this.searchTerm.set(value);
    this.queueSearch(value);
  }

  protected setViewMode(mode: MissionViewMode): void {
    this.viewMode.set(mode);
  }

  protected openCreateDrawer(syncQueryParams = true): void {
    this.editingMissionId.set(null);
    this.deleteTarget.set(null);
    this.patchMissionForm(null);
    this.drawerOpen.set(true);

    if (syncQueryParams) {
      void this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { create: '1' },
        queryParamsHandling: 'merge',
      });
    }
  }

  protected openEditDrawer(mission: MissionResponse): void {
    this.editingMissionId.set(mission.id);
    this.deleteTarget.set(null);
    this.patchMissionForm(mission);
    this.drawerOpen.set(true);

    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { create: null },
      queryParamsHandling: 'merge',
    });
  }

  protected closeDrawer(): void {
    this.drawerOpen.set(false);
    this.editingMissionId.set(null);
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
          this.loadMissions(this.searchTerm());
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
          this.loadMissions(this.searchTerm());
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
          this.loadMissions(this.searchTerm());
        },
        error: (error: any) => {
          const message = error?.error?.message ?? 'Unable to delete mission.';
          this.notificationService.error(message);
        },
      });
  }

  protected toggleMissionStatus(mission: MissionResponse): void {
    const nextStatus: MissionStatus =
      mission.status === 'not_started'
        ? 'in_progress'
        : mission.status === 'in_progress'
          ? 'completed'
          : 'not_started';

    this.missionService
      .updateMission(mission.id, { status: nextStatus })
      .subscribe({
        next: () => {
          this.notificationService.success('Mission status updated.');
          this.loadMissions(this.searchTerm());
        },
        error: (error: any) => {
          const message = error?.error?.message ?? 'Unable to update mission status.';
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

  private loadMissions(search: string | null | undefined = this.searchTerm()): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.missionService
      .searchMyMissions(search)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (missions) => this.missions.set(missions),
        error: (error: any) => {
          const message = error?.error?.message ?? 'Unable to load missions.';
          this.loadError.set(message);
          this.notificationService.error(message, 'Missions unavailable');
        },
      });
  }

  private queueSearch(search: string): void {
    if (this.searchDebounceTimer) {
      clearTimeout(this.searchDebounceTimer);
    }

    this.searchDebounceTimer = setTimeout(() => {
      this.loadMissions(search);
    }, 250);
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
