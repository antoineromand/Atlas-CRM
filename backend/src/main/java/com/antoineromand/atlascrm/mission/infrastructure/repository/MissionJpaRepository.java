package com.antoineromand.atlascrm.mission.infrastructure.repository;

import com.antoineromand.atlascrm.mission.infrastructure.model.MissionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MissionJpaRepository extends JpaRepository<MissionEntity, UUID> {
  Optional<MissionEntity> findByIdAndAccount_Id(UUID missionId, UUID accountId);

  List<MissionEntity> findAllByAccount_IdOrderByCreatedAtDesc(UUID accountId);

  @Query("""
      select m
      from MissionEntity m
      where m.account.id = :accountId
        and (
          :search is null
          or :search = ''
          or lower(m.title) like lower(concat('%', :search, '%'))
          or lower(coalesce(m.roleInProject, '')) like lower(concat('%', :search, '%'))
          or lower(coalesce(m.description, '')) like lower(concat('%', :search, '%'))
          or lower(m.status) like lower(concat('%', :search, '%'))
          or lower(m.priority) like lower(concat('%', :search, '%'))
        )
      order by m.createdAt desc
      """)
  List<MissionEntity> findAllByAccountIdAndSearch(UUID accountId, String search);
}
