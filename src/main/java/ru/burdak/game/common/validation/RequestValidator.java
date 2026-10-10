package ru.burdak.game.common.validation;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class RequestValidator {
  public long requiredLong(JsonNode body, String field) {
    var value = body.path(field);

    if (!value.isIntegralNumber() ||
        !value.canConvertToLong()) {
      throw new IllegalArgumentException(
        "Поле '%s' должно быть целым числом в диапазоне long"
          .formatted(field));
    }
    return value.asLong();
  }

  public String requiredCountry(JsonNode body) {
    var value = body.path("country");

    if (!value.isString()) {
      throw new IllegalArgumentException(
        "Поле 'country' обязательно должно быть строкой");
    }
    var country = value.asString();
    if (!country.matches("[A-Za-z]{1,255}")) {
      throw new IllegalArgumentException(
        "Поле 'country' должно содержать от 1 до 255 латинских букв");
    }
    return country;
  }
}
