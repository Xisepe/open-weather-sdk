package ru.golubev.openweathersdk.model;

import lombok.Value;

/**
 * Describes current weather conditions with main category and detailed description.
 *
 * <p>This class provides both a general weather category and a more specific description
 * of current atmospheric conditions.</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * Weather weather = response.getWeather();
 *
 * System.out.println("Main condition: " + weather.getMain());
 * System.out.println("Description: " + weather.getDescription());
 * }</pre>
 *
 * @since 1.0
 */
@Value
public class Weather {
    /**
     * Main weather condition group.
     *
     * <p>Provides a general categorization of current weather conditions.
     * Useful for quick filtering and high-level weather analysis.</p>
     *
     * <p><b>Possible values:</b></p>
     * <ul>
     *   <li><b>Thunderstorm</b> - Thunderstorm weather</li>
     *   <li><b>Drizzle</b> - Drizzle weather</li>
     *   <li><b>Rain</b> - Rain weather</li>
     *   <li><b>Snow</b> - Snow weather</li>
     *   <li><b>Mist</b> - Mist weather</li>
     *   <li><b>Smoke</b> - Smoke weather</li>
     *   <li><b>Haze</b> - Haze weather</li>
     *   <li><b>Dust</b> - Dust weather</li>
     *   <li><b>Fog</b> - Fog weather</li>
     *   <li><b>Sand</b> - Sand weather</li>
     *   <li><b>Ash</b> - Ash weather</li>
     *   <li><b>Squall</b> - Squall weather</li>
     *   <li><b>Tornado</b> - Tornado weather</li>
     *   <li><b>Clear</b> - Clear sky</li>
     *   <li><b>Clouds</b> - Cloudy weather</li>
     * </ul>
     *
     * <p><b>Example:</b> "Rain"</p>
     */
    String main;

    /**
     * Detailed weather description within the main group.
     *
     * <p>Provides more specific information about weather conditions.
     * Typically includes intensity modifiers and more precise descriptions.</p>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>"light rain"</li>
     *   <li>"heavy intensity shower rain"</li>
     *   <li>"few clouds"</li>
     *   <li>"broken clouds"</li>
     *   <li>"overcast clouds"</li>
     *   <li>"freezing rain"</li>
     * </ul>
     *
     * <p><b>Note:</b> Descriptions are provided in English.</p>
     *
     * <p><b>Example:</b> "moderate rain"</p>
     */
    String description;
}