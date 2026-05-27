import { Injectable, signal } from '@angular/core';
import { ToastInput, ToastNotification, ToastVariant } from '../../interface/notification.interface';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private readonly notificationsSignal = signal<ToastNotification[]>([]);
  readonly notifications = this.notificationsSignal.asReadonly();

  private nextId = 0;
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();

  show(input: ToastInput): number {
    const id = ++this.nextId;
    const notification: ToastNotification = {
      id,
      title: input.title ?? this.defaultTitle(input.variant ?? 'info'),
      message: input.message,
      variant: input.variant ?? 'info',
      duration: input.duration ?? 4500,
    };

    this.notificationsSignal.update((items) => [notification, ...items]);

    if (notification.duration > 0) {
      const timer = setTimeout(() => this.dismiss(id), notification.duration);
      this.timers.set(id, timer);
    }

    return id;
  }

  success(message: string, title = 'Success'): number {
    return this.show({ message, title, variant: 'success' });
  }

  error(message: string, title = 'Something went wrong'): number {
    return this.show({ message, title, variant: 'error', duration: 6000 });
  }

  info(message: string, title = 'Notice'): number {
    return this.show({ message, title, variant: 'info' });
  }

  dismiss(id: number): void {
    const timer = this.timers.get(id);

    if (timer) {
      clearTimeout(timer);
      this.timers.delete(id);
    }

    this.notificationsSignal.update((items) => items.filter((item) => item.id !== id));
  }

  clear(): void {
    this.timers.forEach((timer) => clearTimeout(timer));
    this.timers.clear();
    this.notificationsSignal.set([]);
  }

  private defaultTitle(variant: ToastVariant): string {
    switch (variant) {
      case 'success':
        return 'Success';
      case 'error':
        return 'Error';
      default:
        return 'Notice';
    }
  }
}
