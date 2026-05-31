import { Component, Input } from '@angular/core';
import { AccountResponse } from '../../../../core/interface/account.interface';

@Component({
  selector: 'app-settings-summary-card',
  standalone: true,
  templateUrl: './settings-summary-card-component.html',
  styleUrl: './settings-summary-card-component.scss',
})
export class SettingsSummaryCardComponent {
  @Input() account: AccountResponse | null = null;
  @Input() loading = false;

  protected formatDate(value: string | null | undefined): string {
    if (!value) {
      return 'Not available';
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return 'Not available';
    }

    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(date);
  }

  protected getInitials(): string {
    const firstName = this.account?.firstName?.trim() ?? '';
    const lastName = this.account?.lastName?.trim() ?? '';
    const letters = `${firstName.charAt(0)}${lastName.charAt(0)}`.trim();

    return letters || 'A';
  }
}
