import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {RegisterCommand, RegisterResponse} from '../../interface/auth.interface';
import {HttpClient} from '@angular/common/http';


@Injectable()
export class AuthService {

  // TODO: replace baseUrl with environment variable
  private baseUrl: string = 'http://localhost:3000/api';
  private authEndpoint: string = 'v1/authentication';
  private httpClient = inject(HttpClient);

  public register(command: RegisterCommand): Observable<RegisterResponse>{
    return this.httpClient.post<RegisterResponse>(`${this.baseUrl}/${this.authEndpoint}/register`, command);
  }
}
