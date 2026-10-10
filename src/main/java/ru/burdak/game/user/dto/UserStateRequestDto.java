package ru.burdak.game.user.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@Data

public class UserStateRequestDto {
  @NotNull
  private Long money;
  @NotBlank
  private String country;

  private Map<String, JsonNode> additionalFields = new HashMap<>();


  @JsonAnySetter
  void addField(String name, JsonNode value) {
    additionalFields.put(name, value);
  }
}
