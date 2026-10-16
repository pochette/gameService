package ru.burdak.game.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.burdak.game.common.exception.user.StateTooLargeException;
import ru.burdak.game.common.validation.RequestValidator;
import ru.burdak.game.user.dto.UserStateRequestDto;
import ru.burdak.game.user.dto.UserStateResponseDto;
import ru.burdak.game.user.dto.UserTopResponseDto;
import ru.burdak.game.user.service.UserService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/users")
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserController {
  private static final long MAX_CONTENT_LENGTH = 10_240L;
  private final UserService userService;
  private final ObjectMapper objectMapper;
  private final Validator validator;
  private final RequestValidator requestValidator;

  @PutMapping(
    value = "/{userId}/state",
    consumes = "application/json"
  )
  public void setOrUpdateUsersState(HttpServletRequest request,
                                    @PathVariable(name = "userId") UUID userId,
                                    @RequestBody byte[] body) {
    if (body.length > MAX_CONTENT_LENGTH) {
      throw new StateTooLargeException();
    }
    log.info("Получен запрос {} {}", request.getMethod(), request.getRequestURI());

    var state = objectMapper.readTree(body);
    if (state == null || !state.isObject()) {
      throw new IllegalArgumentException(
        "Тело запроса должно быть JSON-объектом"
      );
    }

    requestValidator.requiredLong(state, "money");
    requestValidator.requiredCountry(state);

    var dto = objectMapper.treeToValue(state, UserStateRequestDto.class);

    var violations = validator.validate(dto);

    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }

    userService.setOrUpdateUsersState(userId, dto);
  }

  @GetMapping("/{userId}/state")
  public UserStateResponseDto getLastSavedState(HttpServletRequest request,
                                                @PathVariable(name = "userId") UUID userId) {
    log.info("Получен запрос {} {} на получение последнего сохранённого состояния пользователя {} ", request.getMethod(),
             request.getRequestURI(), userId);
    return userService.getLastSavedState(userId);
  }

  @GetMapping("/top")
  public List<UserTopResponseDto> getTopUserByCountry(HttpServletRequest request,

                                                      @RequestParam(name = "country")
                                                      @NotBlank @Pattern(regexp = "[A-Za-z]{1,255}")
                                                      String country,

                                                      @RequestParam(name = "limit") @Min(1) @Max(100) Integer limit) {
    log.info("Получен запрос {} {} на получение списка {} топ-игроков страны {} ", request.getMethod(),
             request.getRequestURI(), limit, country);
    return userService.getTopUserByCountry(country, limit);
  }
}
