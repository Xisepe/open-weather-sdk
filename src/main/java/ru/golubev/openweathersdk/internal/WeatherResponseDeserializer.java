package ru.golubev.openweathersdk.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import ru.golubev.openweathersdk.model.System;
import ru.golubev.openweathersdk.model.Temperature;
import ru.golubev.openweathersdk.model.Weather;
import ru.golubev.openweathersdk.model.WeatherResponse;
import ru.golubev.openweathersdk.model.Wind;

/**
 * Custom Jackson deserializer for OpenWeather API response.
 *
 * <p>Maps complex JSON structure from OpenWeather API to flat Java object model.
 * Handles nested objects, arrays, and optional fields with safe data extraction.</p>
 *
 * <p><b>JSON to Java mapping:</b></p>
 * <ul>
 *   <li>{@code weather[0]} → {@link Weather} (takes first array element)</li>
 *   <li>{@code main} → {@link Temperature} (temp, feels_like)</li>
 *   <li>{@code wind} → {@link Wind} (speed)</li>
 *   <li>{@code sys} → {@link System} (sunrise, sunset)</li>
 *   <li>{@code visibility} → Integer</li>
 *   <li>{@code dt} → long (datetime)</li>
 *   <li>{@code timezone} → int</li>
 *   <li>{@code name} → String (city name)</li>
 * </ul>
 */
class WeatherResponseDeserializer extends JsonDeserializer<WeatherResponse> {

    /**
     * Deserializes JSON response from OpenWeather API into WeatherResponse object.
     *
     * @param p JSON parser instance
     * @param ctxt deserialization context
     * @return populated WeatherResponse object
     * @throws IOException on JSON parsing errors
     */
    @Override
    public WeatherResponse deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode root = mapper.readTree(p);

        // weather -> array, take first element for main/description
        Weather weather = null;
        JsonNode weatherArray = root.path("weather");
        if (weatherArray.isArray() && weatherArray.size() > 0) {
            JsonNode w0 = weatherArray.get(0);
            String main = textOrNull(w0, "main");
            String desc = textOrNull(w0, "description");
            weather = new Weather(main, desc);
        } else {
            weather = new Weather(null, null);
        }

        // main -> temp, feels_like
        JsonNode mainNode = root.path("main");
        double temp = doubleOrDefault(mainNode, "temp", 0.0);
        double feels = doubleOrDefault(mainNode, "feels_like", 0.0);
        Temperature temperature = new Temperature(temp, feels);

        // visibility
        Integer visibility = null;
        if (root.has("visibility") && root.get("visibility").isInt()) {
            visibility = root.get("visibility").intValue();
        }

        // wind -> speed
        Double windSpeed = null;
        JsonNode windNode = root.path("wind");
        if (windNode.has("speed") && windNode.get("speed").isNumber()) {
            windSpeed = windNode.get("speed").doubleValue();
        }
        Wind wind = new Wind(windSpeed);

        // dt -> datetime
        long datetime = 0L;
        if (root.has("dt") && root.get("dt").canConvertToLong()) {
            datetime = root.get("dt").longValue();
        }

        // sys -> sunrise, sunset
        long sunrise = 0L;
        long sunset = 0L;
        JsonNode sysNode = root.path("sys");
        if (sysNode.isObject()) {
            if (sysNode.has("sunrise") && sysNode.get("sunrise").canConvertToLong()) {
                sunrise = sysNode.get("sunrise").longValue();
            }
            if (sysNode.has("sunset") && sysNode.get("sunset").canConvertToLong()) {
                sunset = sysNode.get("sunset").longValue();
            }
        }
        System system = new System(sunrise, sunset);

        // timezone
        int timezone = 0;
        if (root.has("timezone") && root.get("timezone").isInt()) {
            timezone = root.get("timezone").intValue();
        }

        // name
        String name = textOrNull(root, "name");

        return new WeatherResponse(weather, temperature, visibility, wind, datetime, system, timezone, name);
    }

    /**
     * Safely extracts text value from JSON node or returns null if missing/invalid.
     */
    private static String textOrNull(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return n.isMissingNode() || n.isNull() ? null : n.asText();
    }

    /**
     * Safely extracts double value from JSON node or returns default if missing/invalid.
     */
    private static double doubleOrDefault(JsonNode node, String field, double def) {
        JsonNode n = node.path(field);
        return n.isNumber() ? n.doubleValue() : def;
    }
}