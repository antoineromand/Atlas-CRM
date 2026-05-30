import { Component } from '@angular/core';
import { StatCardComponent } from '../../../../shared/ui/stat-card/stat-card.component';

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
  imports: [StatCardComponent],
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

}
