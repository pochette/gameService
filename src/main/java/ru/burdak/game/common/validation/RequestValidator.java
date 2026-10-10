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
}
