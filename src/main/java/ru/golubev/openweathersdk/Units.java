package ru.golubev.openweathersdk;

/**
 * Enumeration of measurement units supported by the OpenWeather API.
 *
 * <p>This enum defines the measurement systems that can be used for weather data
 * returned by the OpenWeather API. Different unit systems provide temperature,
 * wind speed, and other measurements in different formats.</p>
 *
 * <p><b>Usage examples:</b></p>
 * <pre>{@code
 * // Create SDK instance with metric units
 * OpenWeatherSdk sdk = new OpenWeatherSdk.Builder()
 *     .apiKey("your-api-key")
 *     .units(Units.METRIC)
 *     .build();
 *
 * // Use imperial units for specific request
 * WeatherRequest request = WeatherRequest.builder()
 *     .city("New York")
 *     .units(Units.IMPERIAL)
 *     .build();
 *
 * // Get unit code for API parameter
 * String unitParam = Units.METRIC.toString(); // Returns "metric"
 * }</pre>
 *
 * <p><b>Unit system comparison:</b></p>
 * <table border="1">
 *   <caption>Unit system comparison:</caption>
 *   <tr>
 *     <th>Measurement</th>
 *     <th>IMPERIAL</th>
 *     <th>METRIC</th>
 *   </tr>
 *   <tr>
 *     <td>Temperature</td>
 *     <td>Fahrenheit (°F)</td>
 *     <td>Celsius (°C)</td>
 *   </tr>
 *   <tr>
 *     <td>Wind Speed</td>
 *     <td>Miles per hour (mph)</td>
 *     <td>Meters per second (m/s)</td>
 *   </tr>
 *   <tr>
 *     <td>Visibility</td>
 *     <td>Miles (mi)</td>
 *     <td>Meters (m)</td>
 *   </tr>
 *   <tr>
 *     <td>Atmospheric Pressure</td>
 *     <td>Hectopascal (hPa)</td>
 *     <td>Hectopascal (hPa)</td>
 *   </tr>
 *   <tr>
 *     <td>Precipitation</td>
 *     <td>Inches (in)</td>
 *     <td>Millimeters (mm)</td>
 *   </tr>
 * </table>
 *
 * <p><b>Regional preferences:</b></p>
 * <ul>
 *   <li><b>IMPERIAL</b> - Commonly used in United States, United Kingdom, and few other countries</li>
 *   <li><b>METRIC</b> - International standard used in most countries worldwide</li>
 * </ul>
 *
 * <p><b>Default behavior:</b> If no units are specified, the OpenWeather API uses
 * Kelvin for temperature and metric units for other measurements by default.</p>
 *
 * @see <a href="https://openweathermap.org/api/one-call-3#data">OpenWeather API Units Documentation</a>
 * @since 1.0
 */
public enum Units {
    IMPERIAL,
    METRIC;

    /**
     * Returns the lowercase string representation of the unit system for API requests.
     *
     * <p>This method converts the enum value to the exact string format expected by
     * the OpenWeather API in the {@code units} parameter.</p>
     *
     * @return the unit system code in lowercase: "imperial" or "metric"
     *
     * <p><b>Output examples:</b></p>
     * <ul>
     *   <li>{@code Units.IMPERIAL.toString()} → "imperial"</li>
     *   <li>{@code Units.METRIC.toString()} → "metric"</li>
     * </ul>
     */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
