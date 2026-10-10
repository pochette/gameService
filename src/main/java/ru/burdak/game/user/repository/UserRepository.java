package ru.burdak.game.user.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.burdak.game.user.model.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
  List<User> findAllByCountry(String country, Pageable pageable);

  Long countByCountryAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(String country, Instant from, Instant to);

  @Modifying
  @Query(value = """
      INSERT INTO users (id, money, country, state, created_at)
      VALUES (:id, :money, :country, CAST(:state as jsonb), :createdAt)
      ON CONFLICT (:id) DO UPDATE 
      SET money = excluded.money,
          state = excluded.state
      WHERE users.country = excluded.country
    """, nativeQuery = true)
  int upsertState(@Param("id") UUID id,
                  @Param("money") long money,
                  @Param("country") String country,
                  @Param("state") String state,
                  @Param("createdAt") Instant createdAt);

}
