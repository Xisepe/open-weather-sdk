package ru.golubev.openweathersdk.exception;

import ru.golubev.openweathersdk.internal.OpenWeatherApi;

/**
 * Exception indicating that an operation was attempted on a shut down OpenWeather API client.
 *
 * <p>This exception is thrown when any method is called on an {@link OpenWeatherApi}
 * instance after it has been shut down via the {@code shutdown()} method. It prevents operations
 * on disposed resources and helps identify resource lifecycle issues.</p>
 *
 * <p><b>Common scenarios:</b></p>
 * <ul>
 *   <li>Calling API methods after explicit {@code shutdown()} call</li>
 *   <li>Using API client after application context destruction</li>
 *   <li>Accessing client from shutdown hooks after main application shutdown</li>
 *   <li>Race conditions during application termination</li>
 * </ul>
 *
 * <p><b>Exception hierarchy:</b></p>
 * <pre>
 * OpenWeatherApiClientException
 * └── ShutdownException (operations on shut down client)
 * </pre>
 *
 * <p><b>Proper usage pattern:</b></p>
 * <pre>{@code
 * OpenWeatherApi api = // ... initialize api
 *
 * try {
 *     WeatherResponse response = api.weather("London", language, units);
 *     // Use response...
 * } finally {
 *     api.shutdown(); // Call shutdown when done
 * }
 *
 * // Subsequent calls will throw ShutdownException
 * api.weather("Paris", language, units); // Throws ShutdownException
 * }</pre>
 *
 * <p><b>Note:</b> This exception extends {@code OpenWeatherApiClientException} to maintain
 * consistency in exception handling while providing specific semantics for shutdown state.</p>
 *
 * @see OpenWeatherApiClientException
 * @see ru.golubev.openweathersdk.internal.OpenWeatherApi#shutdown()
 * @since 1.0
 */
public class ShutdownException extends OpenWeatherApiClientException {

    /**
     * Constructs a new ShutdownException with a standard message indicating
     * that the OpenWeatherApi instance has been shut down.
     */
    public ShutdownException() {
        super("OpenWeatherApi has already been shut down");
    }
}