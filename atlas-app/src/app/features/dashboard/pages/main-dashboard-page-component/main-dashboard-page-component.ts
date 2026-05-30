import { Component } from '@angular/core';
import { StatCardComponent } from '../../../../shared/ui/stat-card/stat-card.component';
import { IncomeReportCardComponent } from '../../../../shared/ui/income-report-card/income-report-card.component';
import {
  RecentActivityCardComponent,
  type RecentActivityItem,
} from '../../../../shared/ui/recent-activity-card/recent-activity-card.component';

interface DashboardStatCard {
  icon: string;
  badge: string;
  label: string;
  value: string;
  footer: string;
  tone: 'primary' | 'secondary' | 'accent' | 'danger';
}

@Component({
  selector: 'app-main-dashboard-page-component',
  standalone: true,
  imports: [IncomeReportCardComponent, RecentActivityCardComponent, StatCardComponent],
  templateUrl: './main-dashboard-page-component.html',
  styleUrl: './main-dashboard-page-component.scss',
})
export class MainDashboardPageComponent {
  protected readonly stats: readonly DashboardStatCard[] = [
    {
      icon: 'payments',
      badge: '+12%',
      label: 'Total revenue',
      value: '$48,250.00',
      footer: 'vs last month',
      tone: 'primary',
    },
    {
      icon: 'rocket_launch',
      badge: '',
      label: 'Active missions',
      value: '12',
      footer: '3 finishing this week',
      tone: 'accent',
    },
    {
      icon: 'pending_actions',
      badge: 'Urgent',
      label: 'Pending invoices',
      value: '8',
      footer: 'Totaling $5,420.00',
      tone: 'danger',
    },
  ];

  protected readonly recentActivities: readonly RecentActivityItem[] = [
    {
      title: 'Mission',
      subject: 'Project Nebula completed',
      meta: '2 hours ago',
    },
    {
      title: 'Added new client',
      subject: 'Solaris Inc.',
      meta: '5 hours ago',
    },
    {
      title: 'Invoice #829 sent to',
      subject: 'Alpha Design',
      meta: 'Yesterday',
    },
  ];

}
