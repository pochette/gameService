package ru.burdak.game.user.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.burdak.game.common.exception.user.CountryChangeNotAllowedException;
import ru.burdak.game.common.exception.user.UserNotFoundException;
import ru.burdak.game.user.dto.UserStateRequestDto;
import ru.burdak.game.user.dto.UserStateResponseDto;
import ru.burdak.game.user.dto.UserTopResponseDto;
import ru.burdak.game.user.model.User;
import ru.burdak.game.user.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  @Transactional

  public void setOrUpdateUsersState(UUID id, UserStateRequestDto dto) {

    var money = dto.getMoney();
    var country = dto.getCountry();

    if (country==null || country.isBlank()) {
      throw new IllegalArgumentException("Поле 'country' обязательно и должно быть непустой строкой");
    }

    if (money==null) {
      throw new IllegalArgumentException(
        "Поле 'money' обязательно и должно быть целым числом в диапазоне long"
      );
    }


    var user = userRepository.findById(id)
      .orElseGet(() -> new User(id, money, country, null, Instant.now()));

    if (!Objects.equals(country, user.getCountry())) {
      throw new CountryChangeNotAllowedException("Запрещено менять страну пользователя");
    }

    var state = objectMapper.createObjectNode();

    dto.getAdditionalFields().forEach(
      state::set
    );

    state.put("money", dto.getMoney());
    state.put("country", dto.getCountry());

    user.setState(state);
    user.setMoney(money);

    userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public UserStateResponseDto getLastSavedState(UUID id) {
    var user = userRepository.findById(id).orElseThrow(
      () -> new UserNotFoundException("Пользователь с ID= %s не найден".formatted(id)));
    return new UserStateResponseDto(id, user.getState());
  }

  @Transactional(readOnly = true)
  public List<UserTopResponseDto> getTopUserByCountry(String country, Integer limit) {
    var pageable = PageRequest.of(0, limit, Sort.by("money").descending().and(Sort.by("id").ascending()));
    return userRepository.findAllByCountry(country, pageable).stream()
      .map(user ->
             new UserTopResponseDto(user.getId(), user.getMoney()))
      .toList();
  }
}
