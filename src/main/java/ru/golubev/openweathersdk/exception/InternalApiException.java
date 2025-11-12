package ru.golubev.openweathersdk.exception;

/**
 * Exception representing server-side errors (5xx) from OpenWeather API.
 *
 * <p>This exception is thrown when the OpenWeather API service experiences
 * internal server errors. These indicate problems on the API provider side
 * rather than client-side issues.</p>
 *
 * <p><b>Common HTTP status codes:</b></p>
 * <ul>
 *   <li><b>500</b> - Internal Server Error</li>
 *   <li><b>502</b> - Bad Gateway</li>
 *   <li><b>503</b> - Service Unavailable</li>
 *   <li><b>504</b> - Gateway Timeout</li>
 * </ul>
 *
 * <p><b>Recommended actions:</b></p>
 * <ul>
 *   <li>Wait and retry after some time</li>
 *   <li>Implement circuit breaker pattern</li>
 *   <li>Check OpenWeather status page for service outages</li>
 *   <li>Use cached data as fallback</li>
 * </ul>
 *
 * @see WeatherApiException
 * @since 1.0
 */
public class InternalApiException extends WeatherApiException {

    public InternalApiException(int code, String status, String message) {
        super(code, status, message);
    }
}