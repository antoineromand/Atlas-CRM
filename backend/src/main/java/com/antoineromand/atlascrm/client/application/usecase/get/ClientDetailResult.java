package com.antoineromand.atlascrm.client.application.usecase.get;

import com.antoineromand.atlascrm.client.domain.Client;
import com.antoineromand.atlascrm.client.domain.ClientActivity;
import com.antoineromand.atlascrm.client.domain.ClientContact;
import com.antoineromand.atlascrm.client.domain.ClientTag;
import com.antoineromand.atlascrm.mission.domain.Mission;
import java.util.List;

public record ClientDetailResult(
    Client client,
    List<ClientContact> contacts,
    List<ClientActivity> activities,
    List<ClientTag> tags,
    List<Mission> missions) {}
