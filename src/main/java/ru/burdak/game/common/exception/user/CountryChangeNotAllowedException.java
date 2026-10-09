package ru.burdak.game.common.exception.user;

public class CountryChangeNotAllowedException extends RuntimeException {
  public CountryChangeNotAllowedException(String message) {
    super(message);
  }
}
