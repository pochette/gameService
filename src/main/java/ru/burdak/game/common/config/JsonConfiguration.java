package ru.burdak.game.common.config;

import org.hibernate.Hibernate;
import org.hibernate.type.format.jackson.Jackson3JsonFormatMapper;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
public class JsonConfiguration {

  @Bean
  JsonMapperBuilderCustomizer preserveDecimalPrecision() {
    return builder -> builder.enable(
      DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS
    );
  }

  @Bean
  HibernatePropertiesCustomizer hibernateJsonMapper(JsonMapper mapper) {
    return properties -> properties.put(
      "hibernate.type.json_format_mapper",
      new Jackson3JsonFormatMapper(mapper)
    );
  }
}
