package ru.burdak.game.user.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.burdak.game.activity.repository.ActivityRepository;
import ru.burdak.game.common.exception.user.CountryChangeNotAllowedException;
import ru.burdak.game.common.exception.user.UserNotFoundException;
import ru.burdak.game.user.dto.UserStateRequestDto;
import ru.burdak.game.user.dto.UserStateResponseDto;
import ru.burdak.game.user.model.User;
import ru.burdak.game.user.repository.UserRepository;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final ActivityRepository activityRepository;



  @Transactional
  public void setOrUpdateUsersState(UUID id, UserStateRequestDto dto) {
    if (dto.country() == null) {
      throw new IllegalArgumentException("Отсутствует страна");
    }

    var user = userRepository.findById(id)
      .orElseGet(() -> new User(id, dto.money(), dto.country(), dto.state(), Instant.now()));
    if (!Objects.equals(dto.country(), user.getCountry())) {
      throw new CountryChangeNotAllowedException("Запрещено менять страну пользователя");
    }
    user.setState(dto.state());
    user.setMoney(dto.money());
    userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public UserStateResponseDto getLastSavedState(UUID id) {
    var user = userRepository.findById(id).orElseThrow(
      () -> new UserNotFoundException("Пользователь с ID= %s не найден".formatted(id)));
    return new UserStateResponseDto(id, user.getState());
  }

  @Transactional


  //var user = userRepository.findById(id).orElseThrow(
  //  () -> new UserNotFoundException("Пользователь с ID= %s не найден".formatted(id)));
}
