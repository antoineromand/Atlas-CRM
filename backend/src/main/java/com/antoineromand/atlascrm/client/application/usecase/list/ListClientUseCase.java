package com.antoineromand.atlascrm.client.application.usecase.list;

import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.repository.IClientRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ListClientUseCase implements IListClientUseCase {

  private final IClientRepository clientRepository;

  public ListClientUseCase(IClientRepository clientRepository) {
    this.clientRepository = clientRepository;
  }

  @Override
  public ClientPageResult execute(ListClientQuery query) {
    if (query == null) {
      throw new IllegalArgumentException("query must not be null");
    }

    if (query.accountId() == null) {
      throw new IllegalArgumentException("accountId must not be null");
    }

    int page = query.page();
    int size = query.size();

    if (page < 1) {
      throw new IllegalArgumentException("page must be greater than or equal to 1");
    }

    if (size < 1) {
      throw new IllegalArgumentException("size must be greater than or equal to 1");
    }

    String normalizedSearch = normalize(query.search());
    String normalizedStatus = normalize(query.status());
    PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    if (normalizedSearch == null && normalizedStatus == null) {
      return this.toPageResult(
          query.accountId(), null, null, page, size, this.clientRepository.findAllByAccountId(query.accountId(), pageable));
    }

    return this.toPageResult(
        query.accountId(),
        normalizedSearch,
        normalizedStatus,
        page,
        size,
        this.clientRepository.findAllByAccountIdAndSearch(
            query.accountId(), normalizedSearch, normalizedStatus, pageable));
  }

  private ClientPageResult toPageResult(
      UUID accountId,
      String search,
      String status,
      int requestedPage,
      int size,
      Page<Client> pageResult) {
    if (pageResult.getTotalElements() > 0
        && pageResult.getTotalPages() > 0
        && requestedPage > pageResult.getTotalPages()) {
      PageRequest lastPageable =
          PageRequest.of(pageResult.getTotalPages() - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
      Page<Client> lastPageResult =
          search == null && status == null
              ? this.clientRepository.findAllByAccountId(accountId, lastPageable)
              : this.clientRepository.findAllByAccountIdAndSearch(accountId, search, status, lastPageable);
      return new ClientPageResult(
          lastPageResult.getContent(),
          lastPageResult.getTotalPages(),
          size,
          lastPageResult.getTotalElements(),
          lastPageResult.getTotalPages(),
          lastPageResult.hasNext(),
          lastPageResult.hasPrevious());
    }

    int normalizedPage = pageResult.getTotalPages() == 0 ? 1 : Math.min(requestedPage, pageResult.getTotalPages());
    return new ClientPageResult(
        pageResult.getContent(),
        normalizedPage,
        size,
        pageResult.getTotalElements(),
        pageResult.getTotalPages(),
        pageResult.hasNext(),
        pageResult.hasPrevious());
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }

    String trimmed = value.trim();
    return trimmed.isBlank() ? null : trimmed;
  }
}
