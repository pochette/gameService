package ru.burdak.game.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.burdak.game.common.exception.user.CountryChangeNotAllowedException;
import ru.burdak.game.common.exception.user.UserNotFoundException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(UserNotFoundException.class)
  public ApiError userNotFoundHandler(UserNotFoundException e) {
    log.warn("Пользователь не найден");
    return new ApiError(
      HttpStatus.NOT_FOUND.getReasonPhrase(),
      "Пользователь не найден");
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(CountryChangeNotAllowedException.class)
  public ApiError countryChangeNotAllowedHandler(CountryChangeNotAllowedException e) {
    log.warn("Изменение страны пользователя запрещено");
    return new ApiError(
      "COUNTRY_IMMUTABLE",
      "Изменение страны пользователя запрещено"
    );
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalStateException.class, IllegalArgumentException.class)
  public ApiError illegalStateExceptionHandler(IllegalStateException e) {
    log.warn("Передан некорректное состояние объекта");
    return new ApiError(
      "ILLEGAL_STATE_EXCEPTION",
      "Передан некорректное состояние объекта"
    );
  }

}
