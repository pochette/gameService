package ru.burdak.game.activity.dto;

import java.time.Instant;
import java.util.UUID;

public record ActivityResponseDto(
  UUID userId,
  long activity,
  Instant receivedAt
) {
}
