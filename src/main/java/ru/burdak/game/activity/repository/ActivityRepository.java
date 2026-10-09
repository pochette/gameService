package ru.burdak.game.activity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.burdak.game.activity.model.Activity;
import ru.burdak.game.user.model.User;

import java.time.Instant;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

  List<Activity> findAllByUserAndReceivedAtGreaterThanEqualAndReceivedAtLessThanOrderByReceivedAtAsc(User user, Instant than, Instant than1);
}
