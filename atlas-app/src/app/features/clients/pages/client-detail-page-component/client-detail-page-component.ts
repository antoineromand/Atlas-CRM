import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { NgStyle } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ClientService } from '../../../../core/services/client/client.service';
import {
  ClientActivityResponse,
  ClientContactResponse,
  ClientDetailResponse,
  ClientResponse,
  ClientTagResponse,
} from '../../../../core/interface/client.interface';
import { StatCardComponent } from '../../../../shared/ui/stat-card/stat-card.component';
import { NotificationService } from '../../../../core/services/notification/notification.service';

interface DetailStatCard {
  icon: string;
  badge: string;
  label: string;
  value: string;
  footer: string;
  tone: 'primary' | 'secondary' | 'accent' | 'danger';
}

@Component({
  selector: 'app-client-detail-page-component',
  standalone: true,
  imports: [RouterLink, StatCardComponent, NgStyle],
  templateUrl: './client-detail-page-component.html',
  styleUrl: './client-detail-page-component.scss',
})
export class ClientDetailPageComponent {
  private readonly clientService = inject(ClientService);
  private readonly notificationService = inject(NotificationService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly detail = signal<ClientDetailResponse | null>(null);
  protected readonly isLoading = signal(true);
  protected readonly loadError = signal<string | null>(null);
  protected readonly clientId = signal<string | null>(null);
  protected readonly deleteTarget = signal<ClientResponse | null>(null);
  protected readonly isDeleting = signal(false);

  protected readonly client = computed<ClientResponse | null>(() => this.detail()?.client ?? null);
  protected readonly contacts = computed<ClientContactResponse[]>(() => this.detail()?.contacts ?? []);
  protected readonly activities = computed<ClientActivityResponse[]>(() => this.detail()?.activities ?? []);
  protected readonly tags = computed<ClientTagResponse[]>(() => this.detail()?.tags ?? []);
  protected readonly primaryContact = computed<ClientContactResponse | null>(
    () => this.contacts().find((contact) => contact.primary) ?? this.contacts()[0] ?? null,
  );

  protected readonly stats = computed<DetailStatCard[]>(() => {
    const client = this.client();
    return [
      {
        icon: 'groups',
        badge: 'Contacts',
        label: 'People linked',
        value: String(this.contacts().length),
        footer: 'Visible in the detailed CRM record',
        tone: 'primary',
      },
      {
        icon: 'history',
        badge: 'Timeline',
        label: 'Activities',
        value: String(this.activities().length),
        footer: 'Calls, emails and notes already logged',
        tone: 'accent',
      },
      {
        icon: 'sell',
        badge: 'Labels',
        label: 'Tags',
        value: String(this.tags().length),
        footer: 'Segments and quick filters',
        tone: 'secondary',
      },
      {
        icon: 'badge',
        badge: 'Status',
        label: 'Client state',
        value: client ? this.statusLabel(client.status) : 'Unknown',
        footer: client ? `Updated ${this.formatDate(client.updatedAt || client.createdAt)}` : 'No client loaded',
        tone: 'danger',
      },
    ];
  });

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const clientId = params.get('clientId');
      this.clientId.set(clientId);

      if (!clientId) {
        this.loadError.set('Missing client identifier.');
        this.isLoading.set(false);
        return;
      }

      this.loadClient(clientId);
    });
  }

  protected reload(): void {
    const clientId = this.clientId();
    if (!clientId) {
      return;
    }

    this.loadClient(clientId);
  }

  protected goBack(): void {
    void this.router.navigate(['/dashboard/clients']);
  }

  protected editClient(): void {
    const clientId = this.clientId();
    if (!clientId) {
      return;
    }

    void this.router.navigate(['/dashboard/clients'], {
      queryParams: { edit: clientId },
    });
  }

  protected requestDelete(): void {
    const client = this.client();
    if (!client) {
      return;
    }

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
          void this.router.navigate(['/dashboard/clients']);
        },
        error: (error: any) => {
          const message = error?.error?.message ?? 'Unable to delete client.';
          this.notificationService.error(message, 'Clients unavailable');
        },
      });
  }

  protected statusLabel(status: ClientResponse['status']): string {
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
        return 'Unknown';
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

  protected formatDateTime(value: string | null | undefined): string {
    if (!value) {
      return 'Not available';
    }

    return new Intl.DateTimeFormat('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(value));
  }

  protected contactName(contact: ClientContactResponse): string {
    const fullName = `${contact.firstName ?? ''} ${contact.lastName ?? ''}`.trim();
    return fullName || 'Unnamed contact';
  }

  protected contactMeta(contact: ClientContactResponse): string {
    const parts = [contact.jobTitle, contact.email].filter((value) => !!value);
    return parts.length > 0 ? parts.join(' · ') : 'No extra information';
  }

  protected activityLabel(activity: ClientActivityResponse): string {
    switch (activity.activityType) {
      case 'call':
        return 'Call';
      case 'email':
        return 'Email';
      case 'meeting':
        return 'Meeting';
      case 'note':
        return 'Note';
      case 'task':
        return 'Task';
      case 'follow_up':
        return 'Follow up';
      case 'status_change':
        return 'Status change';
      default:
        return activity.activityType;
    }
  }

  protected activityTone(activity: ClientActivityResponse): string {
    switch (activity.activityType) {
      case 'call':
      case 'email':
        return 'activity-chip--accent';
      case 'meeting':
        return 'activity-chip--primary';
      case 'task':
        return 'activity-chip--secondary';
      case 'follow_up':
        return 'activity-chip--warning';
      case 'status_change':
        return 'activity-chip--danger';
      default:
        return 'activity-chip--neutral';
    }
  }

  protected tagStyle(tag: ClientTagResponse): Record<string, string> {
    const color = tag.color ?? '#091426';
    return {
      borderColor: this.withAlpha(color, 0.22),
      backgroundColor: this.withAlpha(color, 0.12),
      color,
    };
  }

  protected contactInitials(contact: ClientContactResponse): string {
    const source = `${contact.firstName ?? ''} ${contact.lastName ?? ''}`.trim();
    if (!source) {
      return 'C';
    }

    return source
      .split(/\s+/)
      .slice(0, 2)
      .map((part) => part.charAt(0).toUpperCase())
      .join('');
  }

  private loadClient(clientId: string): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.clientService.getClientById(clientId).subscribe({
      next: (detail) => {
        this.detail.set(detail);
        this.isLoading.set(false);
      },
      error: (error: any) => {
        const message = error?.error?.message ?? 'Unable to load client details.';
        this.detail.set(null);
        this.loadError.set(message);
        this.isLoading.set(false);
      },
    });
  }

  private withAlpha(color: string, alpha: number): string {
    const hex = color.replace('#', '').trim();
    if (hex.length !== 6) {
      return color;
    }

    const red = Number.parseInt(hex.slice(0, 2), 16);
    const green = Number.parseInt(hex.slice(2, 4), 16);
    const blue = Number.parseInt(hex.slice(4, 6), 16);
    return `rgba(${red}, ${green}, ${blue}, ${alpha})`;
  }
}
