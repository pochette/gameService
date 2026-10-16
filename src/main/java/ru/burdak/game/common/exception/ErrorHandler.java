package ru.burdak.game.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
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

  @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ApiError unsupportedMediaTypeHandler(HttpMediaTypeNotSupportedException e) {
    log.warn("Некорректный Content-Type: {}", e.getMessage());

    return new ApiError(
      "UNSUPPORTED_MEDIA_TYPE",
      "Отсутствуют обязательные данные или переданы неверные значения"
    );
  }

  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ApiError httpRequestMethodNotSupportedHandler(HttpRequestMethodNotSupportedException e) {
    log.warn("Ошибка метода обработчика: {} ", e.getMessage());
    return new ApiError(
      "METHOD_NOT_ALLOWED",
      "Для этого HTTP-метода нет обработчика"
    );
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NoResourceFoundException.class)
  public ApiError noResourceFoundHandler(NoResourceFoundException e) {
    log.warn("Ошибка в пути обработчика: {}", e.getMessage());
    return new ApiError(
      "PATH_NOT_FOUND",
      "Запрос ушёл в обработчик статических ресурсов и файла по такому пути нет"
    );
  }


  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public ApiError commonExceptionHandler(Exception e) {

    log.error("Внутренняя ошибка сервера: {}", e.getMessage());

    return new ApiError(
      "INTERNAL_SERVER_ERROR",
      "Произошла внутренняя ошибка сервера"
    );
  }
}
