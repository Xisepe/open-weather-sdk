package ru.golubev.openweathersdk.exception;

/**
 * Exception representing client-side errors during OpenWeather API communication.
 *
 * <p>This exception is thrown when errors occur in the HTTP communication layer
 * between the SDK and OpenWeather API, excluding API-level error responses
 * (which throw {@link WeatherApiException}).</p>
 *
 * <p><b>Common causes:</b></p>
 * <ul>
 *   <li>Network connectivity issues</li>
 *   <li>DNS resolution failures</li>
 *   <li>Socket timeouts</li>
 *   <li>Connection timeouts</li>
 *   <li>SSL/TLS handshake failures</li>
 *   <li>IO exceptions during request/response processing</li>
 *   <li>Server unreachable or connection refused</li>
 * </ul>
 *
 * <p><b>Exception hierarchy:</b></p>
 * <pre>
 * RuntimeException
 * ├── WeatherApiException (API-level errors)
 * ├── OpenWeatherApiClientException (HTTP/network errors)
 * └── RequestCancellationException (cancelled requests)
 * </pre>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * try {
 *     WeatherResponse response = client.getWeather("London");
 * } catch (OpenWeatherApiClientException e) {
 *     log.error("Network error while calling weather API", e);
 *     // Handle network issues, retry logic, etc.
 * } catch (WeatherApiException e) {
 *     // Handle API-level errors (invalid key, city not found, etc.)
 * }
 * }</pre>
 *
 * @see WeatherApiException
 * @see RequestCancellationException
 * @since 1.0
 */
public class OpenWeatherApiClientException extends RuntimeException {

    /**
     * Constructs exception with the underlying cause.
     *
     * @param cause the original exception that caused this error (IOException, etc.)
     */
    public OpenWeatherApiClientException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs exception with a descriptive message.
     *
     * @param message detailed description of the error
     */
    public OpenWeatherApiClientException(String message) {
        super(message);
    }

    /**
     * Constructs exception with both message and underlying cause.
     *
     * @param message detailed description of the error
     * @param cause the original exception that caused this error
     */
    public OpenWeatherApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}