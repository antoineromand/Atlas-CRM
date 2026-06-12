export type ClientStatus = 'prospect' | 'active' | 'inactive' | 'archived';

export interface ClientResponse {
  id: string;
  companyName: string;
  primaryContactFirstName: string | null;
  primaryContactLastName: string | null;
  status: ClientStatus;
  notes: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface ClientPageResponse {
  items: ClientResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface ClientContactResponse {
  id: string;
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  phone: string | null;
  jobTitle: string | null;
  primary: boolean;
  createdAt: string;
  updatedAt: string | null;
}

export interface ClientActivityResponse {
  id: string;
  activityType: string;
  title: string;
  description: string | null;
  occurredAt: string;
  createdAt: string;
  updatedAt: string | null;
}

export interface ClientTagResponse {
  id: string;
  name: string;
  color: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface ClientDetailResponse {
  client: ClientResponse;
  contacts: ClientContactResponse[];
  activities: ClientActivityResponse[];
  tags: ClientTagResponse[];
  missions: import('./mission.interface').MissionResponse[];
}

export interface CreateClientPayload {
  companyName: string;
  status: ClientStatus;
  notes: string | null;
}

export interface UpdateClientPayload {
  companyName?: string | null;
  status?: ClientStatus | null;
  notes?: string | null;
}

export interface CreateClientContactPayload {
  firstName: string;
  lastName: string;
  email: string | null;
  phone: string | null;
  jobTitle: string | null;
  primary: boolean;
}

export interface UpdateClientContactPayload {
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  phone: string | null;
  jobTitle: string | null;
  primary: boolean;
}

export interface CreateClientActivityPayload {
  activityType: string;
  title: string;
  description: string | null;
  occurredAt: string;
}

export interface UpdateClientActivityPayload {
  activityType: string;
  title: string;
  description: string | null;
  occurredAt: string;
}

export interface CreateClientResponse {
  clientId: string;
  message: string;
}
