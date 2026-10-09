package ru.burdak.game.user.dto;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

public record UserStateResponseDto(
  @NotNull
  UUID userId,
  @NotNull
  JsonNode state
) {
}
