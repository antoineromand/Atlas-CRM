import { Component, Input } from '@angular/core';

export interface RecentActivityItem {
  title: string;
  subject: string;
  meta: string;
}

@Component({
  selector: 'app-recent-activity-card',
  standalone: true,
  templateUrl: './recent-activity-card.component.html',
  styleUrl: './recent-activity-card.component.scss',
})
export class RecentActivityCardComponent {
  @Input() title = 'Recent Activity';
  @Input() viewAllLabel = 'View All';
  @Input() viewAllHref = '#';
  @Input() items: readonly RecentActivityItem[] = [];
}
