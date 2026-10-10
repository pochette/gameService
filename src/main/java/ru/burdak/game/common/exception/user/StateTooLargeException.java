package ru.burdak.game.common.exception.user;

public class StateTooLargeException extends RuntimeException {
  public StateTooLargeException() {
    super("Размер состояния не должен превышать 10 240 байт");
  }
}
