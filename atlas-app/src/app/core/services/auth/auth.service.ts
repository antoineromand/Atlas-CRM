import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  LoginCommand,
  MessageResponse,
  RegisterCommand,
  RegisterResponse,
  TokenPair,
} from '../../interface/auth.interface';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly baseUrl = `${environment.apiBaseUrl}/v1/authentication`;
  private readonly httpClient = inject(HttpClient);

  public register(command: RegisterCommand): Observable<RegisterResponse> {
    return this.httpClient.post<RegisterResponse>(`${this.baseUrl}/register`, command, {
      withCredentials: true,
    });
  }

  public login(command: LoginCommand): Observable<TokenPair> {
    return this.httpClient.post<TokenPair>(`${this.baseUrl}/sign-in`, command, {
      withCredentials: true,
    });
  }

  public refreshToken(): Observable<TokenPair> {
    return this.httpClient.post<TokenPair>(`${this.baseUrl}/refresh-token`, {}, {
      withCredentials: true,
    });
  }

  public logout(): Observable<MessageResponse> {
    return this.httpClient.post<MessageResponse>(`${this.baseUrl}/sign-out`, {}, {
      withCredentials: true,
    });
  }
}
