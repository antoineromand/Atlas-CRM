import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateMissionPayload,
  CreateMissionResponse,
  MissionResponse,
  UpdateMissionPayload,
} from '../../interface/mission.interface';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class MissionService {
  private readonly baseUrl = `${environment.apiBaseUrl}/v1/missions`;
  private readonly httpClient = inject(HttpClient);

  listMyMissions(): Observable<MissionResponse[]> {
    return this.httpClient.get<MissionResponse[]>(this.baseUrl, {
      withCredentials: true,
    });
  }

  searchMyMissions(search: string | null | undefined): Observable<MissionResponse[]> {
    const normalizedSearch = search?.trim() ?? '';

    return this.httpClient.get<MissionResponse[]>(this.baseUrl, {
      withCredentials: true,
      params: normalizedSearch ? { search: normalizedSearch } : undefined,
    });
  }

  getMissionById(missionId: string): Observable<MissionResponse> {
    return this.httpClient.get<MissionResponse>(`${this.baseUrl}/${missionId}`, {
      withCredentials: true,
    });
  }

  createMission(payload: CreateMissionPayload): Observable<CreateMissionResponse> {
    return this.httpClient.post<CreateMissionResponse>(this.baseUrl, payload, {
      withCredentials: true,
    });
  }

  updateMission(missionId: string, payload: UpdateMissionPayload): Observable<MissionResponse> {
    return this.httpClient.patch<MissionResponse>(`${this.baseUrl}/${missionId}`, payload, {
      withCredentials: true,
    });
  }

  deleteMission(missionId: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.baseUrl}/${missionId}`, {
      withCredentials: true,
    });
  }
}
