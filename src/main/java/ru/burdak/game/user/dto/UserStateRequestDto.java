package ru.burdak.game.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public record UserStateRequestDto(
  @NotNull
  Long money,
  @NotBlank
  String country,
  @NotNull
  JsonNode state
) {
}
