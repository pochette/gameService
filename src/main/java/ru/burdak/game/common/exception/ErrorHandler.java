package ru.burdak.game.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.burdak.game.common.exception.user.CountryChangeNotAllowedException;
import ru.burdak.game.common.exception.user.StateTooLargeException;
import ru.burdak.game.common.exception.user.UserNotFoundException;
import tools.jackson.core.JacksonException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(UserNotFoundException.class)
  public ApiError userNotFoundHandler(UserNotFoundException e) {

    log.warn("Пользователь не найден {}", e.getMessage());

    return new ApiError(
      "USER_NOT_FOUND",
      "Пользователь не найден");
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(CountryChangeNotAllowedException.class)
  public ApiError countryChangeNotAllowedHandler(CountryChangeNotAllowedException e) {
    log.warn("Изменение страны пользователя запрещено {}", e.getMessage());
    return new ApiError(
      "COUNTRY_IMMUTABLE",
      "Изменение страны пользователя запрещено"
    );
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalStateException.class)
  public ApiError illegalStateExceptionHandler(IllegalStateException e) {
    log.warn("Передан некорректное состояние объекта {}", e.getMessage());
    return new ApiError(
      "ILLEGAL_STATE_EXCEPTION",
      "Передан некорректное состояние объекта"
    );
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalArgumentException.class)
  public ApiError illegalArgumentExceptionHandler(IllegalArgumentException e) {
    log.warn("Некорректные параметры запроса: {}", e.getMessage());

    return new ApiError(
      "INVALID_ARGUMENT",
      "Некорректные параметры запроса"
    );
  }

  @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
  @ExceptionHandler(StateTooLargeException.class)
  public ApiError stateTooLargeHandler(StateTooLargeException e) {
    log.warn("Превышен размер состояния: {}", e.getMessage());
    return new ApiError("STATE_TOO_LARGE", "Превышен размер входных данных");
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(JacksonException.class)
  public ApiError jacksonExceptionHandler(JacksonException e) {
    log.warn("Ошибка разбора JSON: {}", e.getMessage());

    return new ApiError(
      "INVALID_JSON",
      "Некорректный JSON или неверный тип поля"
    );
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class,
    HttpMessageNotReadableException.class,
    MethodArgumentNotValidException.class,
    HandlerMethodValidationException.class,
    ConstraintViolationException.class
  })
  public ApiError requestValidationHandler(Exception e) {
    log.warn("Некорректный запрос: {}", e.getMessage());

    return new ApiError(
      "INVALID_REQUEST",
      "Отсутствуют обязательные данные или переданы неверные значения"
    );
  }
}
