import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ClientService } from '../../../../core/services/client/client.service';
import {
  ClientActivityResponse,
  ClientContactResponse,
  ClientDetailResponse,
} from '../../../../core/interface/client.interface';
import { NotificationService } from '../../../../core/services/notification/notification.service';

interface SummaryCard {
  label: string;
  value: string;
  note: string;
  icon: string;
}

interface TimelineItem {
  icon: string;
  tone: 'primary' | 'secondary' | 'accent';
  title: string;
  description: string;
  meta: string;
}

@Component({
  selector: 'app-client-detail-page-component',
  standalone: true,
  imports: [],
  templateUrl: './client-detail-page-component.html',
  styleUrl: './client-detail-page-component.scss',
})
export class ClientDetailPageComponent implements OnInit {
  private readonly clientService = inject(ClientService);
  private readonly notificationService = inject(NotificationService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly clientDetail = signal<ClientDetailResponse | null>(null);
  protected readonly isLoading = signal(true);
  protected readonly loadError = signal<string | null>(null);

  protected readonly client = computed(() => this.clientDetail()?.client ?? null);
  protected readonly contacts = computed(() => this.clientDetail()?.contacts ?? []);
  protected readonly activities = computed(() => this.clientDetail()?.activities ?? []);
  protected readonly tags = computed(() => this.clientDetail()?.tags ?? []);

  protected readonly primaryContact = computed<ClientContactResponse | null>(() => {
    const contacts = this.contacts();
    return contacts.find((contact) => contact.primary) ?? contacts[0] ?? null;
  });

  protected readonly summaryCards = computed<SummaryCard[]>(() => {
    const client = this.client();
    const contacts = this.contacts();
    const tags = this.tags();
    const activities = this.activities();

    if (!client) {
      return [];
    }

    return [
      {
        label: 'Status',
        value: this.formatStatus(client.status),
        note: 'Client lifecycle',
        icon: 'badge',
      },
      {
        label: 'Contacts',
        value: String(contacts.length),
        note: 'Known stakeholders',
        icon: 'group',
      },
      {
        label: 'Tags',
        value: String(tags.length),
        note: 'Segment and priority',
        icon: 'sell',
      },
      {
        label: 'Activity',
        value: String(activities.length),
        note: 'Logged touchpoints',
        icon: 'timeline',
      },
    ];
  });

  protected readonly timelineEntries = computed(() =>
    this.activities().map((activity) => ({
      activity,
      item: this.toTimelineItem(activity),
    })),
  );

  protected readonly visibleContacts = computed(() =>
    this.contacts().slice(0, 3).map((contact) => ({
      ...contact,
      displayName: this.contactName(contact),
      role: this.contactRole(contact),
      initials: this.contactInitials(contact),
    })),
  );

  protected readonly activeProjectsCount = computed(() => {
    const count = this.timelineEntries().length;
    return count > 2 ? 3 : Math.max(count, 1);
  });

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const clientId = params.get('clientId');

      if (!clientId) {
        this.loadError.set('Missing client identifier.');
        this.isLoading.set(false);
        this.clientDetail.set(null);
        return;
      }

      this.loadClient(clientId);
    });
  }

  protected goBack(): void {
    void this.router.navigate(['/dashboard/clients']);
  }

  protected editClient(): void {
    const client = this.client();
    if (!client) {
      return;
    }

    void this.router.navigate(['/dashboard/clients'], {
      queryParams: { edit: client.id },
    });
  }

  private loadClient(clientId: string): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.clientService
      .getClientById(clientId)
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (detail) => {
          this.clientDetail.set(detail);
        },
        error: (error: any) => {
          this.clientDetail.set(null);
          const message = error?.error?.message ?? 'Unable to load client details.';
          this.loadError.set(message);
          this.notificationService.error(message, 'Client unavailable');
        },
      });
  }

  private contactName(contact: ClientContactResponse): string {
    return [contact.firstName, contact.lastName].filter(Boolean).join(' ') || 'Unnamed contact';
  }

  private contactRole(contact: ClientContactResponse): string {
    return contact.jobTitle?.trim() || 'Contact';
  }

  private contactInitials(contact: ClientContactResponse): string {
    const first = contact.firstName?.trim()?.[0] ?? '';
    const last = contact.lastName?.trim()?.[0] ?? '';
    const initials = `${first}${last}`.trim();
    return initials || '??';
  }

  protected formatStatus(status: string): string {
    return status
      .split('-')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  protected formatRelativeDate(value: string | null | undefined): string {
    if (!value) {
      return 'Unknown date';
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return 'Unknown date';
    }

    return new Intl.DateTimeFormat('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    }).format(date);
  }

  protected formatTimeAgo(value: string | null | undefined): string {
    if (!value) {
      return 'Recently';
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return 'Recently';
    }

    const diffInSeconds = Math.floor((Date.now() - date.getTime()) / 1000);

    if (diffInSeconds < 60) {
      return 'Just now';
    }

    const diffInMinutes = Math.floor(diffInSeconds / 60);
    if (diffInMinutes < 60) {
      return `${diffInMinutes}m ago`;
    }

    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) {
      return `${diffInHours}h ago`;
    }

    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 7) {
      return `${diffInDays}d ago`;
    }

    return this.formatRelativeDate(value);
  }

  protected clientUpdatedAtLabel(client: ClientDetailResponse['client'] | null): string {
    return this.formatTimeAgo(client?.updatedAt);
  }

  protected clientNotes(client: ClientDetailResponse['client'] | null): string {
    return client?.notes ?? '';
  }

  protected toTimelineItem(activity: ClientActivityResponse): TimelineItem {
    const kind = activity.activityType.toLowerCase();

    if (kind.includes('invoice') || kind.includes('payment')) {
      return {
        icon: 'description',
        tone: 'secondary',
        title: activity.title,
        description: activity.description ?? 'Financial milestone logged.',
        meta: this.formatTimeAgo(activity.occurredAt),
      };
    }

    if (kind.includes('call') || kind.includes('meeting') || kind.includes('note')) {
      return {
        icon: 'call',
        tone: 'accent',
        title: activity.title,
        description: activity.description ?? 'Client interaction recorded.',
        meta: this.formatTimeAgo(activity.occurredAt),
      };
    }

    return {
      icon: 'calendar_month',
      tone: 'primary',
      title: activity.title,
      description: activity.description ?? 'Timeline entry recorded.',
      meta: this.formatTimeAgo(activity.occurredAt),
    };
  }

  protected trackByContact(_index: number, contact: ClientContactResponse): string {
    return contact.id;
  }

  protected trackByActivity(_index: number, activity: ClientActivityResponse): string {
    return activity.id;
  }
}
