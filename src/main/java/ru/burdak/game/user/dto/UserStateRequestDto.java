package ru.burdak.game.user.dto;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public record UserStateRequestDto(
  @NotNull
  JsonNode state
) {
}
