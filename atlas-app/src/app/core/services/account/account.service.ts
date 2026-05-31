import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountResponse, UpdateAccountPayload } from '../../interface/account.interface';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private readonly baseUrl = `${environment.apiBaseUrl}/v1/account`;
  private readonly httpClient = inject(HttpClient);

  getMyAccount(): Observable<AccountResponse> {
    return this.httpClient.get<AccountResponse>(`${this.baseUrl}/me`, {
      withCredentials: true,
    });
  }

  updateMyAccount(payload: UpdateAccountPayload): Observable<AccountResponse> {
    return this.httpClient.patch<AccountResponse>(`${this.baseUrl}/me`, payload, {
      withCredentials: true,
    });
  }
}
