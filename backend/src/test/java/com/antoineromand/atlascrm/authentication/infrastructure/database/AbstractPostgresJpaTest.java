package com.antoineromand.atlascrm.authentication.infrastructure.database;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractPostgresJpaTest {

  @DynamicPropertySource
  static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", PostgresTestContainer.getInstance()::getJdbcUrl);
    registry.add("spring.datasource.username", PostgresTestContainer.getInstance()::getUsername);
    registry.add("spring.datasource.password", PostgresTestContainer.getInstance()::getPassword);
  }
}
