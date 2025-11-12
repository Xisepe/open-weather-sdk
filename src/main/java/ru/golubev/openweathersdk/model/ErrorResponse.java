package ru.golubev.openweathersdk.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.golubev.openweathersdk.exception.WeatherApiException;

/**
 * Represents an error response from the OpenWeather API.
 *
 * <p>This class encapsulates error information returned by the API when a request fails.
 * It follows the standard error response format used by OpenWeather.</p>
 *
 * @see WeatherApiException
 * @since 1.0
 */
@AllArgsConstructor
@Getter
public final class ErrorResponse {

    /**
     * HTTP status code or API-specific error code.
     *
     * <p>Common error codes:</p>
     * <ul>
     *   <li><b>401</b> - Invalid API key</li>
     *   <li><b>404</b> - City not found</li>
     *   <li><b>429</b> - API rate limit exceeded</li>
     *   <li><b>5XX</b> - Internal server error</li>
     * </ul>
     */
    private final int cod;

    /**
     * Human-readable error message describing the failure.
     *
     * <p>Provides detailed information about what went wrong with the request.</p>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>"Invalid API key. Please see https://openweathermap.org/faq#error401 for more info."</li>
     *   <li>"city not found"</li>
     *   <li>"Your account is temporary blocked due to exceeding of requests limitation."</li>
     * </ul>
     */
    private final String message;
}
