package ru.burdak.game.activity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.burdak.game.activity.dto.ActivityRequestDto;
import ru.burdak.game.activity.dto.ActivityResponseDto;
import ru.burdak.game.activity.model.Activity;
import ru.burdak.game.activity.repository.ActivityRepository;
import ru.burdak.game.common.exception.user.UserNotFoundException;
import ru.burdak.game.user.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {
  private final ActivityRepository activityRepository;
  private final UserRepository userRepository;

  @Transactional
  public void saveActivity(UUID userId,  long activity) {
    var user = userRepository.findById(userId).orElseThrow(
      () -> new UserNotFoundException("Пользователь с ID= %s не найден".formatted(userId)));
    var activityEntity = new Activity(user, activity, Instant.now());
    activityRepository.save(activityEntity);
  }

  @Transactional(readOnly = true)
  public List<ActivityResponseDto> getActivitiesByUserId(UUID id, Instant from, Instant to) {
    if (!from.isBefore(to)) {
      throw new IllegalArgumentException("Событие 'from' должно быть раньше события 'to'");
    }

    var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Пользователь с id=%s не найден".formatted(id)));
    var activities = activityRepository.findAllByUserAndReceivedAtGreaterThanEqualAndReceivedAtLessThanOrderByReceivedAtAsc(user, from, to);
    return activities.stream()
      .map(activity ->
             new ActivityResponseDto(
               user.getId(),
               activity.getActivity(),
               activity.getReceivedAt()))
      .toList();
  }
}
