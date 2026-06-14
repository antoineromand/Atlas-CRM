import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ClientEditorDrawerComponent } from '../../components/client-editor-drawer/client-editor-drawer.component';
import { ClientContactEditorDrawerComponent } from '../../components/client-contact-editor-drawer/client-contact-editor-drawer.component';
import { ClientActivityEditorDrawerComponent } from '../../components/client-activity-editor-drawer/client-activity-editor-drawer.component';
import { ClientEditorFacade } from '../../services/client-editor.facade';
import { ClientContactEditorFacade } from '../../services/client-contact-editor.facade';
import { ClientActivityEditorFacade } from '../../services/client-activity-editor.facade';
import { ClientService } from '../../../../core/services/client/client.service';
import {
  ClientActivityResponse,
  ClientContactResponse,
  ClientDetailResponse,
} from '../../../../core/interface/client.interface';
import { MissionResponse } from '../../../../core/interface/mission.interface';
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

interface MissionCard {
  id: string;
  title: string;
  statusLabel: string;
  priorityLabel: string;
  meta: string;
}

@Component({
  selector: 'app-client-detail-page-component',
  standalone: true,
  imports: [ClientEditorDrawerComponent, ClientContactEditorDrawerComponent, ClientActivityEditorDrawerComponent],
  templateUrl: './client-detail-page-component.html',
  styleUrl: './client-detail-page-component.scss',
  providers: [ClientEditorFacade, ClientContactEditorFacade, ClientActivityEditorFacade],
})
export class ClientDetailPageComponent implements OnInit {
  private readonly clientService = inject(ClientService);
  private readonly notificationService = inject(NotificationService);
  private readonly clientEditor = inject(ClientEditorFacade);
  private readonly clientContactEditor = inject(ClientContactEditorFacade);
  private readonly clientActivityEditor = inject(ClientActivityEditorFacade);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly clientDetail = signal<ClientDetailResponse | null>(null);
  protected readonly isLoading = signal(true);
  protected readonly loadError = signal<string | null>(null);
  protected readonly contactDeleteTarget = signal<ClientContactResponse | null>(null);
  protected readonly isDeletingContact = signal(false);
  protected readonly activityDeleteTarget = signal<ClientActivityResponse | null>(null);
  protected readonly isDeletingActivity = signal(false);
  protected readonly activityModalOpen = signal(false);
  protected readonly activityPage = signal(1);
  protected readonly activityPageSize = 5;
  private currentClientId: string | null = null;

  protected readonly client = computed(() => this.clientDetail()?.client ?? null);
  protected readonly contacts = computed(() => this.clientDetail()?.contacts ?? []);
  protected readonly activities = computed(() => this.clientDetail()?.activities ?? []);
  protected readonly tags = computed(() => this.clientDetail()?.tags ?? []);
  protected readonly missions = computed(() => this.clientDetail()?.missions ?? []);

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

  protected readonly visibleTimelineEntries = computed(() =>
    this.timelineEntries().slice(0, 4),
  );

  protected readonly activityPageCount = computed(() => {
    const total = this.timelineEntries().length;
    return Math.max(1, Math.ceil(total / this.activityPageSize));
  });

  protected readonly activityModalEntries = computed(() => {
    const entries = this.timelineEntries();
    const pageCount = this.activityPageCount();
    const currentPage = Math.min(this.activityPage(), pageCount);
    const startIndex = (currentPage - 1) * this.activityPageSize;
    return entries.slice(startIndex, startIndex + this.activityPageSize);
  });

  protected readonly visibleContacts = computed(() =>
    this.contacts().slice(0, 3).map((contact) => ({
      ...contact,
      displayName: this.contactName(contact),
      role: this.contactRole(contact),
      initials: this.contactInitials(contact),
    })),
  );

  protected readonly visibleMissions = computed<MissionCard[]>(() =>
    this.missions().slice(0, 4).map((mission) => this.toMissionCard(mission)),
  );

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const clientId = params.get('clientId');

      if (!clientId) {
        this.currentClientId = null;
        this.loadError.set('Missing client identifier.');
        this.isLoading.set(false);
        this.clientDetail.set(null);
        return;
      }

