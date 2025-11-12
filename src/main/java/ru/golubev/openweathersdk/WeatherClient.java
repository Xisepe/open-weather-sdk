package ru.golubev.openweathersdk;

import java.util.concurrent.CompletableFuture;
import ru.golubev.openweathersdk.model.WeatherResponse;

/**
 * Client interface for accessing OpenWeather API functionality.
 *
 * <p>Provides both synchronous and asynchronous methods for retrieving
 * current weather data for specified cities.</p>
 *
 * <p><b>Obtaining instance:</b></p>
 * <pre>{@code
 * WeatherClient client = WeatherSdk.instance()
 *     .build("your-api-key", cfg -> cfg
 *         .language(SupportedLanguage.ENGLISH)
 *         .units(Units.METRIC));
 * }</pre>
 *
 * <p><b>Basic usage:</b></p>
 * <pre>{@code
 * // Synchronous call
 * WeatherResponse weather = client.getWeather("London");
 *
 * // Asynchronous call
 * CompletableFuture<WeatherResponse> future = client.getWeatherAsync("Paris");
 * future.thenAccept(response -> {
 *     System.out.println("Temperature: " + response.getTemperature().getTemp());
 * });
 * }</pre>
 *
 * @see WeatherSdk#build(String) for client creation
 * @see WeatherResponse for response data structure
 * @see AutoCloseable for resource management
 * @since 1.0
 */
public interface WeatherClient extends AutoCloseable {

    /**
     * Retrieves current weather data for specified city synchronously.
     *
     * @param city city name for weather data lookup
     * @return weather response containing current conditions
     * @see ru.golubev.openweathersdk.internal.WeatherClientImpl#getWeather(String) for implementation details
     */
    WeatherResponse getWeather(String city);

    /**
     * Retrieves current weather data for specified city asynchronously.
     *
     * @param city city name for weather data lookup
     * @return CompletableFuture that will contain weather response when complete
     * @see ru.golubev.openweathersdk.internal.WeatherClientImpl#getWeatherAsync(String) for implementation details
     */
    CompletableFuture<WeatherResponse> getWeatherAsync(String city);
}
