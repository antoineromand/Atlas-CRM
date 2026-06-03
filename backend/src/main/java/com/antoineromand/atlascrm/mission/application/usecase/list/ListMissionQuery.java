package com.antoineromand.atlascrm.mission.application.usecase.list;

import java.util.UUID;

public record ListMissionQuery(UUID accountId, String search, int page, int size) {}
