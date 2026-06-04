import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { StatCardComponent } from '../../../../shared/ui/stat-card/stat-card.component';
import { ClientPageFacade } from '../../services/client-page.facade';
import { ClientService } from '../../../../core/services/client/client.service';
import {
  ClientDetailResponse,
  ClientResponse,
  ClientStatus,
  CreateClientPayload,
  CreateClientResponse,
} from '../../../../core/interface/client.interface';
import { NotificationService } from '../../../../core/services/notification/notification.service';

type ClientFilterStatus = ClientStatus | 'all';
type ClientDrawerMode = 'create' | 'edit';

interface ClientStatCard {
  icon: string;
  badge: string;
  label: string;
  value: string;
  footer: string;
  tone: 'primary' | 'secondary' | 'accent' | 'danger';
}

interface ClientFormControls {
  companyName: FormControl<string>;
  status: FormControl<ClientStatus>;
  notes: FormControl<string>;
}

@Component({
  selector: 'app-clients-page-component',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, StatCardComponent],
  templateUrl: './clients-page-component.html',
  styleUrl: './clients-page-component.scss',
})
export class ClientsPageComponent implements OnInit {
  private readonly clientPageFacade = inject(ClientPageFacade);
  private readonly clientService = inject(ClientService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly clients = this.clientPageFacade.clients;
  protected readonly pagination = this.clientPageFacade.pagination;
  protected readonly isLoading = this.clientPageFacade.isLoading;
  protected readonly isRefreshing = this.clientPageFacade.isRefreshing;
  protected readonly loadError = this.clientPageFacade.loadError;
  protected readonly searchTerm = this.clientPageFacade.searchTerm;
  protected readonly searchWarning = this.clientPageFacade.searchWarning;
  protected readonly selectedStatus = this.clientPageFacade.selectedStatus;
  protected readonly drawerOpen = signal(false);
  protected readonly drawerLoading = signal(false);
  protected readonly drawerMode = signal<ClientDrawerMode>('create');
  protected readonly editingClientId = signal<string | null>(null);
  protected readonly deleteTarget = signal<ClientResponse | null>(null);
  protected readonly isSaving = signal(false);
  protected readonly isDeleting = signal(false);

  protected readonly statusFilters: readonly { value: ClientFilterStatus; label: string }[] = [
    { value: 'all', label: 'All clients' },
    { value: 'prospect', label: 'Prospects' },
    { value: 'active', label: 'Active' },
    { value: 'inactive', label: 'Inactive' },
    { value: 'archived', label: 'Archived' },
  ];

  protected readonly clientStatusOptions: readonly { value: ClientStatus; label: string }[] = [
    { value: 'prospect', label: 'Prospect' },
    { value: 'active', label: 'Active' },
    { value: 'inactive', label: 'Inactive' },
    { value: 'archived', label: 'Archived' },
  ];

  protected readonly stats = computed<ClientStatCard[]>(() => {
    const pagination = this.pagination();

    return [
      {
        icon: 'group',
        badge: 'Portfolio',
        label: 'Total clients',
        value: String(this.clientPageFacade.totalClients()),
        footer: pagination ? `Page ${pagination.page} of ${pagination.totalPages || 1}` : 'Current workspace snapshot',
        tone: 'primary',
      },
      {
        icon: 'fact_check',
        badge: 'Current page',
        label: 'Active',
        value: String(this.clientPageFacade.activeClients()),
        footer: 'Visible on the current results page',
        tone: 'accent',
      },
      {
        icon: 'person_search',
        badge: 'Current page',
        label: 'Prospects',
        value: String(this.clientPageFacade.prospectClients()),
        footer: 'Clients still in discovery',
        tone: 'secondary',
      },
      {
        icon: 'filter_alt',
        badge: 'Selection',
        label: 'Filters',
        value: this.selectedStatus() === 'all' ? 'All' : this.statusLabel(this.selectedStatus()),
        footer: this.searchTerm().trim() ? 'Search and status filters combined' : 'No search filter applied',
        tone: 'danger',
      },
    ];
  });

  protected readonly pageItems = computed(() => this.clientPageFacade.paginationItems());

  protected readonly clientForm = new FormGroup<ClientFormControls>({
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

  ngOnInit(): void {
    this.clientPageFacade.initialize();

    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const createParam = params.get('create');
      const editParam = params.get('edit');

      if (editParam) {
        void this.openEditDrawer(editParam, false);
        return;
      }

      if (createParam === '1') {
        this.openCreateDrawer(false);
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

  protected resetFilters(): void {
    this.clientPageFacade.resetFilters();
  }

  protected goToPage(page: number): void {
    this.clientPageFacade.goToPage(page);
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
    this.drawerMode.set('create');
    this.editingClientId.set(null);
    this.deleteTarget.set(null);
    this.patchClientForm(null);
    this.drawerLoading.set(false);
    this.drawerOpen.set(true);

    if (syncQueryParams) {
      void this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { create: '1', edit: null },
        queryParamsHandling: 'merge',
      });
    }
  }

  protected openEditDrawer(clientId: string, syncQueryParams = true): void {
    this.drawerMode.set('edit');
    this.deleteTarget.set(null);
    this.drawerLoading.set(true);
    this.editingClientId.set(clientId);

    this.clientService.getClientById(clientId).subscribe({
      next: (detail) => {
        this.patchClientForm(detail.client);
        this.drawerLoading.set(false);
        this.drawerOpen.set(true);

        if (syncQueryParams) {
          void this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { create: null, edit: clientId },
            queryParamsHandling: 'merge',
          });
        }
      },
      error: (error: any) => {
        this.drawerLoading.set(false);
        this.editingClientId.set(null);
        this.drawerOpen.set(false);
        const message = error?.error?.message ?? 'Unable to load client for editing.';
        this.notificationService.error(message, 'Clients unavailable');
      },
    });
  }

  protected closeDrawer(): void {
    this.drawerOpen.set(false);
    this.drawerLoading.set(false);
    this.drawerMode.set('create');
    this.editingClientId.set(null);
    this.clientForm.markAsPristine();
    this.clientForm.markAsUntouched();

    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { create: null, edit: null },
      queryParamsHandling: 'merge',
    });
  }

  protected submitClient(): void {
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
          next: () => {
            this.notificationService.success('Client updated.');
            this.closeDrawer();
            this.clientPageFacade.reload();
          },
          error: (error: any) => {
            const message = error?.error?.message ?? 'Unable to update client.';
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
          this.clientPageFacade.goToPage(1);
          void this.router.navigate(['/dashboard/clients', response.clientId]);
        },
        error: (error: any) => {
          const message = error?.error?.message ?? 'Unable to create client.';
          this.notificationService.error(message, 'Clients unavailable');
        },
      });
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
        error: (error: any) => {
          const message = error?.error?.message ?? 'Unable to delete client.';
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
        return 'Prospect';
      case 'active':
        return 'Active';
      case 'inactive':
        return 'Inactive';
      case 'archived':
        return 'Archived';
      default:
        return 'All clients';
    }
  }

  protected formatDate(value: string | null | undefined): string {
    if (!value) {
      return 'Not available';
    }

    return new Intl.DateTimeFormat('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    }).format(new Date(value));
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

  protected hasClientFieldError(controlName: keyof ClientFormControls): boolean {
    const control = this.clientForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  protected clientFieldError(controlName: keyof ClientFormControls): string {
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

  protected get isEditing(): boolean {
    return this.drawerMode() === 'edit';
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

  private buildPayload(): CreateClientPayload {
    return {
      companyName: this.normalize(this.clientForm.controls.companyName.value) ?? '',
      status: this.clientForm.controls.status.value,
      notes: this.normalize(this.clientForm.controls.notes.value),
    };
  }

  private normalize(value: string | null | undefined): string | null {
    const trimmed = value?.trim() ?? '';
    return trimmed.length > 0 ? trimmed : null;
  }
}
