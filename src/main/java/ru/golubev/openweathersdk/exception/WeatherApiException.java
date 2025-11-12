package ru.golubev.openweathersdk.exception;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Exception representing errors returned by the OpenWeather API.
 *
 * <p>This exception is thrown when the OpenWeather API returns an error response
 * with HTTP status codes 4xx or 5xx. It encapsulates the complete error information
 * returned by the API for proper error handling and diagnostics.</p>
 *
 * <p><b>Common error scenarios:</b></p>
 * <ul>
 *   <li><b>401 Unauthorized</b> - Invalid or missing API key</li>
 *   <li><b>404 Not Found</b> - City not found</li>
 *   <li><b>429 Too Many Requests</b> - Rate limit exceeded</li>
 *   <li><b>500 Internal Server Error</b> - OpenWeather service issues</li>
 * </ul>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * try {
 *     WeatherResponse response = client.getWeather("London");
 * } catch (WeatherApiException e) {
 *     log.error("API error {}: {}", e.code(), e.message());
 *     // Handle specific error codes
 *     if (e.code() == 401) {
 *         // Handle invalid API key
 *     } else if (e.code() == 404) {
 *         // Handle city not found
 *     }
 * }
 * }</pre>
 *
 * @see OpenWeatherApiClientException
 * @see RequestCancellationException
 * @since 1.0
 */
@Accessors(fluent = true)
@Getter
public class WeatherApiException extends RuntimeException {

    /**
     * HTTP status code returned by the OpenWeather API.
     *
     * <p>Common values:</p>
     * <ul>
     *   <li><b>401</b> - Unauthorized (invalid API key)</li>
     *   <li><b>404</b> - Not Found (city not found)</li>
     *   <li><b>429</b> - Too Many Requests (rate limit)</li>
     *   <li><b>500</b> - Internal Server Error</li>
     *   <li><b>502</b> - Bad Gateway</li>
     *   <li><b>503</b> - Service Unavailable</li>
     * </ul>
     */
    private int code;

    /**
     * HTTP status text or API-specific error status.
     */
    private String status;

    /**
     * Human-readable error message describing the failure.
     */
    private String message;

    /**
     * Constructs a new WeatherApiException with API error details.
     *
     * @param code HTTP status code from API response
     * @param status HTTP status text or API error status
     * @param message descriptive error message from API
     */
    public WeatherApiException(int code, String status, String message) {
        super(String.format("Weather API error: code=%d, status=%s, message=%s", code, status, message));
        this.code = code;
        this.status = status;
        this.message = message;
    }

    /**
     * Constructs a new WeatherApiException with a cause.
     *
     * @param cause the underlying exception that caused this error
     */
    public WeatherApiException(Throwable cause) {
        super(cause);
    }
}