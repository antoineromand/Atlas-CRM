import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { MissionService } from '../../../core/services/mission/mission.service';
import { MissionPageResponse, MissionResponse, MissionSummaryResponse } from '../../../core/interface/mission.interface';
import { NotificationService } from '../../../core/services/notification/notification.service';

type MissionViewMode = 'cards' | 'list';
type MissionPaginationItem = {
  key: string;
  kind: 'page' | 'ellipsis';
  page: number;
};

@Injectable({
  providedIn: 'root',
})
export class MissionPageFacade {
  private readonly missionService = inject(MissionService);
  private readonly notificationService = inject(NotificationService);
  private searchDebounceTimer: ReturnType<typeof setTimeout> | null = null;

  readonly missions = signal<MissionResponse[]>([]);
  readonly pagination = signal<MissionPageResponse | null>(null);
  readonly summary = signal<MissionSummaryResponse | null>(null);
  readonly isLoading = signal(true);
  readonly loadError = signal<string | null>(null);
  readonly searchTerm = signal('');
  readonly searchWarning = signal<string | null>(null);
  readonly viewMode = signal<MissionViewMode>('cards');
  readonly isRefreshing = signal(false);
  readonly currentPage = signal(1);
  readonly pageSize = computed(() => (this.viewMode() === 'cards' ? 6 : 10));

  initialize(): void {
    this.loadMissions(this.searchTerm(), this.currentPage());
    this.loadSummary();
  }

  reload(): void {
    this.loadMissions(this.searchTerm(), this.currentPage());
  }

  reloadSummary(): void {
    this.loadSummary();
  }

  setSearchTerm(value: string): void {
    this.searchTerm.set(value);
    this.searchWarning.set(null);
    this.queueSearch(value);
  }

  clearSearch(): void {
    if (!this.searchTerm()) {
      return;
    }

    this.searchTerm.set('');
    this.searchWarning.set(null);

    if (this.searchDebounceTimer) {
      clearTimeout(this.searchDebounceTimer);
      this.searchDebounceTimer = null;
    }

    this.currentPage.set(1);
    this.loadMissions('', 1);
  }

  setViewMode(mode: MissionViewMode): void {
    if (this.viewMode() === mode) {
      return;
    }

    this.viewMode.set(mode);
    this.currentPage.set(1);
    this.loadMissions(this.searchTerm(), 1);
  }

  goToPage(page: number): void {
    if (this.pagination()?.page === page || this.isRefreshing()) {
      return;
    }

    this.currentPage.set(page);
    this.loadMissions(this.searchTerm(), page);
  }

  goToPreviousPage(): void {
    const pagination = this.pagination();
    if (!pagination?.hasPrevious) {
      return;
    }

    this.goToPage(pagination.page - 1);
  }

  goToNextPage(): void {
    const pagination = this.pagination();
    if (!pagination?.hasNext) {
      return;
    }

    this.goToPage(pagination.page + 1);
  }

  paginationItems(): MissionPaginationItem[] {
    const pagination = this.pagination();
    if (!pagination) {
      return [];
    }

    const totalPages = pagination.totalPages;
    const currentPage = pagination.page;
    const items: MissionPaginationItem[] = [];
    const pushPage = (page: number): void => {
      items.push({ key: `page-${page}`, kind: 'page', page });
    };

    if (totalPages <= 7) {
      for (let page = 1; page <= totalPages; page += 1) {
        pushPage(page);
      }
      return items;
    }

    pushPage(1);

    const left = Math.max(2, currentPage - 1);
    const right = Math.min(totalPages - 1, currentPage + 1);

    if (left > 2) {
      items.push({ key: 'ellipsis-left', kind: 'ellipsis', page: -1 });
    }

    for (let page = left; page <= right; page += 1) {
      pushPage(page);
    }

    if (right < totalPages - 1) {
      items.push({ key: 'ellipsis-right', kind: 'ellipsis', page: -1 });
    }

    pushPage(totalPages);
    return items;
  }

  refreshCurrentPage(): void {
    this.loadMissions(this.searchTerm(), this.currentPage());
  }

  resetToFirstPage(): void {
    this.currentPage.set(1);
    this.loadMissions(this.searchTerm(), 1);
  }

  private loadSummary(): void {
    this.missionService.getMissionSummary().subscribe({
      next: (summary) => {
        this.summary.set(summary);
      },
      error: (error: any) => {
        this.summary.set(null);
        const message = error?.error?.message ?? 'Unable to load mission summary.';
        this.notificationService.error(message, 'Mission summary unavailable');
      },
    });
  }

  private loadMissions(search: string | null | undefined = this.searchTerm(), page = this.currentPage()): void {
    const normalizedSearch = search?.trim() ?? '';
    if (normalizedSearch.length > 0 && normalizedSearch.length < 3) {
      this.searchWarning.set('Type at least 3 characters to search.');
      this.isRefreshing.set(false);
      this.isLoading.set(false);
      return;
    }

    this.searchWarning.set(null);
    const hasExistingContent = this.missions().length > 0;
    this.isLoading.set(!hasExistingContent);
    this.isRefreshing.set(hasExistingContent);
    this.loadError.set(null);
    this.currentPage.set(page);

    this.missionService
      .searchMyMissions(search, page, this.pageSize())
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (pagination) => {
          this.pagination.set(pagination);
          this.missions.set(pagination.items);
          this.currentPage.set(pagination.page);
          this.isRefreshing.set(false);
        },
        error: (error: any) => {
          const message = error?.error?.message ?? 'Unable to load missions.';
          this.isRefreshing.set(false);

          if (hasExistingContent) {
            this.notificationService.error(message, 'Missions unavailable');
            return;
          }

          this.pagination.set(null);
          this.loadError.set(message);
          this.notificationService.error(message, 'Missions unavailable');
        },
      });
  }

  private queueSearch(search: string): void {
    if (this.searchDebounceTimer) {
      clearTimeout(this.searchDebounceTimer);
    }

    const normalizedSearch = search.trim();
    if (normalizedSearch.length > 0 && normalizedSearch.length < 3) {
      this.searchWarning.set('Type at least 3 characters to search.');
      this.isRefreshing.set(false);
      return;
    }

    this.searchDebounceTimer = setTimeout(() => {
      this.currentPage.set(1);
      this.loadMissions(search, 1);
    }, 250);
  }
}
