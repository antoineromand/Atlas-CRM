package com.antoineromand.atlascrm.mission.infrastructure.repository;

import com.antoineromand.atlascrm.mission.infrastructure.model.MissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MissionJpaRepository extends JpaRepository<MissionEntity, UUID> {
  Optional<MissionEntity> findByIdAndAccount_Id(UUID missionId, UUID accountId);

  List<MissionEntity> findAllByAccount_IdOrderByCreatedAtDesc(UUID accountId);

  List<MissionEntity> findAllByClientActivity_IdOrderByCreatedAtDesc(UUID clientId);

  Page<MissionEntity> findAllByAccount_Id(UUID accountId, Pageable pageable);

  @Query("""
      select m
      from MissionEntity m
      where m.account.id = :accountId
        and (
          :search is null
          or :search = ''
          or lower(m.title) like lower(concat('%', :search, '%'))
          or lower(coalesce(m.roleInProject, '')) like lower(concat('%', :search, '%'))
        )
      order by m.createdAt desc
      """)
  List<MissionEntity> findAllByAccountIdAndSearch(UUID accountId, String search);

  @Query("""
      select m
      from MissionEntity m
      where m.account.id = :accountId
        and (
          :search is null
          or :search = ''
          or lower(m.title) like lower(concat('%', :search, '%'))
          or lower(coalesce(m.roleInProject, '')) like lower(concat('%', :search, '%'))
        )
      order by m.createdAt desc
      """)
  Page<MissionEntity> findAllByAccountIdAndSearch(UUID accountId, String search, Pageable pageable);
}
