import { Component, DestroyRef, OnInit, computed, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { StatCardComponent } from '../../../../shared/ui/stat-card/stat-card.component';
import { MissionPageFacade } from '../../services/mission-page.facade';
import { PaginationBarComponent } from '../../../../shared/ui/pagination-bar/pagination-bar.component';
import { MissionEditorFacade, MissionFormControlName } from '../../services/mission-editor.facade';
import { MissionPriority, MissionResponse, MissionStatus } from '../../../../core/interface/mission.interface';

type MissionViewMode = 'cards' | 'list';

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
  providers: [MissionEditorFacade],
  imports: [ReactiveFormsModule, StatCardComponent, PaginationBarComponent],
  templateUrl: './missions-page-component.html',
  styleUrl: './missions-page-component.scss',
})
export class MissionsPageComponent implements OnInit {
  private readonly missionPageFacade = inject(MissionPageFacade);
  private readonly missionEditorFacade = inject(MissionEditorFacade);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly missions = this.missionPageFacade.missions;
  protected readonly pagination = this.missionPageFacade.pagination;
  protected readonly isLoading = this.missionPageFacade.isLoading;
  protected readonly loadError = this.missionPageFacade.loadError;
  protected readonly searchTerm = this.missionPageFacade.searchTerm;
  protected readonly searchWarning = this.missionPageFacade.searchWarning;
  protected readonly viewMode = this.missionPageFacade.viewMode;
  protected readonly isRefreshing = this.missionPageFacade.isRefreshing;
  protected readonly drawerVisible = this.missionEditorFacade.drawerVisible;
  protected readonly drawerOpen = this.missionEditorFacade.drawerOpen;
  protected readonly deleteTarget = this.missionEditorFacade.deleteTarget;
  protected readonly editingMissionId = this.missionEditorFacade.editingMissionId;
  protected readonly isSaving = this.missionEditorFacade.isSaving;
  protected readonly isDeleting = this.missionEditorFacade.isDeleting;
  protected readonly clients = this.missionEditorFacade.clients;
  protected readonly clientsLoaded = this.missionEditorFacade.clientsLoaded;
  protected readonly clientsLoading = this.missionEditorFacade.clientsLoading;
  protected readonly missionForm = this.missionEditorFacade.missionForm;
  protected readonly missionStatusOptions = this.missionEditorFacade.missionStatusOptions;
  protected readonly missionPriorityOptions = this.missionEditorFacade.missionPriorityOptions;
  protected readonly isCreating = this.missionEditorFacade.isCreating;

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

  ngOnInit(): void {
    this.missionPageFacade.initialize();
    this.missionEditorFacade.initialize();

    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      if (params.get('create') === '1') {
        this.openCreateDrawer(false);
      }
    });
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
    this.missionEditorFacade.openCreateDrawer(syncQueryParams);
  }

  protected openEditDrawer(mission: MissionResponse): void {
    this.missionEditorFacade.openEditDrawer(mission);
  }

  protected closeDrawer(): void {
    this.missionEditorFacade.closeDrawer();
  }

  protected submitMission(): void {
    this.missionEditorFacade.submitMission();
  }

  protected requestDelete(mission: MissionResponse): void {
    this.missionEditorFacade.requestDelete(mission);
  }

  protected cancelDelete(): void {
    this.missionEditorFacade.cancelDelete();
  }

  protected confirmDelete(): void {
    this.missionEditorFacade.confirmDelete();
  }

  protected statusLabel(status: MissionStatus): string {
    switch (status) {
      case 'created':
        return 'Created';
      case 'analysed':
        return 'Analysed';
      case 'planned':
        return 'Planned';
      case 'started':
        return 'Started';
      case 'finalized':
        return 'Finalized';
      case 'shipped':
        return 'Shipped';
      case 'completed':
        return 'Completed';
      case 'in_progress':
        return 'In progress';
      default:
        return 'Created';
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

  private parseDate(value: string | null): Date | null {
    if (!value) {
      return null;
    }

    const parsed = new Date(`${value}T00:00:00`);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
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
    return mission.progress;
  }

  protected clientLabel(clientId: string | null): string {
    return this.missionEditorFacade.clientLabel(clientId);
  }

  protected trackMission(_: number, mission: MissionResponse): string {
    return mission.id;
  }

  protected hasMissionFieldError(controlName: MissionFormControlName): boolean {
    return this.missionEditorFacade.hasMissionFieldError(controlName);
  }

  protected missionFieldError(controlName: MissionFormControlName): string {
    return this.missionEditorFacade.missionFieldError(controlName);
  }

  protected get isCreating(): boolean {
    return this.missionEditorFacade.isCreating();
  }

  protected goToPreviousPage(): void {
    this.missionPageFacade.goToPreviousPage();
  }

  protected goToNextPage(): void {
    this.missionPageFacade.goToNextPage();
  }
}
