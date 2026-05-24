package com.antoineromand.atlascrm.authentication.application.usecase.register;

public record RegisterCommand(String email, String password, String firstName, String lastName) {}
