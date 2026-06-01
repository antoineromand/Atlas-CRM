package com.antoineromand.atlascrm.mission.infrastructure.repository;

import com.antoineromand.atlascrm.account.infrastructure.model.AccountEntity;
import com.antoineromand.atlascrm.account.infrastructure.repository.AccountJpaRepository;
import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import com.antoineromand.atlascrm.mission.infrastructure.model.MissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class MissionRepositoryImpl implements IMissionRepository {

  private final MissionJpaRepository missionJpaRepository;
  private final AccountJpaRepository accountJpaRepository;

  public MissionRepositoryImpl(
      MissionJpaRepository missionJpaRepository, AccountJpaRepository accountJpaRepository) {
    this.missionJpaRepository = missionJpaRepository;
    this.accountJpaRepository = accountJpaRepository;
  }

  @Override
  public UUID save(Mission mission) {
    AccountEntity account =
        this.accountJpaRepository
            .findById(mission.getAccountId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    MissionEntity saved = this.missionJpaRepository.save(MissionEntity.fromDomain(mission, account));
    return saved.getId();
  }

  @Override
  public Optional<Mission> findById(UUID missionId) {
    return this.missionJpaRepository.findById(missionId).map(MissionEntity::toDomain);
  }

  @Override
  public Optional<Mission> findByIdAndAccountId(UUID missionId, UUID accountId) {
    return this.missionJpaRepository.findByIdAndAccount_Id(missionId, accountId).map(MissionEntity::toDomain);
  }

  @Override
  public List<Mission> findAllByAccountId(UUID accountId) {
    return this.missionJpaRepository.findAllByAccount_IdOrderByCreatedAtDesc(accountId).stream()
        .map(MissionEntity::toDomain)
        .toList();
  }

  @Override
  public List<Mission> findAllByAccountIdAndSearch(UUID accountId, String search) {
    return this.missionJpaRepository.findAllByAccountIdAndSearch(accountId, search).stream()
        .map(MissionEntity::toDomain)
        .toList();
  }

  @Override
  public Page<Mission> findAllByAccountId(UUID accountId, Pageable pageable) {
    return this.missionJpaRepository.findAllByAccount_Id(accountId, pageable).map(MissionEntity::toDomain);
  }

  @Override
  public Page<Mission> findAllByAccountIdAndSearch(UUID accountId, String search, Pageable pageable) {
    return this.missionJpaRepository.findAllByAccountIdAndSearch(accountId, search, pageable)
        .map(MissionEntity::toDomain);
  }

  @Override
  public void deleteById(UUID missionId) {
    this.missionJpaRepository.deleteById(missionId);
  }
}
