export type MissionStatus =
  | 'created'
  | 'analysed'
  | 'planned'
  | 'started'
  | 'in_progress'
  | 'finalized'
  | 'shipped'
  | 'completed';

export type MissionPriority = 'low' | 'medium' | 'high';

export interface MissionResponse {
  id: string;
  title: string;
  roleInProject: string | null;
  description: string | null;
  status: MissionStatus;
  progress: number;
  priority: MissionPriority;
  startDate: string;
  deadline: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface MissionPageResponse {
  items: MissionResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface MissionSummaryResponse {
  totalMissions: number;
  activeMissions: number;
  completedMissions: number;
  dueSoonMissions: number;
  highPriorityMissions: number;
}

export interface CreateMissionPayload {
  title: string;
  roleInProject: string | null;
  description: string | null;
  status: MissionStatus;
  priority: MissionPriority;
  startDate: string;
  deadline: string | null;
}

export interface UpdateMissionPayload {
  title?: string | null;
  roleInProject?: string | null;
  description?: string | null;
  status?: MissionStatus | null;
  priority?: MissionPriority | null;
  startDate?: string | null;
  deadline?: string | null;
}

export interface CreateMissionResponse {
  missionId: string;
  message: string;
}
