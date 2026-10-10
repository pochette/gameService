package ru.burdak.game.user.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonAutoDetect(
  fieldVisibility = JsonAutoDetect.Visibility.NONE,
  getterVisibility = JsonAutoDetect.Visibility.NONE,
  isGetterVisibility = JsonAutoDetect.Visibility.NONE,
  setterVisibility = JsonAutoDetect.Visibility.NONE
)


public class UserStateRequestDto {

  @JsonProperty("money")
  @NotNull
  private Long money;

  @JsonProperty("country")
  @NotBlank
  private String country;

  private Map<String, JsonNode> additionalFields = new HashMap<>();


  @JsonAnySetter
  void addField(String name, JsonNode value) {
    additionalFields.put(name, value);
  }
}
