package com.antoineromand.atlascrm.api.mission.dto;

public record MissionSummaryResponseDto(
    long totalMissions,
    long activeMissions,
    long completedMissions,
    long dueSoonMissions,
    long highPriorityMissions) {}
