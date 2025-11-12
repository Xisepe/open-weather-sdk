package ru.golubev.openweathersdk.model;

import lombok.Value;

/**
 * Comprehensive weather data response for a specific location.
 *
 * <p>This is the main response object containing all available weather information
 * for the requested location. It aggregates data from various meteorological measurements.</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * System.out.println("City: " + response.getName());
 * System.out.println("Temperature: " + response.getTemperature().getTemp());
 * System.out.println("Weather: " + response.getWeather().getDescription());
 * System.out.println("Wind speed: " + response.getWind().getSpeed());
 * }</pre>
 *
 * @see Weather
 * @see Temperature
 * @see Wind
 * @see System
 * @since 1.0
 */
@Value
public class WeatherResponse {
    /**
     * Current weather conditions description.
     *
     * <p>Contains information about the atmospheric conditions including
     * main category and detailed description.</p>
     *
     * @see Weather
     */
    Weather weather;

    /**
     * Temperature measurements including perceived temperature.
     *
     * <p>Provides both actual air temperature and "feels like" temperature
     * that accounts for human perception factors.</p>
     *
     * @see Temperature
     */
    Temperature temperature;

    /**
     * Horizontal visibility in meters.
     *
     * <p>Indicates the distance at which objects can be clearly seen.
     * Lower values indicate reduced visibility due to fog, rain, or other factors.</p>
     *
     * <p><b>Interpretation guide:</b></p>
     * <ul>
     *   <li><b>10,000+ meters:</b> Excellent visibility</li>
     *   <li><b>5,000-10,000 meters:</b> Good visibility</li>
     *   <li><b>2,000-5,000 meters:</b> Moderate visibility</li>
     *   <li><b>1,000-2,000 meters:</b> Poor visibility</li>
     *   <li><b>&lt;1,000 meters:</b> Very poor visibility</li>
     * </ul>
     *
     * <p><b>Maximum value:</b> 10,000 meters (10km) - indicates unlimited visibility</p>
     *
     * <p><b>Example:</b> 10000 (excellent visibility)</p>
     */
    Integer visibility;

    /**
     * Wind measurements including speed and direction.
     *
     * <p>Contains information about wind conditions at the measurement location.</p>
     *
     * @see Wind
     */
    Wind wind;

    /**
     * Time of data calculation in Unix timestamp format (seconds since January 1, 1970 UTC).
     *
     * <p>Indicates when the weather data was last updated by the OpenWeather system.</p>
     *
     * <p><b>Note:</b> This timestamp is in seconds, not milliseconds. Multiply by 1000
     * when converting to Java {@link java.util.Date}.</p>
     *
     * <p><b>Example:</b> 1675753200 represents Tuesday, February 7, 2023 7:00:00 AM GMT</p>
     */
    long datetime;

    /**
     * Astronomical data including sunrise and sunset times.
     *
     * <p>Provides solar information for the requested location.</p>
     *
     * @see System
     */
    System sys;

    /**
     * Timezone shift in seconds from UTC.
     *
     * <p>Indicates the timezone offset for the requested location. Positive values
     * are east of UTC, negative values are west of UTC.</p>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li><b>3600</b> - UTC+1 (Central European Time)</li>
     *   <li><b>-18000</b> - UTC-5 (Eastern Standard Time)</li>
     *   <li><b>0</b> - UTC (Greenwich Mean Time)</li>
     * </ul>
     *
     * <p>To convert to hours: {@code timezone / 3600}</p>
     */
    int timezone;

    /**
     * City name for which weather data is provided.
     *
     * <p>Returns the name of the location matching the search query.
     * This is particularly useful when using city ID or coordinates for the request,
     * as it confirms which specific location the data represents.</p>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>"London"</li>
     *   <li>"New York"</li>
     *   <li>"Tokyo"</li>
     *   <li>"Paris"</li>
     * </ul>
     */
    String name;
}