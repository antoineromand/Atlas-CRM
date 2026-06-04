import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { ClientService } from '../../../core/services/client/client.service';
import {
  ClientPageResponse,
  ClientResponse,
  ClientStatus,
} from '../../../core/interface/client.interface';
import { NotificationService } from '../../../core/services/notification/notification.service';

type ClientFilterStatus = ClientStatus | 'all';
type ClientPaginationItem = {
  key: string;
  kind: 'page' | 'ellipsis';
  page: number;
};

@Injectable({
  providedIn: 'root',
})
export class ClientPageFacade {
  private readonly clientService = inject(ClientService);
  private readonly notificationService = inject(NotificationService);
  private searchDebounceTimer: ReturnType<typeof setTimeout> | null = null;

  readonly clients = signal<ClientResponse[]>([]);
  readonly pagination = signal<ClientPageResponse | null>(null);
  readonly isLoading = signal(true);
  readonly isRefreshing = signal(false);
  readonly loadError = signal<string | null>(null);
  readonly searchTerm = signal('');
  readonly searchWarning = signal<string | null>(null);
  readonly selectedStatus = signal<ClientFilterStatus>('all');
  readonly currentPage = signal(1);
  readonly pageSize = signal(8);

  readonly totalClients = computed(() => this.pagination()?.totalElements ?? 0);
  readonly currentPageClients = computed(() => this.clients().length);
  readonly activeClients = computed(() => this.clients().filter((client) => client.status === 'active').length);
  readonly prospectClients = computed(() => this.clients().filter((client) => client.status === 'prospect').length);

  initialize(): void {
    this.loadClients(this.searchTerm(), this.selectedStatus(), this.currentPage());
  }

  reload(): void {
    this.loadClients(this.searchTerm(), this.selectedStatus(), this.currentPage());
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
    this.loadClients('', this.selectedStatus(), 1);
  }

  setStatus(status: ClientFilterStatus): void {
    if (this.selectedStatus() === status) {
      return;
    }

    this.selectedStatus.set(status);
    this.currentPage.set(1);
    this.loadClients(this.searchTerm(), status, 1);
  }

  goToPage(page: number): void {
    if (this.pagination()?.page === page || this.isRefreshing()) {
      return;
    }

    this.currentPage.set(page);
    this.loadClients(this.searchTerm(), this.selectedStatus(), page);
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

  resetFilters(): void {
    this.searchTerm.set('');
    this.selectedStatus.set('all');

    if (this.searchDebounceTimer) {
      clearTimeout(this.searchDebounceTimer);
      this.searchDebounceTimer = null;
    }

    this.currentPage.set(1);
    this.loadClients('', 'all', 1);
  }

  paginationItems(): ClientPaginationItem[] {
    const pagination = this.pagination();
    if (!pagination || pagination.totalPages <= 0) {
      return [];
    }

    const totalPages = pagination.totalPages;
    const currentPage = pagination.page;
    const items: ClientPaginationItem[] = [];
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

  private loadClients(
    search: string | null | undefined = this.searchTerm(),
    status: ClientFilterStatus = this.selectedStatus(),
    page = this.currentPage(),
  ): void {
    const normalizedSearch = search?.trim() ?? '';
    if (normalizedSearch.length > 0 && normalizedSearch.length < 3) {
      this.searchWarning.set('Type at least 3 characters to search.');
      this.isRefreshing.set(false);
      this.isLoading.set(false);
      return;
    }

    const apiStatus = status === 'all' ? null : status;
    const hasExistingContent = this.clients().length > 0;
    this.searchWarning.set(null);
    this.isLoading.set(!hasExistingContent);
    this.isRefreshing.set(hasExistingContent);
    this.loadError.set(null);
    this.currentPage.set(page);

    this.clientService
      .listMyClients(normalizedSearch, apiStatus, page, this.pageSize())
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (pagination) => {
          this.pagination.set(pagination);
          this.clients.set(pagination.items);
          this.currentPage.set(pagination.page);
          this.isRefreshing.set(false);
        },
        error: (error: any) => {
          const message = error?.error?.message ?? 'Unable to load clients.';
          this.isRefreshing.set(false);

          if (hasExistingContent) {
            this.notificationService.error(message, 'Clients unavailable');
            return;
          }

          this.pagination.set(null);
          this.loadError.set(message);
          this.notificationService.error(message, 'Clients unavailable');
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
      this.loadClients(search, this.selectedStatus(), 1);
    }, 250);
  }
}
