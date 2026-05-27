import { Component, inject } from '@angular/core';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { ToastNotification } from '../../../core/interface/notification.interface';

@Component({
  selector: 'app-toast',
  standalone: true,
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.scss',
})
export class ToastComponent {
  private readonly notificationService = inject(NotificationService);

  protected readonly notifications = this.notificationService.notifications;

  protected close(notification: ToastNotification): void {
    this.notificationService.dismiss(notification.id);
  }

  protected iconFor(variant: ToastNotification['variant']): string {
    switch (variant) {
      case 'success':
        return 'check_circle';
      case 'error':
        return 'error';
      default:
        return 'info';
    }
  }
}
