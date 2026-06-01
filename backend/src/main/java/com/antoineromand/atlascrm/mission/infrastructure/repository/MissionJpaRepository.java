package com.antoineromand.atlascrm.mission.infrastructure.repository;

import com.antoineromand.atlascrm.mission.infrastructure.model.MissionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionJpaRepository extends JpaRepository<MissionEntity, UUID> {
  Optional<MissionEntity> findByIdAndAccount_Id(UUID missionId, UUID accountId);

  List<MissionEntity> findAllByAccount_IdOrderByCreatedAtDesc(UUID accountId);
}
