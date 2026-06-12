import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateClientActivityPayload,
  CreateClientContactPayload,
  CreateClientPayload,
  CreateClientResponse,
  ClientDetailResponse,
  ClientContactResponse,
  ClientPageResponse,
  ClientStatus,
  UpdateClientActivityPayload,
  UpdateClientContactPayload,
  UpdateClientPayload,
} from '../../interface/client.interface';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ClientService {
  private readonly baseUrl = `${environment.apiBaseUrl}/v1/clients`;
  private readonly httpClient = inject(HttpClient);

  listMyClients(
    search: string | null | undefined,
    status: ClientStatus | null | undefined,
    page = 1,
    size = 8,
  ): Observable<ClientPageResponse> {
    const normalizedSearch = search?.trim() ?? '';

    return this.httpClient.get<ClientPageResponse>(this.baseUrl, {
      withCredentials: true,
      params: normalizedSearch || status
        ? {
            ...(normalizedSearch ? { search: normalizedSearch } : {}),
            ...(status ? { status } : {}),
            page,
            size,
          }
        : { page, size },
    });
  }

  getClientById(clientId: string): Observable<ClientDetailResponse> {
    return this.httpClient.get<ClientDetailResponse>(`${this.baseUrl}/${clientId}`, {
      withCredentials: true,
    });
  }

  createClient(payload: CreateClientPayload): Observable<CreateClientResponse> {
    return this.httpClient.post<CreateClientResponse>(this.baseUrl, payload, {
      withCredentials: true,
    });
  }

  updateClient(clientId: string, payload: UpdateClientPayload): Observable<ClientDetailResponse['client']> {
    return this.httpClient.patch<ClientDetailResponse['client']>(`${this.baseUrl}/${clientId}`, payload, {
      withCredentials: true,
    });
  }

  createClientContact(clientId: string, payload: CreateClientContactPayload): Observable<ClientContactResponse> {
    return this.httpClient.post<ClientContactResponse>(`${this.baseUrl}/${clientId}/contacts`, payload, {
      withCredentials: true,
    });
  }

  createClientActivity(
    clientId: string,
    payload: CreateClientActivityPayload,
  ): Observable<ClientDetailResponse['activities'][number]> {
    return this.httpClient.post<ClientDetailResponse['activities'][number]>(
      `${this.baseUrl}/${clientId}/activities`,
      payload,
      {
        withCredentials: true,
      },
    );
  }

  updateClientContact(
    clientId: string,
    contactId: string,
    payload: UpdateClientContactPayload,
  ): Observable<ClientContactResponse> {
    return this.httpClient.patch<ClientContactResponse>(`${this.baseUrl}/${clientId}/contacts/${contactId}`, payload, {
      withCredentials: true,
    });
  }

  updateClientActivity(
    clientId: string,
    activityId: string,
    payload: UpdateClientActivityPayload,
  ): Observable<ClientDetailResponse['activities'][number]> {
    return this.httpClient.patch<ClientDetailResponse['activities'][number]>(
      `${this.baseUrl}/${clientId}/activities/${activityId}`,
      payload,
      {
        withCredentials: true,
      },
    );
  }

  deleteClientContact(clientId: string, contactId: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.baseUrl}/${clientId}/contacts/${contactId}`, {
      withCredentials: true,
    });
  }

  deleteClientActivity(clientId: string, activityId: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.baseUrl}/${clientId}/activities/${activityId}`, {
      withCredentials: true,
    });
  }

  deleteClient(clientId: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.baseUrl}/${clientId}`, {
      withCredentials: true,
    });
  }
}
