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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.burdak.game.user.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldCreateUser() {
    var userId = UUID.randomUUID();
    var body = """
      {"money":1500,"country":"US","level":17}
      """;
    var before = Instant.now().minusSeconds(1);
    try {
      var response = putState(userId, body);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

      var user = userRepository.findById(userId).orElseThrow();
      assertThat(user.getId()).isEqualTo(userId);
      assertThat(user.getCreatedAt()).isAfter(before);
      assertThat(user.getMoney()).isEqualTo(1500L);
      assertThat(user.getCountry()).isEqualTo("US");

      var expectedState = objectMapper.readTree(body);

      assertThat(user.getState()).isEqualTo(expectedState);
    }
    finally {
      userRepository.deleteById(userId);
    }
  }

  @Test
  void shouldReturnTopUsers() {
    var firstId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    var secondId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    var thirdId = UUID.fromString("00000000-0000-0000-0000-000000000003");
    var otherCountryId = UUID.fromString("00000000-0000-0000-0000-000000000004");
    var ids = List.of(firstId, secondId, thirdId, otherCountryId);

    try {
      assertThat(putState(secondId,
                          """
                            {"money":200,"country":"US"}
                            """).getStatusCode()).isEqualTo(HttpStatus.OK);

      assertThat(putState(thirdId,
                          """
                            {"money":100,"country":"US"}
                            """).getStatusCode()).isEqualTo(HttpStatus.OK);

      assertThat(putState(firstId,
                          """
                            {"money":200,"country":"US"}
                            """).getStatusCode()).isEqualTo(HttpStatus.OK);

      assertThat(putState(otherCountryId,
                          """
                            {"money":999,"country":"CA"}
                            """).getStatusCode()).isEqualTo(HttpStatus.OK);

      var response = restTemplate.getForEntity(
        "/users/top?country=US&limit=2",
        String.class
      );

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(objectMapper.readTree(response.getBody()))
        .isEqualTo(objectMapper.readTree("""
                                           [
                                             {"userId":"00000000-0000-0000-0000-000000000001","money":200},
                                             {"userId":"00000000-0000-0000-0000-000000000002","money":200}
                                           ]
                                           """));
    }
    finally {
      userRepository.deleteAllById(ids);
    }
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
}
