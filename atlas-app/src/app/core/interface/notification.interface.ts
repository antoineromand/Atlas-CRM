export type ToastVariant = 'success' | 'error' | 'info';

export interface ToastNotification {
  id: number;
  title: string;
  message: string;
  variant: ToastVariant;
  duration: number;
}

export interface ToastInput {
  title?: string;
  message: string;
  variant?: ToastVariant;
  duration?: number;
}
