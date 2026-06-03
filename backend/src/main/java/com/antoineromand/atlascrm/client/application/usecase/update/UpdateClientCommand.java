package com.antoineromand.atlascrm.client.application.usecase.update;

import com.antoineromand.atlascrm.account.application.usecase.account.PatchValue;

public record UpdateClientCommand(
    PatchValue<String> companyName,
    PatchValue<String> status,
    PatchValue<String> notes) {}
