package ru.burdak.game.common.exception;

public record ApiError(
  String code,
  String message
) {
}
