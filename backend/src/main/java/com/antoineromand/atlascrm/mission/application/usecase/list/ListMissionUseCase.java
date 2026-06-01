package com.antoineromand.atlascrm.mission.application.usecase.list;

import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ListMissionUseCase implements IListMissionUseCase {

  private final IMissionRepository missionRepository;

  public ListMissionUseCase(IMissionRepository missionRepository) {
    this.missionRepository = missionRepository;
  }

  @Override
  public List<Mission> execute(UUID accountId) {
    return this.missionRepository.findAllByAccountId(accountId);
  }

  @Override
  public List<Mission> execute(UUID accountId, String search) {
    String normalizedSearch = search == null ? null : search.trim();

    if (normalizedSearch == null || normalizedSearch.isBlank()) {
      return this.missionRepository.findAllByAccountId(accountId);
    }

    return this.missionRepository.findAllByAccountIdAndSearch(accountId, normalizedSearch);
  }
}
