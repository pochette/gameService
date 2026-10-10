package ru.burdak.game.activity.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.burdak.game.activity.dto.ActivityResponseDto;
import ru.burdak.game.activity.service.ActivityService;
import ru.burdak.game.common.validation.RequestValidator;
import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/activities")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ActivityController {
  private final ActivityService activityService;
  private final RequestValidator requestValidator;

  @PostMapping
  public void saveActivity(HttpServletRequest request,
                           @PathVariable(name = "userId") UUID userId,
                           @RequestBody @Valid JsonNode body) {
    long activity = requestValidator.requiredLong(body, "activity");
    log.info("Получен запрос {} {} на сохранение активности пользователя с UUID {}", request.getMethod(), request.getRequestURI(), userId);
    activityService.saveActivity(userId, activity);
  }

  @GetMapping
  public List<ActivityResponseDto> getActivitiesByUserId(
    HttpServletRequest request,
    @PathVariable UUID userId,
    @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
    @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
    log.info("Получен запрос {} {} на получение активности пользователя с userId={}", request.getMethod(), request.getRequestURI(), userId);
    log.info("Параметры запроса: from={}, to={}", from, to);
    return activityService.getActivitiesByUserId(userId, from.toInstant(), to.toInstant());
  }
}
