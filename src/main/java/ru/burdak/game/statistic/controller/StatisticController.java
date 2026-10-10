package ru.burdak.game.statistic.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.burdak.game.statistic.service.StatisticService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Validated
@Slf4j
public class StatisticController {
  private final StatisticService statisticService;

  @GetMapping("/new-users")
  public Long getCountOfUserFrom(HttpServletRequest request,

                                 @RequestParam(name = "country")
                                 @NotBlank
                                 @Pattern(regexp = "[A-Za-z]{1,255}")
                                 String country,

                                 @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                 OffsetDateTime from,

                                 @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                 OffsetDateTime to) {
    log.info("Получен запрос {} {} на получение количества новых пользователей, country={}, from={}, to={}", request.getMethod(),
             request.getRequestURI(), country, from, to);
    return statisticService.getCountOfNewUsers(country, from.toInstant(), to.toInstant());
  }
}
