package ru.burdak.game.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import ru.burdak.game.TestcontainersConfiguration;
import ru.burdak.game.user.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiIntegrationTest {
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

  @Test
  void shouldSaveSameUserConcurrently() throws Exception {
    var userId = UUID.randomUUID();
    var firstBody = """
      {"money":1000,"country":"US","level":10}
      """;
    var secondBody = """
      {"money":2000,"country":"US","level":20}
      """;
    var start = new CyclicBarrier(2);

    try (var executor = Executors.newFixedThreadPool(2)) {
      var first = executor.submit(() -> {
        start.await(5, TimeUnit.SECONDS);
        return putState(userId, firstBody);
      });
      var second = executor.submit(() -> {
        start.await(5, TimeUnit.SECONDS);
        return putState(userId, secondBody);
      });

      assertThat(first.get(20, TimeUnit.SECONDS).getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(second.get(20, TimeUnit.SECONDS).getStatusCode()).isEqualTo(HttpStatus.OK);

      var user = userRepository.findById(userId).orElseThrow();
      var state = objectMapper.readTree(user.getState().toString());
      assertThat(state).isIn(objectMapper.readTree(firstBody), objectMapper.readTree(secondBody));
      assertThat(user.getMoney()).isEqualTo(state.path("money").asLong());
      assertThat(user.getCountry()).isEqualTo("US");
    }
    finally {
      userRepository.deleteById(userId);
    }
  }

  @Test
  void shouldReplaceStateAndReturnLatestState() {
    var userId = UUID.randomUUID();
    var initialBody =
      """
          {
                "money": 1000,
                "country": "US",
                "oldField": "must disappear",
                "inventory": {"sword": 1}
              }
        """;
    var updatedBody =
      """
         {
              "money": 2500,
              "country": "US",
              "level": 18,
              "inventory": {"potion": 5},
              "custom": {
                "enabled": true,
                "tags": ["first", "second"],
                "value": null
              }
            }
        """;
    try {
      assertThat(putState(userId, initialBody).getStatusCode()).isEqualTo(HttpStatus.OK);
      var createdAt = userRepository.findById(userId).orElseThrow().getCreatedAt();

      assertThat(putState(userId, updatedBody).getStatusCode()).isEqualTo(HttpStatus.OK);
      var response = restTemplate.getForEntity(
        "/users/{userId}/state",
        String.class, userId);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

      var responseBody = objectMapper.readTree(response.getBody());
      var expectedState = objectMapper.readTree(updatedBody);

      assertThat(responseBody.path("userId").asString()).isEqualTo(userId.toString());
      assertThat(responseBody.path("state")).isEqualTo(expectedState);

      var savedUser = userRepository.findById(userId).orElseThrow();

      assertThat(savedUser.getState()).isEqualTo(expectedState);
      assertThat(savedUser.getMoney()).isEqualTo(2500L);
      assertThat(savedUser.getCountry()).isEqualTo("US");
      assertThat(savedUser.getCreatedAt()).isEqualTo(createdAt);
    }
    finally {
      userRepository.deleteById(userId);
    }
  }

  @Test
  void shouldRejectCountryChangeWithoutChangingSavedState() {
    var userId = UUID.randomUUID();
    var initialBody = """
      {
        "money": 1500,
        "country": "US",
        "level": 17,
        "inventory": {"sword": 2, "potion": 5}
      }
      """;
    var rejectedBody = """
      {
        "money": 9999,
        "country": "CA",
        "level": 99,
        "unexpectedField": true
      }
      """;

    try {
      assertThat(putState(userId, initialBody).getStatusCode()).isEqualTo(HttpStatus.OK);

      var createdAt = userRepository.findById(userId).orElseThrow().getCreatedAt();
      var rejectedResponse = putState(userId, rejectedBody);

      assertThat(rejectedResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
      assertThat(objectMapper.readTree(rejectedResponse.getBody()).path("code").asString()).isEqualTo("COUNTRY_IMMUTABLE");

      var response = restTemplate.getForEntity(
        "/users/{userId}/state",
        String.class,
        userId);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

      var expectedState = objectMapper.readTree(initialBody);

      assertThat(objectMapper.readTree(response.getBody()).path("state")).isEqualTo(expectedState);

      var savedUser = userRepository.findById(userId).orElseThrow();

      assertThat(savedUser.getState()).isEqualTo(expectedState);
      assertThat(savedUser.getCreatedAt()).isEqualTo(createdAt);
      assertThat(savedUser.getMoney()).isEqualTo(1500L);
      assertThat(savedUser.getCountry()).isEqualTo("US");
    }
    finally {
      userRepository.deleteById(userId);
    }
  }

  @Test
  void shouldValidateCountryLengthWithoutChangingSavedState() {
    var userId = UUID.randomUUID();
    var validCountry = "A".repeat(255);
    var invalidCountry = "A".repeat(256);

    var initialBody = """
      {"money":1500,"country":"%s","level":17}
      """.formatted(validCountry);

    var invalidBody = """
      {"money":9999,"country":"%s","level":99}
      """.formatted(invalidCountry);

    try {
      assertThat(putState(userId, initialBody).getStatusCode())
        .isEqualTo(HttpStatus.OK);

      var createdAt = userRepository.findById(userId)
        .orElseThrow()
        .getCreatedAt();

      var response = putState(userId, invalidBody);

      assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(
        objectMapper.readTree(response.getBody())
          .path("code").asString()
      ).isEqualTo("INVALID_ARGUMENT");

      var savedUser = userRepository.findById(userId).orElseThrow();

      assertThat(savedUser.getState())
        .isEqualTo(objectMapper.readTree(initialBody));
      assertThat(savedUser.getMoney()).isEqualTo(1500L);
      assertThat(savedUser.getCountry()).isEqualTo(validCountry);
      assertThat(savedUser.getCreatedAt()).isEqualTo(createdAt);
    }
    finally {
      userRepository.deleteById(userId);
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
}
