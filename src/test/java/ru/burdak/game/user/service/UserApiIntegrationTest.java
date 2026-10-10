package ru.burdak.game.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.burdak.game.user.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Testcontainers
@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiIntegrationTest {
  @SuppressWarnings("resource")
  @Container
  static final GenericContainer<?> POSTGRES =
    new GenericContainer<>("postgres:17-alpine")
      .withEnv("POSTGRES_DB", "game_service_test")
      .withEnv("POSTGRES_USER", "test")
      .withEnv("POSTGRES_PASSWORD", "test")
      .withExposedPorts(5432);

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry properties) {
    properties.add("spring.datasource.url", () ->
      "jdbc:postgresql://%s:%d/game_service_test".formatted(
        POSTGRES.getHost(),
        POSTGRES.getMappedPort(5432)
      )
    );
    properties.add("spring.datasource.username", () -> "test");
    properties.add("spring.datasource.password", () -> "test");
  }

  @LocalServerPort
  private int port;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldCreateUser() {

  }


  private ResponseEntity<String> putState(UUID userId, String body) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return restTemplate.exchange(
      "/users/{userId}/state",
      HttpMethod.PUT,
      new HttpEntity<>(body, headers),
      String.class, userId
    );
  }


}
