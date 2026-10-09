package ru.burdak.game.user.mapper;

import lombok.experimental.UtilityClass;
import ru.burdak.game.user.dto.UserStateRequestDto;
import ru.burdak.game.user.model.User;

import java.time.Instant;
import java.util.UUID;

@UtilityClass
public class UserMapper {
  public User createUser(UUID id, UserStateRequestDto dto) {
    User user = new User(id, dto.money(), dto.country(), dto.state(), Instant.now());
  }
}
