import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {
  LoginCommand,
  MessageResponse,
  RegisterCommand,
  RegisterResponse,
  TokenPair,
} from '../../interface/auth.interface';
import {HttpClient} from '@angular/common/http';


@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly accessTokenKey = 'atlas_access_token';

  // TODO: replace baseUrl with environment variable
  private baseUrl: string = 'http://localhost:3000/api';
  private authEndpoint: string = 'v1/authentication';
  private httpClient = inject(HttpClient);

  public register(command: RegisterCommand): Observable<RegisterResponse> {
    return this.httpClient.post<RegisterResponse>(
      `${this.baseUrl}/${this.authEndpoint}/register`,
      command,
      { withCredentials: true }
    );
  }

  public login(command: LoginCommand): Observable<TokenPair> {
    return this.httpClient.post<TokenPair>(
      `${this.baseUrl}/${this.authEndpoint}/sign-in`,
      command,
      { withCredentials: true }
    );
  }

  public refreshToken(): Observable<TokenPair> {
    return this.httpClient.post<TokenPair>(
      `${this.baseUrl}/${this.authEndpoint}/refresh-token`,
      {},
      { withCredentials: true }
    );
  }

  public logout(): Observable<MessageResponse> {
    return this.httpClient.post<MessageResponse>(
      `${this.baseUrl}/${this.authEndpoint}/sign-out`,
      {},
      { withCredentials: true }
    );
  }

  public setSession(tokens: TokenPair): void {
    this.setAccessToken(tokens.accessToken);
  }

  public setAccessToken(accessToken: string): void {
    if (this.isBrowser()) {
      localStorage.setItem(this.accessTokenKey, accessToken);
    }
  }

  public getAccessToken(): string | null {
    if (!this.isBrowser()) {
      return null;
    }

    return localStorage.getItem(this.accessTokenKey);
  }

  public clearSession(): void {
    if (this.isBrowser()) {
      localStorage.removeItem(this.accessTokenKey);
    }
  }

  public isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  private isBrowser(): boolean {
    return typeof window !== 'undefined' && !!window.localStorage;
  }
}
