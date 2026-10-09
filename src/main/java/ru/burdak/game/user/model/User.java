package ru.burdak.game.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
  @Index(name = "idx_users_country_money_id",
  columnList = "country, money, id"),
  @Index(name = "idx_users_country_created_at",
  columnList = "country, created_at")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class User {
  @Id
  private UUID id;

  @Column(name = "money", nullable = false)
  private Long money;

  @Column(name = "country", nullable = false, updatable = false)
  private String country;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  private JsonNode state;

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp(6) with time zone")
  private Instant createdAt;
}
