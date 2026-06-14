package com.antoineromand.atlascrm.mission.application.usecase.summary;

import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.MissionStatus;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetMissionSummaryUseCase implements IGetMissionSummaryUseCase {

  private final IMissionRepository missionRepository;

  public GetMissionSummaryUseCase(IMissionRepository missionRepository) {
    this.missionRepository = missionRepository;
  }

  @Override
  public MissionSummaryResult execute(UUID accountId) {
    List<Mission> missions = this.missionRepository.findAllByAccountId(accountId);
    long total = missions.size();
    long completed = missions.stream().filter(mission -> mission.getMissionStatus().isCompleted()).count();
    long active = total - completed;
    long dueSoon = this.countDueSoon(missions);
    long highPriority = missions.stream().filter(mission -> "high".equals(mission.getPriority())).count();

    return new MissionSummaryResult(total, active, completed, dueSoon, highPriority);
  }

  private long countDueSoon(List<Mission> missions) {
    LocalDate today = LocalDate.now();

    return missions.stream()
        .filter(mission -> mission.getMissionStatus() != MissionStatus.COMPLETED)
        .filter(mission -> mission.getDeadline() != null)
        .filter(
            mission -> {
              LocalDate deadline = mission.getDeadline();
              return !deadline.isBefore(today) && !deadline.isAfter(today.plusDays(7));
            })
        .count();
  }
}
