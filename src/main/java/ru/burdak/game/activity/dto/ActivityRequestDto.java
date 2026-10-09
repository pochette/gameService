package ru.burdak.game.activity.dto;

import jakarta.validation.constraints.NotNull;

public record ActivityRequestDto (
  @NotNull
  Long activity
){

}
