package com.antoineromand.atlascrm.client.application.usecase.list;

import com.antoineromand.atlascrm.client.domain.Client;
import java.util.List;

public record ClientPageResult(
    List<Client> items,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious) {}
