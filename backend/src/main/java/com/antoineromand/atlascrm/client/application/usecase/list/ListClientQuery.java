package com.antoineromand.atlascrm.client.application.usecase.list;

import java.util.UUID;

public record ListClientQuery(
    UUID accountId, String search, String status, int page, int size) {}
