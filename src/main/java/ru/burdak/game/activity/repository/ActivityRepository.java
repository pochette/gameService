package ru.burdak.game.activity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.burdak.game.activity.model.Activity;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
}
