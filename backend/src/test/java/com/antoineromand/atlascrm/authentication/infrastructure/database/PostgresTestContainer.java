package com.antoineromand.atlascrm.authentication.infrastructure.database;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresTestContainer {
  private static final PostgreSQLContainer<?> CONTAINER =
      new PostgreSQLContainer<>("postgres:16.2-alpine")
          .withDatabaseName("atlascrm_test")
          .withUsername("atlascrm")
          .withPassword("atlascrm");

  static {
    CONTAINER.start();
    Flyway.configure()
        .dataSource(CONTAINER.getJdbcUrl(), CONTAINER.getUsername(), CONTAINER.getPassword())
        .locations("classpath:db/migration")
        .load()
        .migrate();
  }

  private PostgresTestContainer() {}

  public static PostgreSQLContainer<?> getInstance() {
    return CONTAINER;
  }
}
