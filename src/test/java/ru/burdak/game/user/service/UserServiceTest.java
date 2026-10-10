package ru.burdak.game.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.burdak.game.common.exception.user.CountryChangeNotAllowedException;
import ru.burdak.game.user.dto.UserStateRequestDto;
import ru.burdak.game.user.dto.UserTopResponseDto;
import ru.burdak.game.user.model.User;
import ru.burdak.game.user.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock private UserRepository userRepository;
  @Spy private ObjectMapper objectMapper = JsonMapper.builder().build();

  @InjectMocks private UserService service;

  private UUID userId;
  private User user1;
  private UserStateRequestDto requestDto1;


  @BeforeEach
  void setUp() {
    userId = UUID.fromString("cfe0fafa-7754-4e33-8524-cba790233c41");
    requestDto1 = objectMapper.readValue("""
                                           {
                                                           "money": 1500,
                                                           "country": "US",
                                                           "level": 17,
                                                           "inventory": {
                                                               "sword": 2,
                                                               "potion": 5
                                                           }
                                                       }
                                           
                                           """, UserStateRequestDto.class);
    user1 = new User(
      userId,
      1000L,
      "US",
      objectMapper.readTree(
        """
                      {"money": 1000, "country": "US", "level": 10}
          
          """),
      Instant.parse("2026-01-01T00:00:00Z"));
  }

  @Test
  void shouldUpdateExistingUserState() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user1));

    service.setOrUpdateUsersState(userId, requestDto1);

    assertThat(user1.getMoney()).isEqualTo(1500L);
    assertThat(user1.getCountry()).isEqualTo("US");
    assertThat(user1.getCreatedAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));

    var expectedState = objectMapper.createObjectNode();
    expectedState.put("money", 1500L);
    expectedState.put("country", "US");
    expectedState.put("level", 17);
    expectedState.putObject("inventory").put("sword", 2).put("potion", 5);

    assertThat(user1.getState()).isEqualTo(expectedState);

    var response = service.getLastSavedState(userId);

    assertThat(response.userId()).isEqualTo(userId);
    assertThat(response.state()).isEqualTo(expectedState);

    verify(userRepository).save(user1);
  }

  @Test
  void shouldRejectCountryChange() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
    var previousState = user1.getState().deepCopy();
    var requestDto2 = objectMapper.readValue("""
                                               {
                                                               "money": 999,
                                                               "country": "CA",
                                                               "level": 999,
                                                               "inventory": {
                                                                   "sword": 999,
                                                                   "potion": 999
                                                               }
                                                           }
                                               
                                               """, UserStateRequestDto.class);
    assertThatThrownBy(() -> service.setOrUpdateUsersState(userId, requestDto2))
      .isInstanceOf(CountryChangeNotAllowedException.class);

    assertThat(user1.getState()).isEqualTo(previousState);
    assertThat(user1.getCountry()).isEqualTo("US");
    assertThat(user1.getMoney()).isEqualTo(1000L);
    assertThat(user1.getCreatedAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    verify(userRepository, never()).save(any(User.class));
  }
  @Test
  void shouldCreateUser() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    service.setOrUpdateUsersState(userId, requestDto1);

    var captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());

    assertThat(captor.getValue())
      .extracting(User::getId, User::getMoney, User::getCountry)
      .containsExactly(userId, 1500L, "US");
    assertThat(captor.getValue().getCreatedAt()).isNotNull();
    assertThat(captor.getValue().getState().path("level").asInt())
      .isEqualTo(17);
  }

  @Test
  void shouldRejectMissingMoney() {
    requestDto1.setMoney(null);

    assertThatThrownBy(() -> service.setOrUpdateUsersState(userId, requestDto1))
      .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(userRepository);
  }

  @Test
  void shouldReturnTopUsers() {
    when(userRepository.findAllByCountry(eq("US"), any(Pageable.class)))
      .thenReturn(List.of(user1));

    assertThat(service.getTopUserByCountry("US", 1))
      .containsExactly(new UserTopResponseDto(userId, 1000L));

    verify(userRepository).findAllByCountry(
      "US",
      PageRequest.of(0, 1, Sort.by("money").descending()
        .and(Sort.by("id").ascending()))
    );
  }
}