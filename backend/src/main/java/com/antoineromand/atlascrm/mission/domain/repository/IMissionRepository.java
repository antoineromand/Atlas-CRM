package com.antoineromand.atlascrm.mission.domain.repository;

import com.antoineromand.atlascrm.mission.domain.Mission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IMissionRepository {
  UUID save(Mission mission);

  Optional<Mission> findById(UUID missionId);

  List<Mission> findAllByAccountId(UUID accountId);

  void deleteById(UUID missionId);
}
