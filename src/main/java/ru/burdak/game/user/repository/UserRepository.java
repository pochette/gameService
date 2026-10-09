package ru.burdak.game.user.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.burdak.game.user.model.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
  List<User> findAllByCountry(String country, Pageable pageable);

  Long countByCountryAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(String country, Instant from, Instant to);
}
