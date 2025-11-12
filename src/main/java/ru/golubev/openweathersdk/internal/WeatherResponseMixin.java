package ru.golubev.openweathersdk.internal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Mixin that attaches the custom deserializer to WeatherResponseDto
 * so you don't need annotations in the DTO itself.
 */
@JsonDeserialize(using = WeatherResponseDeserializer.class)
abstract class WeatherResponseMixin {
}