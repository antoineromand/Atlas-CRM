package com.antoineromand.atlascrm.client.application.usecase.contact.update;

public record UpdateClientContactCommand(
    String firstName,
    String lastName,
    String email,
    String phone,
    String jobTitle,
    boolean primary) {}
