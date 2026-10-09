package ru.burdak.game.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.burdak.game.user.dto.UserStateRequestDto;
import ru.burdak.game.user.dto.UserStateResponseDto;
import ru.burdak.game.user.dto.UserTopResponseDto;
import ru.burdak.game.user.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/users")
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserController {
  private final UserService userService;

  @PutMapping("/{userId}/state")
  public void setOrUpdateUsersState(HttpServletRequest request,
                                    @PathVariable(name = "userId", required = false) UUID userId,
                                    @RequestBody @Valid UserStateRequestDto dto) {
    log.info("Получен запрос {} {}", request.getMethod(), request.getRequestURI());
    userService.setOrUpdateUsersState(userId, dto);
  }

  @GetMapping("/{userId}/state")
  public UserStateResponseDto getLastSavedState(HttpServletRequest request,
                                                @PathVariable(name = "userId", required = false) UUID userId) {
    log.info("Получен запрос {} {} на получение последнего сохранённого состояния пользователя {} ", request.getMethod(),
             request.getRequestURI(), userId);
    return userService.getLastSavedState(userId);
  }

  @GetMapping("/top")
  public List<UserTopResponseDto> getTopUserByCountry(HttpServletRequest request,
                                                      @RequestParam(name = "country") @NotBlank String country,
                                                      @RequestParam(name = "limit") @Min(1) @Max(100) Integer limit) {
    log.info("Получен запрос {} {} на получение списка {} топ-игроков страны {} ", request.getMethod(),
             request.getRequestURI(), limit, country);
    return userService.getTopUserByCountry(country, limit);
  }
}
