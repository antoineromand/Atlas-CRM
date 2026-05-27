import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AuthStateService {
  private readonly accessTokenSignal = signal<string | null>(null);

  readonly accessToken = this.accessTokenSignal.asReadonly();

  setAccessToken(accessToken: string): void {
    this.accessTokenSignal.set(accessToken);
  }

  clear(): void {
    this.accessTokenSignal.set(null);
  }

  getAccessToken(): string | null {
    return this.accessTokenSignal();
  }

  isAuthenticated(): boolean {
    return !!this.accessTokenSignal();
  }
}
