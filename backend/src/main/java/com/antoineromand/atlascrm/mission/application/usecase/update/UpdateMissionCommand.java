package com.antoineromand.atlascrm.mission.application.usecase.update;

import com.antoineromand.atlascrm.account.application.usecase.account.PatchValue;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateMissionCommand(
    PatchValue<String> title,
    PatchValue<String> roleInProject,
    PatchValue<String> description,
    PatchValue<UUID> clientId,
    PatchValue<String> status,
    PatchValue<String> priority,
    PatchValue<LocalDate> startDate,
    PatchValue<LocalDate> deadline) {}
