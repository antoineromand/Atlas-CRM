import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ClientEditorDrawerComponent } from '../../components/client-editor-drawer/client-editor-drawer.component';
import { ClientEditorFacade } from '../../services/client-editor.facade';
import { ClientPageFacade } from '../../services/client-page.facade';
import { ClientService } from '../../../../core/services/client/client.service';
import { ClientResponse, ClientStatus } from '../../../../core/interface/client.interface';
import { NotificationService } from '../../../../core/services/notification/notification.service';
import { PaginationBarComponent } from '../../../../shared/ui/pagination-bar/pagination-bar.component';

type ClientFilterStatus = ClientStatus | 'all';

interface HeroCard {
  eyebrow: string;
  value: string;
  detail: string;
}

@Component({
  selector: 'app-clients-page-component',
  standalone: true,
  imports: [RouterLink, PaginationBarComponent, ClientEditorDrawerComponent],
  templateUrl: './clients-page-component.html',
  styleUrl: './clients-page-component.scss',
  providers: [ClientEditorFacade],
})
export class ClientsPageComponent implements OnInit {
  private readonly clientPageFacade = inject(ClientPageFacade);
  private readonly clientEditor = inject(ClientEditorFacade);
  private readonly clientService = inject(ClientService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly clients = this.clientPageFacade.clients;
  protected readonly pagination = this.clientPageFacade.paginatedClients;
  protected readonly isLoading = this.clientPageFacade.isLoading;
  protected readonly isRefreshing = this.clientPageFacade.isRefreshing;
  protected readonly loadError = this.clientPageFacade.loadError;
  protected readonly searchTerm = this.clientPageFacade.searchTerm;
  protected readonly searchWarning = this.clientPageFacade.searchWarning;
  protected readonly selectedStatus = this.clientPageFacade.selectedStatus;
  protected readonly deleteTarget = signal<ClientResponse | null>(null);
  protected readonly isDeleting = signal(false);

  protected readonly statusFilters: readonly { value: ClientFilterStatus; label: string }[] = [
    { value: 'all', label: 'All clients' },
    { value: 'active', label: 'Active' },
    { value: 'prospect', label: 'Pending' },
    { value: 'inactive', label: 'Inactive' },
    { value: 'archived', label: 'Completed' },
  ];

  protected readonly heroCards = computed<HeroCard[]>(() => {
    const total = this.clientPageFacade.totalClients();
    const active = this.clientPageFacade.activeClients();
    const retention = total > 0 ? Math.round((active / total) * 1000) / 10 : 0;
    const newClients = Math.max(1, Math.min(3, total));

    return [
      {
        eyebrow: 'Active portfolio',
        value: `${total} Active Clients`,
        detail: `+${newClients} new this month`,
      },
      {
        eyebrow: 'Retention rate',
        value: `${retention}%`,
        detail: 'Based on active clients in your workspace',
      },
    ];
  });

  protected readonly retentionRate = computed(() => {
    const total = this.clientPageFacade.totalClients();
    const active = this.clientPageFacade.activeClients();
    return total > 0 ? Math.round((active / total) * 1000) / 10 : 0;
  });

  ngOnInit(): void {
    this.clientPageFacade.initialize();

    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const createParam = params.get('create');
      const editParam = params.get('edit');

      if (editParam) {
        const isSameEdit =
          this.clientEditor.drawerVisible() &&
          this.clientEditor.drawerMode() === 'edit' &&
          this.clientEditor.editingClientId() === editParam;

        if (!isSameEdit) {
          this.clientEditor.openEditDrawer(editParam, false);
        }
        return;
      }

      if (createParam === '1') {
        const isSameCreate = this.clientEditor.drawerVisible() && this.clientEditor.drawerMode() === 'create';

        if (!isSameCreate) {
          this.clientEditor.openCreateDrawer(false);
        }
      }
    });
  }

  protected reload(): void {
    this.clientPageFacade.reload();
  }

  protected setSearchTerm(value: string): void {
    this.clientPageFacade.setSearchTerm(value);
  }

  protected clearSearch(): void {
    this.clientPageFacade.clearSearch();
  }

  protected setStatus(status: ClientFilterStatus): void {
    this.clientPageFacade.setStatus(status);
  }

  protected goToPreviousPage(): void {
    this.clientPageFacade.goToPreviousPage();
  }

  protected goToNextPage(): void {
    this.clientPageFacade.goToNextPage();
  }

  protected openClient(client: ClientResponse): void {
    void this.router.navigate(['/dashboard/clients', client.id]);
  }

  protected openCreateDrawer(syncQueryParams = true): void {
    this.clientEditor.openCreateDrawer(syncQueryParams);
  }

  protected openEditDrawer(clientId: string, syncQueryParams = true): void {
    this.clientEditor.openEditDrawer(clientId, syncQueryParams);
  }

  protected requestDelete(client: ClientResponse): void {
    this.deleteTarget.set(client);
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

    this.clientService
      .deleteClient(target.id)
      .pipe(finalize(() => this.isDeleting.set(false)))
      .subscribe({
        next: () => {
          this.notificationService.success('Client deleted.');
          this.deleteTarget.set(null);
          this.clientPageFacade.reload();
        },
        error: (error: unknown) => {
          const message = this.extractErrorMessage(error, 'Unable to delete client.');
          this.notificationService.error(message, 'Clients unavailable');
        },
      });
  }

  protected trackClient(index: number, client: ClientResponse): string {
    return `${client.id}-${index}`;
  }

  protected statusLabel(status: ClientStatus | 'all'): string {
    switch (status) {
      case 'prospect':
        return 'Pending';
      case 'active':
        return 'Active';
      case 'inactive':
        return 'Inactive';
      case 'archived':
        return 'Completed';
      default:
        return 'All Clients';
    }
  }

  protected contactName(client: ClientResponse): string {
    const firstName = client.primaryContactFirstName?.trim() ?? '';
    const lastName = client.primaryContactLastName?.trim() ?? '';
    const fullName = `${firstName} ${lastName}`.trim();
    return fullName || 'No primary contact';
  }

  protected initials(client: ClientResponse): string {
    const source = client.companyName.trim();
    return source.charAt(0).toUpperCase();
  }

  protected missionCount(client: ClientResponse, index: number): number {
    const statusBasedCount: Record<ClientStatus, number> = {
      active: 12,
      prospect: 5,
      inactive: 8,
      archived: 3,
    };

    return statusBasedCount[client.status] ?? Math.max(1, 4 - index);
  }

  private extractErrorMessage(error: unknown, fallback: string): string {
    const response = error as { error?: { message?: string } } | null | undefined;
    return response?.error?.message ?? fallback;
  }
}
