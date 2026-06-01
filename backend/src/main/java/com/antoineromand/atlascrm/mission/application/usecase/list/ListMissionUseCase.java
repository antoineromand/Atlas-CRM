package com.antoineromand.atlascrm.mission.application.usecase.list;

import com.antoineromand.atlascrm.mission.domain.Mission;
import com.antoineromand.atlascrm.mission.domain.repository.IMissionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

  @Override
  public MissionPageResult execute(UUID accountId, String search, int page, int size) {
    if (page < 1) {
      throw new IllegalArgumentException("page must be greater than or equal to 1");
    }

    if (size < 1) {
      throw new IllegalArgumentException("size must be greater than or equal to 1");
    }

    String normalizedSearch = search == null ? null : search.trim();
    PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    if (normalizedSearch == null || normalizedSearch.isBlank()) {
      return this.toPageResult(
          accountId, null, page, size, this.missionRepository.findAllByAccountId(accountId, pageable));
    }

    return this.toPageResult(
        accountId,
        normalizedSearch,
        page,
        size,
        this.missionRepository.findAllByAccountIdAndSearch(accountId, normalizedSearch, pageable));
  }

  private MissionPageResult toPageResult(
      UUID accountId, String search, int requestedPage, int size, Page<Mission> pageResult) {
    if (pageResult.getTotalElements() > 0
        && pageResult.getTotalPages() > 0
        && requestedPage > pageResult.getTotalPages()) {
      PageRequest lastPageable =
          PageRequest.of(pageResult.getTotalPages() - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
      Page<Mission> lastPageResult =
          search == null
              ? this.missionRepository.findAllByAccountId(accountId, lastPageable)
              : this.missionRepository.findAllByAccountIdAndSearch(accountId, search, lastPageable);
      return new MissionPageResult(
          lastPageResult.getContent(),
          lastPageResult.getTotalPages(),
          size,
          lastPageResult.getTotalElements(),
          lastPageResult.getTotalPages(),
          lastPageResult.hasNext(),
          lastPageResult.hasPrevious());
    }

    int normalizedPage = pageResult.getTotalPages() == 0 ? 1 : Math.min(requestedPage, pageResult.getTotalPages());
    return new MissionPageResult(
        pageResult.getContent(),
        normalizedPage,
        size,
        pageResult.getTotalElements(),
        pageResult.getTotalPages(),
        pageResult.hasNext(),
        pageResult.hasPrevious());
  }
}
