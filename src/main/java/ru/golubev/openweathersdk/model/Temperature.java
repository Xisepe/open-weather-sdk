package ru.golubev.openweathersdk.model;

import lombok.Value;

/**
 * Represents temperature measurements and perceived temperature.
 *
 * <p>This class contains both the actual air temperature and the "feels like" temperature,
 * which accounts for human perception based on weather conditions like humidity and wind.</p>
 *
 * <p>All temperature values are in Kelvin by default. Use conversion methods to get
 * Celsius or Fahrenheit values. Or provide units to API.</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * Temperature temp = response.getTemperature();
 *
 * double celsius = temp.getTemp() - 273.15;
 * double feelsLikeCelsius = temp.getFeels_like() - 273.15;
 *
 * System.out.println("Temperature: " + celsius + "°C");
 * System.out.println("Feels like: " + feelsLikeCelsius + "°C");
 * }</pre>
 *
 * @see #temp
 * @see #feels_like
 * @since 1.0
 */
@Value
public class Temperature {
    /**
     * Actual air temperature in Kelvin.
     *
     * <p>This is the measured atmospheric temperature at the specified location.</p>
     *
     * <p><b>Conversion formulas:</b></p>
     * <ul>
     *   <li><b>Celsius:</b> {@code temp - 273.15}</li>
     *   <li><b>Fahrenheit:</b> {@code (temp - 273.15) * 9/5 + 32}</li>
     * </ul>
     *
     * <p><b>Typical range:</b> 200K to 330K (-73°C to 57°C)</p>
     *
     * <p><b>Example:</b> 293.15 represents 20°C</p>
     */
    double temp;

    /**
     * Human-perceived temperature in Kelvin.
     *
     * <p>This temperature accounts for factors that affect how humans perceive temperature,
     * such as humidity, wind chill, and heat index. It represents what the temperature
     * "feels like" to people outdoors.</p>
     *
     * <p><b>Conversion formulas:</b></p>
     * <ul>
     *   <li><b>Celsius:</b> {@code feels_like - 273.15}</li>
     *   <li><b>Fahrenheit:</b> {@code (feels_like - 273.15) * 9/5 + 32}</li>
     * </ul>
     *
     * <p>In cold weather, this value is typically lower than actual temperature due to wind chill.
     * In hot, humid weather, it's typically higher due to heat index.</p>
     *
     * <p><b>Example:</b> 295.15 represents 22°C feels like temperature</p>
     */
    double feels_like;
}