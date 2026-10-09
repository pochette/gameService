package ru.burdak.game.activity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.burdak.game.activity.model.Activity;

import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
  Optional<Activity> findByUser_Uuid(UUID uuid);
}
