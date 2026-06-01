package com.antoineromand.atlascrm.mission.domain.repository;

import com.antoineromand.atlascrm.mission.domain.Mission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IMissionRepository {
  UUID save(Mission mission);

  Optional<Mission> findById(UUID missionId);

  Optional<Mission> findByIdAndAccountId(UUID missionId, UUID accountId);

  List<Mission> findAllByAccountId(UUID accountId);

  List<Mission> findAllByAccountIdAndSearch(UUID accountId, String search);

  Page<Mission> findAllByAccountId(UUID accountId, Pageable pageable);

  Page<Mission> findAllByAccountIdAndSearch(UUID accountId, String search, Pageable pageable);

  void deleteById(UUID missionId);
}
