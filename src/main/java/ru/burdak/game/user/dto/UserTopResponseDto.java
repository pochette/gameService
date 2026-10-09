package ru.burdak.game.user.dto;

import java.util.UUID;

public record UserTopResponseDto(
  UUID userId,
  Long money
) {
}
