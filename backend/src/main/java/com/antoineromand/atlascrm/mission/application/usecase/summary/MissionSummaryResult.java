package com.antoineromand.atlascrm.mission.application.usecase.summary;

public record MissionSummaryResult(
    long totalMissions,
    long activeMissions,
    long completedMissions,
    long dueSoonMissions,
    long highPriorityMissions) {}
