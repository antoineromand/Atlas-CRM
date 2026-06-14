package com.antoineromand.atlascrm.client.application.usecase.contact.create;

public record CreateClientContactCommand(
    String firstName,
    String lastName,
    String email,
    String phone,
    String jobTitle,
    boolean primary) {}
