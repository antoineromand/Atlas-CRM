import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AuthStateService {
  private readonly storageKey = 'atlas.access_token';
  private readonly accessTokenSignal = signal<string | null>(this.readFromStorage());

  readonly accessToken = this.accessTokenSignal.asReadonly();

  setAccessToken(accessToken: string): void {
    const normalizedToken = this.normalizeToken(accessToken);

    if (!normalizedToken) {
      this.clear();
      return;
    }

    this.accessTokenSignal.set(normalizedToken);
    this.safeSetItem(normalizedToken);
  }

  clear(): void {
    this.accessTokenSignal.set(null);
    this.safeRemoveItem();
  }

  getAccessToken(): string | null {
    const token = this.accessTokenSignal();

    if (!token) {
      return null;
    }

    if (this.isExpired(token)) {
      this.clear();
      return null;
    }

    return token;
  }

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  private readFromStorage(): string | null {
    try {
      const token = localStorage.getItem(this.storageKey);

      if (!token || this.isExpired(token)) {
        return null;
      }

      return token;
    } catch {
      return null;
    }
  }

  private normalizeToken(token: string): string | null {
    return token.trim().length > 0 ? token : null;
  }

  private isExpired(token: string): boolean {
    const parts = token.split('.');

    if (parts.length !== 3) {
      return false;
    }

    try {
      const payload = JSON.parse(atob(this.base64UrlToBase64(parts[1])));
      const expiresAt = typeof payload?.exp === 'number' ? payload.exp * 1000 : null;

      return expiresAt !== null ? Date.now() >= expiresAt : false;
    } catch {
      return false;
    }
  }

  private safeSetItem(value: string): void {
    try {
      localStorage.setItem(this.storageKey, value);
    } catch {
      // Ignore storage failures in constrained environments.
    }
  }

  private safeRemoveItem(): void {
    try {
      localStorage.removeItem(this.storageKey);
    } catch {
      // Ignore storage failures in constrained environments.
    }
  }

  private base64UrlToBase64(value: string): string {
    const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
    const padding = base64.length % 4;

    return padding === 0 ? base64 : `${base64}${'='.repeat(4 - padding)}`;
  }
}