      this.currentClientId = clientId;
      this.loadClient(clientId);
    });

    this.clientEditor.mutationCompleted.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((event) => {
      if (event.kind !== 'saved' || event.clientId !== this.currentClientId) {
        return;
      }

      this.loadClient(event.clientId);
    });

    this.clientContactEditor.mutationCompleted.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((event) => {
      if (event.clientId !== this.currentClientId) {
        return;
      }

      this.loadClient(event.clientId);
    });

    this.clientActivityEditor.mutationCompleted.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((event) => {
      if (event.clientId !== this.currentClientId) {
        return;
      }

      this.loadClient(event.clientId);
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

    this.clientEditor.openEditDrawer(client.id, false);
  }

  protected openCreateContactDrawer(): void {
    const client = this.client();
    if (!client) {
      return;
    }

    this.clientContactEditor.openCreateDrawer(client.id, !this.primaryContact());
  }

  protected openEditContactDrawer(contact: ClientContactResponse): void {
    const client = this.client();
    if (!client) {
      return;
    }

    this.clientContactEditor.openEditDrawer(client.id, contact);
  }

  protected openCreateActivityDrawer(): void {
    const client = this.client();
    if (!client) {
      return;
    }

    this.closeActivityModal();
    this.clientActivityEditor.openCreateDrawer(client.id);
  }

  protected openEditActivityDrawer(activity: ClientActivityResponse): void {
    const client = this.client();
    if (!client) {
      return;
    }

    this.closeActivityModal();
    this.clientActivityEditor.openEditDrawer(client.id, activity);
  }

  protected requestDeleteContact(contact: ClientContactResponse): void {
    this.contactDeleteTarget.set(contact);
  }

  protected cancelDeleteContact(): void {
    this.contactDeleteTarget.set(null);
  }

  protected confirmDeleteContact(): void {
    const client = this.client();
    const contact = this.contactDeleteTarget();

    if (!client || !contact) {
      return;
    }

    this.isDeletingContact.set(true);
    this.clientService
      .deleteClientContact(client.id, contact.id)
      .pipe(
        finalize(() => {
          this.isDeletingContact.set(false);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.notificationService.success('Contact deleted.');
          this.contactDeleteTarget.set(null);
          this.clientContactEditor.closeDrawer();
          this.loadClient(client.id);
        },
        error: (error: unknown) => {
          const message = this.extractErrorMessage(error, 'Unable to delete contact.');
          this.notificationService.error(message, 'Contacts unavailable');
      },
    });
  }

  protected requestDeleteActivity(activity: ClientActivityResponse): void {
    this.closeActivityModal();
    this.activityDeleteTarget.set(activity);
  }

  protected cancelDeleteActivity(): void {
    this.activityDeleteTarget.set(null);
  }

  protected confirmDeleteActivity(): void {
    const client = this.client();
    const activity = this.activityDeleteTarget();

    if (!client || !activity) {
      return;
    }

    this.isDeletingActivity.set(true);
    this.clientService
      .deleteClientActivity(client.id, activity.id)
      .pipe(
        finalize(() => {
          this.isDeletingActivity.set(false);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.notificationService.success('Activity deleted.');
          this.activityDeleteTarget.set(null);
          this.clientActivityEditor.closeDrawer();
          this.loadClient(client.id);
        },
        error: (error: unknown) => {
          const message = this.extractErrorMessage(error, 'Unable to delete activity.');
          this.notificationService.error(message, 'Activities unavailable');
        },
      });
  }

  protected openActivityModal(): void {
    this.activityPage.set(1);
    this.activityModalOpen.set(true);
  }

  protected closeActivityModal(): void {
    this.activityModalOpen.set(false);
  }

  protected previousActivityPage(): void {
    this.activityPage.update((value) => Math.max(1, value - 1));
  }

  protected nextActivityPage(): void {
    this.activityPage.update((value) => Math.min(this.activityPageCount(), value + 1));
  }

  protected reloadClient(): void {
    if (!this.currentClientId) {
      return;
    }

    this.loadClient(this.currentClientId);
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
        error: (error: unknown) => {
          this.clientDetail.set(null);
          const message = this.extractErrorMessage(error, 'Unable to load client details.');
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

  private extractErrorMessage(error: unknown, fallback: string): string {
    const response = error as { error?: { message?: string } } | null | undefined;
    return response?.error?.message ?? fallback;
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

    if (kind.includes('task') || kind.includes('follow') || kind.includes('status')) {
      return {
        icon: 'check_circle',
        tone: 'secondary',
        title: activity.title,
        description: activity.description ?? 'Operational update recorded.',
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

  protected formatActivityType(activityType: string): string {
    return activityType
      .split(/[_-]/)
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  protected trackByContact(_index: number, contact: ClientContactResponse): string {
    return contact.id;
  }

  protected trackByActivity(_index: number, activity: ClientActivityResponse): string {
    return activity.id;
  }

  protected trackByMission(_index: number, mission: MissionCard): string {
    return mission.id;
  }

  private toMissionCard(mission: MissionResponse): MissionCard {
    return {
      id: mission.id,
      title: mission.title,
      statusLabel: this.formatMissionStatus(mission.status),
      priorityLabel: this.formatMissionPriority(mission.priority),
      meta: this.formatMissionDate(mission.deadline ?? mission.startDate),
    };
  }

  private formatMissionStatus(status: string): string {
    return status
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  private formatMissionPriority(priority: string): string {
    return priority.charAt(0).toUpperCase() + priority.slice(1);
  }

  private formatMissionDate(value: string | null | undefined): string {
    return this.formatRelativeDate(value);
  }
}
