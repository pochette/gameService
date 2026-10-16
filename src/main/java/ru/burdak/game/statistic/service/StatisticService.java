package ru.burdak.game.statistic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.burdak.game.user.repository.UserRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StatisticService {
  private final UserRepository userRepository;

  public Long getCountOfNewUsers(String country, Instant from, Instant to) {
    if (!from.isBefore(to)) {
      throw new IllegalArgumentException("Дата 'from' должна быть раньше даты 'to'");
    }
    return userRepository.countByCountryAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(country, from, to);
  }
}